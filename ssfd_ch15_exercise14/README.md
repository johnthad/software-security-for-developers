# WebAuthn demo

This is a small app that shows how passkey sign in can work in a web site.

It has a web page at http://localhost:8080 and a set of api endpoints under path /webauthn.

How to run and test

1. Open the project in your Java IDE. For example IntelliJ IDEA or Eclipse.
2. Find the class named Main in package com.example.ssfd_ch15_exercise14.
3. Run that class. It starts the server on port 8080.
4. Open a modern browser that supports passkeys. For example Chrome, Edge, or Safari.
5. Go to http://localhost:8080
6. Type a user name. For example alice.
7. Click Register Credential. Your browser will show a prompt. Approve it.
8. After that click Sign In. Your browser will show a prompt again. Approve it.
9. The box on the page will show a small json with status ok.

How it works

The flow has two parts. First you make a new passkey for a user. Then you sign in with that passkey.

The page uses the api to start each step. The api sends WebAuthn options to the page. The page asks the browser to do the passkey step. Then the page sends the result back to the api to finish the step.

The app keeps user data only in memory. When you stop the app all data is gone.

Endpoints

All endpoints use json. All use the POST method. The base path is /webauthn.

1. POST /webauthn/register/start
   What it does
   1. Start a new passkey for the user.
   Request body
   1. { "username": "alice" }
   Response body
   1. { "publicKey": { ... options for the browser ... } }

2. POST /webauthn/register/finish
   What it does
   1. Save the new passkey id for the user.
   Request body
   1. { "username": "alice", "credential": { ... data from the browser ... } }
   Response body
   1. { "status": "ok" }

3. POST /webauthn/login/start
   What it does
   1. Start a sign in for the user.
   2. Needs that the user has a passkey saved from the register step.
   Request body
   1. { "username": "alice" }
   Response body
   1. { "publicKey": { ... options for the browser ... } }

4. POST /webauthn/login/finish
   What it does
   1. Check that the passkey id sent by the browser matches a saved id for the user.
   Request body
   1. { "username": "alice", "credential": { ... data from the browser ... } }
   Response body
   1. { "status": "ok" }

Notes

This is a demo. It does not do full WebAuthn checks.
It only keeps data in memory.
Use it only for learning.
