import {api,money,toast} from './api.js';

document.querySelectorAll('.brand').forEach((brand,index)=>{
  brand.classList.add('brand-logo');
  brand.innerHTML=`<img src="${index>0?'/assets/brand/fernanda-azevedo-logo-light.svg':'/assets/brand/fernanda-azevedo-logo.svg'}" alt="Fernanda Azevedo — Saúde perto de você">`;
  if(index>0)brand.classList.add('brand-logo--light');
});

const publicPref=JSON.parse(localStorage.getItem('fa-accessibility')||'{}');
const applyPublicPreferences=()=>{
  const html=document.documentElement;
  html.classList.toggle('dark',!!publicPref.dark);
  html.classList.toggle('high-contrast',!!publicPref.contrast);
  html.classList.toggle('reduce-motion',!!publicPref.motion);
  html.classList.toggle('underline-links',!!publicPref.links);
  html.classList.toggle('comfortable-reading',!!publicPref.reading);
  html.classList.remove('font-large','font-xlarge');
  if(publicPref.font===1)html.classList.add('font-large');
  if(publicPref.font===2)html.classList.add('font-xlarge');
};
const savePreferences=()=>{localStorage.setItem('fa-accessibility',JSON.stringify(publicPref));applyPublicPreferences()};
applyPublicPreferences();

document.body.insertAdjacentHTML('beforeend','<button class="a11y-fab" aria-label="Abrir acessibilidade"><span aria-hidden="true">✦</span><small>Acessibilidade</small></button>');
document.querySelector('.a11y-fab').onclick=showAccessibility;

function showAccessibility(){
  const previous=document.querySelector('.a11y-panel');
  if(previous)return previous.remove();
  document.body.insertAdjacentHTML('beforeend',`<section class="a11y-panel" role="dialog" aria-modal="false" aria-labelledby="a11y-title"><div class="a11y-panel__header"><div><span class="a11y-kicker">Preferências pessoais</span><h2 id="a11y-title">Acessibilidade</h2></div><button class="icon-btn a11y-close" aria-label="Fechar acessibilidade">×</button></div><div class="setting-row setting-row--stack"><span><b>◉</b> Tamanho do texto</span><div class="setting-actions" role="group" aria-label="Tamanho do texto"><button data-font="0" class="${!publicPref.font?'active':''}" aria-label="Texto padrão">A</button><button data-font="1" class="${publicPref.font===1?'active':''}" aria-label="Texto grande">A+</button><button data-font="2" class="${publicPref.font===2?'active':''}" aria-label="Texto muito grande">A++</button></div></div>${toggle('dark','☾','Modo noturno')}${toggle('contrast','◐','Alto contraste')}${toggle('links','U̲','Sublinhar links')}${toggle('reading','≋','Leitura confortável')}${toggle('motion','◌','Reduzir animações')}<div class="a11y-footer"><button class="btn btn-ghost" id="pub-reset">Restaurar</button><button class="btn btn-primary" id="pub-close">Concluir</button></div></section>`);
  const panel=document.querySelector('.a11y-panel'),close=()=>panel.remove();
  panel.querySelector('.a11y-close').onclick=close;
  panel.querySelectorAll('[data-font]').forEach(button=>button.onclick=()=>{publicPref.font=Number(button.dataset.font);savePreferences();panel.querySelectorAll('[data-font]').forEach(item=>item.classList.toggle('active',item===button))});
  ['dark','contrast','links','reading','motion'].forEach(key=>panel.querySelector('#pub-'+key).onchange=event=>{publicPref[key]=event.target.checked;savePreferences()});
  panel.querySelector('#pub-reset').onclick=()=>{Object.keys(publicPref).forEach(key=>delete publicPref[key]);savePreferences();close();showAccessibility()};
  panel.querySelector('#pub-close').onclick=close;
  panel.querySelector('.a11y-close').focus();
}

function toggle(key,icon,label){return `<label class="setting-row"><span><b>${icon}</b> ${label}</span><input class="a11y-switch" id="pub-${key}" type="checkbox" ${publicPref[key]?'checked':''}></label>`}
const esc=value=>String(value||'').replace(/[&<>"']/g,char=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
const safeUrl=url=>String(url||'').startsWith('/')||String(url||'').startsWith('#')?url:'#';

api('/api/public/settings').then(settings=>document.querySelector('#public-price').textContent=money(settings.consultationPrice)).catch(()=>document.querySelector('#public-price').textContent='Valor indisponível');
api('/api/public/campaigns').then(items=>{if(items[0])document.querySelector('#campaign-slot').innerHTML=`<section class="section campaign-wrap"><div class="panel campaign-card"><span class="eyebrow">Novidade</span><h2>${esc(items[0].title)}</h2><p>${esc(items[0].subtitle)}</p><a class="btn btn-secondary" href="${safeUrl(items[0].ctaUrl)}">${esc(items[0].ctaLabel||'Saiba mais')}</a></div></section>`});
document.querySelector('.mobile-menu').onclick=event=>{const nav=document.querySelector('.site-header nav');nav.classList.toggle('open');event.currentTarget.setAttribute('aria-expanded',String(nav.classList.contains('open')))};
document.querySelector('.chat-fab').onclick=()=>{toast('Faça login para conversar com o Assistente Fernanda Azevedo.');setTimeout(()=>location.href='/login?next=/paciente/suporte',800)};

const revealObserver='IntersectionObserver'in window?new IntersectionObserver(entries=>entries.forEach(entry=>{if(entry.isIntersecting){entry.target.classList.add('is-visible');revealObserver.unobserve(entry.target)}}),{threshold:.12,rootMargin:'0px 0px -45px'}):null;
const decorate=()=>document.querySelectorAll('.hero-copy,.brand-photo,.section>.eyebrow,.section>h2,.steps article,.value-grid article,.feature-grid article,.faq details,.campaign-card').forEach((element,index)=>{if(element.dataset.motionReady)return;element.dataset.motionReady='true';element.classList.add('reveal-item');element.style.setProperty('--reveal-delay',`${Math.min(index%6,5)*70}ms`);if(publicPref.motion||!revealObserver)element.classList.add('is-visible');else revealObserver.observe(element)});
decorate();
new MutationObserver(decorate).observe(document.querySelector('#campaign-slot'),{childList:true,subtree:true});
