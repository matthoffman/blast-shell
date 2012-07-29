package blast.shell.repls.groovy;

import blast.shell.jline.BackspaceWrappingInputStream;
import blast.shell.repls.BeanSelector;
import groovy.lang.Binding;
import jline.Terminal;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.AbstractAction;
import org.codehaus.groovy.tools.shell.IO;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Map;

/**
 *
 *
 */

@Command(scope = "repl", name = "groovy", description = "Starts a Groovy shell")
public class GroovyShellAction extends AbstractAction implements BeanFactoryAware {
    BeanFactory beanFactory;

    private Map<String, Object> bindings;

    private BeanSelector beanSelector;

    @Override
    protected Object doExecute() throws Exception {

        Binding binding = new Binding();

        if (bindings != null) {
            for (Map.Entry<String, Object> nextBinding : bindings.entrySet()) {
                binding.setVariable(nextBinding.getKey(), nextBinding.getValue());
            }
        }
        binding.setVariable("out", session.getConsole());
        binding.setVariable("applicationContext", beanFactory);
        if (beanFactory instanceof ListableBeanFactory) {
            String[] beanNames = ((ListableBeanFactory) beanFactory).getBeanDefinitionNames();
            if (beanSelector != null) {
                for (String beanName : beanNames) {
                    try {
                        Object bean = beanFactory.getBean(beanName);
                        if (beanSelector.filter(beanName, AopUtils.getTargetClass(bean), bean)) {
                            binding.setVariable(beanName, bean);
                        }
                    } catch (Throwable t) {
                        log.debug("Problem filling Groovy Shell bindings; this is non-terminal: ", t);
                    }
                }
            }
        }
        IO io = new IO(new BackspaceWrappingInputStream(session.getKeyboard()), session.getConsole(), session.getConsole());
        final Groovysh groovy = new Groovysh(binding, io);

        session.getConsole().println(getMessage());
        // This makes the Groovy shell lavender. Not sure I'm a huge fan of that, but I like it being a different color.
        // Perhaps a light gray?
        session.getConsole().println("\u001B[36m");
        Terminal terminal = (Terminal) session.get(".jline.terminal");
        try {
            groovy.run(terminal, "");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // set console color back to normal.
            session.getConsole().println("\u001B[0m");
        }


        return null;
    }

    protected String getMessage() {
        return "Entering a Groovy shell.  Commands can span multiple lines; type 'go' to execute the lines currently" +
                " in the buffer. Type 'binding.getVariables()' to see the available variables.\n" +
                "\u001B[1mWarning!  You can directly modify and seriously compromise the running system using this shell!\u001B[0m" +
                "\n\n";
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setBindings(final Map<String, Object> bindings) {
        this.bindings = bindings;
    }

    public void setBeanSelector(BeanSelector beanSelector) {
        this.beanSelector = beanSelector;
    }
}
