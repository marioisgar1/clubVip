/* Portrait cropper (employee.xhtml). People photos are shown inside a circle on the web and in the
   app, so the user frames the face: drag to move, zoom from "fill the square" up to 3x. The result
   is a square OUT x OUT JPEG, so the circle shows exactly what was framed here.
   Usage: cvPhotoCrop.pick(fileInput, function (dataUrl) {...}); texts come from window.cvPhotoCropTexts. */
var cvPhotoCrop = (function () {
    var OUT = 512, VIEW = 320, MAX_ZOOM = 3;
    var state = null;

    var css = '.cvpc-backdrop{position:fixed;inset:0;z-index:100000;background:rgba(11,11,12,.62);display:flex;align-items:center;justify-content:center;padding:16px;font-family:Poppins,system-ui,sans-serif}'
        + '.cvpc{background:#fff;border-radius:24px;box-shadow:0 24px 60px rgba(0,0,0,.3);width:min(440px,100%);padding:22px 24px 20px;box-sizing:border-box;color:#292827}'
        + '.cvpc h3{margin:0 0 4px;font-size:1.1rem;font-weight:600;color:#18181B}'
        + '.cvpc p{margin:0 0 14px;font-size:.84rem;color:#5D5F66;line-height:1.5}'
        + '.cvpc-stage{position:relative;width:min(' + VIEW + 'px,100%);aspect-ratio:1/1;margin:0 auto;border-radius:18px;overflow:hidden;cursor:grab;touch-action:none;background:#111;user-select:none}'
        + '.cvpc-stage.drag{cursor:grabbing}.cvpc-stage canvas{width:100%;height:100%;display:block}'
        + '.cvpc-mask{position:absolute;inset:0;pointer-events:none;background:radial-gradient(circle closest-side,transparent 98.5%,rgba(11,11,12,.6) 100%)}'
        + '.cvpc-ring{position:absolute;inset:0;border-radius:50%;pointer-events:none;box-shadow:inset 0 0 0 2px #CEF187,0 0 0 1px rgba(255,255,255,.25)}'
        + '.cvpc-row{display:flex;align-items:center;gap:10px;margin-top:14px;flex-wrap:wrap}'
        + '.cvpc-row label{font-size:.78rem;font-weight:600;color:#5B7720;text-transform:uppercase;letter-spacing:.05em}'
        + '.cvpc-row input[type=range]{flex:1;min-width:120px;accent-color:#5B7720}'
        + '.cvpc-btn{border:1px solid #E4E2DA;background:#fff;border-radius:999px;padding:8px 16px;font:inherit;font-size:.82rem;font-weight:600;cursor:pointer;color:#292827}'
        + '.cvpc-btn:hover{border-color:#5B7720}'
        + '.cvpc-actions{display:flex;justify-content:flex-end;gap:10px;margin-top:18px}'
        + '.cvpc-actions .cvpc-center{margin-right:auto}'
        + '.cvpc-primary{background:#CEF187;border-color:#CEF187;color:#0B0B0C;padding:10px 22px}.cvpc-primary:hover{box-shadow:0 6px 18px rgba(91,119,32,.25)}'
        + '@media (max-width:480px){.cvpc{padding:18px 16px}.cvpc-actions{flex-direction:column-reverse}.cvpc-actions .cvpc-btn{width:100%;margin:0}}';

    function t(key, fallback) {
        var texts = window.cvPhotoCropTexts || {};
        return texts[key] || fallback;
    }

    function ensureStyle() {
        if (document.getElementById('cvpc-style')) { return; }
        var s = document.createElement('style');
        s.id = 'cvpc-style';
        s.textContent = css;
        document.head.appendChild(s);
    }

    /* Draws the image with the current zoom/offset (offsets in output pixels). */
    function draw(canvas, size) {
        var ctx = canvas.getContext('2d');
        ctx.fillStyle = '#FFFFFF';
        ctx.fillRect(0, 0, size, size);
        var k = size / OUT;
        var dw = state.img.width * state.scale * k, dh = state.img.height * state.scale * k;
        var dx = (size - dw) / 2 + state.ox * k, dy = (size - dh) / 2 + state.oy * k;
        ctx.imageSmoothingQuality = 'high';
        ctx.drawImage(state.img, dx, dy, dw, dh);
    }

    /* The photo always covers the whole square: no empty corners inside the circle. */
    function clamp() {
        var dw = state.img.width * state.scale, dh = state.img.height * state.scale;
        var mx = Math.max(0, (dw - OUT) / 2), my = Math.max(0, (dh - OUT) / 2);
        state.ox = Math.min(mx, Math.max(-mx, state.ox));
        state.oy = Math.min(my, Math.max(-my, state.oy));
    }

    function setScale(scale) {
        state.scale = Math.min(state.cover * MAX_ZOOM, Math.max(state.cover, scale));
        clamp();
        state.slider.value = Math.round(100 * (state.scale - state.cover) / (state.cover * (MAX_ZOOM - 1)));
        draw(state.canvas, VIEW);
    }

    function exportPhoto() {
        var out = document.createElement('canvas');
        out.width = OUT;
        out.height = OUT;
        draw(out, OUT);
        return out.toDataURL('image/jpeg', 0.88);
    }

    function close() {
        if (state && state.root) { state.root.remove(); }
        state = null;
    }

    function open(img, onDone) {
        ensureStyle();
        var cover = Math.max(OUT / img.width, OUT / img.height);
        var root = document.createElement('div');
        root.className = 'cvpc-backdrop';
        root.innerHTML = '<div class="cvpc" role="dialog" aria-modal="true">'
            + '<h3></h3><p></p>'
            + '<div class="cvpc-stage"><canvas width="' + VIEW + '" height="' + VIEW + '"></canvas><div class="cvpc-mask"></div><div class="cvpc-ring"></div></div>'
            + '<div class="cvpc-row"><label></label><input type="range" min="0" max="100" value="0"/></div>'
            + '<div class="cvpc-actions"><button type="button" class="cvpc-btn cvpc-center"></button>'
            + '<button type="button" class="cvpc-btn cvpc-cancel"></button>'
            + '<button type="button" class="cvpc-btn cvpc-primary cvpc-use"></button></div></div>';
        root.querySelector('h3').textContent = t('title', 'Encuadra la foto');
        root.querySelector('p').textContent = t('hint', 'Arrastra la foto y usa el zoom para que la cara quede dentro del círculo.');
        root.querySelector('label').textContent = t('zoom', 'Zoom');
        root.querySelector('.cvpc-center').textContent = t('center', 'Centrar');
        root.querySelector('.cvpc-cancel').textContent = t('cancel', 'Cancelar');
        root.querySelector('.cvpc-use').textContent = t('use', 'Usar foto');
        document.body.appendChild(root);

        state = {img: img, cover: cover, scale: cover, ox: 0, oy: 0, root: root,
            canvas: root.querySelector('canvas'), slider: root.querySelector('input')};
        setScale(cover);

        state.slider.oninput = function () {
            setScale(cover + cover * (MAX_ZOOM - 1) * (this.value / 100));
        };
        root.querySelector('.cvpc-center').onclick = function () { state.ox = state.oy = 0; setScale(cover); };
        root.querySelector('.cvpc-cancel').onclick = close;
        root.querySelector('.cvpc-use').onclick = function () {
            var data = exportPhoto();
            close();
            onDone(data);
        };
        root.addEventListener('keydown', function (e) { if (e.key === 'Escape') { close(); } });

        // Drag with mouse or touch; offsets are in output pixels, so scale by the stage size.
        var stage = root.querySelector('.cvpc-stage'), last = null;
        stage.addEventListener('pointerdown', function (e) {
            last = {x: e.clientX, y: e.clientY};
            stage.classList.add('drag');
            stage.setPointerCapture(e.pointerId);
        });
        stage.addEventListener('pointermove', function (e) {
            if (!last) { return; }
            var k = OUT / stage.clientWidth;
            state.ox += (e.clientX - last.x) * k;
            state.oy += (e.clientY - last.y) * k;
            last = {x: e.clientX, y: e.clientY};
            clamp();
            draw(state.canvas, VIEW);
        });
        var end = function () { last = null; stage.classList.remove('drag'); };
        stage.addEventListener('pointerup', end);
        stage.addEventListener('pointercancel', end);
        stage.addEventListener('wheel', function (e) {
            e.preventDefault();
            setScale(state.scale * (e.deltaY < 0 ? 1.06 : 0.94));
        }, {passive: false});
    }

    /* Entry point from the file input: the dialog always opens, so the user frames every photo. */
    function pick(input, onDone) {
        if (!input.files || !input.files[0]) { return; }
        var reader = new FileReader();
        reader.onload = function (e) {
            openSrc(e.target.result, onDone);
            input.value = '';
        };
        reader.readAsDataURL(input.files[0]);
    }

    function openSrc(src, onDone) {
        var img = new Image();
        img.onload = function () { open(img, onDone); };
        img.src = src;
    }

    return {pick: pick, openSrc: openSrc, SIZE: OUT};
})();
