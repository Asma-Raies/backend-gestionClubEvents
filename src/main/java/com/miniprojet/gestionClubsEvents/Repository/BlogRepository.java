package com.miniprojet.gestionClubsEvents.Repository;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.miniprojet.gestionClubsEvents.Model.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Page<Blog> findByClubIdOrderByDatePublicationDesc(Long clubId, Pageable pageable);
    Page<Blog> findAllByOrderByDatePublicationDesc(Pageable pageable);
    
}