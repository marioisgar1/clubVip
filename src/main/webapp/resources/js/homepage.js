(function(){
  /* ---- menú en móvil ---- */
  var nav=document.querySelector('.hp-nav'), burger=document.getElementById('hpBurger');
  function setMenu(open){
    nav.classList.toggle('open',open);
    burger.setAttribute('aria-expanded',open?'true':'false');
  }
  if(burger){
    burger.addEventListener('click',function(){setMenu(!nav.classList.contains('open'))});
    [].forEach.call(document.querySelectorAll('.hp-links a'),function(a){a.addEventListener('click',function(){setMenu(false)})});
    document.addEventListener('keydown',function(e){if(e.key==='Escape'&&nav.classList.contains('open')){setMenu(false);burger.focus();}});
  }

  /* ---- WhatsApp: espacios como %20 (algunos clientes muestran el "+") ---- */
  var chat=document.querySelector('.hp-chat');
  if(chat) chat.href=chat.href.replace(/\+/g,'%20');

  /* ---- prender / apagar módulos ---- */
  var sw=document.getElementById('whoSwitch'), who=document.getElementById('para-quien');
  if(sw) sw.addEventListener('click',function(){
    var on=sw.getAttribute('aria-pressed')!=='true';
    sw.setAttribute('aria-pressed',on?'true':'false');
    who.classList.toggle('off',!on);
  });

  /* ---- calculadora ---- */
  var fmt=new Intl.NumberFormat('es-MX',{style:'currency',currency:'MXN',maximumFractionDigits:0});
  var ns=document.getElementById('noShow'),tk=document.getElementById('ticket'),ex=document.getElementById('extra');
  if(!ns) return;
  function fill(i){i.style.setProperty('--p',((i.value-i.min)/(i.max-i.min)*100)+'%')}
  function calc(){
    var n=+ns.value,t=+tk.value,e=+ex.value;
    document.getElementById('noShowOut').textContent=n;
    document.getElementById('ticketOut').textContent=fmt.format(t);
    document.getElementById('extraOut').textContent=fmt.format(e);
    var m=Math.round(n*(t+e)*4.33/10)*10;
    var yr=document.getElementById('yearOut');
    document.getElementById('lossOut').textContent=fmt.format(m);
    yr.textContent='≈ '+fmt.format(m*12)+' '+yr.dataset.suffix;
    [ns,tk,ex].forEach(fill);
  }
  [ns,tk,ex].forEach(function(i){i.addEventListener('input',calc)});
  calc();
})();
