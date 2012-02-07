package blast.shell;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.basic.AbstractCommand;
import org.apache.karaf.shell.console.CompletableFunction;
import org.apache.karaf.shell.console.Completer;

import java.util.List;

/**
 *
 *
 */
public class SimpleSpringBeanCommand extends AbstractCommand {

    ActionFactory actionFactory;
    String beanName;
    protected List<Completer> completers;


    public SimpleSpringBeanCommand(ActionFactory factory, String actionBeanName) {
        this.beanName = actionBeanName;
        this.actionFactory = factory;
    }

    @Override
    public Action createNewAction() {
        return actionFactory.getAction(beanName);
    }

    /*
    @Override
    public List<Completer> getCompleters() {
        return completers;
    }

    public void setCompleters(List<Completer> completers) {
        this.completers = completers;
    }
    */
}
