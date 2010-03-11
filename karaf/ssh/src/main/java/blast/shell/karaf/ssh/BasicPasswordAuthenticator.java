package blast.shell.karaf.ssh;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import java.util.Properties;

/**
 *
 *
 */
public class BasicPasswordAuthenticator implements PasswordAuthenticator {
    private Properties users;

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        if (users != null) {
            if (users.get(username) != null) {
                if (users.get(username).equals(password)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setUsers(Properties users) {
        this.users = users;
    }
}