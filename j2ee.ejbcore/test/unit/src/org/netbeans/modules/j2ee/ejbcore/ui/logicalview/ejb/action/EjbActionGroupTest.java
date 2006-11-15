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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.io.File;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.ejbcore.test.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.EjbJarProviderImpl;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.j2ee.ejbcore.test.TestUtilities;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public class EjbActionGroupTest extends TestBase {
    
    private EjbJarProviderImpl ejbJarProvider;
    private ClassPathProviderImpl classPathProvider;
    private FileObject dataDir;
    private FileObject testFO;
    private EJBActionGroup ejbActionGroup;
    
    public EjbActionGroupTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        File file = new File(getWorkDir(),"cache");	//NOI18N
        file.mkdirs();
        IndexUtil.setCacheFolder(file);
        ejbJarProvider = new EjbJarProviderImpl();
        classPathProvider = new ClassPathProviderImpl();
        setLookups(ejbJarProvider, classPathProvider);
        dataDir = FileUtil.toFileObject(getDataDir());
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
        ejbActionGroup = new EJBActionGroup();
    }
    
    public void testEnable() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   public TestClass() { " +
                "   }" +
                "   public void method() {" +
                "   }" +
                "}");
        Node node = new AbstractNode(Children.LEAF, Lookups.singleton(testFO));
        assertFalse(ejbActionGroup.enable(new Node[] {node}));

        FileObject beanClass = dataDir.getFileObject("EJBModule1/src/java/foo/NewSessionBean.java");
        FileObject ddFileObject = dataDir.getFileObject("EJBModule1/src/conf/ejb-jar.xml");
        FileObject[] sources = new FileObject[] {dataDir.getFileObject("EJBModule1/src/java")};
        ejbJarProvider.setEjbModule(EjbProjectConstants.J2EE_14_LEVEL, ddFileObject, sources);
        classPathProvider.setClassPath(sources);
        
        node = new AbstractNode(Children.LEAF, Lookups.singleton(beanClass));
        assertTrue(ejbActionGroup.enable(new Node[] {node}));
    }
    
}
