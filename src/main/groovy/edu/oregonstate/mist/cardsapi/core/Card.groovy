package edu.oregonstate.mist.cardsapi.core
import com.fasterxml.jackson.annotation.JsonIgnore

class Card {
    @JsonIgnore
    Integer id
    String type
    String name
    String color
    String rarity
    Integer energy
    String description
}