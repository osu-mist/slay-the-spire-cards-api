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
import javax.ws.rs.PUT
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
    private CardFluent cardFluent
    private List<String> validTypes
    private List<String> validColors
    private List<String> validRarities
    Integer energyMin = 0
    Integer energyMax = 999

    // Regular expression for allowed name or description of card
    String validPattern = '[a-zA-Z0-9 ."+-]*'

    CardsResource(CardDAO cardDAO, DBI dbi, CardFluent cardFluent,
                  List<String> validTypes, List<String> validColors,
                  List<String> validRarities) {
        this.cardDAO = cardDAO
        this.dbi = dbi
        this.cardFluent = cardFluent
        this.validTypes = validTypes
        this.validColors = validColors
        this.validRarities = validRarities
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
                        "Valid types are: " + validTypes.join(", ")).build()
            }
        }

        if(!colors) {
            colors = validColors
        } else {
            List<String> invalidColors = colors - validColors
            if(invalidColors) {
                return badRequest("Invalid colors: \'${invalidColors.join("\', \'")}\'. " +
                        "Valid colors are: " + validColors.join(", ")).build()
            }
        }

        if(!rarities) {
            rarities = validRarities
        } else {
            List<String> invalidRarities = rarities - validRarities
            if(invalidRarities) {
                return badRequest("Invalid rarities: \'${invalidRarities.join("\', \'")}\'. " +
                        "Valid rarities are: " + validRarities.join(", ")).build()
            }
        }

        if(!(name.or("")).matches(validPattern)) {
            return badRequest("Invalid name: \'" + name.get() +
                    "\'. Name must match pattern: " +
                    validPattern).build()
        }

        List<String> invalidKeywords = keywords.findAll {!it.matches(validPattern)}
        if(invalidKeywords) {
            return badRequest("Invalid keywords: \'${invalidKeywords.join("\', \'")}\'. " +
                    "All keywords " +
                    "must match pattern: " + validPattern).build()
        }

        List<Card> cards = cardFluent.getCards(types, name.orNull(), colors, rarities,
                energyMin.or(0), energyMax.or(999),
                keywords, number.or(10), isRandom.or(true))

        ResultObject cardResult = cardsResult(cards)
        ok(cardResult).build()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response postCard (@Valid ResultObject newResultObject) {

        Response badResponse = resultObjectValidator(newResultObject)
        if(badResponse) {
            return badResponse
        }
        Integer id = cardDAO.getNextId()
        cardDAO.postCard(id, (Card)newResultObject.data.attributes)

        Card card = cardDAO.getCardById(id)
        ResultObject cardResult = cardsResult(card)
        created(cardResult).build()
    }

    Response resultObjectValidator(ResultObject resultObject) {
        if(!(resultObject && resultObject.data.attributes)) {
            return badRequest("Invalid syntax: Object must contain data.attributes field").build()
        }
        if(!resultObject.data.attributes.type instanceof String ||
                !validTypes.contains(resultObject.data.attributes.type)) {
    @PUT
    @Path('{id}')
    @Consumes(MediaType.APPLICATION_JSON)
    Response putCard(@PathParam('id') IntParam id, @Valid Card updateCard) {

        if(!cardDAO.cardExists(id.get())) {
            return notFound().build()
        }
        Response badResponse = cardValidator(updateCard)
        if(badResponse) {
            return badResponse
        }

        cardDAO.putCard(id.get(), updateCard.type, updateCard.name,
                updateCard.color, updateCard.rarity,
                updateCard.energy, updateCard.description)
        Card card  = cardDAO.getCardById(id.get())
        ResultObject cardResult = cardsResult(card)
        ok(cardResult).build()
    }

    // Returns 400 response with error message if any errors found.
    // Otherwise, returns null
    Response cardValidator(Card card) {
        if(!validTypes.contains(card.type)) {
            return badRequest("Invalid type. " +
                    "Valid types are skill, attack, power, status, curse").build()
        }
        if(!resultObject.data.attributes.color instanceof String ||
                !validColors.contains(resultObject.data.attributes.color)) {
            return badRequest("Invalid color. " +
                    "Valid colors are red, green, blue, colorless").build()
        }
        if(!resultObject.data.attributes.rarity instanceof String ||
                !validRarities.contains(resultObject.data.attributes.rarity)) {
            return badRequest("Invalid rarity. " +
                    "Valid rarities are basic, common, uncommon, rare").build()
        }
        if(!(resultObject.data.attributes.name instanceof String)) {
            return badRequest("Invalid name. " +
                    "Name must match pattern: " +
                    validPattern).build()
        }
        if(!resultObject.data.attributes.name.matches(validPattern)) {
            return badRequest("Invalid name: \'" + resultObject.data.attributes.name +
                    "\'. Name must match pattern: " +
                    validPattern).build()
        }
        if(!(resultObject.data.attributes.description instanceof String)) {
            return badRequest("Invalid description. " +
                    "Description must match pattern: " +
                    validPattern).build()
        }
        if(!resultObject.data.attributes.description.matches(validPattern)) {
            return badRequest("Invalid description: \'" + resultObject.data.attributes.description +
                    "\'. Description must match pattern: " +
                    validPattern).build()
        }
        if(!(resultObject.data.attributes.energy instanceof Integer
                && resultObject.data.attributes.energy >= energyMin
                && resultObject.data.attributes.energy <= energyMax)) {
            return badRequest("Invalid energy number. " +
                    "Energy must be between ${energyMin} and ${energyMax}").build()
        }
        null
    }
}
