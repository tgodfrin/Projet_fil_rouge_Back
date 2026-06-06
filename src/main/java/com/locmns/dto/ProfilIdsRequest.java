package com.locmns.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Corps de PUT /equipment-family/{id}/profils : les profils autorisés à emprunter cette famille.
@Getter
@Setter
public class ProfilIdsRequest {
    private List<Integer> profilIds;
}
