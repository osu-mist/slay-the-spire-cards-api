package edu.oregonstate.mist.cardsapi.core

import com.fasterxml.jackson.annotation.JsonIgnore
import io.dropwizard.validation.OneOf
import io.dropwizard.validation.ValidationMethod
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

class Card {
    @JsonIgnore
    Integer id

//    @OneOf(value = ["skill", "attack", "power", "status", "curse"])
    @NotEmpty
    String type

    @NotEmpty
    String name

//    @OneOf(value = ["red", "green", "blue", "colorless"])
    @NotEmpty
    String color

//    @OneOf(value = ["basic", "common", "uncommon", "rare"])
    @NotEmpty
    String rarity

//    @Max(999)
//    @Min(0)
    @NotNull
    Integer energy

    @NotEmpty
    String description
}