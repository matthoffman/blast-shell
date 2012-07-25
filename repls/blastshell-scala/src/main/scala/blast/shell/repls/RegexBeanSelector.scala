package blast.shell.repls

import java.util.regex.Pattern

/**
 *  Patterns are implicitly "OR'd" -- that is, a bean has to match only one pattern.
 *
 */
class RegexBeanSelector(val regexes: Seq[String]) extends BeanSelector {

  val patterns: Seq[Pattern] = regexes.map {
    pattern: String => Pattern.compile(pattern)
  }

  @Override
  override def filter(beanName: String, clazz: Class[_], bean: Any): Boolean = {
    if (patterns != null) {
      for (pattern <- patterns) {
        if (pattern.matcher(beanName).matches()) {
          true;
        }
      }
    }
    false;
  }

}
