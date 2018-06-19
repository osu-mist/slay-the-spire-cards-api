package edu.oregonstate.mist.cardsapi.db

import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.mapper.CardsMapper
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Query
import org.skife.jdbi.v2.Handle

class CardFluent {

    private DBI dbi

    CardFluent(DBI dbi) {
        this.dbi = dbi
    }

    // Converts list of strings to comma-separated list in SQL
    static String listToSql (List<String> list) {
        String str = "("
        list.each {
            if(it == list.last()) {
                str += "\'" + it + "\')"
            } else {
                str += "\'" + it + "\',"
            }
        }
        str
    }

    // Builds list of LIKE statements for each string in keywords
    static String keywordsToSql (List<String> keywords) {
        String str = ""
        keywords.each {
            str += "AND LOWER(DESCRIPTION) LIKE LOWER('%'||"
            str += "\'" + it + "\'"
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
        
        SELECT
            ID,
            TYPE,
            NAME,
            COLOR,
            RARITY,
            ENERGY,
            DESCRIPTION
            
        FROM (
            SELECT
            CARDS.ID,
            CARDS.NAME,
            CARDS.ENERGY,
            CARDS.DESCRIPTION,
            
            CARD_TYPES.TYPE,
            CARD_COLORS.COLOR,
            CARD_RARITIES.RARITY

            FROM CARDS
            
            INNER JOIN CARD_TYPES ON CARDS.TYPE_ID = CARD_TYPES.TYPE_ID
            INNER JOIN CARD_COLORS ON CARDS.COLOR_ID = CARD_COLORS.COLOR_ID
            INNER JOIN CARD_RARITIES ON CARDS.RARITY_ID = CARD_RARITIES.RARITY_ID
            
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
        query += "AND COLOR IN "
        query += listToSql(colors)
        query += "AND RARITY IN "
        query += listToSql(rarities)
        if(keywords) {
            query += keywordsToSql(keywords)
        }
        query += "FETCH FIRST :cardNumber ROWS ONLY"

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
}
