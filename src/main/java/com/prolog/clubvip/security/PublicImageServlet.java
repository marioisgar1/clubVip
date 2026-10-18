package com.prolog.clubvip.security;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

import com.prolog.clubvip.rest.RestClient;
import com.prolog.clubvip.rest.RestException;
import com.prolog.clubvip.vo.ImageVo;

import javax.imageio.ImageIO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Images of the public pages, no session needed: {@code /publicImage?logo=businessId} (business
 * logo), {@code /publicImage?photo=productId} (main photo of a store product),
 * {@code /publicImage?storePhoto=photoId} (any photo of a store product, detail gallery) and
 * {@code /publicImage?pagePhoto=photoId} (cover / gallery of "Mi página"). The backend only
 * answers for active businesses (and, for photos, with the sales module on). 404 when there is none.
 * {@code &trim=1} on a logo cuts the transparent margin around the artwork (logos are stored padded
 * to 4:1, so a square badge would otherwise show tiny in the public header).
 */
@WebServlet("/publicImage")
public class PublicImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer logo = parseId(request.getParameter("logo"));
        Integer photo = parseId(request.getParameter("photo"));
        Integer pagePhoto = parseId(request.getParameter("pagePhoto"));
        Integer storePhoto = parseId(request.getParameter("storePhoto"));
        String path = logo != null ? "business/getLogo/" + logo
                : photo != null ? "product/getStorePhoto/" + photo
                : storePhoto != null ? "product/getStorePhotoById/" + storePhoto
                : pagePhoto != null ? "businessPage/getPhoto/" + pagePhoto : null;
        byte[] bytes = null;
        if (path != null) {
            try {
                ImageVo vo = RestClient.get(path, ImageVo.class, null, new Locale("es"));
                if (vo != null && vo.getImageBase64() != null && !vo.getImageBase64().isBlank()) {
                    bytes = Base64.getMimeDecoder().decode(vo.getImageBase64());
                }
            } catch (RestException | IllegalArgumentException e) {
                bytes = null;
            }
        }
        if (bytes == null || bytes.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (logo != null && "1".equals(request.getParameter("trim"))) {
            bytes = trimTransparent(bytes);
        }
        response.setContentType(BusinessLogo.contentType(bytes));
        response.setHeader("Cache-Control", "public, max-age=3600");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    /**
     * PNG cropped to the non-transparent pixels plus a small breathing margin; the original bytes when
     * the image has no alpha, nothing to cut or cannot be read.
     */
    static byte[] trimTransparent(byte[] bytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null || !img.getColorModel().hasAlpha()) {
                return bytes;
            }
            int w = img.getWidth();
            int h = img.getHeight();
            int minX = w;
            int minY = h;
            int maxX = -1;
            int maxY = -1;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if ((img.getRGB(x, y) >>> 24) > 12) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            if (maxX < 0 || (maxX - minX + 1) * (maxY - minY + 1) > w * h * 0.92) {
                return bytes; // fully transparent, or almost no margin to cut
            }
            int pad = Math.max(2, Math.round((maxY - minY + 1) * 0.02f));
            minX = Math.max(0, minX - pad);
            minY = Math.max(0, minY - pad);
            maxX = Math.min(w - 1, maxX + pad);
            maxY = Math.min(h - 1, maxY + pad);
            BufferedImage out = img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            ImageIO.write(out, "png", buf);
            return buf.toByteArray();
        } catch (IOException | RuntimeException e) {
            return bytes;
        }
    }

    /**
     * Square favicon (128x128 PNG) of a logo: the artwork trimmed of its transparent margin and
     * centred on a transparent square, so a round or square badge fills the browser tab and a wide
     * logo stays readable instead of shrinking to a thin strip. The original bytes when unreadable.
     */
    static byte[] squareIcon(byte[] bytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(trimTransparent(bytes)));
            if (img == null) {
                return bytes;
            }
            final int size = 128;
            double scale = Math.min((double) size / img.getWidth(), (double) size / img.getHeight());
            int w = Math.max(1, (int) Math.round(img.getWidth() * scale));
            int h = Math.max(1, (int) Math.round(img.getHeight() * scale));
            BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = out.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(img, (size - w) / 2, (size - h) / 2, w, h, null);
            g.dispose();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            ImageIO.write(out, "png", buf);
            return buf.toByteArray();
        } catch (IOException | RuntimeException e) {
            return bytes;
        }
    }

    private static Integer parseId(String value) {
        try {
            return value != null ? Integer.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
