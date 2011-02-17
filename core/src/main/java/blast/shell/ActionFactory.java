package blast.shell;

import org.apache.felix.gogo.commands.Action;

/**
 *
 *
 */
public interface ActionFactory {

    Action getAction(String actionName);
}
