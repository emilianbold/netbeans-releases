/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.support;

import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.dlight.api.impl.DLightToolConfigurationAccessor;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import static org.junit.Assert.*;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mt154047
 */
public class DLightToolConfigurationProviderFactoryTest {

    private FileObject folder;

    public DLightToolConfigurationProviderFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        folder = FileUtil.getConfigFile("DLight/XMLToolConfigurations");
        assertNotNull("testing layer is loaded: ", folder);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of create method, of class DLightToolConfigurationProviderFactory.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        Map map = null;
        DLightToolConfiguration result = null;
        try {
             result = readAction("XMLFopsToolConfiguration.instance");
        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("DLightToolConfiguration should not be null", result);
        DLightToolConfigurationAccessor toolConfigurationAccessor = DLightToolConfigurationAccessor.getDefault();
        System.out.println("name=" + toolConfigurationAccessor.getToolName(result));
        System.out.println("displayedName=" + toolConfigurationAccessor.getDetailedToolName(result));
        System.out.println("id=" + result.getID());
        // TODO review the generated test code and remove the default call to fail.
        
    }

     private DLightToolConfiguration readAction(String fileName) throws Exception {
        FileObject fo = this.folder.getFileObject(fileName);
        assertNotNull("file " + fileName, fo);

        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("File object has not null instanceCreate attribute", obj);
        

        if (!(obj instanceof DLightToolConfiguration)) {
            fail("Object needs to be DLightConfiguration: " + obj);
        }

        return (DLightToolConfiguration)obj;
    }
    
}
