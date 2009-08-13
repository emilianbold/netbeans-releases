package org.netbeans.modules.apisupport.project.layers;

import java.util.Locale;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;

class LayerFileSystem extends MultiFileSystem {

    protected final BadgingSupport status;

    public LayerFileSystem(final FileSystem[] layers, final ClassPath cp) {
        super(layers);
        status = new BadgingSupport(this);
        status.setClasspath(cp);
        status.setSuffix("_" + Locale.getDefault());
    }

    @Override
    public FileSystem.Status getStatus() {
        return status;
    }


    public FileSystem[] getLayerFileSystems() {
        return getDelegates();
    }
}
