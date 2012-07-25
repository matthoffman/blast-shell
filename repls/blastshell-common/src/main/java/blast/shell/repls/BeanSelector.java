package blast.shell.repls;

/**
 *
 *
 */
public interface BeanSelector {
    
    public boolean filter(String beanName, Class clazz, Object bean);
    
}
