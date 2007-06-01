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

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import javax.ejb.Stateless;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class WebAppTestCase extends JavaSourceTestCase {

    public WebAppTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        URL root = FileUtil.getArchiveRoot(Stateless.class.getProtectionDomain().getCodeSource().getLocation());
        addCompileRoots(Collections.singletonList(root));
    }

    protected MetadataModel<WebAppMetadata> createModel(MetadataUnit metadataUnit) throws IOException, InterruptedException {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        return WebAppMetadataModelFactory.createMetadataModel(metadataUnit);
    }

    protected MetadataUnit createMetadataUnit(File deploymentDescriptor) {
        return MetadataUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE),
                deploymentDescriptor);
    }
}
