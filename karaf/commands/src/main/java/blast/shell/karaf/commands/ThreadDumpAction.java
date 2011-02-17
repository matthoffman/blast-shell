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
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] allThreads = threadMXBean.dumpAllThreads(threadMXBean.isObjectMonitorUsageSupported(), threadMXBean.isSynchronizerUsageSupported());
        for (ThreadInfo thread : allThreads) {
            if (pattern != null) {
                if (!pattern.matcher(thread.getThreadName()).matches()) {
                    continue;
                }
            }
            println("**********************************************************************");
            if (!summary) {
                println("Thread " + thread.toString());
            } else {
                println(getThreadDisplayName(thread));
            }
            if (threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeEnabled()) {
                long userTimeMs = threadMXBean.getThreadUserTime(thread.getThreadId()) / 1000;// it's in nanos
                long cpuTimeMs = threadMXBean.getThreadCpuTime(thread.getThreadId()) / 1000;  // it's in nanos
                // i'm seriously considering pulling in JodaTime or Commons Lang for period formatting here:
                println(" - user time: " + userTimeMs + "ms, system time: " + (cpuTimeMs - userTimeMs) + "ms");
            }
            if (summary) {
                printLocksHeld(thread);
                printLocksWaiting(thread);
            }
            StringBuilder str = new StringBuilder(" - waiting " + thread.getWaitedCount() + " times");
            if (contentionMonitoringEnabled(threadMXBean)) {
                str.append("for ").append(thread.getWaitedTime()).append("ms");
            }
            if (contentionMonitoringEnabled(threadMXBean)) {
                println(" - blocked " + thread.getBlockedCount() + " times for " + thread.getBlockedTime() + "ms");
            }
            println(str.toString());

            println("");

        }
        println("");

        printDeadlockInfo(threadMXBean);

        return null;
    }

    private boolean contentionMonitoringEnabled(ThreadMXBean threadMXBean) {
        return threadMXBean.isThreadContentionMonitoringSupported() && threadMXBean.isThreadContentionMonitoringEnabled();
    }

    private void printDeadlockInfo(ThreadMXBean threadMXBean) {
        if (threadMXBean.isSynchronizerUsageSupported()) {
            long[] threadIds = threadMXBean.findDeadlockedThreads();
            if (threadIds != null && threadIds.length > 0) {
                println("Deadlocks detected!");
                for (long threadId : threadIds) {
                    ThreadInfo thread = threadMXBean.getThreadInfo(threadId);
                    println(getThreadDisplayName(thread));
                    printLocksWaiting(thread);
                    printLocksHeld(thread);
                }
            }
        } else {
            println("deadlock detection not supported by this JVM");
        }
    }

    private String getThreadDisplayName(ThreadInfo thread) {
        return "Thread " + thread.getThreadName() + " (" + thread.getThreadState() + "):" + ((thread.isSuspended()) ? "SUSPENDED" : "");
    }

    private void printLocksHeld(ThreadInfo thread) {
        if (thread.getLockedSynchronizers().length > 0) {
            println(" - currently locking:");
            for (LockInfo lockInfo : thread.getLockedSynchronizers()) {
                println("    - " + lockInfo.toString());
            }
        }
    }

    private void printLocksWaiting(ThreadInfo thread) {
        if (thread.getLockInfo() != null) {
            String str = " - waiting on lock " + thread.getLockName();
            if (thread.getLockOwnerName() != null) {
                str += " which is held by " + thread.getLockOwnerName();
            } else if (thread.getLockOwnerId() > 0) {
                str += " which is held by " + thread.getLockOwnerId();
            }
            println(str);
            if (thread.getLockedMonitors() != null && thread.getLockedMonitors().length > 0) {
                MonitorInfo[] monitorInfo = thread.getLockedMonitors();
                for (MonitorInfo info : monitorInfo) {
                    println(" - locked <" + info.getIdentityHashCode() + "> (a " + info.getClassName() + ")");
                }
            }
        }
    }

    protected void println(String s) {
        this.session.getConsole().println(s);
    }
}
