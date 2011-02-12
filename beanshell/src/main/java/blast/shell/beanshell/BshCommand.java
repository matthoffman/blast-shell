package blast.shell.beanshell;

import blast.shell.CommandSupport;
import bsh.Interpreter;
import groovy.lang.Binding;
import groovy.ui.InteractiveShell;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.AbstractAction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;

/**
 *
 *
 */
@Command(scope = "repl", name = "bsh", description = "Starts a Beanshell shell")
public class BshCommand extends AbstractAction implements BeanFactoryAware {
    BeanFactory beanFactory;

    private Map<String, Object> bindings;

    @Override
    protected Object doExecute() throws Exception {
        // SshAction uses import org.apache.sshd.common.util.NoCloseInputStream and NoCloseOutputStream, which I don't seem
        // to have access to. But if closing this shell seems to close the in and out stream, that's probably why...
        Interpreter shell = new bsh.Interpreter(new InputStreamReader(System.in), new PrintStream(System.out), new PrintStream(System.out), true);
        shell.run();

        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setBindings(final Map<String, Object> bindings) {
        this.bindings = bindings;
    }

}
