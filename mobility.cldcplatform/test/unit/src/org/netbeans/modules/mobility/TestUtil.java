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

package org.netbeans.modules.mobility;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import junit.framework.Assert;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Anton Chechel
 */
public class TestUtil extends ProxyLookup {
    private static TestUtil instance;
    
    static {
        TestUtil.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", TestUtil.class.getName());
        Assert.assertEquals(TestUtil.class, Lookup.getDefault().getClass());
    }
    
    public TestUtil() {
        Assert.assertNull(instance);
        instance = this;
        setLookups(new Lookup[] {
            Lookups.singleton(TestUtil.class.getClassLoader()),
        });
        System.setProperty("netbeans.user","test/manowar");
    }
    
    /**
     * Set the global default lookup.
     */
    public static void setLookup(Lookup l) {
        instance.setLookups(new Lookup[] {l});
    }
    
    public static void setLookup(Lookup[] l) {
        instance.setLookups(l);
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object[] instances, ClassLoader cl) {
        instance.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    public static EntityCatalog testEntityCatalog() {
        return new MyEntityCatalog();
    }
    
    public static ProjectChooserFactory testProjectChooserFactory() {
        return new TestProjectChooserFactory();
    }

    public static class MyEntityCatalog extends EntityCatalog {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    }

    private static final class TestProjectChooserFactory implements ProjectChooserFactory {
        private File file;
        
        public javax.swing.JFileChooser createProjectChooser() {
            return null;
        }
        
        public org.openide.WizardDescriptor.Panel createSimpleTargetChooser(Project project, org.netbeans.api.project.SourceGroup[] folders, org.openide.WizardDescriptor.Panel bottomPanel) {
            return null;
        }
        
        public File getProjectsFolder() {
            return file;
        }
        
        public void setProjectsFolder(File file) {
            this.file = file;
        }
    }
}
