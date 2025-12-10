package com.miniprojet.gestionClubsEvents.Model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "moderateur")
@Getter @Setter
public class Moderateur extends User {

    @OneToOne(mappedBy = "moderateur")
    @JsonIgnore 
    private Club club;
}
