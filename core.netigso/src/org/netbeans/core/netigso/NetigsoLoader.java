package org.netbeans.core.netigso;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.ProxyClassLoader;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;
import org.osgi.framework.Bundle;

final class NetigsoLoader extends ProxyClassLoader {
    private static final Logger LOG = Logger.getLogger(NetigsoLoader.class.getName());
    private Bundle bundle;

    NetigsoLoader() {
        super(new ClassLoader[0], true);
    }

    void init(Bundle bundle) {
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
        NetigsoModuleFactory.start();
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load resource before initialization finished {0}", name);
            return null;
        }
        return b.getResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) {
        NetigsoModuleFactory.start();
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load resource before initialization finished {0}", name);
            return Enumerations.empty();
        }
        Enumeration ret = null;
        try {
            ret = b.getResources(name);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ret == null ? Enumerations.<URL>empty() : NbCollections.checkedEnumerationByFilter(ret, URL.class, true);
    }

    @Override
    protected Class<?> doLoadClass(String pkg, String name) {
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load class before initialization finished {0}", pkg + '.' + name);
            return null;
        }
        try {
            return b.loadClass(name);
        } catch (ClassNotFoundException ex) {
            if (NetigsoModule.LOG.isLoggable(Level.FINEST)) {
                NetigsoModule.LOG.log(Level.FINEST, "No class found in " + this, ex);
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
        Bundle b = bundle;
        if (b == null) {
            LOG.log(Level.WARNING, "Trying to load class before initialization finished {0}", new Object[] { name });
            return null;
        }
        try {
            c = b.loadClass(name);
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
        Bundle b = bundle;
        if (b == null) {
            return "Netigso[uninitialized]";
        }
        return "Netigso[" + b.getLocation() + "]";
    }
}
