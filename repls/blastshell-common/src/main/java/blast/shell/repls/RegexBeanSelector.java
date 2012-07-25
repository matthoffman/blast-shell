package blast.shell.repls;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *  Patterns are implicitly "OR'd" -- that is, a bean has to match only one pattern.
 *
 */
public class RegexBeanSelector implements BeanSelector {
    
    List<Pattern> patterns;
    
    @Override
    public boolean filter(String beanName, Class clazz, Object bean) {
        if (patterns != null) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(beanName).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void setPatterns(List<String> strings) {
        this.patterns = new ArrayList<Pattern>();
        for (String pattern : strings) {
            patterns.add(Pattern.compile(pattern));
        }
    }
}
