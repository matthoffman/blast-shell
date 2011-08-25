package blast.shell;

import org.apache.sshd.SshServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 *
 */
public class SampleSshApp {

    public ApplicationContext start() {
        // create the Spring application context
        return new ClassPathXmlApplicationContext("classpath*:META-INF/shell/*-context.xml");
    }


    public static void main(String[] args) {

        Logger log = LoggerFactory.getLogger(App.class);
        SampleSshApp app = new SampleSshApp();

        try {
            ApplicationContext context = app.start();
            SshServer server = (SshServer) context.getBean("sshServer");
            log.info("Server started on port " + server.getPort());
        } catch (Throwable t) {
            log.error("Error starting application: ", t);
            log.error("Exiting.");
            System.exit(1);
        }


    }

}
