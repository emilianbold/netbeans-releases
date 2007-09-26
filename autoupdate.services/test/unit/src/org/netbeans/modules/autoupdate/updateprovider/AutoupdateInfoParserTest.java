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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateInfoParserTest extends DefaultTestCase {
    
    public AutoupdateInfoParserTest (String testName) {
        super (testName);
    }
    
    private static File NBM_FILE = null;
    private static final String LICENSE_NAME = "AD9FBBC9";
    
    protected void setUp() throws Exception {
        super.setUp();
        URL urlToFile = AutoupdateInfoParserTest.class.getResource ("data/org-yourorghere-depending.nbm");
        NBM_FILE = new File (urlToFile.toURI ());
        assertNotNull ("data/org-yourorghere-depending.nbm file must found.", NBM_FILE);
    }

    public void testGetUpdateItem() throws URISyntaxException, IOException, SAXException {
        Document doc = AutoupdateInfoParser.getAutoupdateInfo (NBM_FILE);
        assertNotNull ("info.xml found in " + NBM_FILE, doc);
    }

    public void testGetLicenses() {
        Map<String, String> licenses = AutoupdateInfoParser.getLicenses (NBM_FILE);
        assertNotNull (NBM_FILE + " contains License element.", licenses);
        assertEquals (NBM_FILE + " contains only one License element.", 1, licenses.size());
        assertNotNull ("License " + LICENSE_NAME + " found.", licenses.get (LICENSE_NAME));
        assertEquals ("Correct content of " + LICENSE_NAME, "[NO LICENSE SPECIFIED]", licenses.get (LICENSE_NAME));
    }

    public void testCreateSimpleItems () throws IOException, SAXException {
        Map<String, UpdateItem> items = AutoupdateInfoParser.getUpdateItems (NBM_FILE);
        assertNotNull ("UpdateItems found in file " + NBM_FILE, items);
        assertEquals ("UpdateItems contains only once item in file" + NBM_FILE, 1, items.size ());
        String id = items.keySet ().iterator ().next ();
        assertNotNull (NBM_FILE + " contains UpdateItem.", items.get (id));
    }

}
