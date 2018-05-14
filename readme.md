# Slay the Spire Cards API

## Purpose
The purpose of this project is to learn about RESTful APIs by creating one using Dropwizard.

## Description
[Slay the Spire](https://store.steampowered.com/app/646570/Slay_the_Spire/) is a rogue-like video game in which the player starts with a basic deck of [cards](http://slay-the-spire.wikia.com/wiki/Cards). Over the course of a run, cards will be added or removed from the player's deck. Cards come in 5 types: Attack, Skill, Power, Status, and Curse. This API will allow a user to interact with a database of these cards.

## GET

### /cards
Returns cards that fit parameters. Parameters include: id, type, name, color, rarity, energy (energy cost), description, and number (number of cards to return). There will be some default number of cards to return if the number parameter is not specified.

### /cards/{id}
Returns the card with the specified id.

## POST

### /cards
Adds a new card to the database.

## PUT

### /cards/{id}
Edits a card specified by id in the database by supplying new parameters.

## DELETE

### /cards/{id}
Removes a card with a specific id from the database.
