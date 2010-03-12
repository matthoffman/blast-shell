package blast.shell;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.Closeable;

/**
 *
 *
 */
@Command(scope = "blast", name = "exit", description = "Exits this console")
public class ExitAction extends CommandSupport implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Option(name = "-f", aliases = {"--force"}, description = "force close this application (calling System.exit())", required = false)
    boolean force;

    Closeable shutdownCloseable;
    DisposableBean shutdownDisposer;


    @Override
    protected Object doExecute() throws Exception {
        if (force) {
            forceQuit();
        }
        // else...

        if (shutdownCloseable != null) {
            log.debug("Calling shutdown callback.");
            shutdownCloseable.close();
        } else if (shutdownDisposer != null) {
            log.debug("Calling shutdown callback.");
            shutdownDisposer.destroy();
        } else {
            if (applicationContext == null || !(applicationContext instanceof AbstractApplicationContext)) {
                // we're not in a Spring environment, or at least not one we understand.  We don't know how to shut down peacefully.
                // TODO
            } else {
                ((AbstractApplicationContext) applicationContext).stop();
                ((AbstractApplicationContext) applicationContext).close();
                log.debug("Shut down Spring Application Context");
                System.out.println("Shut down context");
            }
        }

        return null;
    }

    private void forceQuit() {
        log.info("Shutting down system forcefully");
        System.exit(0);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setShutdownCallback(Closeable shutdownCallback) {
        this.shutdownCloseable = shutdownCallback;
    }

    public void setShutdownCallback(DisposableBean shutdownCallback) {
        this.shutdownDisposer = shutdownCallback;
    }

}
