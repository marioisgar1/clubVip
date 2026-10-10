/* "Mi página" editor (pages/myPage.xhtml).
   - Template and color are applied to the preview iframe at once (same math as PageTheme.java).
   - Any other change is saved by itself (debounced) and the preview reloads.
   - Photos are shrunk in the browser (max 1600 px, JPEG) before they are uploaded. */
(function () {
  'use strict';

  var saveTimer = null;

  function $(sel) { return document.querySelector(sel); }
  function frame() { return document.getElementById('mpFrame'); }
  function frameDoc() {
    var f = frame();
    try { return f && f.contentDocument; } catch (e) { return null; }
  }
  function selectedTemplate() { return $('.mp-tpl.sel'); }

  /* ------------------------------------------------------------ color math */
  function rgb(hex) {
    var h = hex.replace('#', '');
    return [parseInt(h.substr(0, 2), 16), parseInt(h.substr(2, 2), 16), parseInt(h.substr(4, 2), 16)];
  }
  function hex(c) {
    return '#' + c.map(function (v) { return ('0' + v.toString(16)).slice(-2); }).join('').toUpperCase();
  }
  function lum(c) {
    var ch = c.map(function (v) { v = v / 255; return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4); });
    return 0.2126 * ch[0] + 0.7152 * ch[1] + 0.0722 * ch[2];
  }
  function contrast(a, b) {
    var la = lum(a), lb = lum(b);
    return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
  }
  function mix(a, b, k) { return [0, 1, 2].map(function (i) { return Math.round(a[i] * k + b[i] * (1 - k)); }); }
  function ensure(color, bg, ratio) {
    var target = lum(bg) > 0.5 ? [0, 0, 0] : [255, 255, 255];
    var c = color;
    for (var i = 0; i < 20 && contrast(c, bg) < ratio; i++) { c = mix(target, color, (i + 1) * 0.05); }
    return c;
  }
  function validHex(v) { return /^#[0-9A-Fa-f]{6}$/.test(v || ''); }

  function colorVars(accent, bgHex, dark) {
    var bg = rgb(bgHex), base = rgb(accent);
    var fill = ensure(base, bg, 1.8), text = ensure(base, bg, 4.5);
    var white = [255, 255, 255], ink = [20, 22, 26];
    var on = contrast(white, fill) >= contrast(ink, fill) ? white : ink;
    return '--accent:' + hex(fill) + ';--on-accent:' + hex(on) + ';--accent-text:' + hex(text) +
      ';--accent-soft:' + hex(mix(base, bg, dark ? 0.20 : 0.12)) + ';--accent-line:' + hex(mix(base, bg, dark ? 0.45 : 0.35));
  }

  /* ------------------------------------------------------------ live preview */
  function applyLive() {
    var doc = frameDoc(), tpl = selectedTemplate();
    if (!doc || !doc.body || !tpl || !doc.body.classList.contains('pg')) { return; }
    var body = doc.body;
    Array.prototype.slice.call(body.classList).forEach(function (c) {
      if (/^pg-(elegante|luminosa|mercado|minimal|editorial|noche|natural|urbano|boutique|fresco|dark)$/.test(c)) { body.classList.remove(c); }
    });
    body.classList.add(tpl.dataset.cls);
    if (tpl.dataset.dark === 'true') { body.classList.add('pg-dark'); }
    var accent = document.getElementById('form:accent').value;
    body.setAttribute('style', colorVars(validHex(accent) ? accent : tpl.dataset.default, tpl.dataset.bg, tpl.dataset.dark === 'true'));
    var fonts = doc.getElementById('pgFonts');
    if (fonts && fonts.getAttribute('href') !== tpl.dataset.fonts) { fonts.setAttribute('href', tpl.dataset.fonts); }
  }

  window.mpPickTemplate = function (btn) {
    document.querySelectorAll('.mp-tpl').forEach(function (b) { b.classList.remove('sel'); });
    btn.classList.add('sel');
    document.getElementById('form:template').value = btn.dataset.code;
    applyLive();
    mpQueueSave(400);
  };

  window.mpSetColor = function (value, persist) {
    var v = (value || '').trim();
    if (v && v.charAt(0) !== '#') { v = '#' + v; }
    if (!validHex(v)) { return; }
    v = v.toUpperCase();
    document.getElementById('form:accent').value = v;
    var picker = document.getElementById('mpColor'), field = document.getElementById('mpHex');
    if (picker && picker.value.toUpperCase() !== v) { picker.value = v.toLowerCase(); }
    if (field && field.value.toUpperCase() !== v) { field.value = v; }
    document.querySelectorAll('.mp-sw').forEach(function (s) { s.classList.toggle('sel', (s.title || '').toUpperCase() === v); });
    applyLive();
    if (persist) { mpQueueSave(400); }
  };

  /** Main color of the business logo: the average of its colorful pixels. */
  window.mpLogoColor = function (url) {
    if (!url) { return; }
    var img = new Image();
    img.onload = function () {
      var size = 48, canvas = document.createElement('canvas');
      canvas.width = size; canvas.height = size;
      var ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0, size, size);
      var data = ctx.getImageData(0, 0, size, size).data, r = 0, g = 0, b = 0, n = 0, rr = 0, gg = 0, bb = 0, m = 0;
      for (var i = 0; i < data.length; i += 4) {
        if (data[i + 3] < 128) { continue; }
        var R = data[i], G = data[i + 1], B = data[i + 2];
        var max = Math.max(R, G, B), min = Math.min(R, G, B);
        rr += R; gg += G; bb += B; m++;
        if (max - min > 40 && max > 40 && min < 225) { r += R; g += G; b += B; n++; }
      }
      if (n > 10) { mpSetColor(hex([Math.round(r / n), Math.round(g / n), Math.round(b / n)]), true); }
      else if (m > 0) { mpSetColor(hex([Math.round(rr / m), Math.round(gg / m), Math.round(bb / m)]), true); }
    };
    img.src = url + (url.indexOf('?') > 0 ? '&' : '?') + 't=' + Date.now();
  };

  /* ------------------------------------------------------------ saving */
  window.mpQueueSave = function (delay) {
    clearTimeout(saveTimer);
    saveTimer = setTimeout(function () { if (window.mpSave) { window.mpSave(); } }, delay || 900);
  };

  window.mpAfterSave = function () {
    var s = document.getElementById('mpSaved');
    if (s) { s.classList.add('show'); setTimeout(function () { s.classList.remove('show'); }, 1600); }
    mpReload();
  };

  window.mpReload = function () {
    var f = frame();
    if (!f) { return; }
    var y = 0;
    try { y = f.contentWindow.scrollY; } catch (e) { y = 0; }
    f.dataset.scroll = y;
    f.src = f.dataset.base + '&v=' + Date.now();
  };

  /* ------------------------------------------------------------ photos */
  window.mpReadPhoto = function (input, kind) {
    if (!input.files || !input.files[0]) { return; }
    var reader = new FileReader();
    reader.onload = function (e) {
      var img = new Image();
      img.onload = function () {
        var max = 1600, scale = Math.min(1, max / Math.max(img.width, img.height));
        var w = Math.round(img.width * scale), h = Math.round(img.height * scale);
        var canvas = document.createElement('canvas');
        canvas.width = w; canvas.height = h;
        var ctx = canvas.getContext('2d');
        ctx.fillStyle = '#FFFFFF'; ctx.fillRect(0, 0, w, h);
        ctx.drawImage(img, 0, 0, w, h);
        var dataUrl = canvas.toDataURL('image/jpeg', 0.82);
        var ta = $('textarea.mp-' + kind + '-b64');
        if (ta) { ta.value = dataUrl; }
        var wait = document.getElementById('mpUploading');
        if (wait) { wait.style.display = 'block'; }
        var btn = $('.mp-' + kind + '-up');
        if (btn) { btn.click(); }
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(input.files[0]);
    input.value = '';
  };

  window.mpPhotoDone = function () { mpReload(); };

  /* ------------------------------------------------------------ misc */
  window.mpDevice = function (btn) {
    document.querySelectorAll('.mp-dev').forEach(function (b) { b.classList.remove('sel'); });
    btn.classList.add('sel');
    var stage = document.getElementById('mpStage');
    if (stage) { stage.dataset.mode = btn.dataset.mode; }
  };

  window.mpCopy = function (btn) {
    var input = btn.parentNode.querySelector('input');
    var done = function () {
      btn.innerHTML = '<i class="pi pi-check"></i>';
      setTimeout(function () { btn.innerHTML = '<i class="pi pi-copy"></i>'; }, 1500);
    };
    if (navigator.clipboard && window.isSecureContext) {
      navigator.clipboard.writeText(input.value).then(done);
    } else { input.select(); document.execCommand('copy'); done(); }
  };

  function wire() {
    var f = frame();
    if (f && !f.dataset.wired) {
      f.dataset.wired = '1';
      f.addEventListener('load', function () {
        applyLive();
        var y = parseInt(f.dataset.scroll || '0', 10);
        if (y) { try { f.contentWindow.scrollTo(0, y); } catch (e) { /* cross-origin never happens here */ } }
      });
    }
    // Text fields save by themselves a moment after the owner stops typing.
    document.addEventListener('input', function (e) {
      if (e.target && e.target.classList && e.target.classList.contains('mp-auto')) { mpQueueSave(1200); }
    });
    var accent = document.getElementById('form:accent');
    if (accent) { mpSetColor(accent.value, false); }
  }

  if (document.readyState === 'loading') { document.addEventListener('DOMContentLoaded', wire); } else { wire(); }
})();
