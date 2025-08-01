package com.disrupton.service;

import com.disrupton.model.Comment;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private static final String COMENTARIOS_O = "comentarios_objetos";
    private static final String COMENTARIOS_M = "comentarios_mural";

    public List<Comment> obtenerComentariosObject(String objetoId, int page, int size) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        CollectionReference comentariosRef = db.collection(COMENTARIOS_O);
        Query query = comentariosRef
                .whereEqualTo("objetoCulturalId", objetoId)
                .orderBy("fechaComentario", Query.Direction.DESCENDING)
                .offset(page * size)
                .limit(size);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        return querySnapshot.get().getDocuments().stream().map(doc -> {
            Comment comentario = doc.toObject(Comment.class);
            comentario.setId(doc.getId());
            return comentario;
        }).collect(Collectors.toList());
    }

    public List<Comment> obtenerComentariosMural(String preguntaId, int page, int size) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        CollectionReference comentariosRef = db.collection(COMENTARIOS_M);
        Query query = comentariosRef
                .whereEqualTo("preguntaId", preguntaId)
                .limit(size);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        return querySnapshot.get().getDocuments().stream().map(doc -> {
            Comment comentario = doc.toObject(Comment.class);
            comentario.setId(doc.getId());
            return comentario;
        }).collect(Collectors.toList());
    }

    public void saveCommentObject(String content, String id_Object, boolean isModerated, long responseTimeMs) {
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("isModerated", isModerated);
        data.put("culturalObjectId", id_Object); // importante incluir el ID
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("responseTimeMs", responseTimeMs);

        ApiFuture<DocumentReference> future = db.collection(COMENTARIOS_O).add(data);
        ApiFutures.addCallback(future, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(DocumentReference result) {
                System.out.println("Comentario mural guardado con ID: " + result.getId());
            }
            @Override
            public void onFailure(Throwable t) {
                System.err.println("Error al guardar comentario mural: " + t.getMessage());
            }
        }, Runnable::run); // Ejecuta el callback en el mismo hilo (o puedes usar un executor)
    }


    public void saveCommentMural(String content, String preguntaId, boolean isModerated, long responseTimeMs) {
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("isModerated", isModerated);
        data.put("preguntaId", preguntaId); // <-- aseguramos que se asocie a una pregunta
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("responseTimeMs", responseTimeMs);


        ApiFuture<DocumentReference> future = db.collection(COMENTARIOS_M).add(data);
        ApiFutures.addCallback(future, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(DocumentReference result) {
                System.out.println("Comentario mural guardado con ID: " + result.getId());
            }

            @Override
            public void onFailure(Throwable t) {
                System.err.println("Error al guardar comentario mural: " + t.getMessage());
            }
        }, Runnable::run); // Ejecuta el callback en el mismo hilo (o puedes usar un executor)
    }

}
