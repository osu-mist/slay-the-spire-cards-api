package edu.oregonstate.mist.cardsapi.mapper

import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper
import java.sql.ResultSet
import java.sql.SQLException

class ListsMapper implements ResultSetMapper<List<String>> {
    List<String> map(int idx, ResultSet rs, StatementContext sc) throws SQLException {
        List<String> list = null
        for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
            list.add(rs.getString(i))
        }
        list
    }
}