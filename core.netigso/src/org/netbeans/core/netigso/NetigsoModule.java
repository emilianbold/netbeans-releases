package org.netbeans.core.netigso;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

final class NetigsoModule extends Module {
    static final Logger LOG = Logger.getLogger(NetigsoModule.class.getPackage().getName());

    private File jar;
    private Bundle bundle;
    private NetigsoLoader loader;
    private Manifest manifest;

    public NetigsoModule(Manifest mani, File jar, ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        super(mgr, ev, history, reloadable, autoload, eager);
        this.jar = jar;
        this.manifest = mani;
    }

    @Override
    public String[] getProvides() {
        return new String[0];
    }

    @Override
    public String getCodeName() {
        return getCodeNameBase();
    }

    @Override
    public String getCodeNameBase() {
        String version = manifest.getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
        return version.replace('-', '_');
    }

    @Override
    public int getCodeNameRelease() {
        String version = manifest.getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
        int slash = version.lastIndexOf('/');
        if (slash != -1) {
            return Integer.parseInt(version.substring(slash + 1));
        }
        return -1;
    }

    @Override
    public SpecificationVersion getSpecificationVersion() {
        String version = manifest.getMainAttributes().getValue("Bundle-Version"); // NOI18N
        if (version == null) {
            NetigsoModule.LOG.log(Level.WARNING, "No Bundle-Version for {0}", jar);
            return new SpecificationVersion("0.0");
        }
        int pos = -1;
        for (int i = 0; i < 3; i++) {
            pos = version.indexOf('.', pos + 1);
            if (pos == -1) {
                return new SpecificationVersion(version);
            }
        }
        return new SpecificationVersion(version.substring(0, pos));
    }

    @Override
    public String getImplementationVersion() {
        String explicit = super.getImplementationVersion(); // OIDE-M-I-V/-B-V added by NB build harness
        return explicit != null ? explicit : manifest.getMainAttributes().getValue("Bundle-Version"); // NOI18N
    }

    @Override
    protected void parseManifest() throws InvalidException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<File> getAllJars() {
        return Collections.emptyList();
    }

    @Override
    public void setReloadable(boolean r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reload() throws IOException {
        throw new UnsupportedOperationException();
    }

    final void start() throws IOException {
        if (bundle != null) {
            return;
        }
        Bundle b = null;
        try {
            BundleContext bc = NetigsoModuleFactory.getContainer().getBundleContext();
            b = bc.installBundle(jar.toURI().toURL().toExternalForm());
            loader.init(b);
            b.start();
        } catch (BundleException ex) {
            throw (IOException)new IOException(ex.getMessage()).initCause(ex);
        }
        bundle = b;
    }

    @Override
    protected void classLoaderUp(Set<Module> parents) throws IOException {
        loader = new NetigsoLoader();
        NetigsoModuleFactory.classLoaderUp(this);
    }

    @Override
    protected void classLoaderDown() {
        NetigsoModule.LOG.log(Level.FINE, "classLoaderDown {0}", getCodeNameBase()); // NOI18N
        if (bundle == null) {
            NetigsoModuleFactory.classLoaderDown(this);
            return;
        }
        assert bundle.getState() == Bundle.ACTIVE;
        try {
            bundle.stop();
        } catch (BundleException ex) {
            throw new IllegalStateException(ex);
        }
        loader = null;
    }

    @Override
    public ClassLoader getClassLoader() throws IllegalArgumentException {
        if (loader == null) {
            try {
                classLoaderUp(null);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (loader == null) {
            throw new IllegalArgumentException("No classloader for " + getCodeNameBase()); // NOI18N
        }
        return loader;
    }

    @Override
    protected void cleanup() {
    }

    @Override
    protected void destroy() {
    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public Manifest getManifest() {
        return manifest;
    }

    @Override
    public Object getLocalizedAttribute(String attr) {
        // TBD;
        return null;
    }

    @Override
    public String toString() {
        return "Netigso: " + jar;
    }
}
