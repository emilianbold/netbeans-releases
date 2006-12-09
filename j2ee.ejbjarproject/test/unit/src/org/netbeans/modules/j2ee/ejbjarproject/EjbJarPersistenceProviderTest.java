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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.support.PersistenceScopesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class EjbJarPersistenceProviderTest extends NbTestCase {

    // TODO also test the contents of the classpaths

    private Project project;
    private EjbJarPersistenceProvider provider;
    private FileObject persistenceLocation;

    public EjbJarPersistenceProviderTest(String testName) {
        super(testName);
    }

    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    public PrintStream getLog() {
        return System.err;
    }

    public void setUp() throws Exception {
        // in an attempt to find the cause of issue 90762
        Logger.getLogger(PersistenceScopesHelper.class.getName()).setLevel(Level.FINEST);
        // setup the project
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        provider = (EjbJarPersistenceProvider)project.getLookup().lookup(EjbJarPersistenceProvider.class);
        persistenceLocation = project.getProjectDirectory().getFileObject("src/conf");

        FileObject persistenceXml = persistenceLocation.getFileObject("persistence.xml");
        if (persistenceXml != null) {
            persistenceXml.delete();
        }
    }

    public void testPersistenceLocation() throws Exception {
        assertEquals(persistenceLocation, provider.getLocation());
        assertEquals(persistenceLocation, provider.createLocation());
    }

    public void testPersistenceScopes() throws Exception {
        class PCL implements PropertyChangeListener {
            private int changeCount;

            public void propertyChange(PropertyChangeEvent event) {
                changeCount++;
            }
        }

        Sources src = (Sources)project.getLookup().lookup(Sources.class);
        SourceGroup[] groups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject root = groups[0].getRootFolder();

        PersistenceScopes persistenceScopes = PersistenceScopes.getPersistenceScopes(project);
        PCL listener = new PCL();
        persistenceScopes.addPropertyChangeListener(listener);

        // no persistence scope
        PersistenceScope persistenceScope = provider.findPersistenceScope(root);
        assertNull(persistenceScope);
        assertEquals(0, listener.changeCount);
        assertEquals(0, persistenceScopes.getPersistenceScopes().length);

        // adding persistence scope
        FileObject persistenceXml = persistenceLocation.createData("persistence.xml"); // NOI18N
        persistenceScope = provider.findPersistenceScope(root);
        assertNotNull(persistenceScope);
        assertEquals(persistenceXml, persistenceScope.getPersistenceXml());
        assertEquals(1, listener.changeCount);
        assertSame(persistenceScope, persistenceScopes.getPersistenceScopes()[0]);

        // testing the persistence scope classpath
        ClassPath scopeCP = persistenceScope.getClassPath();
        assertNotNull(scopeCP);
        persistenceScope = provider.findPersistenceScope(root);
        assertSame("Should return the same classpath object", scopeCP, persistenceScope.getClassPath());

        // removing persistence.xml
        persistenceXml.delete();
        assertNull("Should return a null persistence.xml", persistenceScope.getPersistenceXml());
        persistenceScope = provider.findPersistenceScope(root);
        assertNull(persistenceScope);
        assertEquals(2, listener.changeCount);
        assertEquals(0, persistenceScopes.getPersistenceScopes().length);

        // re-adding persistence scope
        persistenceLocation.createData("persistence.xml"); // NOI18N
        persistenceScope = provider.findPersistenceScope(root);
        assertTrue("Should always return a valid persistence.xml", persistenceScope.getPersistenceXml().isValid());
    }

    public void testPersistenceClassPath() throws Exception {
        ClassPath persistenceCP = provider.getClassPath();
        assertNotNull(persistenceCP);
        assertSame("Should return the same classpath object", persistenceCP, provider.getClassPath());
    }
}
