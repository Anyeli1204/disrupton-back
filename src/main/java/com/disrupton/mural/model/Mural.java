package com.disrupton.mural.model;

import lombok.Data;

import java.util.List;

@Data
public class Mural {
    private String id;
    private String pregunta;
    private List<String> imagenes; // URLs a imágenes de referencia
    private long timestamp;
}
