package com.example.chatapp.controller;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatRepo;

    private static final String IMAGE_UPLOAD_DIR = "src/main/resources/static/uploads/";

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam String sender,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) MultipartFile image
    ) throws IOException {
        ChatMessage chat = new ChatMessage();
        chat.setSender(sender);
        chat.setMessage(message);

        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path path = Paths.get(IMAGE_UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, image.getBytes());
            chat.setImageUrl("/uploads/" + fileName);
        }

        chatRepo.save(chat);
        return ResponseEntity.ok(chat);
    }

    @GetMapping("/all")
    public List<ChatMessage> getAllMessages() {
        return chatRepo.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) throws IOException {
        ChatMessage msg = chatRepo.findById(id).orElse(null);
        if (msg == null) return ResponseEntity.notFound().build();

        if (msg.getImageUrl() != null) {
            Path imagePath = Paths.get("src/main/resources/static/uploads/", Paths.get(msg.getImageUrl()).getFileName().toString());
            Files.deleteIfExists(imagePath);
        }

        chatRepo.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }
}
