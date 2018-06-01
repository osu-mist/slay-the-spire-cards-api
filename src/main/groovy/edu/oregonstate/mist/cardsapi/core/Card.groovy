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
    @NotEmpty
    String type
    @NotEmpty
    String name
    @NotEmpty
    String color
    @NotEmpty
    String rarity
    @NotNull
    Integer energy
    @NotEmpty
    String description
}