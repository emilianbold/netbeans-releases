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

package org.netbeans.modules.j2ee.dd.impl.client.annotation;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.client.DDProvider;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Default implemetation of the SPI for <code>MetadataModel</code>.
 * @author Tomas Mysik
 */
public class AppClientMetadataModelImpl implements MetadataModelImplementation<AppClientMetadata> {
    
    private final AnnotationModelHelper helper;
    private final AppClient root;
    private final AppClientMetadata metadata;
    
    /**
     * Constructor with all properties.
     * @param metadataUnit XXX ???
     */
    public AppClientMetadataModelImpl(MetadataUnit metadataUnit) {
        ClasspathInfo cpi = ClasspathInfo.create(
                metadataUnit.getBootPath(), metadataUnit.getCompilePath(), metadataUnit.getSourcePath());
        helper = AnnotationModelHelper.create(cpi);
        
        AppClient ddRoot = null;        
        FileObject ddFO = metadataUnit.getDeploymentDescriptor();
        if (ddFO != null) {
            try {
                ddRoot = DDProvider.getDefault().getDDRoot(ddFO);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        if (ddRoot != null && ddRoot.getVersion().doubleValue() < Double.valueOf(AppClient.VERSION_5_0).doubleValue() ) {            
            root = ddRoot;
        } else {
            root = new AppClientImpl(helper);            
        }
        metadata = new AppClientMetadataImpl(root);
    }
    
    public <R> R runReadAction(final MetadataModelAction<AppClientMetadata, R> action) throws IOException {
        return helper.runJavaSourceTask(new Callable<R>() {
            public R call() throws Exception {
                return action.run(metadata);
            }
        });
    }
    
    public boolean isReady() {
        return !helper.isJavaScanInProgress();
    }
    
    public <R> Future<R> runReadActionWhenReady(final MetadataModelAction<AppClientMetadata, R> action) throws IOException {
        return helper.runJavaSourceTaskWhenScanFinished(new Callable<R>() {
            public R call() throws Exception {
                return action.run(metadata);
            }
        });
    }
    
}
