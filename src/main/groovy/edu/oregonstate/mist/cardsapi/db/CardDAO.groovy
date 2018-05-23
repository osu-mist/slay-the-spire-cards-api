package edu.oregonstate.mist.cardsapi.db

import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.mapper.CardsMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator
import org.skife.jdbi.v2.unstable.BindIn

// This will query the database and return a Card object
@RegisterMapper(CardsMapper)
@UseStringTemplate3StatementLocator
interface CardDAO extends Closeable {
    // GET by parameters (work in progress)
    @SqlQuery ("""
        SELECT
            CARDS.ID,
            CARDS.TYPE_ID,
            CARDS.NAME,
            CARDS.COLOR_ID,
            CARDS.RARITY_ID,
            CARDS.ENERGY,
            CARDS.DESCRIPTION,

            CARD_TYPES.TYPE,
            CARD_COLORS.COLOR,
            CARD_RARITIES.RARITY

        FROM CARDS

        LEFT JOIN CARD_TYPES ON CARDS.TYPE_ID = CARD_TYPES.TYPE_ID
        LEFT JOIN CARD_COLORS ON CARDS.COLOR_ID = CARD_COLORS.COLOR_ID
        LEFT JOIN CARD_RARITIES ON CARDS.RARITY_ID = CARD_RARITIES.RARITY_ID

        WHERE
            CARDS.NAME LIKE '%'||:name||'%'
            AND CARDS.ENERGY >= :energyMin
            AND CARDS.ENERGY \\<= :energyMax
     """)
//            CARD_TYPES.TYPE IN (\\<types>)
//            AND CARDS.NAME LIKE :name
//            AND CARD_COLORS.COLOR IN (\\<colors>)
//            AND CARD_RARITIES.RARITY IN (\\<rarities>)
//            AND CARDS.ENERGY >= :energyMin
//            AND CARDS.ENERGY \\<= :energyMax
//            AND CARDS.DESCRIPTION IN (\\<keywords>)
//
//        FETCH FIRST :cardNumber ROWS ONLY
//    """)
    List<Card> getCards(@BindIn("types") List<String> types,
                          @Bind("name") String name,
                          @BindIn("colors") List<String> colors,
                          @BindIn("rarities") List<String> rarities,
                          @Bind("energyMin") Integer energyMin,
                          @Bind("energyMax") Integer energyMax,
                          @BindIn("keywords") List<String> keywords,
                          @Bind("cardNumber") Integer cardNumber,
                          @Bind("isRandom") Boolean isRandom)

    @SqlQuery ("""
        SELECT
            CARDS.ID,
            CARDS.TYPE_ID,
            CARDS.NAME,
            CARDS.COLOR_ID,
            CARDS.RARITY_ID,
            CARDS.ENERGY,
            CARDS.DESCRIPTION,

            CARD_TYPES.TYPE,
            CARD_COLORS.COLOR,
            CARD_RARITIES.RARITY

        FROM CARDS

        LEFT JOIN CARD_TYPES ON CARDS.TYPE_ID = CARD_TYPES.TYPE_ID
        LEFT JOIN CARD_COLORS ON CARDS.COLOR_ID = CARD_COLORS.COLOR_ID
        LEFT JOIN CARD_RARITIES ON CARDS.RARITY_ID = CARD_RARITIES.RARITY_ID
        
        WHERE CARDS.ID = :id
    """)
    Card getCardById(@Bind("id") Integer id)

    @Override
    void close()
}

//final CardDAO dao = database.onDemand(CardDAO.class)