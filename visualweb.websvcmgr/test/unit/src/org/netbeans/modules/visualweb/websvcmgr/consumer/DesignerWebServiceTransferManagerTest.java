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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.websvcmgr.consumer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.HashMap;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.test.SetupData;
import org.netbeans.modules.websvc.manager.test.SetupUtil;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.SaasServicesModelTest;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.util.SaasTransferable;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author quynguyen
 */
public class DesignerWebServiceTransferManagerTest extends NbTestCase {

    private WebServiceData wsData;
    private WsdlSaas saas;
    private SetupData data;

    public DesignerWebServiceTransferManagerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        data = SetupUtil.commonSetUp(getWorkDir());
        wsData = new WebServiceData(
                data.getLocalWsdlFile().getAbsolutePath(),
                data.getLocalOriginalWsdl().toURI().toURL().toExternalForm(),
                WebServiceListModel.DEFAULT_GROUP);
        
        wsData.setPackageName("websvc");
        wsData.setCatalog(data.getLocalCatalogFile().getAbsolutePath());
        WebServiceManager.getInstance().addWebService(wsData, false);

        wsData.setCompiled(true);
        wsData.setState(WebServiceData.State.WSDL_SERVICE_COMPILED);

        File jarsDir = new File(data.getWebsvcHome(), "USZip/jaxws");
        jarsDir.mkdirs();

        File clientJar = new File(jarsDir, "USZip.jar");
        File srcJar = new File(jarsDir, "USZip-src.jar");

        SetupUtil.retrieveURL(clientJar,
                DesignerWebServiceExtImplTest.class.getResource(DesignerWebServiceExtImplTest.CLIENT_JAR_PATH));
        SetupUtil.retrieveURL(srcJar,
                DesignerWebServiceExtImplTest.class.getResource(DesignerWebServiceExtImplTest.SRC_JAR_PATH));

        assertTrue("JAX-WS proxy jar was not copied", clientJar.exists());
        assertTrue("JAX-WS source jar was not copied", srcJar.exists());

        WebServiceDescriptor descriptor = new WebServiceDescriptor(
                wsData.getName(), wsData.getPackageName(),
                WebServiceDescriptor.JAX_WS_TYPE,
                new File(wsData.getWsdlFile()).toURI().toURL(),
                new File(jarsDir, "descriptor.xml"), wsData.getWsdlService());

        descriptor.addJar("USZip.jar", WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE);
        descriptor.addJar("USZip-src.jar", WebServiceDescriptor.JarEntry.SRC_JAR_TYPE);

        wsData.setJaxWsDescriptor(descriptor);
        wsData.setJaxWsDescriptorPath(descriptor.getXmlDescriptor());
        SaasGroup group = SaasServicesModel.getInstance().getRootGroup();
        saas = new WsdlSaas(group, "test", data.getLocalOriginalWsdl().toURL().toExternalForm(), "websvc");
        SaasServicesModelTest.setWsdlData(saas, wsData);
        saas.toStateReady(true);
        assertTrue(wsData.isReady());
        assertEquals(Saas.State.READY, saas.getState());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        SetupUtil.commonTearDown();
    }

    /**
     * Test of addDataFlavors method, of class DesignerWebServiceTransferManager.
     */
    public void testPortAddDataFlavors() throws Exception {
        System.out.println("addDataFlavors(port)");

        Transferable t = new SaasTransferable(saas.getPorts().get(0), SaasTransferable.WSDL_PORT_FLAVORS);
        Transferable transferable = ExTransferable.create(t);

        HashMap<DataFlavor, Object> origMappings = new HashMap<DataFlavor, Object>();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            origMappings.put(flavors[i], transferable.getTransferData(flavors[i]));
        }

        DesignerWebServiceTransferManager instance = new DesignerWebServiceTransferManager();
        Transferable result = instance.addDataFlavors(transferable);

        assertTrue("Port display item flavor not added to transferable",
                result.isDataFlavorSupported(DesignerWebServiceTransferManager.FLAVOR_PORT_DISPLAY_ITEM));

        Object transferData = result.getTransferData(DesignerWebServiceTransferManager.FLAVOR_PORT_DISPLAY_ITEM);
        assertTrue("Transferable did not return value of type PortBeanCreateInfo",
                transferData instanceof DesignerWebServiceTransferManager.PortBeanCreateInfo);
        
        for (DataFlavor flavor : origMappings.keySet()) {
            Object updatedData = result.getTransferData(flavor);
            Object origData = origMappings.get(flavor);
            assertEquals("DataFlavor was modified: " + flavor.getHumanPresentableName(), updatedData, origData);
        }
    }

    public void testMethodAddDataFlavors() throws Exception {
        Transferable t = new SaasTransferable(saas.getPorts().get(0).getWsdlMethods().get(0), SaasTransferable.WSDL_METHOD_FLAVORS);
        Transferable transferable = ExTransferable.create(t);

        HashMap<DataFlavor, Object> origMappings = new HashMap<DataFlavor, Object>();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            origMappings.put(flavors[i], transferable.getTransferData(flavors[i]));
        }

        DesignerWebServiceTransferManager instance = new DesignerWebServiceTransferManager();
        Transferable result = instance.addDataFlavors(transferable);

        assertTrue("Method display item flavor not added to transferable",
                result.isDataFlavorSupported(DesignerWebServiceTransferManager.FLAVOR_METHOD_DISPLAY_ITEM));

        Object transferData = result.getTransferData(DesignerWebServiceTransferManager.FLAVOR_METHOD_DISPLAY_ITEM);
        assertTrue("Transferable did not return value of type MethodBeanCreateInfo",
                transferData instanceof DesignerWebServiceTransferManager.MethodBeanCreateInfo);
        
        for (DataFlavor flavor : origMappings.keySet()) {
            Object updatedData = result.getTransferData(flavor);
            Object origData = origMappings.get(flavor);
            assertEquals("DataFlavor was modified: " + flavor.getHumanPresentableName(), updatedData, origData);
        }
    }
}
