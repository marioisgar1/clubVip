/*
 * Public booking (/b/{slug}/citas), step 1: client-side search, sort and specialty chips over the
 * service cards, plus the collapsible specialty groups (<details>). The list is re-rendered by
 * PrimeFaces ajax every time a service is chosen, so the state lives here and is re-applied after
 * each update (pfAjaxComplete). Purely visual: choosing a service still goes through the server.
 */
(function () {
    var state = { q: '', group: '', sort: '', open: {}, selected: null };

    function norm(s) {
        return (s || '').toString().toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
    }
    function $all(sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); }
    function container() { return document.querySelector('.bk-groups'); }

    function apply() {
        var root = container();
        if (!root) { return; }
        // A new choice re-folds the groups to their default (the chosen one open, the rest closed).
        var sel = root.getAttribute('data-selected') || '';
        if (state.selected !== null && state.selected !== sel) { state.open = {}; }
        state.selected = sel;

        // Restore the controls (they are re-rendered too).
        var input = document.querySelector('.bk-search-input');
        if (input && input.value !== state.q) { input.value = state.q; }
        var select = document.querySelector('.bk-sort-select');
        if (select && select.value !== state.sort) { select.value = state.sort; }
        $all('.bk-fchip').forEach(function (b) {
            var on = (b.getAttribute('data-group') || '') === state.group;
            b.classList.toggle('on', on);
            b.setAttribute('aria-pressed', on ? 'true' : 'false');
        });

        var q = norm(state.q).trim();
        var words = q ? q.split(/\s+/) : [];
        var total = 0;
        var lists = $all('.bk-group').length ? $all('.bk-group') : [root];
        lists.forEach(function (box) {
            var isGroup = box.classList.contains('bk-group');
            var key = isGroup ? box.getAttribute('data-group') : '';
            var groupOk = !isGroup || !state.group || state.group === key;
            var visible = 0;
            var cards = $all('.bk-service', box);
            cards.forEach(function (c) {
                var text = norm(c.getAttribute('data-search'));
                var ok = groupOk && words.every(function (w) { return text.indexOf(w) >= 0; });
                c.style.display = ok ? '' : 'none';
                if (ok) { visible++; }
            });
            sortCards(box.classList.contains('bk-services') ? box : box.querySelector('.bk-services'), cards);
            total += visible;
            if (isGroup) {
                box.style.display = visible ? '' : 'none';
                // Searching or picking a chip opens what matches; otherwise the user's choice or the default.
                var open;
                if (q || state.group) { open = visible > 0; }
                else if (state.open.hasOwnProperty(key)) { open = state.open[key]; }
                else { open = box.getAttribute('data-default-open') === 'true'; }
                box.open = open;
            }
        });
        var empty = document.querySelector('.bk-filter-empty');
        if (empty) { empty.style.display = total ? 'none' : ''; }
    }

    function sortCards(list, cards) {
        if (!list) { return; }
        cards.forEach(function (c, i) { if (!c.hasAttribute('data-order')) { c.setAttribute('data-order', i); } });
        var key = state.sort;
        var sorted = cards.slice().sort(function (a, b) {
            var d = 0;
            if (key === 'price-asc' || key === 'price-desc') {
                d = parseFloat(a.getAttribute('data-price') || 0) - parseFloat(b.getAttribute('data-price') || 0);
                if (key === 'price-desc') { d = -d; }
            } else if (key === 'minutes-asc') {
                d = parseInt(a.getAttribute('data-minutes') || 0, 10) - parseInt(b.getAttribute('data-minutes') || 0, 10);
            }
            return d || (a.getAttribute('data-order') - b.getAttribute('data-order'));
        });
        sorted.forEach(function (c) { list.appendChild(c); });
    }

    document.addEventListener('input', function (e) {
        if (e.target.classList && e.target.classList.contains('bk-search-input')) { state.q = e.target.value; apply(); }
    });
    document.addEventListener('change', function (e) {
        if (e.target.classList && e.target.classList.contains('bk-sort-select')) { state.sort = e.target.value; apply(); }
    });
    document.addEventListener('click', function (e) {
        var chip = e.target.closest && e.target.closest('.bk-fchip');
        if (chip) {
            var g = chip.getAttribute('data-group') || '';
            state.group = state.group === g ? '' : g;
            apply();
            return;
        }
        if (e.target.closest && e.target.closest('.bk-filter-clear')) {
            state.q = ''; state.group = ''; state.sort = '';
            apply();
            return;
        }
        // Remember a manual fold / unfold (the toggle happens after the click).
        var head = e.target.closest && e.target.closest('.bk-group-head');
        if (head) {
            var box = head.parentNode;
            setTimeout(function () { state.open[box.getAttribute('data-group')] = box.open; }, 0);
        }
    });
    document.addEventListener('DOMContentLoaded', apply);
    if (window.jQuery) { jQuery(document).on('pfAjaxComplete', apply); }
    apply();
})();
