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
