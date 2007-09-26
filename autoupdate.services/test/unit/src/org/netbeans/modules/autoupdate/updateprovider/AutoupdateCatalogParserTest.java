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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author jirka
 */
public class AutoupdateCatalogParserTest extends NbTestCase {
    
    public AutoupdateCatalogParserTest (String testName) {
        super (testName);
    }
    
    private URL URL_TO_TEST_CATALOG = null;
    private int COUNT_OF_SIMPLE_ITEMS = 317;
    private int COUNT_OF_UPDATE_ITEMS = 285;
    private int COUNT_OF_LICENSES = 32;
    
//    private UpdateItem TEST_UPDATE_ITEM;
    
    protected void setUp () throws Exception {
        URL_TO_TEST_CATALOG = AutoupdateCatalogParserTest.class.getResource ("data/catalog.xml");
//        Manifest manifest = new Manifest ();
//        Attributes mfAttrs = manifest.getMainAttributes ();
//        mfAttrs.put (new Attributes.Name ("OpenIDE-Module"), "org.myorg.module/1");
//        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Implementation-Version"), "060216");
//        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Long-Description"), "Real module Hello installs Hello menu item into Help menu.");
//        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Module-Dependencies"), "org.openide.util &gt; 6.9.0.1");
//        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Name"), "module");
//        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Requires"), "org.openide.modules.ModuleFormat1");
//        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Specification-Version"), "1.0");
//        UpdateLicense lic = UpdateLicense.createUpdateLicense ("no-license", "");
//        TEST_UPDATE_ITEM = UpdateItem.createModule ("org.myorg.module",
//                                            "1.0",
//                                            new URL ("http://platform.netbeans.org/org-myorg-module.nbm"),
//                                            "Jiri Rechtacek",
//                                            "111",
//                                            "http://www.youorghere.org",
//                                            manifest,
//                                            Boolean.FALSE,
//                                            Boolean.FALSE,
//                                            "",
//                                            lic);
    }
    
    public void testGetDocument () throws IOException, SAXException {
        assertNotNull ("Document in URL " + URL_TO_TEST_CATALOG + "  is not null.", AutoupdateCatalogParser.getDocument (URL_TO_TEST_CATALOG, null));
    }
    
    public void testCreateSimpleItems () throws IOException, SAXException {
        Document d = AutoupdateCatalogParser.getDocument (URL_TO_TEST_CATALOG, null);
        List<SimpleItem> items = AutoupdateCatalogParser.createSimpleItems (d);
        assertFalse ("Items not empty.", items.isEmpty ());
        assertEquals("All x items are read.", COUNT_OF_SIMPLE_ITEMS, items.size ());
        
        SimpleItem item = items.get (0);
        assertTrue ("Item " + item + " instanceof SimpleItem.Feature", item instanceof SimpleItem.Feature);
        SimpleItem.Feature f = (SimpleItem.Feature) item;
        assertEquals ("Correct mapping to Collaboration", "collaboration-feature_1.0", f.getId());
    }
    
    public void testRelativeUrlPath () {
        Map<String, UpdateItem> items = AutoupdateCatalogParser.getUpdateItems (URL_TO_TEST_CATALOG, URL_TO_TEST_CATALOG);
        assertFalse ("Items not empty.", items.isEmpty());
        assertEquals("All x items are read.", COUNT_OF_UPDATE_ITEMS, items.size());
        
        UpdateItem item = items.get ("org.myorg.relative.module_1.0");
        assertNotNull ("Test module found.", item);
    }
    
    public void testGetLicenses () {
        Map<String, String> licenses = AutoupdateCatalogParser.getLicenses (URL_TO_TEST_CATALOG, null);
        assertFalse ("Items not empty.", licenses.isEmpty());
        assertEquals("All x items are read.", COUNT_OF_LICENSES, licenses.size ());
        
        assertNotNull ("no-license.txt found.", licenses.get ("no-license.txt"));
        assertEquals ("Right content of no-license.txt", "[NO LICENSE SPECIFIED]", licenses.get ("no-license.txt"));
    }
    
}
