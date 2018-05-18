package edu.oregonstate.mist.cardsapi.resources

import io.dropwizard.jersey.params.IntParam
import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.db.CardDAO
import edu.oregonstate.mist.api.Resource
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response
import javax.ws.rs.core.MediaType
import javax.ws.rs.QueryParam
import javax.validation.Valid

// This will get a Card object from CardDAO and send responses for different endpoints

@Path('/cards')
class CardsResource extends Resource {

    private final CardDAO cardDAO

    CardsResource(CardDAO cardDAO) {
        this.cardDAO = cardDAO
    }

    // Get card by id
    @GET
    @Path ('{id}')
    @Produces(MediaType.APPLICATION_JSON)
    Response getCardById(@PathParam('id') IntParam id) {

        Response response
        Card card = cardDAO.getCardById(id.get())

        if(card) {
            response = ok(Card).build()
        } else {
            response = notFound().build()
        }
        response
    }
}
