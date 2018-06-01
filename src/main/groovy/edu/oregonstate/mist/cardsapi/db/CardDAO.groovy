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
//    GET by parameters (replaced by method in CardsResource)
//    @SqlQuery ("""
//
//        SELECT *
//
//        FROM (
//            SELECT *
//            FROM CARDS
//
//            LEFT JOIN CARD_TYPES ON CARDS.TYPE_ID = CARD_TYPES.TYPE_ID
//            LEFT JOIN CARD_COLORS ON CARDS.COLOR_ID = CARD_COLORS.COLOR_ID
//            LEFT JOIN CARD_RARITIES ON CARDS.RARITY_ID = CARD_RARITIES.RARITY_ID
//
//            ORDER BY DBMS_RANDOM.VALUE)
//
//        WHERE
//            NAME LIKE '%'||:name||'%'
//            AND ENERGY >= :energyMin
//            AND ENERGY \\<= :energyMax
//            AND TYPE IN (<types>)
//            AND COLOR IN (<colors>)
//            AND RARITY IN (<rarities>)
//
//        FETCH FIRST :cardNumber ROWS ONLY
//     """)
//
//    List<Card> getCards(@BindIn("types") List<String> types,
//                          @Bind("name") String name,
//                          @BindIn("colors") List<String> colors,
//                          @BindIn("rarities") List<String> rarities,
//                          @Bind("energyMin") Integer energyMin,
//                          @Bind("energyMax") Integer energyMax,
//                          @BindIn("keywords") List<String> keywords,
//                          @Bind("cardNumber") Integer cardNumber,
//                          @Bind("randomInt") Integer randomInt)

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

    @SqlUpdate ("""
        INSERT INTO CARDS (ID, TYPE_ID, NAME, COLOR_ID, RARITY_ID, ENERGY, DESCRIPTION)
        VALUES (
            (:id),
            (SELECT TYPE_ID FROM CARD_TYPES WHERE TYPE = :type),
            (:name),
            (SELECT COLOR_ID FROM CARD_COLORS WHERE COLOR = :color),
            (SELECT RARITY_ID FROM CARD_RARITIES WHERE RARITY = :rarity),
            (:energy),
            (:description)
        )
        """)
    void postCard(@Bind("id") Integer id,
                  @Bind("type") String type,
                  @Bind("name") String name,
                  @Bind("color") String color,
                  @Bind("rarity") String rarity,
                  @Bind("energy") Integer energy,
                  @Bind("description") String description)

    @SqlQuery("SELECT CARD_INSTANCE_SEQ.NEXTVAL FROM DUAL")
    Integer getNextId()

    @SqlUpdate ("""
        UPDATE CARDS
            SET TYPE_ID = (SELECT TYPE_ID FROM CARD_TYPES WHERE TYPE = :type),
            NAME = :name,
            COLOR_ID = (SELECT COLOR_ID FROM CARD_COLORS WHERE COLOR = :color),
            RARITY_ID = (SELECT RARITY_ID FROM CARD_RARITIES WHERE RARITY = :rarity),
            ENERGY = :energy,
            DESCRIPTION = :description
        WHERE CARDS.ID = :id
        
    """)
    void putCard(@Bind("id") Integer id,
                 @Bind("type") String type,
                 @Bind("name") String name,
                 @Bind("color") String color,
                 @Bind("rarity") String rarity,
                 @Bind("energy") Integer energy,
                 @Bind("description") String description)

    // Check if card exists
    @SqlQuery ("""
        SELECT CASE
            WHEN EXISTS (SELECT *
                         FROM CARDS
                         WHERE CARDS.ID = :id)
            THEN 1
            ELSE 0
            END
        FROM DUAL
    """)
    Integer cardExists(@Bind("id") Integer id)

    @SqlUpdate ("""
        DELETE FROM CARDS
        WHERE CARDS.ID = :id
    """)
    void deleteCard(@Bind("id") Integer id)

    @Override
    void close()
}

//final CardDAO dao = database.onDemand(CardDAO.class)