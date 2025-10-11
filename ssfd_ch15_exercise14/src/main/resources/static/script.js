function base64urlToArrayBuffer(value) {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padding = normalized.length % 4 ? '='.repeat(4 - (normalized.length % 4)) : '';
  const base64 = normalized + padding;

  const binary = atob(base64);
  const buffer = new ArrayBuffer(binary.length);
  const view = new Uint8Array(buffer);

  for (let i = 0; i < binary.length; i++) {
    view[i] = binary.charCodeAt(i);
  }
  return buffer;
}

function arrayBufferToBase64url(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';

  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }

  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

function display(value) {
  const output = document.getElementById('out');
  output.textContent = typeof value === 'string' ? value : JSON.stringify(value, null, 2);
}

async function postJson(url, data) {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });

  if (!response.ok) {
    throw new Error(await response.text());
  }

  return response.json();
}

function toCreationOptions(options) {
  const copy = structuredClone(options);
  copy.challenge = base64urlToArrayBuffer(copy.challenge);
  copy.user.id = base64urlToArrayBuffer(copy.user.id);
  return copy;
}

function toRequestOptions(options) {
  const copy = structuredClone(options);
  copy.challenge = base64urlToArrayBuffer(copy.challenge);

  if (copy.allowCredentials) {
    copy.allowCredentials = copy.allowCredentials.map(item => ({
      type: item.type,
      id: base64urlToArrayBuffer(item.id)
    }));
  }

  return copy;
}

function credentialToJson(credential) {
  return {
    id: credential.id,
    rawId: arrayBufferToBase64url(credential.rawId),
    type: credential.type,
    response: {
      clientDataJSON: arrayBufferToBase64url(credential.response.clientDataJSON),
      attestationObject: credential.response.attestationObject
        ? arrayBufferToBase64url(credential.response.attestationObject)
        : undefined,
      authenticatorData: credential.response.authenticatorData
        ? arrayBufferToBase64url(credential.response.authenticatorData)
        : undefined,
      signature: credential.response.signature
        ? arrayBufferToBase64url(credential.response.signature)
        : undefined,
      userHandle: credential.response.userHandle
        ? arrayBufferToBase64url(credential.response.userHandle)
        : undefined
    }
  };
}

const usernameInput = document.getElementById('username');
const registerButton = document.getElementById('register');
const loginButton = document.getElementById('login');

registerButton.onclick = async () => {
  try {
    const username = usernameInput.value;
    const start = await postJson('/webauthn/register/start', { username });
    const publicKey = start.publicKey;

    const credential = await navigator.credentials.create({
      publicKey: toCreationOptions(publicKey)
    });

    const result = await postJson('/webauthn/register/finish', {
      username,
      credential: credentialToJson(credential)
    });

    display(result);
  } catch (error) {
    display(String(error));
  }
};

loginButton.onclick = async () => {
  try {
    const username = usernameInput.value;
    const start = await postJson('/webauthn/login/start', { username });
    const publicKey = start.publicKey;

    const credential = await navigator.credentials.get({
      publicKey: toRequestOptions(publicKey)
    });

    const result = await postJson('/webauthn/login/finish', {
      username,
      credential: credentialToJson(credential)
    });

    display(result);
  } catch (error) {
    display(String(error));
  }
};
