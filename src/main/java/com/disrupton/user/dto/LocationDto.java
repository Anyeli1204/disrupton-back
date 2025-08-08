package com.disrupton.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    private String department; // Departamento/Estado
    private String district; // Distrito/Suburbio
    private String street; // Calle
    private String city; // Ciudad
    private String country; // País
    private String postalCode; // Código postal
    private Double latitude; // Latitud
    private Double longitude; // Longitud
    private String fullAddress; // Dirección completa
} 