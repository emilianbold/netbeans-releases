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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.io.File;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class WebAppMetadataModelTest extends WebAppTestCase {

    // XXX also test version in testDelegation

    public WebAppMetadataModelTest(String name) {
        super(name);
    }

    public void testDelegation() throws Exception {
        FileObject webXmlFO = TestUtilities.copyStringToFileObject(srcFO, "web.xml",
                "<?xml version='1.0' encoding='UTF-8'?>" +
                "<web-app version='2.5' xmlns='http://java.sun.com/xml/ns/javaee' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd'>" +
                "   <servlet>" +
                "       <servlet-name>Servlet</servlet-name>" +
                "       <servlet-class>org.example.Servlet</servlet-class>" +
                "   </servlet>" +
                "</web-app>");
        File webXmlFile = FileUtil.toFile(webXmlFO);
        MetadataUnit metadataUnit = createMetadataUnit(webXmlFile);
        MetadataModel model = createModel(metadataUnit);
        MetadataModelAction<WebAppMetadata, Integer> action = new MetadataModelAction<WebAppMetadata, Integer>() {
            public Integer run(WebAppMetadata metadata) throws Exception {
                return metadata.getRoot().getServlet().length;
            }
        };
        assertEquals(1, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(new File("/foo/bar/baz/web.xml"));
        assertEquals(0, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(null);
        assertEquals(0, model.runReadAction(action));
        metadataUnit.changeDeploymentDescriptor(webXmlFile);
        assertEquals(1, model.runReadAction(action));
    }
}
