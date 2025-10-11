package com.example.ssfd_ch15_exercise14.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class WebAuthnService {
    private final Map<String, byte[]> registerChallenges = new HashMap<>();
    private final Map<String, byte[]> loginChallenges = new HashMap<>();
    private final Map<String, List<byte[]>> userCredentials = new HashMap<>();
    private final Map<String, byte[]> userIds = new HashMap<>();

    public Map<String, Object> startRegistration(String username) {

        byte[] userIdBytes = userIds.computeIfAbsent(username, k -> generateRandomBytes(16));
        byte[] registrationChallenge = generateRandomBytes(32);
        registerChallenges.put(username, registrationChallenge);
        return buildRegistrationOptions(userIdBytes, registrationChallenge, username);
    }

    public void finishRegistration(String username, Map<String, Object> credentialResponse) {

        String credentialIdBase64Url = String.valueOf(credentialResponse.get("id"));
        byte[] credentialIdBytes = Base64.getUrlDecoder().decode(credentialIdBase64Url);
        userCredentials.computeIfAbsent(username, k -> new ArrayList<>()).add(credentialIdBytes);
    }

    public Map<String, Object> startLogin(String username) {
        List<byte[]> credentialIdList = userCredentials.get(username);
        if (credentialIdList == null || credentialIdList.isEmpty()) throw new RuntimeException("no creds");
        byte[] loginChallenge = generateRandomBytes(32);
        loginChallenges.put(username, loginChallenge);
        return buildLoginOptions(credentialIdList, loginChallenge);
    }

    public void finishLogin(String username, Map<String, Object> credentialResponse) {

        String credentialIdBase64Url = String.valueOf(credentialResponse.get("id"));
        byte[] credentialIdBytes = Base64.getUrlDecoder().decode(credentialIdBase64Url);
        List<byte[]> userCredentialList = userCredentials.get(username);
        if (userCredentialList == null) throw new RuntimeException("no creds");
        boolean credentialMatch = userCredentialList.stream().anyMatch(registeredId -> Arrays.equals(registeredId, credentialIdBytes));
        if (!credentialMatch) throw new RuntimeException("unknown cred");
    }

    private Map<String, Object> buildRegistrationOptions(byte[] userIdBytes, byte[] challengeBytes, String username) {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes));

        Map<String, Object> relyingParty = new LinkedHashMap<>();
        relyingParty.put("name", "Demo RP");
        relyingParty.put("id", "localhost");
        options.put("rp", relyingParty);

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", Base64.getUrlEncoder().withoutPadding().encodeToString(userIdBytes));
        userMap.put("name", username);
        userMap.put("displayName", username);
        options.put("user", userMap);

        List<Map<String, Object>> pubKeyCredParams = new ArrayList<>();
        Map<String, Object> paramEntry = new LinkedHashMap<>();
        paramEntry.put("type", "public-key");
        paramEntry.put("alg", -7);
        pubKeyCredParams.add(paramEntry);
        options.put("pubKeyCredParams", pubKeyCredParams);

        options.put("attestation", "none");
        options.put("timeout", 60000);

        Map<String, Object> authenticatorSelection = new LinkedHashMap<>();
        authenticatorSelection.put("residentKey", "preferred");
        authenticatorSelection.put("userVerification", "preferred");
        options.put("authenticatorSelection", authenticatorSelection);
        return options;
    }

    private Map<String, Object> buildLoginOptions(List<byte[]> credentialIdList, byte[] challengeBytes) {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("challenge", Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes));
        options.put("timeout", 60000);
        options.put("userVerification", "preferred");

        List<Map<String, Object>> allowCredentials = new ArrayList<>();
        for (byte[] credentialId : credentialIdList) {
            Map<String, Object> descriptor = new LinkedHashMap<>();
            descriptor.put("type", "public-key");
            descriptor.put("id", Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
            allowCredentials.add(descriptor);
        }
        options.put("allowCredentials", allowCredentials);
        return options;
    }

    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

}
