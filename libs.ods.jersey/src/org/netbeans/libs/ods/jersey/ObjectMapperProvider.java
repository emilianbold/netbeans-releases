package org.netbeans.libs.ods.jersey;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author japod
 */
@ServiceProvider(service=ContextResolver.class)
@Provider
public final class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper defaultObjectMapper;

    public ObjectMapperProvider() {
        ObjectMapper result = new ObjectMapper();
        result.configure(Feature.INDENT_OUTPUT, true);
        result.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        result.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
//        result.setDeserializationConfig(result.getDeserializationConfig().with(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE));
        defaultObjectMapper = result;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return defaultObjectMapper;
    }
}
