package blast.shell;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.runtime.CommandProcessorImpl;
import org.apache.felix.gogo.runtime.CommandSessionImpl;
import org.apache.felix.service.command.CommandSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Serves as a registry for commands -- it detects when command objects are loaded by Spring.
 */
public class CommandRegistry implements BeanPostProcessor, BeanFactoryAware {
    Logger log = LoggerFactory.getLogger(CommandRegistry.class);

    CommandProcessorImpl commandShell;


    Map<String, Action> commandRegistry = new HashMap<String, Action>();
    private ListableBeanFactory beanFactory;
    protected ActionFactory actionFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Action) {
            commandRegistry.put(beanName, (Action) bean);
        }

        return bean;
    }

    private Tuple<String, Object> getName(Object bean, String beanName) {
        Command command = null;
        if (bean.getClass().isAnnotationPresent(Command.class)) {
            command = bean.getClass().getAnnotation(Command.class);
        } else if (bean instanceof Advised) {
            try {
                Object target = ((Advised) bean).getTargetSource().getTarget();
                if (target.getClass().isAnnotationPresent(Command.class)) {
                    command = target.getClass().getAnnotation(Command.class);
                }
            } catch (Exception e) {
                log.warn("Error while trying to determine if Advised bean " + beanName + " is a Command.  This is " +
                        " non-terminal, but this bean will not be exposed as a command.  Error was:", e);
            }
        }

        if (command != null) {
            String scope = command.scope();
            String function = command.name();
            if (scope != null && function != null) {
                Object obj;
                if (bean instanceof Action) {
                    obj = new SimpleSpringBeanCommand(actionFactory, beanName);
                } else {
                    obj = bean;
                }
                commandShell.addCommand(scope, obj, function);
                return new Tuple<String, Object>(scope + ":" + function, obj);
            }
        }

        return null;
    }

    public CommandProcessorImpl getCommandShell() {
        return commandShell;
    }

    public void setCommandShell(CommandProcessorImpl commandShell) {
        this.commandShell = commandShell;
    }

    @SuppressWarnings({"unchecked"})
    public void registerCommandsInSession(CommandSession session) {
        // it would be more proper to do "getBeanNamesOfType(Action.class)",
        // then for each bean name, call the action factory.  But this is quicker.
        Map beans = beanFactory.getBeansOfType(Action.class);
        commandRegistry.putAll(beans);// unchecked, sadly...
        Set<String> commandNames = new HashSet<String>();
        for (Map.Entry<String, Action> entry : commandRegistry.entrySet()) {
            Tuple<String, Object> commandNameTuple = getName(entry.getValue(), entry.getKey());
            commandNames.add(commandNameTuple.key);
            session.put(commandNameTuple.key, commandNameTuple.value);
        }
        session.put(CommandSessionImpl.COMMANDS, commandNames);

    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ListableBeanFactory)) {
            throw new IllegalArgumentException("Bean Factory must implement ListableBeanFactory");
        }
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

    @Required
    public void setActionFactory(ActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }

    private class Tuple<T1, T2> {
        T1 key;
        T2 value;

        private Tuple(T1 key, T2 value) {
            this.key = key;
            this.value = value;
        }
    }
}
