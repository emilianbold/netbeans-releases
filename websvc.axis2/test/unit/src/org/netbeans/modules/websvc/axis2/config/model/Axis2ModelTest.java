/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.axis2.config.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.axis2.TestUtil;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author mkuchtiak
 */
public class Axis2ModelTest extends NbTestCase {

    private FileObject axis2Fo;

    public Axis2ModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        axis2Fo =  FileUtil.toFileObject(new File(getDataDir(),"axis2.xml"));
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /** Test service model for AddNumbers service
     */
    public void testServiceModel() throws IOException {
        assertNotNull(axis2Fo);
        ModelSource source = null;
        try {
            source = TestUtil.createModelSource(axis2Fo, true);
        } catch (DataObjectNotFoundException ex) {
            assert false;
            ex.printStackTrace();
            return;
        }
        
        Axis2ModelFactory instance = Axis2ModelFactory.getInstance();
        Axis2Model model = instance.getModel(source);
        assertNotNull(model);
        
        Axis2 axis2 = model.getRootComponent();
        assertNotNull(axis2);
        
        List<Service> services = axis2.getServices();        
        assertEquals(1, services.size());
        Service service = services.get(0);
        
        assertEquals("AddressBookService", service.getNameAttr());
        assertEquals("sample.addressbook.service.AddressBookService", service.getServiceClass());
        
        GenerateWsdl generateWsdl = service.getGenerateWsdl();
        assertNotNull(generateWsdl);
        assertEquals("http://address.book/",generateWsdl.getTargetNamespaceAttr());
        assertEquals("http://address.book/xsd",generateWsdl.getSchemaNamespaceAttr());
        
    }
}
