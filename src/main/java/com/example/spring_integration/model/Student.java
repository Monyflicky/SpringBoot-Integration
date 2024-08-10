package com.example.spring_integration.model;

import jakarta.persistence.Id;

public record Student(@Id String id, String name, String school) {
}
