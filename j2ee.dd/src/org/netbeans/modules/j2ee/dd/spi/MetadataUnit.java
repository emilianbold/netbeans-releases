/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class MetadataUnit {

    // XXX need to listen on the DD file

    public static final String PROP_DEPLOYMENT_DESCRIPTOR = "deploymentDescriptor"; // NOI18N

    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

    private final ClassPath bootPath;
    private final ClassPath compilePath;
    private final ClassPath sourcePath;

    private File deploymentDescriptor;

    public static MetadataUnit create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, File deploymentDescriptor) {
        return new MetadataUnit(bootPath, compilePath, sourcePath, deploymentDescriptor);
    }

    private MetadataUnit(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath, File deploymentDescriptor) {
        this.bootPath = bootPath;
        this.compilePath = compilePath;
        this.sourcePath = sourcePath;
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    public ClassPath getBootPath() {
        return bootPath;
    }

    public ClassPath getCompilePath() {
        return compilePath;
    }

    public ClassPath getSourcePath() {
        return sourcePath;
    }

    public synchronized FileObject getDeploymentDescriptor() {
        return deploymentDescriptor != null ? FileUtil.toFileObject(deploymentDescriptor) : null;
    }

    public void changeDeploymentDescriptor(File deploymentDescriptor) {
        synchronized (this) {
            this.deploymentDescriptor = deploymentDescriptor;
        }
        propChangeSupport.firePropertyChange(PROP_DEPLOYMENT_DESCRIPTOR, null, null);
    }
}
