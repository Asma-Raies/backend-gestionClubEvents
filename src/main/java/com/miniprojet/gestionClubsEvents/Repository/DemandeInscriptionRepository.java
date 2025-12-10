package com.miniprojet.gestionClubsEvents.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniprojet.gestionClubsEvents.Model.DemandeInscription;
import com.miniprojet.gestionClubsEvents.Model.StatutDemande;

public interface DemandeInscriptionRepository extends JpaRepository<DemandeInscription, Long> {
    List<DemandeInscription> findByStatut(StatutDemande statut);
    List<DemandeInscription> findByClubId(Long clubId);
    List<DemandeInscription> findByClubIdAndStatut(Long clubId, StatutDemande statut);
 //   List<DemandeInscription> findByClubIdAndStatut(Long clubId, StatutDemande statut);
    Optional<DemandeInscription> findByEmailAndClubId(String email, Long clubId);
  //  List<DemandeInscription> findByStatut(StatutDemande statut);

}