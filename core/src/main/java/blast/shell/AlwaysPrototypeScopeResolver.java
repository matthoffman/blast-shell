package blast.shell;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Use this for annotation-based command scanners where you always want the commands to be prototypes.
 */
public class AlwaysPrototypeScopeResolver implements ScopeMetadataResolver {
    @Override
    public ScopeMetadata resolveScopeMetadata(BeanDefinition beanDefinition) {
        ScopeMetadata meta = new ScopeMetadata();
        meta.setScopedProxyMode(ScopedProxyMode.NO);
        meta.setScopeName("prototype");
        return meta;
    }
}
