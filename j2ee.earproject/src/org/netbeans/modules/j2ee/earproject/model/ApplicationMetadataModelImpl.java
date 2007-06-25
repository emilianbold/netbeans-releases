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

package org.netbeans.modules.j2ee.earproject.model;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.Exceptions;
import org.openide.util.MutexException;

/**
 * Default implemetation of the SPI for <code>MetadataModel</code>.
 * <p>
 * This class uses {@link org.netbeans.api.project.ProjectManager#mutex() write mutex}
 * because it reads metadata model which should to be immutable during reading.
 * @author Tomas Mysik
 */
public class ApplicationMetadataModelImpl implements MetadataModelImplementation<ApplicationMetadata> {
    
    private final Application root;
    private final ApplicationMetadata metadata;
    
    /**
     * Constructor with all properties.
     * @param earProject EAR project instance for which corresponding model is created.
     */
    public ApplicationMetadataModelImpl(final EarProject earProject) {
        
        Application ddRoot = null;
        FileObject ddFO = getDeploymentDescriptor(earProject);
        if (ddFO != null) {
            try {
                ddRoot = DDProvider.getDefault().getDDRoot(ddFO);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        if (ddRoot != null) {
            root = ddRoot;
        } else {
            // see javadoc of this class
            root = ProjectManager.mutex().writeAccess(new Mutex.Action<Application>() {
                public Application run() {
                    return new ApplicationImpl(earProject);
                }
            });
        }
        metadata = new ApplicationMetadataImpl(root);
    }

    public <R> R runReadAction(final MetadataModelAction<ApplicationMetadata, R> action) throws MetadataModelException, IOException {
        try {
            // see javadoc of this class
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<R>() {
                public R run() throws Exception {
                    enterRunReadAction();
                    try {
                        return action.run(metadata);
                    } finally {
                        leaveRunReadAction();
                    }
                }
            });
        } catch (MutexException mutexException) {
            throw new MetadataModelException(mutexException.getException());
        }
    }
    
    public boolean isReady() {
        return true;
    }
    
    public <R> Future<R> runReadActionWhenReady(final MetadataModelAction<ApplicationMetadata, R> action) throws IOException {
        return new SimpleFuture<R>(runReadAction(action));
    }
    
    private FileObject getDeploymentDescriptor(final EarProject earProject) {
        FileObject ddFO = earProject.getAppModule().getDeploymentDescriptor();
        if (ddFO == null
                && EarProjectUtil.isDDCompulsory(earProject)) {
            try {
                ddFO = EarProjectGenerator.setupDD(
                        earProject.getJ2eePlatformVersion(),
                        earProject.getAppModule().getMetaInf(),
                        earProject,
                        true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return ddFO;
    }
    
    private void enterRunReadAction() {
        Application application = metadata.getRoot();
        if (application instanceof ApplicationImpl) {
            ((ApplicationImpl) application).enterRunReadAction();
        }
    }

    private void leaveRunReadAction() {
        Application application = metadata.getRoot();
        if (application instanceof ApplicationImpl) {
            ((ApplicationImpl) application).leaveRunReadAction();
        }
    }
    
    private static final class SimpleFuture<R> implements Future<R> {
        
        private volatile R result;
        
        public SimpleFuture(R result) {
            this.result = result;
        }
        
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
        
        public boolean isCancelled() {
            return false;
        }
        
        public boolean isDone() {
            return true;
        }
        
        public R get() throws InterruptedException, ExecutionException {
            return result;
        }
        
        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
