package org.activate;

import java.lang.reflect.Method;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Main implements BundleActivator {
    public static BundleContext start;
    public static BundleContext stop;

    @Override
    public void start(BundleContext bc) throws Exception {
        assert start == null;
        start = bc;

        String clazzName = System.getProperty("start.class");
        if (clazzName != null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> clazz = Class.forName(clazzName, true, cl);
            clazz.newInstance();
        }
        
        String fileObject = System.getProperty("activate.layer.test");
        if (fileObject != null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> util = Class.forName("org.openide.filesystems.FileUtil", true, cl);
            Method m = util.getDeclaredMethod("getConfigFile", String.class);
            Object res = m.invoke(null, fileObject);
            if (res == null) {
                throw new IllegalStateException("FileObject has to be found: " + res);
            }
        }
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        assert stop == null;
        stop = bc;
    }
}

