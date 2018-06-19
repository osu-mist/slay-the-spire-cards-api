package edu.oregonstate.mist.cardsapi.db

import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.mapper.CardsMapper

import org.skife.jdbi.v2.sqlobject.Bind
import org.skife.jdbi.v2.sqlobject.BindBean
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper

@RegisterMapper(CardsMapper)
interface CardDAO extends Closeable {

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
            (SELECT TYPE_ID FROM CARD_TYPES WHERE TYPE = :c.type),
            (:c.name),
            (SELECT COLOR_ID FROM CARD_COLORS WHERE COLOR = :c.color),
            (SELECT RARITY_ID FROM CARD_RARITIES WHERE RARITY = :c.rarity),
            (:c.energy),
            (:c.description)
        )
        """)
    void postCard(@Bind("id") Integer id,
                  @BindBean("c") Card card)

    @SqlQuery("SELECT CARD_INSTANCE_SEQ.NEXTVAL FROM DUAL")
    Integer getNextId()

    @SqlUpdate ("""
        UPDATE CARDS
            SET TYPE_ID = (SELECT TYPE_ID FROM CARD_TYPES WHERE TYPE = :card.type),
            NAME = :card.name,
            COLOR_ID = (SELECT COLOR_ID FROM CARD_COLORS WHERE COLOR = :card.color),
            RARITY_ID = (SELECT RARITY_ID FROM CARD_RARITIES WHERE RARITY = :card.rarity),
            ENERGY = :card.energy,
            DESCRIPTION = :card.description
        WHERE CARDS.ID = :id
        
    """)
    void putCard(@Bind("id") Integer id,
                 @BindBean("card") Card card)

    // Check if card exists
    @SqlQuery ("""
        SELECT COUNT(*)
        FROM CARDS
        WHERE CARDS.ID = :id
    """)
    Boolean cardExists(@Bind("id") Integer id)

    @SqlQuery ("""
        SELECT TYPE
        FROM CARD_TYPES
    """)
    List<String> getValidTypes()

    @SqlQuery ("""
        SELECT COLOR
        FROM CARD_COLORS
    """)
    List<String> getValidColors()

    @SqlQuery ("""
        SELECT RARITY
        FROM CARD_RARITIES
    """)
    List<String> getValidRarities()

    @SqlUpdate ("""
        DELETE FROM CARDS
        WHERE CARDS.ID = :id
    """)
    void deleteCard(@Bind("id") Integer id)

    @Override
    void close()
}