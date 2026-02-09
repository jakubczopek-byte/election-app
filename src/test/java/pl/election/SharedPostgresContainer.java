package pl.election;

import org.testcontainers.containers.PostgreSQLContainer;

public final class SharedPostgresContainer {

    private static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("election_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    static {
        INSTANCE.start();
    }

    private SharedPostgresContainer() {
    }

    public static PostgreSQLContainer<?> getInstance() {
        return INSTANCE;
    }
}
