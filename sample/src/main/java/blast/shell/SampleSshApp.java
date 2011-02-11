package blast.shell;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.sshd.SshServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.File;
import java.io.FileNotFoundException;

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


        try {
            initLogging();
        } catch (FileNotFoundException e) {
            System.err.println("Error initializing logging: ");
            e.printStackTrace();
            return;
        }
        Logger log = Logger.getLogger(App.class);
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


    private static void initLogging() throws FileNotFoundException {
        String location = System.getProperty("log4j.configuration", "classpath:log4j.xml");
        String resolvedLocation = SystemPropertyUtils.resolvePlaceholders(location);
        File file = ResourceUtils.getFile(resolvedLocation);
        if (!file.exists()) {
            throw new FileNotFoundException("Log4j config file [" + resolvedLocation + "] not found");
        }
        DOMConfigurator.configureAndWatch(file.getAbsolutePath(), 10 * 1000 /* 10 seconds */);
    }

}
