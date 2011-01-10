/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.windows;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;

public class TopComponentProcessorTest extends  NbTestCase {

    public TopComponentProcessorTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return getName().contains("InEQ");
    }

    public void testTCRegisteredFine() throws Exception {
        FileObject set = FileUtil.getConfigFile("Windows2/Components/my-tc.settings");
        assertNotNull("Settings file found", set);
        assertValidate(set.asText());
    }

    public void testModeIsOK() throws Exception {
        FileObject set = FileUtil.getConfigFile("Windows2/Modes/output/my-tc.wstcref");
        assertNotNull("Mode file found", set);
        final String t = set.asText();
        assertValidate(t);
        assertEquals("not opened, no true in there", -1, t.indexOf("true"));
    }
    
    public void testFactoryRegisteredFine() throws Exception {
        FileObject set = FileUtil.getConfigFile("Windows2/Components/factory-tc.settings");
        assertNotNull("Settings file found", set);
        assertValidate(set.asText());
    }

    public void testFactoryModeIsOK() throws Exception {
        FileObject set = FileUtil.getConfigFile("Windows2/Modes/explorer/factory-tc.wstcref");
        assertNotNull("Mode file found", set);
        final String t = set.asText();
        assertValidate(t);
        assertTrue("opened, no true in there", t.indexOf("true") > 0);
    }

    public void testFactoryActionIsOKInEQ() throws Exception {
        assertTrue("This one needs to run in EQT", EventQueue.isDispatchThread());
        FileObject fo = FileUtil.getConfigFile("Actions/Windows/open-factory-tc.instance");
        assertNotNull("Action generated", fo);
        Action a = (Action)fo.getAttribute("instanceCreate");
        assertNotNull("Action found", a);
        assertEquals("Not created yet", 0, TC.cnt);
        a.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Created", 1, TC.cnt);
        assertEquals("Hello TC!", a.getValue(Action.NAME));
        assertEquals("any/iconbase.png", a.getValue("iconBase"));
        assertEquals("preferredID found", "factory.tc", fo.getAttribute("preferredID"));
        
        FileObject dir = FileUtil.getConfigFile("Kuk/Huk");
        assertNotNull("Kuk/Huk found", dir);
        FileObject ref = dir.getFileObject("open-factory-tc.shadow");
        assertNotNull("Reference found: " + Arrays.toString(dir.getChildren()), ref);
        assertEquals(fo.getPath(), ref.getAttribute("originalFile"));
    }
    public void testMultipleUsageInEQ() throws Exception {
        FileObject pukMuk = FileUtil.getConfigFile("Puk/Muk/multi-use.shadow");
        assertNotNull("One reference found", pukMuk);

        FileObject jukLuk = FileUtil.getConfigFile("Juk/Luk/multi-use.shadow");
        assertNotNull("2nd reference found", jukLuk);
        
        FileObject fo = FileUtil.getConfigFile("Actions/Windows/multi-use.instance");
        assertNotNull("Action generated", fo);
        Action a = (Action)fo.getAttribute("instanceCreate");
        assertNotNull("Action created", a);
        assertEquals("No call to withReferences factory yet", 0, TC.cnt2);
        a.actionPerformed(new ActionEvent(this, 0, null));
        assertEquals("One call to factory", 1, TC.cnt2);
    }
    
    private static void assertValidate(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(true);
        DocumentBuilder b = f.newDocumentBuilder();
        Document res = b.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        assertNotNull("Parsed OK", res);
    }
    
    @TopComponent.Registration(
        mode="output",
        openAtStartup=false
    )
    @TopComponent.Description(
        preferredID="my-tc", iconBase="org/openide/windows/Icon.png"
    )
    public static class TC1 extends TopComponent {
    }
    
    @TopComponent.Registration(
        mode="explorer",
        openAtStartup=true
    )
    @TopComponent.Description(
        preferredID="factory.tc", iconBase="any/iconbase.png"
    )
    public static class TC extends TopComponent {
        static int cnt;
        static int cnt2;
        
        @ActionID(category="Windows", id="open.factory.tc")
        @TopComponent.OpenActionRegistration(displayName="#TEST_ACTION",preferredID="factory.tc")
        @ActionReference(path="Kuk/Huk")
        public static TC create() {
            cnt++;
            return new TC();
        }
        
        @ActionID(category="Windows", id="multi.use")
        @TopComponent.OpenActionRegistration(displayName="#TEST_ACTION",preferredID="multi.use")
        @ActionReferences({
            @ActionReference(path="Puk/Muk"),
            @ActionReference(path="Juk/Luk")
        })
        public static TC withReferences() {
            cnt2++;
            return new TC();
        }
    }
}
