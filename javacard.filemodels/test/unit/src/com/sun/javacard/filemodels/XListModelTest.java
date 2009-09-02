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

package com.sun.javacard.filemodels;

import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class XListModelTest {

    @Test
    public void testModel() {
        System.out.println("testModel");
        ByteArrayInputStream in = new ByteArrayInputStream (CONTENT.getBytes());
        XListModel mdl = new XListModel(in, null);
        List<? extends XListEntry> entries = mdl.getData();
        assertEquals (4, entries.size());
        String[] names = new String[] {
            "ClassicApplet1",
            "ExtendedApplet1",
            "ClassicLibrary1",
            "WebApplication3",
        };
        String[] types = new String[] {
            "classic-applet",
            "extended-applet",
            "classic-lib",
            "web",
        };
        int[] instanceCounts = new int[] {
            1, 4, 0, 1
        };
        for (int i=0; i < entries.size(); i++) {
            XListEntry e = entries.get(i);
            assertEquals (names[i], e.getDisplayName());
            assertEquals (types[i], e.getType());
            assertEquals (instanceCounts[i], e.getInstances().size());
        }

        String xml = mdl.toXml();
        XListModel b = new XListModel(new ByteArrayInputStream(xml.getBytes()), null);
        assertEquals (mdl, b);
    }

    private static String CONTENT = "<list>\n"
        +"    <bundle>\n"
        +"        <name>ClassicApplet1</name>\n"
        +"        <type>classic-applet</type>\n"
        +"        <instances>\n"
        +"            <instance>//aid/DE43A8C922/DF</instance>\n"
        +"        </instances>\n"
        +"    </bundle>\n"
        +"\n"
        +"    <bundle>\n"
        +"        <name>ExtendedApplet1</name>\n"
        +"        <type>extended-applet</type>\n"
        +"        <instances>\n"
        +"            <instance>//aid/DE43A8C922/C9</instance>\n"
        +"            <instance>//aid/DE43A8C922/C9B2</instance>\n"
        +"            <instance>//aid/DE43A8C922/CA</instance>\n"
        +"\n"
        +"            <instance>//aid/DE43A8C922/CB</instance>\n"
        +"        </instances>\n"
        +"    </bundle>\n"
        +"    <bundle>\n"
        +"        <name>ClassicLibrary1</name>\n"
        +"        <type>classic-lib</type>\n"
        +"    </bundle>\n"
        +"\n"
        +"    <bundle>\n"
        +"        <name>WebApplication3</name>\n"
        +"        <type>web</type>\n"
        +"        <instances>\n"
        +"            <instance>///webapplication3</instance>\n"
        +"        </instances>\n"
        +"    </bundle>\n"
        +"\n"
        +"</list>\n";
}