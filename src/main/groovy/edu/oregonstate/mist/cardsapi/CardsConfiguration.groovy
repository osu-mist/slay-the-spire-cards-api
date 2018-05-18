package edu.oregonstate.mist.cardsapi

import com.fasterxml.jackson.annotation.JsonProperty
import edu.oregonstate.mist.api.Configuration
import io.dropwizard.db.DataSourceFactory

import javax.validation.Valid
import javax.validation.constraints.NotNull

class CardsConfiguration extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory()

    @JsonProperty("database")
    void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory
    }

    @JsonProperty("database")
    DataSourceFactory getDataSourceFactory() {
        return database
    }
}
