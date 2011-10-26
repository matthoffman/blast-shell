package jline

import jline.console.history.FileHistory
import jline.console.history.MemoryHistory

/**
 *
 * Here for compatibility with jline 0.95.x, when package hierarchies were different
 */
class History extends MemoryHistory {

  public static MemoryHistory getMemoryHistory() {
    return new MemoryHistory();
  }

  public static FileHistory getFileHistory(File file) {
    return new FileHistory(file);
  }

}
