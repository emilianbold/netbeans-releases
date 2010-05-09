/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        
        public org.openide.WizardDescriptor.Panel createSimpleTargetChooser(Project project, org.netbeans.api.project.SourceGroup[] folders, org.openide.WizardDescriptor.Panel bottomPanel, boolean freeFileExtension) {
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
