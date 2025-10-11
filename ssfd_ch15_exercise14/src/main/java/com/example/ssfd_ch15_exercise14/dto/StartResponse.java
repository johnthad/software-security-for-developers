package com.example.ssfd_ch15_exercise14.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartResponse {
    private Map<String, Object> publicKey;
}
