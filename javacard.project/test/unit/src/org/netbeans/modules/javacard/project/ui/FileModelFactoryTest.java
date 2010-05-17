/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.ui;

import com.sun.javacard.AID;
import com.sun.javacard.filemodels.DeploymentXmlAppletEntry;
import com.sun.javacard.filemodels.DeploymentXmlInstanceEntry;
import com.sun.javacard.filemodels.DeploymentXmlModel;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
/**
 *
 * @author Tim Boudreau
 */
public class FileModelFactoryTest {

    public FileModelFactoryTest() {
    }

    @Test
    public void testDeploymentModel() throws Exception {
        InputStream in = FileModelFactoryTest.class.getResourceAsStream("deployment-good.xml");
        DeploymentXmlModel mdl;
        try {
            mdl = new DeploymentXmlModel(in);
        } finally {
            in.close();
        }
        assertEquals (2, mdl.getData().size());
        AbstractNode[] nds = new AbstractNode[] {
            new AbstractNode(Children.LEAF, Lookups.singleton("com.foo.bar.MyApplet")),
            new AbstractNode(Children.LEAF, Lookups.singleton("com.foo.bar.MyOtherApplet")),
        };
        nds[0].setValue (CheckboxListView.SELECTED, Boolean.TRUE);
        nds[1].setValue (CheckboxListView.SELECTED, Boolean.TRUE);
        nds[0].setValue (FileModelFactory.ORDER, 0);
        nds[0].setValue (FileModelFactory.APPLET_AID, AID.parse("//aid/F880E6C8B8/7D"));
        nds[1].setValue (FileModelFactory.APPLET_AID, AID.parse("//aid/F880E6C8B8/FFF0"));
        nds[1].setValue (FileModelFactory.ORDER, 0);


        FileModelFactory.writeTo(mdl, nds);
        assertNotNull (nds[0].getValue(FileModelFactory.DEPLOYMENT_ENTRY));
        assertNotNull (nds[1].getValue(FileModelFactory.DEPLOYMENT_ENTRY));

        DeploymentXmlModel nue = FileModelFactory.deploymentXmlModel(nds);
        System.out.println("OLD XML:");
        System.out.println(mdl.toXml());
        System.out.println("NEW XML:");
        System.out.println(nue.toXml());
        assertEquals (mdl, nue);

        DeploymentXmlAppletEntry e = (DeploymentXmlAppletEntry) nds[0].getValue(FileModelFactory.DEPLOYMENT_ENTRY);
        e.getData().get(0).setDeploymentParams("EEEE11");

        System.out.println("NEW XML NOW");
        System.out.println(nue.toXml());
        assertFalse (mdl.equals(nue));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter (out);
        try {
            pw.println(nue.toXml());
        } finally {
            pw.close();
            out.close();
        }
        ByteArrayInputStream in2 = new ByteArrayInputStream(out.toByteArray());
        DeploymentXmlModel newer = new DeploymentXmlModel(in2);
        in2.close();
        System.out.println("NEWER:\n" + newer.toXml());
        assertEquals (nue, newer);
        DeploymentXmlInstanceEntry er = newer.getData().get(0).getData().get(0);
        assertEquals ("EEEE11", er.getDeploymentParams());
        assertEquals (AID.parse("//aid/F880E6C8B8/7E"), er.getInstanceAID());
        assertFalse (mdl.equals(newer));
    }
}