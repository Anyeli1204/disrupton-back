package com.disrupton.mural.model;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MuralQuestion {
    private String id;
    @PropertyName("pregunta")
    private String content;

    @PropertyName("startDate")
    private Timestamp startDate;

    @PropertyName("endDate")
    private Timestamp endDate;

    // Campo opcional, se puede calcular con las fechas
    private boolean isActive;

    private List<String> imagenes; // URLs a imágenes de referencia


    // Campos adicionales para fechas legibles
    @PropertyName("fechaCreacionStr")
    private String fechaCreacionStr;

    @PropertyName("fechaExpiracionStr")
    private String fechaExpiracionStr;

    public boolean isCurrentlyActive() {
        Timestamp now = Timestamp.now();
        return now.compareTo(startDate) >= 0 && now.compareTo(endDate) <= 0;
    }
}
