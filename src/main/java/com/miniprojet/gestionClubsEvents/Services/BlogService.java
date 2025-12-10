package com.miniprojet.gestionClubsEvents.Services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.miniprojet.gestionClubsEvents.DTO.BlogDTO;
import com.miniprojet.gestionClubsEvents.DTO.CommentaireDTO;
import com.miniprojet.gestionClubsEvents.DTO.CreateBlogRequest;
import com.miniprojet.gestionClubsEvents.Model.Admin;
import com.miniprojet.gestionClubsEvents.Model.Blog;
import com.miniprojet.gestionClubsEvents.Model.Club;
import com.miniprojet.gestionClubsEvents.Model.Commentaire;
import com.miniprojet.gestionClubsEvents.Model.Etudiant;
import com.miniprojet.gestionClubsEvents.Model.Moderateur;
import com.miniprojet.gestionClubsEvents.Model.Reaction;
import com.miniprojet.gestionClubsEvents.Model.TypeReaction;
import com.miniprojet.gestionClubsEvents.Model.User;
import com.miniprojet.gestionClubsEvents.Repository.BlogRepository;
import com.miniprojet.gestionClubsEvents.Repository.ClubRepository;
import com.miniprojet.gestionClubsEvents.Repository.CommentaireRepository;
import com.miniprojet.gestionClubsEvents.Repository.ReactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogService {

    private final BlogRepository blogRepo;
    private final ClubRepository clubRepo;
    private final ReactionRepository reactionRepo;
    private final CommentaireRepository commentaireRepo;

    public Page<BlogDTO> getAllBlogs(Pageable pageable, User currentUser) {
        return blogRepo.findAllByOrderByDatePublicationDesc(pageable)
                       .map(blog -> toDto(blog, currentUser));
    }
    
    public Page<BlogDTO> getBlogsByClub(Long clubId, Pageable pageable, User currentUser) {
        return blogRepo.findByClubIdOrderByDatePublicationDesc(clubId, pageable)
                .map(blog -> toDto(blog, currentUser));
    }

    // -----------------------
   
    public BlogDTO createBlog(CreateBlogRequest req, User auteur) {
      
        if (auteur == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        // Récupère le club
        Club club = clubRepo.findById(req.getClubId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club non trouvé"));

        // Récupère le rôle de l'utilisateur
        String role = getRole(auteur); // <-- méthode utilitaire à définir

        boolean isAdmin = "ADMIN".equals(role);
        boolean isModerateur = "MODERATEUR".equals(role) && club.getModerateur() != null
                && club.getModerateur().getId().equals(auteur.getId());

        if (!isAdmin && !isModerateur) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seuls les admins ou modérateurs peuvent publier des blogs.");
        }

        // Création du blog
        Blog blog = new Blog();
        blog.setTitre(req.getTitre());
        blog.setContenu(req.getContenu());
        blog.setImageUrl(req.getImageUrl());
        blog.setFichierUrl(req.getFichierUrl());
        blog.setFichierNom(req.getFichierNom());
        blog.setCategorie(req.getCategorie());
        blog.setClub(club);
        blog.setAuteur(auteur);

        Blog saved = blogRepo.save(blog);
        return toDto(saved, auteur);
    }


    public BlogDTO getBlogById(Long blogId, User currentUser) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog introuvable"));

        // Si pas d'utilisateur connecté → on passe null (pour les visiteurs)
        return toDto(blog, currentUser);
    }
    public BlogDTO updateBlog(Long blogId, CreateBlogRequest req, User user) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog introuvable"));

        // Vérification des droits
        boolean isAdmin = user instanceof Admin;
        boolean isOwner = blog.getAuteur().getId().equals(user.getId());
        boolean isModOfClub = user instanceof Moderateur && 
                              blog.getClub().getModerateur() != null &&
                              blog.getClub().getModerateur().getId().equals(user.getId());

        if (!isAdmin && !isOwner && !isModOfClub) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }

        blog.setTitre(req.getTitre());
        blog.setContenu(req.getContenu());
        blog.setCategorie(req.getCategorie());
        if (req.getImageUrl() != null) blog.setImageUrl(req.getImageUrl());
        if (req.getFichierUrl() != null) {
            blog.setFichierUrl(req.getFichierUrl());
            blog.setFichierNom(req.getFichierNom());
        }

        Blog saved = blogRepo.save(blog);
        return toDto(saved, user);
    }

    public void deleteBlog(Long blogId, User user) {
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isAdmin = user instanceof Admin;
        boolean isOwner = blog.getAuteur().getId().equals(user.getId());
        boolean isModOfClub = user instanceof Moderateur && 
                              blog.getClub().getModerateur() != null &&
                              blog.getClub().getModerateur().getId().equals(user.getId());

        if (!isAdmin && !isOwner && !isModOfClub) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }

        blogRepo.delete(blog);
    }
  
    public CommentaireDTO addComment(Long blogId, String contenu, User user) {
        // Vérifie si l'étudiant est bloqué
        if (user instanceof Etudiant etudiant && etudiant.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous êtes bloqué");
        }

        String role = getRole(user); // méthode utilitaire

        boolean isAdmin = "ADMIN".equals(role);
        boolean isModerateur = "MODERATEUR".equals(role);
        boolean isEtudiant = "ETUDIANT".equals(role);

        if (!(isAdmin || isModerateur || isEtudiant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Vous n'avez pas le droit de commenter.");
        }

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog introuvable"));

        Commentaire c = new Commentaire();
        c.setContenu(contenu);
        c.setBlog(blog);
        c.setAuteur(user);

        Commentaire saved = commentaireRepo.save(c);

        return CommentaireDTO.builder()
                .id(saved.getId())
                .contenu(saved.getContenu())
                .dateCommentaire(saved.getDateCommentaire())
                .auteurId(user.getId())
                .auteurNomComplet(user.getPrenom() + " " + user.getNom())
                .reponses(List.of())
                .build();
    }

    private String getRole(User user) {
        if (user instanceof Admin) return "ADMIN";
        if (user instanceof Moderateur) return "MODERATEUR";
        if (user instanceof Etudiant) return "ETUDIANT";
        return user.getClass().getSimpleName().toUpperCase(); // fallback
    }

    // -----------------------
    // LIKE / UNLIKE (ALL USERS CAN INTERACT)
    // -----------------------
    public Map<String, Object> toggleReaction(Long blogId, User user, TypeReaction type) {
        // Vérifie si l'étudiant est bloqué
        if (user instanceof Etudiant etudiant && etudiant.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous êtes bloqué");
        }

        // Vérifie les rôles
        if (!(user instanceof Etudiant || user instanceof Moderateur || user instanceof Admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit d'interagir.");
        }

        // Récupère le blog
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog introuvable"));

        // Vérifie si l'utilisateur a déjà une réaction sur ce blog
        Optional<Reaction> existingReactionOpt = reactionRepo.findByBlogIdAndAuteurId(blogId, user.getId());

        boolean liked = false;
        boolean disliked = false;

        if (existingReactionOpt.isPresent()) {
            Reaction existing = existingReactionOpt.get();

            if (existing.getType() == type) {
                // Si la même réaction existe, on la supprime (toggle)
                reactionRepo.delete(existing);
            } else {
                // Si l'utilisateur change de réaction (like -> dislike ou inverse)
                existing.setType(type);
                reactionRepo.save(existing);
            }
        } else {
            // Sinon, crée une nouvelle réaction
            Reaction r = new Reaction();
            r.setBlog(blog);
            r.setAuteur(user);
            r.setType(type);
            reactionRepo.save(r);
        }

        // Mettre à jour le compteur de likes et dislikes depuis la base
        long likesCount = reactionRepo.countByBlogIdAndType(blogId, TypeReaction.LIKE);
        long dislikesCount = reactionRepo.countByBlogIdAndType(blogId, TypeReaction.DISLIKE);

        liked = type == TypeReaction.LIKE && reactionRepo.existsByBlogIdAndAuteurIdAndType(blogId, user.getId(), TypeReaction.LIKE);
        disliked = type == TypeReaction.DISLIKE && reactionRepo.existsByBlogIdAndAuteurIdAndType(blogId, user.getId(), TypeReaction.DISLIKE);

        return Map.of(
                "liked", liked,
                "disliked", disliked,
                "likesCount", likesCount,
                "dislikesCount", dislikesCount
        );
    }


    // -----------------------
    // BLOG → DTO
    // -----------------------
    private BlogDTO toDto(Blog blog, User currentUser) {

        boolean liked = reactionRepo.existsByBlogIdAndAuteurId(blog.getId(), currentUser.getId());

        List<CommentaireDTO> commentairesDto = commentaireRepo
                .findByBlogIdAndParentIsNullOrderByDateCommentaireAsc(blog.getId())
                .stream()
                .map(this::commentToDto)
                .toList();

        return BlogDTO.builder()
                .id(blog.getId())
                .titre(blog.getTitre())
                .contenu(blog.getContenu())
                .imageUrl(blog.getImageUrl())
                .fichierUrl(blog.getFichierUrl())
                .fichierNom(blog.getFichierNom())
                .categorie(blog.getCategorie())
                .datePublication(blog.getDatePublication())
                .clubId(blog.getClub().getId())
                .clubNom(blog.getClub().getNom())
                .auteurId(blog.getAuteur().getId())
                .auteurNomComplet(blog.getAuteur().getPrenom() + " " + blog.getAuteur().getNom())
                .likesCount(blog.getLikesCount())
                .likedByCurrentUser(liked)
                .commentaires(commentairesDto)
                .build();
    }

    private CommentaireDTO commentToDto(Commentaire c) {

        List<CommentaireDTO> replies = c.getReponses().stream()
                .map(this::commentToDto)
                .toList();

        return CommentaireDTO.builder()
                .id(c.getId())
                .contenu(c.getContenu())
                .dateCommentaire(c.getDateCommentaire())
                .auteurId(c.getAuteur().getId())
                .auteurNomComplet(c.getAuteur().getPrenom() + " " + c.getAuteur().getNom())
                .reponses(replies)
                .build();
    }
}
