package blast.shell.karaf.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.AbstractAction;

import java.lang.management.*;
import java.util.regex.Pattern;

/**
 *
 *
 */
@Command(scope = "shell", name = "threads", description = "Prints thread dump")
public class ThreadDumpAction extends AbstractAction {

    @Option(name = "-s", aliases = "--summary", description = "print a summary of all threads, not full stack traces", required = false, multiValued = false)
    boolean summary;

    @Option(name = "-g", aliases = "--grep", description = "only print threads matching the given regular expression", required = false, multiValued = false)
    String grep;

    @Override
    protected Object doExecute() throws Exception {
        Pattern pattern = null;
        if (grep != null) {
            pattern = Pattern.compile(grep);
        }
        StringBuilder returnString = new StringBuilder();

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] allThreads = threadMXBean.dumpAllThreads(threadMXBean.isObjectMonitorUsageSupported(), threadMXBean.isSynchronizerUsageSupported());
        for (ThreadInfo thread : allThreads) {
            if (pattern != null) {
                if (!pattern.matcher(thread.getThreadName()).matches()) {
                    continue;
                }
            }
            returnString.append("**********************************************************************\n");
            if (!summary) {
                returnString.append("Thread ").append(thread.toString());
            } else {
                returnString.append(getThreadDisplayName(thread)).append("\n");
            }
            if (threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeEnabled()) {
                long userTimeMs = threadMXBean.getThreadUserTime(thread.getThreadId()) / 1000;// it's in nanos
                long cpuTimeMs = threadMXBean.getThreadCpuTime(thread.getThreadId()) / 1000;  // it's in nanos
                // i'm seriously considering pulling in JodaTime or Commons Lang for period formatting here:
                returnString.append(" - user time: ").append(userTimeMs).append("ms, system time: ").append(cpuTimeMs - userTimeMs).append("ms").append("\n");
            }
            if (summary) {
                returnString.append(printLocksHeld(thread));
                returnString.append(printLocksWaiting(thread));
            }
            StringBuilder str = new StringBuilder(" - waiting " + thread.getWaitedCount() + " times");
            if (contentionMonitoringEnabled(threadMXBean)) {
                str.append("for ").append(thread.getWaitedTime()).append("ms");
            }
            if (contentionMonitoringEnabled(threadMXBean)) {
                returnString.append("\n").append(" - blocked ").append(thread.getBlockedCount()).append(" times for ")
                        .append(thread.getBlockedTime()).append("ms");
            }
            returnString.append(str.toString()).append("\n");
            returnString.append("\n");
        }
        returnString.append("\n");

        returnString.append(printDeadlockInfo(threadMXBean));

        return returnString.toString();
    }

    private boolean contentionMonitoringEnabled(ThreadMXBean threadMXBean) {
        return threadMXBean.isThreadContentionMonitoringSupported() && threadMXBean.isThreadContentionMonitoringEnabled();
    }

    private String printDeadlockInfo(ThreadMXBean threadMXBean) {
        StringBuilder returnString = new StringBuilder();
        if (threadMXBean.isSynchronizerUsageSupported()) {
            long[] threadIds = threadMXBean.findDeadlockedThreads();
            if (threadIds != null && threadIds.length > 0) {
                returnString.append("Deadlocks detected!").append("\n");
                for (long threadId : threadIds) {
                    ThreadInfo thread = threadMXBean.getThreadInfo(threadId);
                    returnString.append(getThreadDisplayName(thread)).append("\n");
                    printLocksWaiting(thread);
                    printLocksHeld(thread);
                }
            }
        } else {
            returnString.append("deadlock detection not supported by this JVM").append("\n");
        }
        return returnString.toString();
    }

    private String getThreadDisplayName(ThreadInfo thread) {
        return "Thread " + thread.getThreadName() + " (" + thread.getThreadState() + "):" + ((thread.isSuspended()) ? "SUSPENDED" : "");
    }

    private String printLocksHeld(ThreadInfo thread) {
        StringBuilder returnString = new StringBuilder();
        if (thread.getLockedSynchronizers().length > 0) {
            returnString.append(" - currently locking:").append("\n");
            for (LockInfo lockInfo : thread.getLockedSynchronizers()) {
                returnString.append("    - ").append(lockInfo.toString()).append("\n");
            }
        }
        return returnString.toString();
    }

    private String printLocksWaiting(ThreadInfo thread) {
        StringBuilder returnString = new StringBuilder();
        if (thread.getLockInfo() != null) {
            String str = " - waiting on lock " + thread.getLockName();
            if (thread.getLockOwnerName() != null) {
                str += " which is held by " + thread.getLockOwnerName();
            } else if (thread.getLockOwnerId() > 0) {
                str += " which is held by " + thread.getLockOwnerId();
            }
            returnString.append(str).append("\n");
            if (thread.getLockedMonitors() != null && thread.getLockedMonitors().length > 0) {
                MonitorInfo[] monitorInfo = thread.getLockedMonitors();
                for (MonitorInfo info : monitorInfo) {
                    returnString.append(" - locked <").append(info.getIdentityHashCode()).append("> (a ").append(info.getClassName()).append(")").append("\n");
                }
            }
        }
        return returnString.toString();
    }
}
