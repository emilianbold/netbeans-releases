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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import java.io.File;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.ejbcore.naming.EJBNameOptions;
import org.netbeans.modules.j2ee.ejbcore.test.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.EjbJarProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.FakeJavaDataLoaderPool;
import org.netbeans.modules.j2ee.ejbcore.test.FileOwnerQueryImpl;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class SessionGeneratorTest extends TestBase {
    
    private EjbJarProviderImpl ejbJarProvider;
    private ClassPathProviderImpl classPathProvider;
    private FileObject dataDir;
    private EJBNameOptions ejbNames;

    public SessionGeneratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File file = new File(getWorkDir(),"cache");	//NOI18N
        file.mkdirs();
        IndexUtil.setCacheFolder(file);
        ejbJarProvider = new EjbJarProviderImpl();
        classPathProvider = new ClassPathProviderImpl();
        setLookups(
                ejbJarProvider, 
                classPathProvider, 
                new FileOwnerQueryImpl(),
                new FakeJavaDataLoaderPool()
                );
        dataDir = FileUtil.toFileObject(getDataDir());
        ejbNames = new EJBNameOptions();
    }

    public void testGenerate() throws Exception {
        FileObject ddFileObject = dataDir.getFileObject("EJBModule1/src/conf/ejb-jar.xml");
        FileObject[] sources = new FileObject[] {dataDir.getFileObject("EJBModule1/src/java")};
        ejbJarProvider.setEjbModule(EjbProjectConstants.J2EE_14_LEVEL, ddFileObject, sources);
        classPathProvider.setClassPath(sources);
        
        
        FileObject pkg = sources[0].getFileObject("stateless21");
        if (pkg != null) {
            pkg.delete();
        }
        pkg = sources[0].createFolder("stateless21");
        String name = "Stateless21";
        SessionGenerator sessionGenerator = SessionGenerator.create(name, pkg, true, true, false, false, false, true);
        sessionGenerator.generate();
        
        FileObject ejbClass = pkg.getFileObject(ejbNames.getSessionEjbClassPrefix() + name + ejbNames.getSessionEjbClassSuffix() + ".java");
        assertNotNull(ejbClass);
        FileObject remote = pkg.getFileObject(ejbNames.getSessionRemotePrefix() + name + ejbNames.getSessionRemoteSuffix() + ".java");
        assertNotNull(remote);
        FileObject remoteHome = pkg.getFileObject(ejbNames.getSessionRemoteHomePrefix() + name + ejbNames.getSessionRemoteHomeSuffix() + ".java");
        assertNotNull(remoteHome);
        FileObject local = pkg.getFileObject(ejbNames.getSessionLocalPrefix() + name + ejbNames.getSessionLocalSuffix() + ".java");
        assertNotNull(local);
        FileObject localHome = pkg.getFileObject(ejbNames.getSessionLocalHomePrefix() + name + ejbNames.getSessionLocalHomeSuffix() + ".java");
        assertNotNull(localHome);
    }
    
}
