package com.disrupton.mural;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MuralService {

    private static final String PREGUNTAS_MURALES = "preguntas_murales";

    public Mural crearPregunta(String textoPregunta, List<String> imagenes) {
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> data = new HashMap<>();
        data.put("pregunta", textoPregunta);
        data.put("imagenes", imagenes);
        data.put("timestamp", FieldValue.serverTimestamp());

        String id = UUID.randomUUID().toString();

        db.collection(PREGUNTAS_MURALES).document(id).set(data); // No es necesario capturar ApiFuture aquí

        Mural pregunta = new Mural();
        pregunta.setId(id);
        pregunta.setPregunta(textoPregunta);
        pregunta.setImagenes(imagenes);
        pregunta.setTimestamp(System.currentTimeMillis()); // Solo representativo

        return pregunta;
    }
}
