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
package com.sun.javacard.filemodels;
import com.sun.javacard.AID;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Tim Boudreau
 */
public class AppletXmlModelTest {

    public AppletXmlModelTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAllAIDs() {
    }

    public static final byte[] DEFAULT_RID = new byte[] { 1, 2, 3, 4, 5 };

    @Test
    public void testContainsClass() throws IOException {
        InputStream in = AppletXmlModelTest.class.getResourceAsStream("applet-good.xml");
        AppletXmlModel mdl = new AppletXmlModel (in);
        assertTrue (mdl.containsClass("com.foo.baz.SomeOtherApplet"));
        assertTrue (mdl.containsClass("com.foo.bar.MyApplet"));
        assertFalse (mdl.containsClass("com.foo.bar.Nothing"));
        assertTrue (mdl.isClosed());
        Exception ex = null;
        try {
            mdl.add(new AppletXmlAppletEntry("FOO", "com.foo.BAR", AID.generateApplicationAid(DEFAULT_RID, "com.foo.BAR"), 3));
        } catch (Exception x) {
            ex = x;
        }
        assertNotNull ("Exception should have been thrown adding to a closed model", ex);
        Set<AID> aids = new HashSet<AID>();
        for (AID a : mdl.allAIDs()) {
            aids.add (a);
        }
        assertEquals (2, aids.size());
        assertTrue (aids.contains(AID.parse("//aid/F880E6C8B8/7E")));
        assertTrue (aids.contains(AID.parse("//aid/F880E6C8B8/7D")));

        AppletXmlModel nue = new AppletXmlModel();
        int ix = 0;
        for (AppletXmlAppletEntry e : mdl.getData()) {
            AppletXmlAppletEntry ee = new AppletXmlAppletEntry(e.displayName, e.clazz, e.aid, ix);
            ix++;
            nue.add (e);
        }
        assertEquals (mdl.toXml(), nue.toXml());
        assertEquals ("Expected\n'" + mdl.toXml()+"'\n but got \n'" + nue.toXml() +"'\n", mdl, nue);
        nue.add (new AppletXmlAppletEntry("HiThere", "org.boo.Baz", AID.parse("//aid/F880E6C8B8/32"), ix));
        assertFalse (mdl.equals(nue));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter p = new PrintWriter(out);
        p.println(nue.toXml());
        p.flush();
        p.close();
        out.close();

        ByteArrayInputStream ins = new ByteArrayInputStream(out.toByteArray());
        AppletXmlModel newer = new AppletXmlModel (ins);
        ins.close();
        assertEquals (nue, newer);
        assertFalse (mdl.equals(newer));
        assertNull (mdl.getProblem());
        assertNull (nue.getProblem());
        assertNull (newer.getProblem());
    }

    @Test
    public void testEntryEquality() {
        System.out.println("testEntryEquality");
        AppletXmlAppletEntry a = new AppletXmlAppletEntry("hello", "com.foo.bar", AID.parse("//aid/1234567890/03"), 0);
        AppletXmlAppletEntry b = new AppletXmlAppletEntry("hello", "com.foo.bar", AID.parse("//aid/1234567890/03"), 0);
        assertEquals (a.toXml(), b.toXml());
        assertEquals (a, b);
    }

    @Test
    public void testUnexpectedTags() throws IOException {
        System.out.println("testUnexpectedTags");
         InputStream in = AppletXmlModelTest.class.getResourceAsStream("applet-with-junk.xml");
         AppletXmlModel mdl = new AppletXmlModel(in);
         assertNotNull (mdl.hasUnknownTags());
         assertNull (mdl.getProblem());
    }
}