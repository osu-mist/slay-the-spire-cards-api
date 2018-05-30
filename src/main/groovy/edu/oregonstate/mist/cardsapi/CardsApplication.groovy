package edu.oregonstate.mist.cardsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.cardsapi.db.CardDAO
import edu.oregonstate.mist.cardsapi.resources.CardsResource
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Environment
import io.dropwizard.setup.Bootstrap
import org.skife.jdbi.v2.DBI

/**
 * Main application class.
 */
class CardsApplication extends Application<CardsConfiguration> {
    /**
     * Initializes application bootstrap.
     * @param bootstrap
     */
    @Override
    void initialize(Bootstrap<CardsConfiguration> bootstrap) {}

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    void run(CardsConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)

        final DBIFactory FACTORY = new DBIFactory()
        final DBI JDBI = FACTORY.build(environment,
                configuration.getDataSourceFactory(),
                "jdbi")
        final CardDAO DAO = JDBI.onDemand(CardDAO.class)
        environment.jersey().register(new CardsResource(DAO, JDBI))
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    static void main(String[] arguments) throws Exception {
        new CardsApplication().run(arguments)
    }
}
