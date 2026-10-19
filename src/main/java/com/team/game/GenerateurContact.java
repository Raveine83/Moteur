package com.team.game;

import java.util.List;

/**
 * Interface commune des générateurs de contacts (interpénétrations, câbles,
 * tiges, sol...). Chaque frame, le gestionnaire demande à chaque générateur
 * d'ajouter ses contacts à la liste, puis le résolveur les traite.
 */
public interface GenerateurContact
{
    /**
     * Ajoute à la liste les contacts actuellement détectés.
     */
    void genererContacts(List<Contact> contacts);
}
