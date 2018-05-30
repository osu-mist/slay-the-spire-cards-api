package edu.oregonstate.mist.cardsapi.mapper

import edu.oregonstate.mist.cardsapi.core.Card
import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet
import java.sql.SQLException

class CardsMapper implements ResultSetMapper<Card> {
    Card map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        new Card(
                id: rs.getInt('ID'),
                type: rs.getString('TYPE'),
                name: rs.getString('NAME'),
                color: rs.getString('COLOR'),
                rarity: rs.getString('RARITY'),
                energy: rs.getInt('ENERGY'),
                description: rs.getString('DESCRIPTION')
        )
    }
}
