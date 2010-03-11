package blast.shell.osgi;

import org.apache.felix.karaf.shell.console.BlueprintContainerAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A somewhat hackish wrapper that looks for beans that implement BlueprintContainerAware and hands them a wrapped
 * Spring ApplicationContext instead.
 * This will work only if all they're really trying to do is look up services, in which case they'll just be looking up
 * Spring beans instead. 
 */
public class BlueprintContainerAwarePostProcessor implements BeanPostProcessor, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof BlueprintContainerAware) {
            ((BlueprintContainerAware)bean).setBlueprintContainer(new SpringBackedBlueprintContainer(applicationContext));
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
