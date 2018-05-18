package edu.oregonstate.mist.cardsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.cardsapi.db.CardDAO
import io.dropwizard.jdbi.DBIFactory
import io.dropwizard.setup.Environment
import org.skife.jdbi.v2.DBI

/**
 * Main application class.
 */
class CardsApplication extends Application<CardsConfiguration> {
    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    void run(CardsConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)

        final DBIFactory factory = new DBIFactory()
        final DBI jdbi = factory.build(environment, config.getDataSourceFactory(), "postgresql")
        final CardDAO dao = jdbi.onDemand(UserDAO.class)
        //environment.jersey().register(new UserResource(dao))


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
