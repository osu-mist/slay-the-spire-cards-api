package edu.oregonstate.mist.cardsapi.resources

import io.dropwizard.auth.Auth
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
import javax.validation.constraints.Pattern
import javax.ws.rs.DELETE
import javax.ws.rs.DefaultValue
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

import org.skife.jdbi.v2.Handle
import org.skife.jdbi.v2.Query
import edu.oregonstate.mist.cardsapi.mapper.CardsMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator
import org.skife.jdbi.v2.unstable.BindIn
import org.skife.jdbi.v2.DBI

// This will get a Card object from CardDAO and send responses for different endpoints

@Path('/cards')
@Produces(MediaType.APPLICATION_JSON)
//@RegisterMapper(CardsMapper)
@UseStringTemplate3StatementLocator
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
    Response getCardById(@Auth @PathParam('id') IntParam id) {

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
                      @QueryParam("name") Optional<String> name,
                      @QueryParam("colors") List<String> colors,
                      @QueryParam("rarities") List<String> rarities,
                      @QueryParam("energyMin") Optional<Integer> energyMin,
                      @QueryParam("energyMax") Optional<Integer> energyMax,
                      @QueryParam("keywords") List<String> keywords,
                      @QueryParam("number") Optional<Integer> number,
                      @QueryParam("isRandom") Optional<Boolean> isRandom) {

        Response response

        List<String> validTypes = ["skill", "attack", "power", "status", "curse"]
        List<String> validColors =["red", "green", "blue", "colorless"]
        List<String> validRarities = ["basic", "common", "uncommon", "rare"]

        if(types.empty) {
            types = validTypes
        } else {
            for(int i = 0; i < types.size(); i++) {
                if(!validTypes.contains(types[i])) {
                    response = badRequest("Invalid types").build()
                    return response
                }
            }
        }

        if(colors.empty) {
            colors = validColors
        } else {
            for(int i = 0; i < colors.size(); i++) {
                if(!validColors.contains(colors[i])) {
                    response = badRequest("Invalid colors").build()
                    return response
                }
            }
        }

        if(rarities.empty) {
            rarities = validRarities
        } else {
            for(int i = 0; i < rarities.size(); i++) {
                if(!validRarities.contains(rarities[i])) {
                    response = badRequest("Invalid rarities").build()
                    return response
                }
            }
        }

        for(int i = 0; i < keywords.size(); i++) {
            if(!keywords[i].matches('[a-zA-Z0-9 ."]+')) {
                response = badRequest("Invalid use of keywords").build()
                return response
            }
        }

        List<Card> cards = getCards(types, name.orNull(), colors, rarities,
                energyMin.or(0), energyMax.or(999),
                keywords, number.or(10), isRandom.or(true))

//        List<Card> cards = cardDAO.getCards(types, name.orNull(), colors, rarities,
//                energyMin.or(0), energyMax.or(999), keywords, number, randomInt)

        ResultObject cardResult = cardsResult(cards)
        response = ok(cardResult).build()
        response
    }

    String listToSql (List<String> list) {
        String str = "("
        for(int i = 0; i < list.size() - 1; i++) {
            str += "\'" + list[i] + "\',"
        }
        str += "\'" + list[list.size() - 1] + "\')"
        str
    }

    String keywordsToSql (List<String> keywords) {
        String str = ""
        for(int i = 0; i < keywords.size(); i++) {
            str += "AND DESCRIPTION LIKE '%'||"
            str += "\'" + keywords[i] + "\'"
            str += "||'%'"
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
            query += "NAME LIKE '%'||:name||'%'\nAND "
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
        cards
    }
}
