const https = require('https');

// When true, no real MSG91 account is configured - calls are logged and simulated as successful
// so the rest of the messaging flow (API, rate limiting, Android UI) can be tested end-to-end.
function isMockMode() {
  return !process.env.MSG91_AUTH_KEY;
}

function mockSend(channel, mobileNumber, message) {
  console.log(`[MSG91 MOCK] ${channel} -> 91${mobileNumber}: ${message}`);
  return Promise.resolve({ mock: true, channel, mobileNumber, message });
}

function postJson(hostname, pathName, headers, body) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const req = https.request(
      {
        hostname,
        path: pathName,
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data), ...headers },
      },
      (res) => {
        let raw = '';
        res.on('data', (chunk) => (raw += chunk));
        res.on('end', () => {
          let parsed;
          try {
            parsed = JSON.parse(raw);
          } catch {
            parsed = { raw };
          }
          if (res.statusCode >= 200 && res.statusCode < 300) resolve(parsed);
          else reject(new Error(`MSG91 error (${res.statusCode}): ${raw}`));
        });
      }
    );
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

// Sends a plain SMS via MSG91's Flow API using a pre-approved DLT template.
// The template must contain a single variable, e.g. "##message##".
async function sendSms(mobileNumber, message) {
  if (isMockMode()) return mockSend('SMS', mobileNumber, message);

  const authKey = process.env.MSG91_AUTH_KEY;
  const templateId = process.env.MSG91_SMS_TEMPLATE_ID;
  if (!templateId) {
    throw new Error('MSG91_SMS_TEMPLATE_ID must be configured to send SMS');
  }
  return postJson(
    'control.msg91.com',
    '/api/v5/flow',
    { authkey: authKey },
    {
      template_id: templateId,
      short_url: '0',
      recipients: [{ mobiles: `91${mobileNumber}`, message }],
    }
  );
}

// Sends a WhatsApp Business API template message via MSG91.
async function sendWhatsApp(mobileNumber, message) {
  if (isMockMode()) return mockSend('WhatsApp', mobileNumber, message);

  const authKey = process.env.MSG91_AUTH_KEY;
  const integratedNumber = process.env.MSG91_WHATSAPP_INTEGRATED_NUMBER;
  const templateName = process.env.MSG91_WHATSAPP_TEMPLATE_NAME;
  if (!integratedNumber || !templateName) {
    throw new Error(
      'MSG91_WHATSAPP_INTEGRATED_NUMBER and MSG91_WHATSAPP_TEMPLATE_NAME must be configured to send WhatsApp messages'
    );
  }
  return postJson(
    'control.msg91.com',
    '/api/v5/whatsapp/whatsapp-outbound-message/bulk/',
    { authkey: authKey },
    {
      integrated_number: integratedNumber,
      content_type: 'template',
      payload: {
        messaging_product: 'whatsapp',
        type: 'template',
        template: {
          name: templateName,
          language: { code: 'en', policy: 'deterministic' },
          to_and_components: [
            {
              to: [`91${mobileNumber}`],
              components: { body_1: { type: 'text', value: message } },
            },
          ],
        },
      },
    }
  );
}

module.exports = { sendSms, sendWhatsApp };
