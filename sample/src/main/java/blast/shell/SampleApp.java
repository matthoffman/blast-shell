package blast.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Hello world!
 */
public class SampleApp {

    public static void main(String[] args) {

        Logger log = LoggerFactory.getLogger(App.class);

        App app = new App();
        try {
            ApplicationContext context = app.start();

        } catch (Throwable t) {
            log.error("Error starting application: ", t);
            log.error("Exiting.");
            System.exit(1);
        }
    }

}
