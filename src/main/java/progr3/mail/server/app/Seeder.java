package progr3.mail.server.app;

import progr3.mail.server.log.ILogger;
import progr3.mail.server.user.IUserRepository;
import progr3.mail.server.user.UserConstructor;

public class Seeder {

    private final ILogger logger;

    public Seeder(ILogger logger) {
        this.logger = logger;
    }

    public void seedUsers(IUserRepository userRepository) {
        logger.startScope();

        // Check if users already exist to avoid duplicates
        if (!userRepository.getAllUsers().isEmpty()) {
            logger.logInfo("Users already seeded. Skipping seeding process.");
            logger.endScope();
            return;
        }

        try {
            userRepository.saveUser(UserConstructor.create("mariorossi@unito.com", "Mario Rossi"));
            userRepository.saveUser(UserConstructor.create("luca.bianchi@unito.com", "Luca Bianchi"));
            userRepository.saveUser(UserConstructor.create("alessio-bagno@unito.com", "Alessio Bagno"));
            logger.logInfo("Default users seeded successfully.");
        } catch (Exception e) {
            logger.logError("Error seeding default users", e);
        } finally {
            logger.endScope();
        }
    }
}
