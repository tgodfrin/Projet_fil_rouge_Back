package com.locmns.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Body for PUT /equipment-family/{id}/profils — the profils allowed to borrow this family
@Getter
@Setter
public class ProfilIdsRequest {
    private List<Integer> profilIds;
}
