package jline

import jline.console.completer.ArgumentCompleter
import jline.console.completer.Completer

/**
 *
 *
 */
class ArgumentCompletor extends ArgumentCompleter implements Completor {

  ArgumentCompletor(jline.console.completer.ArgumentCompleter.ArgumentDelimiter delimiter, Collection<Completer> completers) {
    super(delimiter, completers)
  }

  ArgumentCompletor(jline.console.completer.ArgumentCompleter.ArgumentDelimiter delimiter, Completer... completers) {
    super(delimiter, completers)
  }

  ArgumentCompletor(Completer... completers) {
    super(completers)
  }

  ArgumentCompletor(List<Completer> completers) {
    super(completers)
  }

  ArgumentCompletor(jline.console.completer.ArgumentCompleter.ArgumentDelimiter delimiter, Completor... completers) {
    super(delimiter, completers)
  }
//
  //  ArgumentCompletor(Completor... completers) {
  //    super(completers)
  //  }


}
