package edu.oregonstate.mist.cardsapi.resources

import io.dropwizard.jersey.params.IntParam
import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.db.CardDAO
import edu.oregonstate.mist.cardsapi.db.CardFluent
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject

import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.POST
import javax.annotation.security.PermitAll
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response
import javax.ws.rs.core.MediaType
import javax.ws.rs.QueryParam
import javax.validation.Valid
import com.google.common.base.Optional

import org.skife.jdbi.v2.DBI

// This will get a Card object from CardDAO and send responses for different endpoints

@Path('/cards')
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
class CardsResource extends Resource {

    private final CardDAO cardDAO
    private DBI dbi
    CardFluent cardFluent = new CardFluent(dbi)

    List<String> validTypes = ["skill", "attack", "power", "status", "curse"]
    List<String> validColors = ["red", "green", "blue", "colorless"]
    List<String> validRarities = ["basic", "common", "uncommon", "rare"]
    Integer energyMin = 0
    Integer energyMax = 999
    String regEx = '[a-zA-Z0-9 ."+-]*'

    CardsResource(CardDAO cardDAO, DBI dbi) {
        this.cardDAO = cardDAO
        this.dbi = dbi
    }

    ResourceObject cardsResource(Card card) {
        new ResourceObject(
                id: card.id,
                type: 'card',
                attributes: card,
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

        Card card = cardDAO.getCardById(id.get())

        if(card) {
            ResultObject cardResult = cardsResult(card)
            ok(cardResult).build()
        } else {
            notFound().build()
        }

    }

    //Get cards by parameters
    @GET
    @Path ('')
    @Produces(MediaType.APPLICATION_JSON)
    Response getCards(@QueryParam("types") List<String> types,
                      @QueryParam("name") Optional<String> name,
                      @QueryParam("colors") List<String> colors,
                      @QueryParam("rarities") List<String> rarities,
                      @QueryParam("energyMin") Optional<Integer> energyMin,
                      @QueryParam("energyMax") Optional<Integer> energyMax,
                      @QueryParam("keywords") List<String> keywords,
                      @QueryParam("number") Optional<Integer> number,
                      @QueryParam("isRandom") Optional<Boolean> isRandom) {

        if(!types) {
            types = validTypes
        } else {
            List<String> invalidTypes = types - validTypes
            if(invalidTypes) {
                return badRequest("Invalid types: \'${invalidTypes.join("\', \'")}\'. " +
                        "Valid types are: " +
                        "skill, attack, power, status, curse").build()
            }
        }

        if(!colors) {
            colors = validColors
        } else {
            List<String> invalidColors = colors - validColors
            if(invalidColors) {
                return badRequest("Invalid colors: \'${invalidColors.join("\', \'")}\'. " +
                        "Valid colors are: " +
                        "red, green, blue, colorless.").build()
            }
        }

        if(!rarities) {
            rarities = validRarities
        } else {
            List<String> invalidRarities = rarities - validRarities
            if(invalidRarities) {
                return badRequest("Invalid rarities: \'${invalidRarities.join("\', \'")}\'. " +
                        "Valid rarities are: " +
                        "basic, common, uncommon, rare").build()
            }
        }

        if(!(name.or("")).matches(regEx)) {
            return badRequest("Invalid name: \'" + name.get() +
                    "\'. Name must match pattern: " +
                    regEx).build()
        }

        for(int i = 0; i < keywords.size(); i++) {
            if(!keywords[i].matches(regEx)) {
                return badRequest("Invalid keyword: \'" + keywords[i] +
                        "\'. All keywords " +
                        "must match pattern: " + regEx).build()
            }
        }

        List<Card> cards = cardFluent.getCards(types, name.orNull(), colors, rarities,
                energyMin.or(0), energyMax.or(999),
                keywords, number.or(10), isRandom.or(true))

        ResultObject cardResult = cardsResult(cards)
        ok(cardResult).build()
    }

    @POST
    @Path('')
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response postCard (@Valid ResultObject newResultObject) {

        Response response = resultObjectValidator(newResultObject)
        if(response) {
            return response
        }

        Integer id = cardDAO.getNextId()
        cardDAO.postCard(id, (Card)newResultObject.data.attributes)

        Card card = cardDAO.getCardById(id)
        ResultObject cardResult = cardsResult(card)
        created(cardResult).build()
    }

    Response resultObjectValidator(ResultObject resultObject) {
        if(!resultObject.data.attributes) {
            return badRequest("Invalid syntax: Object must contain data.attributes field").build()
        }
        if(!validTypes.contains(resultObject.data.attributes.type)) {
            return badRequest("Invalid type. " +
                    "Valid types are skill, attack, power, status, curse").build()
        }
        if(!validColors.contains(resultObject.data.attributes.color)) {
            return badRequest("Invalid color. " +
                    "Valid colors are red, green, blue, colorless").build()
        }
        if(!validRarities.contains(resultObject.data.attributes.rarity)) {
            return badRequest("Invalid rarity. " +
                    "Valid rarities are basic, common, uncommon, rare").build()
        }
        if(!resultObject.data.attributes.name.matches(regEx)) {
            return badRequest("Invalid name: \'" + resultObject.data.attributes.name +
                    "\'. Name must match pattern: " +
                    regEx).build()
        }
        if(!resultObject.data.attributes.description.matches(regEx)) {
            return badRequest("Invalid description: \'" + resultObject.data.attributes.description +
                    "\'. Description must match pattern: " +
                    regEx).build()
        }
        if(!(resultObject.data.attributes.energy >= energyMin
                && resultObject.data.attributes.energy <= energyMax)) {
            return badRequest("Invalid energy number. " +
                    "Energy must be between ${energyMin} and ${energyMax}").build()
        }
        null
    }
}
