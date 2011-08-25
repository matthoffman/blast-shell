package blast.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 */
public class App {

    public ApplicationContext start() {
        // create the Spring application context
        return new ClassPathXmlApplicationContext("classpath*:META-INF/shell/*-context.xml");
    }

    public static void main(String[] args) {

        Logger log = LoggerFactory.getLogger(App.class);

        App q = new App();
        try {
            ApplicationContext context = q.start();

        } catch (Throwable t) {
            log.error("Error starting application: ", t);
            log.error("Exiting.");
            System.exit(1);
        }
    }
}
