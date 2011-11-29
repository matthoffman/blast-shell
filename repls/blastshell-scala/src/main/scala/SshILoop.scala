/* NSC -- new Scala compiler
 * Copyright 2005-2011 LAMP/EPFL
 * @author Alexander Spoon
 */

package scala.tools.nsc.interpreter

/*
 * ILoop.loop(), but allow external exit
 */
class SshILoop(in0: Option[java.io.BufferedReader], out: JPrintWriter)
        extends scala.tools.nsc.interpreter.ILoop(in0, out) {
  var inShutdown = false

  override def loop() {
    def readOneLine() = {
      out.flush()
      in readLine prompt
    }
    def processLine(line: String): Boolean = {
      if (line eq null) false // assume null means EOF
      else command(line) match {
        case Result(false, _) => false
        case Result(_, Some(finalLine)) => addReplay(finalLine); true
        case _ => true
      }
    }

    while (!inShutdown) {
      try if (!processLine(readOneLine)) return
      catch {
        case _: scala.runtime.NonLocalReturnControl[_] =>
          inShutdown = true
        case ex: Throwable => {
          out.write(scala.tools.nsc.util.stackTraceString(ex))
          out.write('\n')
        }
      }
    }
  }
}
