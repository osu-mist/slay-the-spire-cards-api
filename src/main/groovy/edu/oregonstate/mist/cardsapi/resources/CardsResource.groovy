package edu.oregonstate.mist.cardsapi.resources

import io.dropwizard.jersey.params.IntParam
import io.dropwizard.auth.Auth
import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.cardsapi.db.CardDAO
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import io.dropwizard.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

import org.skife.jdbi.v2.Handle
import org.skife.jdbi.v2.Query
import edu.oregonstate.mist.cardsapi.mapper.CardsMapper
import org.skife.jdbi.v2.DBI

// This will get a Card object from CardDAO and send responses for different endpoints

@Path('/cards')
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
class CardsResource extends Resource {
    Logger logger = LoggerFactory.getLogger(CardsResource.class)

    private final CardDAO cardDAO
    private DBI dbi

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

        List<String> validTypes = ["skill", "attack", "power", "status", "curse"]
        List<String> validColors = ["red", "green", "blue", "colorless"]
        List<String> validRarities = ["basic", "common", "uncommon", "rare"]

        if(types.empty) {
            types = validTypes
        } else {
            for(int i = 0; i < types.size(); i++) {
                if(!validTypes.contains(types[i])) {
                    return badRequest("Invalid types").build()
                }
            }
        }

        if(colors.empty) {
            colors = validColors
        } else {
            for(int i = 0; i < colors.size(); i++) {
                if(!validColors.contains(colors[i])) {
                    return badRequest("Invalid colors").build()
                }
            }
        }

        if(rarities.empty) {
            rarities = validRarities
        } else {
            for(int i = 0; i < rarities.size(); i++) {
                if(!validRarities.contains(rarities[i])) {
                    return badRequest("Invalid rarities").build()
                }
            }
        }

        if(!(name.or("")).matches('[a-zA-Z0-9 ."+-]*')) {
            return badRequest("Invalid name").build()
        }

        for(int i = 0; i < keywords.size(); i++) {
            if(!keywords[i].matches('[a-zA-Z0-9 ."+-]*')) {
                return badRequest("Invalid use of keywords").build()
            }
        }

        List<Card> cards = getCards(types, name.orNull(), colors, rarities,
                energyMin.or(0), energyMax.or(999),
                keywords, number.or(10), isRandom.or(true))

        ResultObject cardResult = cardsResult(cards)
        ok(cardResult).build()
    }

    @POST
    @Path('')
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response postCard (@Validated Card newCard) {

        if(!cardValidator(newCard)) {
            return badRequest("Invalid card object").build()
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

    // Converts list of strings to comma-separated list in SQL
    static String listToSql (List<String> list) {
        String str = "("
        for(int i = 0; i < list.size() - 1; i++) {
            str += "\'" + list[i] + "\',"
        }
        str += "\'" + list[list.size() - 1] + "\')"
        str
    }

    // Builds list of LIKE statements for each string in keywords
    static String keywordsToSql (List<String> keywords) {
        String str = ""
        for(int i = 0; i < keywords.size(); i++) {
            str += "AND LOWER(DESCRIPTION) LIKE LOWER('%'||"
            str += "\'" + keywords[i] + "\'"
            str += "||'%')"
        }
        str
    }

    List<Card> getCards(List<String> types,
                        String name,
                        List<String> colors,
                        List<String> rarities,
                        Integer energyMin,
                        Integer energyMax,
                        List<String> keywords,
                        Integer cardNumber,
                        Boolean isRandom) {
        Handle h = dbi.open()
        String query = """
        
        SELECT *

        FROM (
            SELECT *
            FROM CARDS
            
            LEFT JOIN CARD_TYPES ON CARDS.TYPE_ID = CARD_TYPES.TYPE_ID
            LEFT JOIN CARD_COLORS ON CARDS.COLOR_ID = CARD_COLORS.COLOR_ID
            LEFT JOIN CARD_RARITIES ON CARDS.RARITY_ID = CARD_RARITIES.RARITY_ID
            
            ORDER BY """
        if (isRandom) {
            query += "DBMS_RANDOM.VALUE"
        } else {
            query += "ID ASC"
        }
        query += """)

        WHERE """
        if(name) {
            query += "LOWER(NAME) LIKE LOWER('%'||:name||'%')\nAND "
        }
        query += """ENERGY >= :energyMin
            AND ENERGY \\<= :energyMax
            AND TYPE IN """
        query += listToSql(types)
        query += """AND COLOR IN """
        query += listToSql(colors)
        query += """AND RARITY IN """
        query += listToSql(rarities)
        if(keywords) {
            query += keywordsToSql(keywords)
        }
        query += """FETCH FIRST :cardNumber ROWS ONLY"""

        Query<Map<String, Object>> q = h.createQuery(query)
            .bind("types", types)
            .bind("name", name)
            .bind("colors", colors)
            .bind("rarities", rarities)
            .bind("energyMin", energyMin)
            .bind("energyMax", energyMax)
            .bind("keywords", keywords)
            .bind("cardNumber", cardNumber)
            .map(new CardsMapper())

        List<Card> cards = q.list()
        h.close()
        cards
    }

    static boolean cardValidator(Card card) {
        List<String> validTypes = ["skill", "attack", "power", "status", "curse"]
        List<String> validColors = ["red", "green", "blue", "colorless"]
        List<String> validRarities = ["basic", "common", "uncommon", "rare"]

        if(!(validTypes.contains(card.type)
                && validColors.contains(card.color)
                && validRarities.contains(card.rarity)
                && card.name.matches('[a-zA-Z0-9 ."+-]*')
                && card.description.matches('[a-zA-Z0-9 ."+-]*')
                && card.energy >= 0
                && card.energy <= 999)) {
            false
        } else {
            true
        }
    }
}
