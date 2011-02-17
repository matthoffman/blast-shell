package blast.shell;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.basic.AbstractCommand;

/**
 *
 *
 */
public class SimpleSpringBeanCommand extends AbstractCommand {

    ActionFactory actionFactory;
    String beanName;

    public SimpleSpringBeanCommand(ActionFactory factory, String actionBeanName) {
        this.beanName = actionBeanName;
        this.actionFactory = factory;
    }

    @Override
    public Action createNewAction() {
        return actionFactory.getAction(beanName);
    }
}