(function(){
  var nav=document.querySelector('.hp-nav'), burger=document.getElementById('hpBurger');

  /* Los efectos de sección esperan a que la página deje de moverse: al picar el menú el scroll suave cruza la
     sección y, sin esto, el efecto ya habría terminado al llegar. whenStill(el, fn) corre fn cuando no hubo
     scroll en 180 ms y sólo si el elemento sigue en pantalla (el._vis lo mantienen los observers). */
  var lastScroll=0;
  window.addEventListener('scroll',function(){lastScroll=Date.now()},{passive:true});
  function whenStill(el,fn){
    clearTimeout(el._still);
    (function chk(){
      var w=Date.now()-lastScroll;
      if(w<180){el._still=setTimeout(chk,180-w);return;}
      if(el._vis) fn();
    })();
  }

  /* ---- revelado al hacer scroll (sólo si <html class="hp-io">, lo pone un script en el head) ---- */
  if(document.documentElement.classList.contains('hp-io')){
    var revs=document.querySelectorAll('[data-reveal]');
    [].forEach.call(revs,function(el){
      if(/\bstagger\b/.test(el.getAttribute('data-reveal'))){
        [].forEach.call(el.children,function(c,i){c.style.setProperty('--i',Math.min(i,7))});
      }
    });
    var isReplay=function(el){return /\breplay\b/.test(el.getAttribute('data-reveal'))};
    /* revelado de una sola vez */
    var io=new IntersectionObserver(function(entries){
      entries.forEach(function(e){
        if(e.isIntersecting){e.target.classList.add('is-in');io.unobserve(e.target);}
      });
    },{threshold:.08,rootMargin:'0px 0px -8% 0px'});
    /* "replay": se repite cada vez que la sección vuelve a mostrarse (subiendo, bajando o desde el menú);
       arranca cuando la página ya se detuvo y se ve al menos una cuarta parte */
    var rio=new IntersectionObserver(function(entries){
      entries.forEach(function(e){
        var el=e.target; el._vis=e.isIntersecting;
        if(e.isIntersecting) whenStill(el,function(){el.classList.add('is-in')});
        else el.classList.remove('is-in');
      });
    },{threshold:.25});
    [].forEach.call(revs,function(el){(isReplay(el)?rio:io).observe(el)});
    /* red de seguridad: nada se queda oculto si algo falla */
    window.addEventListener('load',function(){setTimeout(function(){
      [].forEach.call(revs,function(el){
        var r=el.getBoundingClientRect();
        if(r.top<window.innerHeight&&r.bottom>0) el.classList.add('is-in');
      });
    },1200)});
  }

  /* ---- al picar el menú, la sección queda justo debajo del nav: el margen de scroll es la altura real del nav
          (cambia con el ancho de pantalla), no un número fijo ---- */
  if(nav){
    var fitPad=function(){document.documentElement.style.scrollPaddingTop=nav.offsetHeight+'px'};
    fitPad(); window.addEventListener('resize',fitPad);
  }

  /* ---- sombra del nav al dejar el tope ---- */
  if(nav){
    var onScroll=function(){nav.classList.toggle('scrolled',window.scrollY>8)};
    window.addEventListener('scroll',onScroll,{passive:true}); onScroll();
  }

  /* ---- menú en móvil ---- */
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

  var motion=document.documentElement.classList.contains('hp-io');

  /* ---- propulsores: al entrar en pantalla, un "mouse over" recorre los cuatro, uno tras otro (una pasada por entrada) ---- */
  var boost=document.getElementById('propulsores');
  if(boost&&motion){
    var tiles=boost.querySelectorAll('.hp-boost-item'), tour=null, seen=false, held=false;
    var lit=function(i){[].forEach.call(tiles,function(t,j){t.classList.toggle('is-lit',j===i)})};
    var stopTour=function(){clearTimeout(tour);tour=null;lit(-1)};
    var runTour=function(i){
      if(i>=tiles.length){stopTour();return;}
      lit(i); tour=setTimeout(function(){runTour(i+1)},1500);
    };
    new IntersectionObserver(function(es){
      var vis=boost._vis=es[0].isIntersecting;
      if(vis&&!seen&&!held){seen=true;stopTour();whenStill(boost,function(){if(!held)tour=setTimeout(function(){runTour(0)},250)});}
      else if(!vis){seen=false;stopTour();}
    },{threshold:.3}).observe(boost);
    /* si la persona pone el mouse o el foco, manda ella */
    ['mouseenter','focusin'].forEach(function(ev){boost.addEventListener(ev,function(){held=true;stopTour()})});
    ['mouseleave','focusout'].forEach(function(ev){boost.addEventListener(ev,function(){held=false})});
  }

  /* ---- agenda: las opciones están fijas; al entrar el teléfono en pantalla se levantan 3 opciones al azar, una tras otra,
          para que se note que están vivas; luego todas descansan. Se repite al volver; el mouse lo detiene. ---- */
  var agMenu=document.querySelector('.hp-agenda-menu');
  if(agMenu&&motion){
    var opts=agMenu.children, agT=null;
    var featOn=function(i){[].forEach.call(opts,function(o,j){o.classList.toggle('is-feat',j===i)})};
    var agStop=function(){clearTimeout(agT);agT=null;featOn(-1)};
    var agTour=function(left,prev){
      if(!left){agStop();return;}
      var i; do{i=Math.floor(Math.random()*opts.length)}while(i===prev);
      featOn(i); agT=setTimeout(function(){agTour(left-1,i)},1200);
    };
    var agPhone=agMenu.parentNode;
    new IntersectionObserver(function(es){
      agStop();
      if((agPhone._vis=es[0].isIntersecting)) whenStill(agPhone,function(){agT=setTimeout(function(){agTour(3,-1)},350)});
    },{threshold:.4}).observe(agPhone);
    agMenu.addEventListener('mouseenter',agStop);
  }

  /* ---- teléfono de módulos (#modulos): las tarjetas se prenden y apagan; Consignación requiere Venta.
          Al mostrarse la sección, el dedo "toca" Nómina y se prende (se repite al volver, salvo que ya lo hayas tocado). ---- */
  var mp=document.getElementById('modsPanel');
  if(mp){
    var mpCard=function(k){return mp.querySelector('[data-mod="'+k+'"]')};
    var mpOn=function(c){return c.getAttribute('aria-checked')==='true'};
    var mpSet=function(c,on){c.setAttribute('aria-checked',on?'true':'false')};
    var mpSync=function(){
      var sale=mpOn(mpCard('2')), cons=mpCard('3');
      if(!sale) mpSet(cons,false);
      if(sale) cons.removeAttribute('aria-disabled'); else cons.setAttribute('aria-disabled','true');
    };
    var mpTouched=false, mpT=null;
    [].forEach.call(mp.querySelectorAll('.hp-mp-card'),function(c){
      c.addEventListener('click',function(){
        mpTouched=true; clearTimeout(mpT);
        if(c.getAttribute('aria-disabled')==='true'){c.classList.remove('is-shake');void c.offsetWidth;c.classList.add('is-shake');return;}
        mpSet(c,!mpOn(c)); mpSync();
      });
    });
    if(motion){
      var pay=mpCard('4'), tap=mp.querySelector('.hp-mp-tap'), mpPhone=mp.parentNode;
      new IntersectionObserver(function(es){
        clearTimeout(mpT);
        if(mpTouched) return;
        if((mpPhone._vis=es[0].isIntersecting)){
          whenStill(mpPhone,function(){mpT=setTimeout(function(){
            if(mpTouched) return;
            tap.classList.remove('is-on');void tap.offsetWidth;tap.classList.add('is-on');
            setTimeout(function(){if(!mpTouched)mpSet(pay,true)},140);
          },700)});
        } else mpSet(pay,false);
      },{threshold:.5}).observe(mpPhone);
    }
  }

  /* ---- prender / apagar módulos ---- */
  var sw=document.getElementById('whoSwitch'), who=document.getElementById('para-quien'), touched=false;
  var setWho=function(on){sw.setAttribute('aria-pressed',on?'true':'false');who.classList.toggle('off',!on)};
  if(sw) sw.addEventListener('click',function(){
    touched=true; setWho(sw.getAttribute('aria-pressed')!=='true');
  });
  /* al llegar a la sección el switch arranca en off y se prende solo; se repite cada vez que vuelves, salvo que ya lo hayas tocado */
  if(sw&&motion){
    var flip=null;
    setWho(false);
    new IntersectionObserver(function(es){
      if(touched) return;
      clearTimeout(flip);
      if(es[0].isIntersecting) flip=setTimeout(function(){if(!touched)setWho(true)},700);
      else setWho(false);
    },{threshold:.6}).observe(sw);
  }

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
    var yr=document.getElementById('yearOut'), big=document.getElementById('lossOut');
    if(big.textContent!==fmt.format(m)&&big.dataset.ready){big.classList.remove('bump');void big.offsetWidth;big.classList.add('bump');}
    big.textContent=fmt.format(m); big.dataset.ready='1';
    yr.textContent='≈ '+fmt.format(m*12)+' '+yr.dataset.suffix;
    [ns,tk,ex].forEach(fill);
  }
  [ns,tk,ex].forEach(function(i){i.addEventListener('input',calc)});
  calc();
})();
