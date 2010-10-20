package org.foo;
// Does not do anything, just needs to be here & loadable.

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Something {
    protected String something() {
        return "hello";
    }
    
    private static final Logger LOG = Logger.getLogger(Something.class.getName());
    public static Class<?> loadClass(String name, ClassLoader ldr) throws ClassNotFoundException {
        LOG.log(Level.INFO, "Trying to load from {0} class named: {1}", new Object[]{ldr, name});
        if (ldr == null) {
            return Class.forName(name);
        }
        return Class.forName(name, true, ldr);
    }
    public static URL loadResource(String name, ClassLoader ldr) {
        LOG.log(Level.INFO, "Trying to load from {0} resource named: {1}", new Object[]{ldr, name});
        if (ldr == null) {
            return Something.class.getResource("/" + name);
        }
        return ldr.getResource(name);
    }
    public static InputStream loadResourceAsStream(String name, ClassLoader ldr) {
        LOG.log(Level.INFO, "Trying to load from {0} resource as stream named: {1}", new Object[]{ldr, name});
        if (ldr == null) {
            return Something.class.getResourceAsStream("/" + name);
        }
        return ldr.getResourceAsStream(name);
    }
    
}
