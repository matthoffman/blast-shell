package blast.shell.karaf.commands.repl.scala

import java.io.PrintWriter
import org.apache.felix.gogo.commands.Command
import org.apache.karaf.shell.console.AbstractAction
import tools.jline.console.completer.Completer
import tools.nsc.interpreter._
import tools.nsc.interpreter.Completion.{Candidates, ScalaCompleter}
import org.springframework.beans.factory.{ListableBeanFactory, BeanFactory, BeanFactoryAware}
import tools.jline.console.Key

/**
 * Start a Scala console within your shell session. All Spring beans are bound in scope.
 *
 * Includes some code from peak6 SSH shell: https://github.com/peak6/scala-ssh-shell
 * Many thanks to Scott R. Parish!
 */
@Command(scope = "repl", name = "scala", description = "Starts a Scala shell")
class ScalaShellAction extends AbstractAction with BeanFactoryAware {


  /**
   * Putting variables here is very un-scala, but required for how we're instantiating these Action classes.
   */
  @scala.reflect.BeanProperty
  var name = "scala"
  var beanFactory: BeanFactory = null

  def createBindings(): Seq[(String, String, Any)] = beanFactory match {
    case a: ListableBeanFactory =>
      //        var list : Seq[(String, String, Any)] = IndexedSeq()
      // go over each bean and turn each into a tuple of bean name, bean type, and object
      a.getBeanDefinitionNames.map {
        name =>
          try {
            val bean = beanFactory.getBean(name)
            (name, bean.getClass.getName, bean)
          } catch {
            case t: Throwable =>
              log.debug("Problem filling Scala Shell bindings; this is non-terminal: ", t)
              (name, null, null)
          }
      }
    case _ => IndexedSeq() // we get here if beanFactory is null or not a ListableBeanFactory
  }


  override def doExecute(): Object = {
    val out = session.getConsole
    val in = new BackspaceWrappingInputStream(session.getKeyboard)
    val pw = new PrintWriter(out)
    pw.write("Connected to %s, starting repl...\n".format(name))
    pw.flush()

    val bindings = createBindings()

    val il = new scala.tools.nsc.interpreter.SshILoop(None, pw)
    il.setPrompt(name + "> ")
    il.settings = new scala.tools.nsc.Settings()
    il.settings.embeddedDefaults(
      getClass.getClassLoader)
    il.settings.usejavacp.value = true
    il.createInterpreter()


    il.in = new scala.tools.nsc.interpreter.JLineIOReader(
      in,
      out,
      new scala.tools.nsc.interpreter.JLineCompletion(il.intp))

    if (il.intp.reporter.hasErrors) {
      log.error("Got errors, abandoning REPL")
      return "Got errors, abandoning REPL"
    }

    il.printWelcome()
    try {
      il.intp.initialize()
      il.intp.beQuietDuring {
        il.intp.bind("stdout", pw)
        for ((bname, btype, bval) <- bindings)
          il.bind(bname, btype, bval)
      }
      il.intp.quietRun(
        """def println(a: Any) = {
          stdout.write(a.toString)
          stdout.write('\n')
        }""")
      il.intp.quietRun(
        """def exit = println("Use ctrl-D to exit shell.")""")

      il.loop()
    } finally il.closeInterpreter()

    log.info("Exited Scala repl.")
    pw.write("Bye.\r\n")
    pw.flush()

    // return an empty string on success.
    ""
  }


  def scalaToJline(tc: ScalaCompleter): Completer = new Completer {
    def complete(_buf: String, cursor: Int, candidates: JList[CharSequence]): Int = {
      val buf = if (_buf == null) "" else _buf
      val Candidates(newCursor, newCandidates) = tc.complete(buf, cursor)
      newCandidates foreach (candidates add _)
      newCursor
    }
  }


  def setBeanFactory(bf: BeanFactory) {
    beanFactory = bf
  }


}

/**
 * When embedded inside an SSH session, the Scala shell doesn't know how to handle backspaces.
 * So we'll wrap them here.
 * There very well may be a better/more elegant/more correct way to do this, but I haven't found it yet.
 * @param inputStream
 */
class BackspaceWrappingInputStream(val inputStream: InputStream) extends InputStream {
  def read() = {
    var c = inputStream.read();
    if (Key.valueOf(c) == Key.DELETE) {
      c = Key.BACKSPACE.code;
    }
    c
  }
}