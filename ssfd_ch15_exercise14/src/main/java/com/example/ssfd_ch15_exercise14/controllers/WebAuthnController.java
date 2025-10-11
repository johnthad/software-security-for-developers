package com.example.ssfd_ch15_exercise14.controllers;

import com.example.ssfd_ch15_exercise14.dto.FinishRequest;
import com.example.ssfd_ch15_exercise14.dto.SimpleResponse;
import com.example.ssfd_ch15_exercise14.dto.StartRequest;
import com.example.ssfd_ch15_exercise14.dto.StartResponse;
import com.example.ssfd_ch15_exercise14.services.WebAuthnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/webauthn")
@RequiredArgsConstructor
public class WebAuthnController {

    private final WebAuthnService webAuthnService;

    @PostMapping(value = "/register/start", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public StartResponse registerStart(@RequestBody StartRequest req) {
        Map<String,Object> pk = webAuthnService.startRegistration(req.getUsername());
        return new StartResponse(pk);
    }

    @PostMapping(value = "/register/finish", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleResponse registerFinish(@RequestBody FinishRequest req) {
        webAuthnService.finishRegistration(req.getUsername(), req.getCredential());
        return new SimpleResponse("ok");
    }

    @PostMapping(value = "/login/start", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public StartResponse loginStart(@RequestBody StartRequest req) {
        Map<String,Object> pk = webAuthnService.startLogin(req.getUsername());
        return new StartResponse(pk);
    }

    @PostMapping(value = "/login/finish", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SimpleResponse loginFinish(@RequestBody FinishRequest req) {
        webAuthnService.finishLogin(req.getUsername(), req.getCredential());
        return new SimpleResponse("ok");
    }
}
