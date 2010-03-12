package blast.shell;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.runtime.shell.CommandShellImpl;
import blast.shell.Completable;
import blast.shell.completer.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serves as a registry for commands -- it detects when command objects are loaded by Spring.
 * Also serves as a completer for command names.
 */
public class CommandRegistry implements BeanPostProcessor, Completer {
    CommandShellImpl commandShell;

    Map<String, Action> commandRegistry = new HashMap<String, Action>();

    private final Map<String, Completer> completers = new ConcurrentHashMap<String, Completer>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Action) {
            commandRegistry.put(beanName, (Action) bean);
        }
        String command = getName(bean);
        if (command != null) {
            List<Completer> cl = new ArrayList<Completer>();
            cl.add(new StringsCompleter(new String[]{command}));

            if (bean instanceof Completable) {
                List<Completer> fcl = ((Completable) bean).getCompleters();
                if (fcl != null) {
                    for (Completer c : fcl) {
                        cl.add(c == null ? NullCompleter.INSTANCE : c);
                    }
                } else {
                    cl.add(NullCompleter.INSTANCE);
                }
            } else {
                cl.add(NullCompleter.INSTANCE);
            }
            ArgumentCompleter c = new ArgumentCompleter(cl);
            completers.put(beanName, c);
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        int res = new AggregateCompleter(completers.values()).complete(buffer, cursor, candidates);
        Collections.sort(candidates);
        return res;
    }

    private String getName(Object bean) {

        if (bean.getClass().isAnnotationPresent(Command.class)) {
            Command command = bean.getClass().getAnnotation(Command.class);
            String scope = command.scope();
            String function = command.name();
            if (scope != null && function != null) {
                if (bean instanceof Action) {
                    commandShell.addCommand(scope, new SimpleSpringBeanCommand((Action) bean), function);
                } else {
                    commandShell.addCommand(scope, bean, function);
                }
                return scope + ":" + function;
            }
        }
        return null;
    }

    public CommandShellImpl getCommandShell() {
        return commandShell;
    }

    public void setCommandShell(CommandShellImpl commandShell) {
        this.commandShell = commandShell;
    }

    public Map<String, Completer> getCompleters() {
        return completers;
    }
}
