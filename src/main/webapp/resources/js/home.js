(function(){
  /* ---- hero tabs ---- */
  var tabs=[].slice.call(document.querySelectorAll('#heroTabs .hl'));
  var heroTabs=document.getElementById('heroTabs');
  var reduce=window.matchMedia&&window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var DUR=6500, idx=0, timer=null;
  if(reduce) heroTabs.classList.add('static');
  tabs.forEach(function(t){ if(!t.querySelector('.hl-bar')){var b=document.createElement('span');b.className='hl-bar';t.appendChild(b);} });
  function show(i,focus){
    idx=(i+tabs.length)%tabs.length;
    tabs.forEach(function(t,k){
      var on=k===idx;
      t.setAttribute('aria-selected',on?'true':'false');
      t.tabIndex=on?0:-1;
      document.getElementById(t.getAttribute('aria-controls')).classList.toggle('on',on);
      if(on){var bar=t.querySelector('.hl-bar');bar.style.animation='none';void bar.offsetWidth;bar.style.animation='';}
    });
    if(focus) tabs[idx].focus();
    schedule();
  }
  function schedule(){
    clearTimeout(timer);
    if(reduce||heroTabs.classList.contains('paused')) return;
    timer=setTimeout(function(){show(idx+1)},DUR);
  }
  tabs.forEach(function(t,k){
    t.addEventListener('click',function(){heroTabs.classList.add('paused');show(k);});
    t.addEventListener('keydown',function(e){
      if(e.key==='ArrowDown'||e.key==='ArrowRight'){e.preventDefault();heroTabs.classList.add('paused');show(idx+1,true);}
      if(e.key==='ArrowUp'||e.key==='ArrowLeft'){e.preventDefault();heroTabs.classList.add('paused');show(idx-1,true);}
    });
  });
  var hero=document.querySelector('.hero');
  hero.addEventListener('mouseenter',function(){if(!heroTabs.classList.contains('paused')){clearTimeout(timer);heroTabs.classList.add('hover');}});
  hero.addEventListener('mouseleave',function(){if(heroTabs.classList.contains('hover')){heroTabs.classList.remove('hover');show(idx);}});
  schedule();

  /* ---- modules ---- */
  var mods=[].slice.call(document.querySelectorAll('#mods .mod'));
  var box=document.getElementById('modSummary'), d=box.dataset;
  var note='';
  function byKey(k){return mods.filter(function(m){return m.dataset.key===k})[0]}
  function isOn(m){return m.getAttribute('aria-pressed')==='true'}
  function esc(t){var e=document.createElement('span');e.textContent=t;return e.innerHTML}
  function sum(){
    var on=mods.filter(isOn);
    var txt=document.getElementById('sumText'), hint=document.getElementById('sumHint');
    if(!on.length){txt.innerHTML=esc(d.prefix)+' <em>'+esc(d.empty)+'</em>';hint.textContent=d.emptyHint;return;}
    txt.innerHTML=esc(d.prefix)+' <em>'+esc(on.map(function(m){return m.dataset.name}).join(' + '))+'</em>';
    var g=on.map(function(m){return m.dataset.gain});
    var line=on.length===mods.length?d.all
      :d.for+' '+(g.length>1?g.slice(0,-1).join(', ')+' '+d.and+' '+g[g.length-1]:g[0])+'.';
    hint.textContent=line+(note?' '+note:'');
    note='';
  }
  mods.forEach(function(m){m.addEventListener('click',function(){
    m.setAttribute('aria-pressed',isOn(m)?'false':'true');
    var pos=byKey('pos'), cons=byKey('cons');
    if(m===cons&&isOn(cons)&&!isOn(pos)){pos.setAttribute('aria-pressed','true');note=d.noteOn;}
    if(m===pos&&!isOn(pos)&&isOn(cons)){cons.setAttribute('aria-pressed','false');note=d.noteOff;}
    sum();
  });});
  sum();

  /* ---- calculator ---- */
  var fmt=new Intl.NumberFormat('es-MX',{style:'currency',currency:'MXN',maximumFractionDigits:0});
  var ns=document.getElementById('noShow'),tk=document.getElementById('ticket'),ex=document.getElementById('extra');
  function calc(){
    var n=+ns.value,t=+tk.value,e=+ex.value;
    document.getElementById('noShowOut').textContent=n;
    document.getElementById('ticketOut').textContent=fmt.format(t);
    document.getElementById('extraOut').textContent=fmt.format(e);
    var m=Math.round(n*(t+e)*4.33/10)*10;
    document.getElementById('lossOut').textContent=fmt.format(m);
    document.getElementById('yearOut').textContent='≈ '+fmt.format(m*12)+' '+document.getElementById('yearOut').dataset.suffix;
  }
  [ns,tk,ex].forEach(function(i){i.addEventListener('input',calc)});
  calc();
})();
