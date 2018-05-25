package edu.oregonstate.mist.cardsapi.resources

import io.dropwizard.jersey.params.IntParam
import io.dropwizard.auth.Auth
import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.api.AuthenticatedUser

import edu.oregonstate.mist.cardsapi.db.CardDAO
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response
import javax.ws.rs.core.MediaType

// This will get a Card object from CardDAO and send responses for different endpoints

@Path('/cards')
@Produces(MediaType.APPLICATION_JSON)
class CardsResource extends Resource {
    Logger logger = LoggerFactory.getLogger(CardsResource.class)

    private final CardDAO cardDAO

    CardsResource(CardDAO cardDAO) {
        this.cardDAO = cardDAO
    }

    ResourceObject cardsResource(Card card) {
        new ResourceObject(
                id: card.id,
                type: 'card',
                attributes: new Card (
                        type: card.type,
                        name: card.name,
                        color: card.color,
                        rarity: card.rarity,
                        energy: card.energy,
                        description: card.description
                ),
                links: null
        )
    }

    ResultObject cardsResult(Card card) {
        new ResultObject(
                data: cardsResource(card)
        )
    }

    // Get card by id
    @GET
    @Path ('{id}')
    @Produces(MediaType.APPLICATION_JSON)
    Response getCardById(@Auth AuthenticatedUser authenticatedUser, @PathParam('id') IntParam id) {

        Card card = cardDAO.getCardById(id.get())

        if(card) {
            ResultObject cardResult = cardsResult(card)
            ok(cardResult).build()
        } else {
            notFound().build()
        }

    }
}
