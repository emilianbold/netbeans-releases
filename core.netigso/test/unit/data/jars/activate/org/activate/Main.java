package org.activate;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Main implements BundleActivator {
    public static BundleContext start;
    public static BundleContext stop;

    public void start(BundleContext bc) throws Exception {
        assert start == null;
        start = bc;
    }

    public void stop(BundleContext bc) throws Exception {
        assert stop == null;
        stop = bc;
    }
}

