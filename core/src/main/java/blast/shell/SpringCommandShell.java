package blast.shell;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.runtime.shell.CommandSessionImpl;
import org.apache.felix.gogo.runtime.shell.CommandShellImpl;
import org.osgi.service.command.CommandSession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 */
public class SpringCommandShell extends CommandShellImpl implements BeanPostProcessor {

    Map<String, Action> commandRegistry = new HashMap<String, Action>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Action) {
            commandRegistry.put(beanName, (Action) bean);
        }

        return bean;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Pass in a bean; if it contains a "Command" annotation, we'll add it as an available command.
     *
     * @param bean
     * @return
     */
    protected String processBean(Object bean) {

        if (bean.getClass().isAnnotationPresent(Command.class)) {
            Command command = bean.getClass().getAnnotation(Command.class);
            String scope = command.scope();
            String function = command.name();
            if (scope != null && function != null) {
                if (bean instanceof Action) {
                    this.addCommand(scope, new SimpleSpringBeanCommand((Action) bean), function);
                } else {
                    this.addCommand(scope, bean, function);
                }
                return scope + ":" + function;
            }
        }
        return null;
    }


    public void registerCommandsInSession(CommandSession session) {
        Set<String> commandNames = new HashSet<String>();
        for (Action action : commandRegistry.values()) {
            String name = processBean(action);
            commandNames.add(name);
            session.put(name, new SimpleSpringBeanCommand(action));
        }
        session.put(CommandSessionImpl.COMMANDS, commandNames);

    }


    @Override
    public CommandSession createSession(InputStream in, PrintStream out, PrintStream err) {
        CommandSession session = super.createSession(in, out, err);

        // tell it all about the available commands
        registerCommandsInSession(session);

        return session;
    }
}
