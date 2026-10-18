/* Business logo cropper (business.xhtml, register.xhtml).
   Every logo is stored in one standard format, LOGO_W x LOGO_H (4:1), so the same file fills the
   menu column of the web topbar and the drawer header of the app. When the picked image already
   has that shape it is only downscaled; otherwise this dialog lets the user place it inside a 4:1
   frame: drag to move, zoom from "whole logo centered" (letterboxed, transparent) up to a crop.
   Usage: cvLogoCrop.pick(fileInput, function (dataUrl) {...}); texts come from window.cvLogoCropTexts. */
var cvLogoCrop = (function () {
    var LOGO_W = 960, LOGO_H = 240, RATIO = LOGO_W / LOGO_H;
    var VIEW_W = 480, VIEW_H = 120;
    var state = null;

    var css = '.cvlc-backdrop{position:fixed;inset:0;z-index:100000;background:rgba(11,11,12,.62);display:flex;align-items:center;justify-content:center;padding:16px;font-family:Poppins,system-ui,sans-serif}'
        + '.cvlc{background:#fff;border-radius:24px;box-shadow:0 24px 60px rgba(0,0,0,.3);width:min(560px,100%);padding:22px 24px 20px;box-sizing:border-box;color:#292827}'
        + '.cvlc h3{margin:0 0 4px;font-size:1.1rem;font-weight:600;color:#18181B}'
        + '.cvlc p{margin:0 0 14px;font-size:.84rem;color:#5D5F66;line-height:1.5}'
        + '.cvlc-stage{position:relative;width:100%;aspect-ratio:4/1;border-radius:14px;overflow:hidden;cursor:grab;touch-action:none;background:#FBFCF5 repeating-conic-gradient(#EEF0E2 0 25%,#FBFCF5 0 50%) 0 0/16px 16px;box-shadow:inset 0 0 0 2px #CEF187}'
        + '.cvlc-stage.drag{cursor:grabbing}.cvlc-stage canvas{width:100%;height:100%;display:block}'
        + '.cvlc-row{display:flex;align-items:center;gap:10px;margin-top:14px;flex-wrap:wrap}'
        + '.cvlc-row label{font-size:.78rem;font-weight:600;color:#5B7720;text-transform:uppercase;letter-spacing:.05em}'
        + '.cvlc-row input[type=range]{flex:1;min-width:120px;accent-color:#5B7720}'
        + '.cvlc-modes{margin-top:10px}.cvlc-modes .cvlc-btn{flex:1}'
        + '.cvlc-btn{border:1px solid #E4E2DA;background:#fff;border-radius:999px;padding:8px 16px;font:inherit;font-size:.82rem;font-weight:600;cursor:pointer;color:#292827}'
        + '.cvlc-btn:hover{border-color:#5B7720}.cvlc-btn.on{background:#CEF187;border-color:#CEF187;color:#0B0B0C}'
        + '.cvlc-actions{display:flex;justify-content:flex-end;gap:10px;margin-top:18px}'
        + '.cvlc-primary{background:#CEF187;border-color:#CEF187;color:#0B0B0C;padding:10px 22px}.cvlc-primary:hover{box-shadow:0 6px 18px rgba(91,119,32,.25)}'
        + '@media (max-width:480px){.cvlc{padding:18px 16px}.cvlc-actions{flex-direction:column-reverse}.cvlc-actions .cvlc-btn{width:100%}}';

    function t(key, fallback) {
        var texts = window.cvLogoCropTexts || {};
        return texts[key] || fallback;
    }

    function ensureStyle() {
        if (document.getElementById('cvlc-style')) { return; }
        var s = document.createElement('style');
        s.id = 'cvlc-style';
        s.textContent = css;
        document.head.appendChild(s);
    }

    /* Draws the image into the given canvas with the current zoom/offset (logo pixel space). */
    function draw(canvas, w, h) {
        var ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, w, h);
        var k = w / LOGO_W;
        var dw = state.img.width * state.scale * k, dh = state.img.height * state.scale * k;
        var dx = (w - dw) / 2 + state.ox * k, dy = (h - dh) / 2 + state.oy * k;
        ctx.imageSmoothingQuality = 'high';
        ctx.drawImage(state.img, dx, dy, dw, dh);
    }

    /* Keeps the image inside the frame when it is larger than it, centered when smaller. */
    function clamp() {
        var dw = state.img.width * state.scale, dh = state.img.height * state.scale;
        var mx = Math.max(0, (dw - LOGO_W) / 2), my = Math.max(0, (dh - LOGO_H) / 2);
        state.ox = Math.min(mx, Math.max(-mx, state.ox));
        state.oy = Math.min(my, Math.max(-my, state.oy));
    }

    function setScale(scale) {
        state.scale = scale;
        clamp();
        state.slider.value = Math.round(100 * (scale - state.fit) / (state.cover * 1.5 - state.fit));
        state.fitBtn.classList.toggle('on', Math.abs(scale - state.fit) < 1e-6);
        state.fillBtn.classList.toggle('on', Math.abs(scale - state.cover) < 1e-6);
        draw(state.canvas, VIEW_W, VIEW_H);
    }

    function exportLogo() {
        var out = document.createElement('canvas');
        out.width = LOGO_W;
        out.height = LOGO_H;
        draw(out, LOGO_W, LOGO_H);
        return out.toDataURL('image/png');
    }

    function close() {
        if (state && state.root) { state.root.remove(); }
        state = null;
    }

    function open(img, onDone) {
        ensureStyle();
        var fit = Math.min(LOGO_W / img.width, LOGO_H / img.height);
        var cover = Math.max(LOGO_W / img.width, LOGO_H / img.height);
        var root = document.createElement('div');
        root.className = 'cvlc-backdrop';
        root.innerHTML = '<div class="cvlc" role="dialog" aria-modal="true">'
            + '<h3></h3><p></p>'
            + '<div class="cvlc-stage"><canvas width="' + VIEW_W + '" height="' + VIEW_H + '"></canvas></div>'
            + '<div class="cvlc-row"><label></label><input type="range" min="0" max="100" value="0"/></div>'
            + '<div class="cvlc-row cvlc-modes"><button type="button" class="cvlc-btn cvlc-fit"></button><button type="button" class="cvlc-btn cvlc-fill"></button></div>'
            + '<div class="cvlc-actions"><button type="button" class="cvlc-btn cvlc-cancel"></button>'
            + '<button type="button" class="cvlc-btn cvlc-primary cvlc-use"></button></div></div>';
        root.querySelector('h3').textContent = t('title', 'Ajusta tu logotipo');
        root.querySelector('p').textContent = t('hint', 'El logotipo se guarda en formato horizontal 4:1. Arrástralo y usa el zoom para acomodarlo.');
        root.querySelector('label').textContent = t('zoom', 'Zoom');
        root.querySelector('.cvlc-fit').textContent = t('fit', 'Completo');
        root.querySelector('.cvlc-fill').textContent = t('fill', 'Llenar');
        root.querySelector('.cvlc-cancel').textContent = t('cancel', 'Cancelar');
        root.querySelector('.cvlc-use').textContent = t('use', 'Usar logotipo');
        document.body.appendChild(root);

        state = {img: img, fit: fit, cover: cover, scale: fit, ox: 0, oy: 0, root: root,
            canvas: root.querySelector('canvas'), slider: root.querySelector('input'),
            fitBtn: root.querySelector('.cvlc-fit'), fillBtn: root.querySelector('.cvlc-fill')};
        setScale(fit);

        state.slider.oninput = function () {
            setScale(fit + (cover * 1.5 - fit) * (this.value / 100));
        };
        state.fitBtn.onclick = function () { state.ox = state.oy = 0; setScale(fit); };
        state.fillBtn.onclick = function () { setScale(cover); };
        root.querySelector('.cvlc-cancel').onclick = close;
        root.querySelector('.cvlc-use').onclick = function () {
            var data = exportLogo();
            close();
            onDone(data);
        };
        root.addEventListener('keydown', function (e) { if (e.key === 'Escape') { close(); } });

        // Drag with mouse or touch; offsets are in logo pixels, so scale by the stage size.
        var stage = root.querySelector('.cvlc-stage'), last = null;
        stage.addEventListener('pointerdown', function (e) {
            last = {x: e.clientX, y: e.clientY};
            stage.classList.add('drag');
            stage.setPointerCapture(e.pointerId);
        });
        stage.addEventListener('pointermove', function (e) {
            if (!last) { return; }
            var k = LOGO_W / stage.clientWidth;
            state.ox += (e.clientX - last.x) * k;
            state.oy += (e.clientY - last.y) * k;
            last = {x: e.clientX, y: e.clientY};
            clamp();
            draw(state.canvas, VIEW_W, VIEW_H);
        });
        var end = function () { last = null; stage.classList.remove('drag'); };
        stage.addEventListener('pointerup', end);
        stage.addEventListener('pointercancel', end);
        stage.addEventListener('wheel', function (e) {
            e.preventDefault();
            var next = Math.min(cover * 1.5, Math.max(fit, state.scale * (e.deltaY < 0 ? 1.06 : 0.94)));
            setScale(next);
        }, {passive: false});
    }

    /* Entry point from the file input: already 4:1 → straight downscale, else the dialog. */
    function pick(input, onDone) {
        if (!input.files || !input.files[0]) { return; }
        var reader = new FileReader();
        reader.onload = function (e) {
            var img = new Image();
            img.onload = function () {
                input.value = '';
                if (Math.abs(img.width / img.height - RATIO) < 0.02) {
                    state = {img: img, scale: LOGO_W / img.width, ox: 0, oy: 0};
                    var data = exportLogo();
                    state = null;
                    onDone(data);
                } else {
                    open(img, onDone);
                }
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(input.files[0]);
    }

    return {pick: pick, WIDTH: LOGO_W, HEIGHT: LOGO_H};
})();
