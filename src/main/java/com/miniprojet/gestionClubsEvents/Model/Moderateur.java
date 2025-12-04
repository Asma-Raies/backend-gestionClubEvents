package com.miniprojet.gestionClubsEvents.Model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Table(name = "moderateur")
@Data
public class Moderateur extends User {

    @OneToOne(mappedBy = "moderateur")
    @JsonIgnore 
    private Club club;
}
