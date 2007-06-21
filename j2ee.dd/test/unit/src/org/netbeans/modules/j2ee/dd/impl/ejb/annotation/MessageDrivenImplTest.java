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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Martin Adamek
 */
public class MessageDrivenImplTest extends CommonTestCase  {

    public MessageDrivenImplTest(String testName) {
        super(testName);
    }
    
    public void testMessageDriven() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerMDB.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "import javax.jms.*;" +
                "@MessageDriven(mappedName = \"jms/CustomerMDB\", activationConfig =  {" +
                "        @ActivationConfigProperty(propertyName = \"acknowledgeMode\", propertyValue = \"Auto-acknowledge\")," +
                "        @ActivationConfigProperty(propertyName = \"destinationType\", propertyValue = \"javax.jms.Queue\")" +
                "    })" +
                "public class CustomerMDB implements MessageListener {" +
                "public void onMessage(Message message) {}" +
                "}");

        final String expectedResult = "foo";

        Void result = createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test CustomerMDB
                MessageDriven messageDriven = (MessageDriven) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getMessageDriven(), "CustomerMDB");
                assertEquals("foo.CustomerMDB", messageDriven.getEjbClass());
                assertEquals("CustomerMDB", messageDriven.getEjbName());
                return null;
            }
        });
    }
    
}
