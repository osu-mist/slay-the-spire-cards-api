package edu.oregonstate.mist.cardsapi.core

import com.fasterxml.jackson.annotation.JsonIgnore
import io.dropwizard.validation.OneOf
import io.dropwizard.validation.ValidationMethod
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Max
import javax.validation.constraints.Min

class Card {
    @JsonIgnore
    Integer id

    @NotEmpty
//    @OneOf(value = ["skill", "attack", "power", "status", "curse"])
    String type

    @NotEmpty
    String name

    @NotEmpty
//    @OneOf(value = ["red", "green", "blue", "colorless"])
    String color

    @NotEmpty
//    @OneOf(value = ["basic", "common", "uncommon", "rare"])
    String rarity

//    @Max(999)
//    @Min(0)
    Integer energy

    @NotEmpty
    String description
}