package edu.oregonstate.mist.cardsapi.resources

import io.dropwizard.jersey.params.IntParam
import io.dropwizard.auth.Auth
import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.cardsapi.db.CardDAO
import edu.oregonstate.mist.cardsapi.db.CardFluent
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import io.dropwizard.validation.Validated
import io.dropwizard.validation.ValidationMethod
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    Logger logger = LoggerFactory.getLogger(CardsResource.class)

    private final CardDAO cardDAO
    private DBI dbi
    CardFluent cardFluent = new CardFluent(dbi)

    List<String> validTypes = ["skill", "attack", "power", "status", "curse"]
    List<String> validColors = ["red", "green", "blue", "colorless"]
    List<String> validRarities = ["basic", "common", "uncommon", "rare"]
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
    Response postCard (@Valid Card newCard) {

        Response response = cardValidator(newCard)
        if(response) {
            return response
        }

        Integer id = cardDAO.getNextId()
        cardDAO.postCard(
                id,
                newCard.type,
                newCard.name,
                newCard.color,
                newCard.rarity,
                newCard.energy,
                newCard.description)

        Card card = cardDAO.getCardById(id)
        ResultObject cardResult = cardsResult(card)
        created(cardResult).build()
    }

    @PUT
    @Path('{id}')
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response putCard(@PathParam('id') IntParam id, @Valid Card updateCard) {

        if(cardDAO.cardExists(id.get()) != 1) {
            return notFound().build()
        }
        Response response = cardValidator(updateCard)
        if(response) {
            return response
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
        if(!validColors.contains(card.color)) {
            return badRequest("Invalid color. " +
                    "Valid colors are red, green, blue, colorless").build()
        }
        if(!validRarities.contains(card.rarity)) {
            return badRequest("Invalid rarity. " +
                    "Valid rarities are basic, common, uncommon, rare").build()
        }
        if(!card.name.matches(regEx)) {
            return badRequest("Invalid name: \'" + card.name +
                    "\'. Name must match pattern: " +
                    regEx).build()
        }
        if(!card.description.matches(regEx)) {
            return badRequest("Invalid description: \'" + card.description +
                    "\'. Description must match pattern: " +
                    regEx).build()
        }
        if(!(card.energy >= 0 && card.energy <= 999)) {
            return badRequest("Invalid energy number. " +
                    "Energy must be between 0 and 999").build()
        }
        null
    }
}
