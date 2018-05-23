package edu.oregonstate.mist.cardsapi.resources

import io.dropwizard.jersey.params.IntParam
import io.dropwizard.jersey.params.NonEmptyStringParam
import io.dropwizard.jersey.params.BooleanParam
import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.core.SimpleCard
import edu.oregonstate.mist.cardsapi.db.CardDAO
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.print.attribute.standard.Media
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
import com.google.common.base.Optional

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
                attributes: new SimpleCard (
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

    ResultObject cardsResult(List<Card> cards) {
        new ResultObject(
                data: cards.collect {singleCard -> cardsResource(singleCard)}
        )
    }

    // Get card by id
    @GET
    @Path ('{id}')
    @Produces(MediaType.APPLICATION_JSON)
    Response getCardById(@PathParam('id') IntParam id) {

        Response response
        Card card = cardDAO.getCardById(id.get())

        if(card == null) {
            response = notFound().build()
        } else {
            ResultObject cardResult = cardsResult(card)
            response = ok(cardResult).build()
        }
        response
    }

    //Get cards by parameters
    @GET
    @Path ('')
    @Produces(MediaType.APPLICATION_JSON)
    Response getCards(@QueryParam("types") List<String> types,
                      @QueryParam("name") String name,
                      @QueryParam("colors") List<String> colors,
                      @QueryParam("rarities") List<String> rarities,
                      @QueryParam("energyMin") Integer energyMin,
                      @QueryParam("energyMax") Integer energyMax,
                      @QueryParam("keywords") List<String> keywords,
                      @QueryParam("number") Integer number,
                      @QueryParam("isRandom") Boolean isRandom) {

        Response response

//        if(types == null) {
//            types = []
//        }

        List<Card> cards = cardDAO.getCards(types, name, colors, rarities,
                energyMin, energyMax, keywords, number, isRandom)

        ResultObject cardResult = cardsResult(cards)
        response = ok(cardResult).build()
        response
    }
}
