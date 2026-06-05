package com.locmns.enums;

// Statut de décision d'un événement (demande de retour anticipé / prolongation)
// PENDING  = en attente de la décision du gestionnaire
// ACCEPTED = demande acceptée par le gestionnaire
// REFUSED  = demande refusée par le gestionnaire (le refus est tracé explicitement)
public enum EventStatusType {
    PENDING,
    ACCEPTED,
    REFUSED
}
