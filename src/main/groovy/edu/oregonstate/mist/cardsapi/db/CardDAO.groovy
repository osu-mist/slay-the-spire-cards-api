package edu.oregonstate.mist.cardsapi.db

import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.mapper.CardsMapper
import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper

// This will query the database and return a Card object
@RegisterMapper(CardsMapper)
interface CardDAO extends Closeable {
    // GET by parameters (work in progress)
//    @SqlQuery ("""
//        SELECT
//            CARDS.ID,
//            CARDS.TYPE_ID,
//            CARDS.NAME,
//            CARDS.COLOR_ID,
//            CARDS.RARITY_ID,
//            CARDS.ENERGY,
//            CARDS.DESCRIPTION,
//
//            CARD_TYPES.TYPE,
//            CARD_COLORS.COLOR,
//            CARD_RARITIES.RARITY
//
//        FROM CARDS
//
//        INNER JOIN CARD_TYPES ON CARDS.TYPE_ID = CARD_TYPES.TYPE_ID
//        INNER JOIN CARD_COLORS ON CARDS.COLOR_ID = CARD_COLORS.COLOR_ID
//        INNER JOIN CARD_RARITIES ON CARDS.RARITY_ID = CARD_RARITIES.RARITY_ID
//
//        WHERE
//            CARDS.TYPE LIKE
//
//
//    """)
//    List<Card> getCards(@Bind("types") String[] types,
//                          @Bind("name") String name,
//                          @Bind("colors") String[] colors,
//                          @Bind("rarities") String [] rarities,
//                          @Bind("energyMin") int energyMin,
//                          @Bind("energyMax") int energyMax,
//                          @Bind("keywords") String[] keywords,
//                          @Bind("number") int number,
//                          @Bind("isRandom") boolean isRandom)
    
    // GET card by id
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