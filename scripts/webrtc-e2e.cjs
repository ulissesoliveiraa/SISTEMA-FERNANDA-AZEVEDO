const { chromium } = require('playwright');
const assert = require('node:assert/strict');

const baseURL = process.env.E2E_BASE_URL || 'http://127.0.0.1:8080';
const executablePath = process.env.CHROME_PATH || 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

async function login(context, email) {
  const page = await context.newPage();
  await page.goto(`${baseURL}/login`);
  await page.locator('[name=email]').fill(email);
  await page.locator('[name=password]').fill('Demo@123');
  await page.locator('#login button[type=submit]').click();
  await page.waitForURL(/\/dashboard$/);
  return page;
}

(async () => {
  const browser = await chromium.launch({headless:true, executablePath, args:['--use-fake-device-for-media-stream','--use-fake-ui-for-media-stream','--autoplay-policy=no-user-gesture-required']});
  const patientContext = await browser.newContext({permissions:['camera','microphone']});
  const doctorContext = await browser.newContext({permissions:['camera','microphone']});
  const patient = await login(patientContext, 'paciente@fernandaazevedo.demo');
  const doctor = await login(doctorContext, 'medico@fernandaazevedo.demo');
  const appointment = await patient.evaluate(async()=>{const r=await fetch('/api/appointments');return (await r.json()).find(x=>x.reference==='FA-LIVE-DEMO')});
  assert.ok(appointment?.id, 'Consulta FA-LIVE-DEMO não encontrada');
  await Promise.all([
    patient.goto(`${baseURL}/paciente/consulta/${appointment.id}`),
    doctor.goto(`${baseURL}/medico/consulta/${appointment.id}`)
  ]);
  await Promise.all([patient.waitForSelector('#self'), doctor.waitForSelector('#self')]);
  await Promise.all([
    patient.waitForFunction(()=>document.querySelector('#remote')?.srcObject?.getVideoTracks().length===1,{timeout:20000}),
    doctor.waitForFunction(()=>document.querySelector('#remote')?.srcObject?.getVideoTracks().length===1,{timeout:20000})
  ]);
  for (const page of [patient,doctor]) {
    const media = await page.evaluate(()=>({localVideo:document.querySelector('#self').srcObject.getVideoTracks().length,localAudio:document.querySelector('#self').srcObject.getAudioTracks().length,remoteVideo:document.querySelector('#remote').srcObject.getVideoTracks().length,remoteAudio:document.querySelector('#remote').srcObject.getAudioTracks().length,state:document.querySelector('#connection-state').textContent}));
    assert.deepEqual({...media,state:'Conectado'},{localVideo:1,localAudio:1,remoteVideo:1,remoteAudio:1,state:'Conectado'});
  }
  console.log(`WEBRTC_E2E_OK consultation=${appointment.id} participants=2 audio=bidirectional video=bidirectional`);
  await browser.close();
})().catch(error=>{console.error(error);process.exitCode=1});
