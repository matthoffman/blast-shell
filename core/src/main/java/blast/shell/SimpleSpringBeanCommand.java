package blast.shell;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.basic.AbstractCommand;

/**
 *
 *
 */
public class SimpleSpringBeanCommand extends AbstractCommand {

    Action actionBean;

    public SimpleSpringBeanCommand(Action actionBean) {
        this.actionBean = actionBean;
    }

    @Override
    public Action createNewAction() {
        return actionBean;
    }
}
