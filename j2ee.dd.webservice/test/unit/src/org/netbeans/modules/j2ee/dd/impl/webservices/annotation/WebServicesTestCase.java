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

package org.netbeans.modules.j2ee.dd.impl.webservices.annotation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea, Milan Kuchtiak
 */
public class WebServicesTestCase extends JavaSourceTestCase {
    
    public WebServicesTestCase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        URL root1 = FileUtil.getArchiveRoot(javax.ejb.Stateless.class.getProtectionDomain().getCodeSource().getLocation());
        URL root2 = FileUtil.getArchiveRoot(javax.jws.WebService.class.getProtectionDomain().getCodeSource().getLocation());
        List<URL> roots = new ArrayList<URL>();
        roots.add(root1);roots.add(root2);
        addCompileRoots(roots);
    }
    
    protected MetadataModel<WebservicesMetadata> createModel() throws IOException, InterruptedException {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        MetadataUnit metadataUnit = MetadataUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
                null
                );
        return WebservicesMetadataModelFactory.createMetadataModel(metadataUnit);
    }
}
