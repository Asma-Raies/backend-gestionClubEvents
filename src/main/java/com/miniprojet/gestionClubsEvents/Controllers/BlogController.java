package com.miniprojet.gestionClubsEvents.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.miniprojet.gestionClubsEvents.Config.CustomUserDetails;
import com.miniprojet.gestionClubsEvents.DTO.BlogDTO;
import com.miniprojet.gestionClubsEvents.DTO.CommentaireDTO;
import com.miniprojet.gestionClubsEvents.DTO.CreateBlogRequest;
import com.miniprojet.gestionClubsEvents.Model.Blog;
import com.miniprojet.gestionClubsEvents.Model.TypeReaction;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.BlogRepository;
import com.miniprojet.gestionClubsEvents.Services.BlogService;

import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
   // private final BlogRepository blogRepo ;

    @GetMapping("/club/all")
    public ResponseEntity<Page<BlogDTO>> getAllBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails customUser) { // ← changer ici

        User currentUser = customUser.getUser(); // <-- récupère le User JPA
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogDTO> dtoPage = blogService.getAllBlogs(pageable, currentUser);
        return ResponseEntity.ok(dtoPage);
    }


    @GetMapping("/club/{clubId}")
    public ResponseEntity<Page<BlogDTO>> getBlogsByClub(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogDTO> blogs = blogService.getBlogsByClub(clubId, pageable, currentUser);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{blogId}")
    public ResponseEntity<BlogDTO> getBlogById(
            @PathVariable Long blogId,
            @AuthenticationPrincipal CustomUserDetails customUser) {

        User currentUser = customUser != null ? customUser.getUser() : null;

        BlogDTO blog = blogService.getBlogById(blogId, currentUser);
        return ResponseEntity.ok(blog);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogDTO> createBlog(
            @RequestPart("blog") CreateBlogRequest blogRequest,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "fichier", required = false) MultipartFile fichier,
            @AuthenticationPrincipal CustomUserDetails customUser) {

        User auteur = customUser.getUser();
        if (auteur == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        // Gestion de l'image
        if (image != null && !image.isEmpty()) {
            String imageUrl = saveFile(image, "images");
            blogRequest.setImageUrl(imageUrl);
        }

        // Gestion du fichier (PDF, TXT, etc.)
        if (fichier != null && !fichier.isEmpty()) {
            String fichierUrl = saveFile(fichier, "files");
            blogRequest.setFichierUrl(fichierUrl);
            blogRequest.setFichierNom(fichier.getOriginalFilename());
        }

        BlogDTO dto = blogService.createBlog(blogRequest, auteur);
        return ResponseEntity.status(201).body(dto);
    }

    private String saveFile(MultipartFile file, String type) {
        try {
            Path uploadDir = Paths.get(System.getProperty("user.home"), "gestion-clubs-uploads", type);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = System.currentTimeMillis() + extension;

            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());

            // RETOURNE LE CHEMIN CORRECT POUR LE FRONTEND
            return "/images/" + fileName;  // ou /uploads/images/fileName si tu préfères
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur upload : " + e.getMessage());
        }
    }
    @PutMapping(value = "/{blogId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BlogDTO> updateBlog(
            @PathVariable Long blogId,
            @RequestPart("blog") CreateBlogRequest blogRequest,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "fichier", required = false) MultipartFile fichier,
            @AuthenticationPrincipal CustomUserDetails customUser) {

        User auteur = customUser.getUser();
        if (auteur == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non authentifié");
        }

        // Gestion image/fichier
        if (image != null && !image.isEmpty()) {
            String imageUrl = saveFile(image, "images");
            blogRequest.setImageUrl(imageUrl);
        }
        if (fichier != null && !fichier.isEmpty()) {
            String fichierUrl = saveFile(fichier, "files");
            blogRequest.setFichierUrl(fichierUrl);
            blogRequest.setFichierNom(fichier.getOriginalFilename());
        }

        BlogDTO updated = blogService.updateBlog(blogId, blogRequest, auteur);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{blogId}")
    public ResponseEntity<Void> deleteBlog(
            @PathVariable Long blogId,
            @AuthenticationPrincipal CustomUserDetails customUser) {

        User user = customUser.getUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        blogService.deleteBlog(blogId, user);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{blogId}/comment")
    public ResponseEntity<CommentaireDTO> addComment(
            @PathVariable Long blogId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser(); // récupère l'entité User réelle
        String contenu = payload.get("contenu");
        CommentaireDTO dto = blogService.addComment(blogId, contenu, user);
        return ResponseEntity.ok(dto);
    }



    @PostMapping("/{blogId}/reaction")
    public ResponseEntity<Map<String, Object>> toggleReaction(
            @PathVariable Long blogId,
            @RequestParam String type, // "like" or "dislike"
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();

        // Convert type to enum
        TypeReaction reactionType;
        if ("like".equalsIgnoreCase(type)) {
            reactionType = TypeReaction.LIKE;
        } else if ("dislike".equalsIgnoreCase(type)) {
            reactionType = TypeReaction.DISLIKE;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de réaction invalide");
        }

        Map<String, Object> response = blogService.toggleReaction(blogId, user, reactionType);
        return ResponseEntity.ok(response);
    }
}
