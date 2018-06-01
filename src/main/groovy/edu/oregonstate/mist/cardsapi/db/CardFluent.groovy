package edu.oregonstate.mist.cardsapi.db

import edu.oregonstate.mist.cardsapi.core.Card
import edu.oregonstate.mist.cardsapi.mapper.CardsMapper
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Query
import org.skife.jdbi.v2.Handle
import edu.oregonstate.mist.cardsapi.mapper.CardsMapper

class CardFluent {

    private DBI dbi

    CardFluent(DBI dbi) {
        this.dbi = dbi
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
}
