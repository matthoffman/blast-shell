package blast.shell.osgi;

import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 */
public class SpringBackedBlueprintContainer implements org.osgi.service.blueprint.container.BlueprintContainer, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public SpringBackedBlueprintContainer() {
    }

    public SpringBackedBlueprintContainer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Set getComponentIds() {
        String[] names = applicationContext.getBeanDefinitionNames();
        return new HashSet<String>(Arrays.asList(names));
    }

    @Override
    public Object getComponentInstance(String beanName) {
        return applicationContext.getBean(beanName);
    }

    @Override
    public ComponentMetadata getComponentMetadata(String s) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection getMetadata(Class aClass) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
