package jline

/**
 *
 *
 */
class NullCompletor implements jline.console.completer.Completer {

  int complete(String s, int i, List<CharSequence> charSequences) {
    return -1;
  }

}
