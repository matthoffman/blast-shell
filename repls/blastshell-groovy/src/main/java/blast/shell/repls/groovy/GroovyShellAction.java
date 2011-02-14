package blast.shell.beanshell;

import groovy.lang.Binding;
import groovy.ui.InteractiveShell;
import jline.ANSIBuffer;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.AbstractAction;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderer;
import org.fusesource.jansi.AnsiString;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 *
 *
 */

@Command(scope = "repl", name = "groovy", description = "Starts a Groovy shell")
public class GroovyShellAction extends AbstractAction implements BeanFactoryAware {
    BeanFactory beanFactory;

    private Map<String, Object> bindings;

    @Override
    protected Object doExecute() throws Exception {

        // SshAction uses import org.apache.sshd.common.util.NoCloseInputStream and NoCloseOutputStream, which I don't seem
        // to have access to. But if closing this shell seems to close the in and out stream, that's probably why...
        final PrintStream out = new PrintStream(System.out);
        final InputStream in = System.in;
        final PrintStream err = new PrintStream(System.err);

        Binding binding = new Binding();

        if (bindings != null) {
            for (Map.Entry<String, Object> nextBinding : bindings.entrySet()) {
                binding.setVariable(nextBinding.getKey(), nextBinding.getValue());
            }
        }
        binding.setVariable("out", out);
        binding.setVariable("applicationContext", beanFactory);
        if (beanFactory instanceof ListableBeanFactory) {
            String[] beanNames = ((ListableBeanFactory)beanFactory).getBeanDefinitionNames();
            for (String beanName : beanNames) {
                try {
                    binding.setVariable(beanName, beanFactory.getBean(beanName));
                }catch (Throwable t) {
                    log.debug("Problem filling Groovy Shell bindings; this is non-terminal: ", t);
                }
            }
        }
        //TODO: Find the proper way to do this now that InteractiveShell is deprecated.
        final InteractiveShell groovy = new InteractiveShell(binding, in, out, err);
        out.println("Entering a Groovy shell.  Commands can span multiple lines; type 'go' to execute the lines currently" +
                " in the buffer. Type 'binding' to see the available variables (which are all Spring beans in the system).\n" +
                "\u001B[1mWarning!  You can directly modify and seriously compromise the running system using this shell!\u001B[0m" +
                "\n\n");
        // This makes the Groovy shell lavender. Not sure I'm a huge fan of that, but I like it being a different color.
        // Perhaps a light gray?
        out.println("\u001B[36m");
        try {
            groovy.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // set console color back to normal.
            out.println("\u001B[0m");
        }


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
