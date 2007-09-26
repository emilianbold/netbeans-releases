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

package org.netbeans.modules.apisupport.project.metainf;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileUtil;

/**
 * @author pzajac
 */
public class ServiceTest extends TestBase {
    
    public ServiceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(ServiceTest.class);
    }
    
    public void testReadServices() {
        File jarFile = new File(getDataDir(),"ServiceTest.jar");
        List /*Service*/ services = Service.readServices(jarFile);
        
        Iterator it = services.iterator() ;
        
        Service service = (Service) it.next();
        assertEquals("testicek", service.getCodebase() );
        assertEquals("java.io.InputStream", service.getFileName());
        Iterator cIt = service.getClasses().iterator() ;
        assertEquals("java.io.FileInputStream",cIt.next());
        assertEquals("java.io.BufferedInputStream",cIt.next());
        assertFalse(cIt.hasNext());
        
        service = (Service) it.next();
        assertEquals("testicek", service.getCodebase() );
        assertEquals("java.util.Collection", service.getFileName());
        cIt = service.getClasses().iterator() ;
        assertEquals("java.util.ArrayList",cIt.next());
        assertEquals("java.util.Stack",cIt.next());
        assertFalse(cIt.hasNext());
    }
    
    public void testGetOnlyProjectServices() throws IOException {
        NbModuleProject module = generateStandaloneModule("module");
        FileUtil.createData(module.getSourceDirectory(), "META-INF/services/some.test.MyService");
        FileUtil.createData(module.getSourceDirectory(), "META-INF/services/.#some.test.MyService.1.2");
        assertEquals("one service", 1, Service.getOnlyProjectServices(module).size());
    }
    
}
