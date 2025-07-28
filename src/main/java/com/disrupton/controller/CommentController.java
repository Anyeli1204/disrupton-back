package com.disrupton.controller;
import com.disrupton.model.Comment;
import com.disrupton.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/objects")
public class CommentController {

    @Autowired
    private CommentService comentarioService;

    @GetMapping("/{id}/comments")
    public List<Comment> obtenerComentarios(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws ExecutionException, InterruptedException {
        return comentarioService.obtenerComentarios(id, page, size);
    }
}
