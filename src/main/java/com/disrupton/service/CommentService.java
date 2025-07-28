package com.disrupton.service;

import com.disrupton.model.Comment;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private static final String COLECCION = "comentarios_objetos";

    public List<Comment> obtenerComentarios(String objetoId, int page, int size) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        CollectionReference comentariosRef = db.collection(COLECCION);
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
}
