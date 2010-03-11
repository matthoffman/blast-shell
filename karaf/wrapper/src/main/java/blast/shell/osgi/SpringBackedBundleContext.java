package blast.shell.osgi;

import org.apache.log4j.Logger;
import org.osgi.framework.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 *
 *
 */
public class SpringBackedBundleContext implements BundleContext, ApplicationContextAware {

    private static final Logger log = Logger.getLogger(SpringBackedBundleContext.class);
    
    ApplicationContext applicationContext;

    @Override
    public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        return getAllServiceReferences(clazz, filter);
    }

    @Override
    public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        List<ServiceReference> list = new ArrayList<ServiceReference>();
        try {
            String[] beanNames = applicationContext.getBeanNamesForType(Class.forName(clazz));
            if (beanNames != null && beanNames.length > 0) {
                for (String beanName : beanNames) {
                    list.add(new MockServiceReference(new MockBundle(beanName)));
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Error loading class name "+ clazz+": ", e);
        }
        return list.toArray(new ServiceReference[list.size()]);
    }

    @Override
    public ServiceReference getServiceReference(String clazz) {
        try {
            ServiceReference[] refs = getAllServiceReferences(clazz, null);
            if (refs != null && refs.length > 0){
                return refs[0];
            }
            else return null;
        } catch (InvalidSyntaxException e) {
            log.error("Error getting reference for class "+ clazz+": ", e);
            return null;
        }
    }

    /**
     * We care about this one
     * @param reference
     * @return
     */
    @Override
    public Object getService(ServiceReference reference) {
        return applicationContext.getBean(reference.getBundle().getSymbolicName());
    }

    /**
     * We care about this one
     * @param reference
     * @return
     */
    @Override
    public boolean ungetService(ServiceReference reference) {
        return true; // nothing to do here.
    }


    
    @Override
    public String getProperty(String key) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Bundle getBundle() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Bundle installBundle(String location) throws BundleException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Bundle getBundle(long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Bundle[] getBundles() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addBundleListener(BundleListener listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeBundleListener(BundleListener listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addFrameworkListener(FrameworkListener listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeFrameworkListener(FrameworkListener listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public File getDataFile(String filename) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Filter createFilter(String filter) throws InvalidSyntaxException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
