export async function api(path, options={}) {
  const init={credentials:'same-origin',...options,headers:{...(options.body instanceof FormData?{}:{'Content-Type':'application/json'}),...(options.headers||{})}};
  const response=await fetch(path,init); const data=await response.json().catch(()=>({}));
  if(!response.ok) throw Object.assign(new Error(data.message||'Não foi possível concluir.'),{status:response.status,data});
  return data;
}
export const money=v=>new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(Number(v||0));
export const date=v=>v?new Intl.DateTimeFormat('pt-BR',{dateStyle:'medium',timeStyle:v.includes?.('T')?'short':undefined}).format(new Date(v)): '—';
export function toast(message){const el=document.querySelector('#toast');if(!el)return;el.textContent=message;el.classList.add('show');clearTimeout(window.__toast);window.__toast=setTimeout(()=>el.classList.remove('show'),3200)}
