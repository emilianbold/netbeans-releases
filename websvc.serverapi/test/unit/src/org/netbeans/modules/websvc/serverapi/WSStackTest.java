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
package org.netbeans.modules.websvc.serverapi;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.serverapi.api.WSStack;
import org.netbeans.modules.websvc.serverapi.api.WSStackFeature;
import org.netbeans.modules.websvc.serverapi.api.WSStackProvider;
import org.netbeans.modules.websvc.serverapi.api.WSUriDescriptor;
import org.netbeans.modules.websvc.serverapi.spi.WSStackFactory;
import org.netbeans.modules.websvc.serverapi.spi.WSStackSPI;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkuchtiak
 */
public class WSStackTest extends NbTestCase {

    private FileObject axis2Fo;

    public WSStackTest(String testName) {
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
        WSStackSPI jdkStackImpl = new WSStackSPI() {

            public String getName() {
                return WSStack.STACK_JAX_WS;
            }

            public String getVersion() {
                return "1.2";
            }

            public WSStackProvider getWSStackProvider() {
                return WSStackProvider.SERVER;
            }

            public Set<String> getSupportedTools() {
                Set<String> supportedTools = new HashSet<String>();
                supportedTools.add(WSStack.TOOL_WSGEN);
                supportedTools.add(WSStack.TOOL_WSIMPORT);
                return supportedTools;
            }

            public File[] getToolClassPathEntries(String toolName) {
                return new File[]{};
            }

            public WSUriDescriptor getServiceUriDescriptor() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Set<WSStackFeature> getServiceFeatures() {
                return Collections.<WSStackFeature>singleton(WSStackFeature.TESTER_PAGE);
            }
            
        };
        WSStack jdkStack = WSStackFactory.createWSStack(jdkStackImpl);
        assertNotNull(jdkStack);
        assertEquals(jdkStack.getName(),WSStack.STACK_JAX_WS);
        assertEquals(jdkStack.getVersion(),"1.2");
        assertEquals(jdkStack.getWSStackProvider(),WSStackProvider.SERVER);
        assertEquals(2, jdkStack.getSupportedTools().size());
    }
}
