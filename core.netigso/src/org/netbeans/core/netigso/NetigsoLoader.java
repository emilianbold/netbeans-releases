package org.netbeans.core.netigso;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.ProxyClassLoader;
import org.openide.util.Exceptions;
import org.osgi.framework.Bundle;

final class NetigsoLoader extends ProxyClassLoader {

    private final Bundle bundle;

    public NetigsoLoader(Bundle bundle) {
        super(new ClassLoader[0], true);
        this.bundle = bundle;
        Set<String> pkgs = new HashSet<String>();
        Enumeration en = bundle.findEntries("", "", true);
        while (en.hasMoreElements()) {
            URL url = (URL)en.nextElement();
            if (url.getFile().startsWith("/META-INF")) {
                continue;
            }
            pkgs.add(url.getFile().substring(1).replaceFirst("/[^/]*$", "").replace('/', '.'));
        }
        addCoveredPackages(pkgs);
    }

    @Override
    public URL findResource(String name) {
        return bundle.getResource(name);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Enumeration<URL> findResources(String name) {
        try {
            return bundle.getResources(name);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    protected Class<?> doLoadClass(String pkg, String name) {
        try {
            return bundle.loadClass(name);
        } catch (ClassNotFoundException ex) {
            if (NetigsoActivator.LOG.isLoggable(Level.FINEST)) {
                NetigsoActivator.LOG.log(Level.FINEST, "No class found in " + this, ex);
            }
            return null;
        }
    }

    @Override
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        try {
            c = bundle.loadClass(name);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } catch (ClassNotFoundException x) {
        }
        return super.loadClass(name, resolve);
    }


    @Override
    public String toString() {
        return "Netigso[" + bundle.getLocation() + "]";
    }
}
