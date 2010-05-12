package org.activate;

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
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        assert stop == null;
        stop = bc;
    }
}

