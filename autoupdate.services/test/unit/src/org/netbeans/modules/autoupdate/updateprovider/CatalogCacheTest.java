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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.net.URL;
import org.xml.sax.SAXException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jirka
 */
public class CatalogCacheTest extends NbTestCase {
    
    public CatalogCacheTest (String testName) {
        super (testName);
    }
    
    private URL URL_TO_TEST_CATALOG = null;
    private AutoupdateCatalogCache cache = null;
        
    @Override
    protected void setUp () throws Exception {
        clearWorkDir ();
        super.setUp ();
        
        URL_TO_TEST_CATALOG = this.getClass ().getResource ("data/catalog.xml");
        System.setProperty ("netbeans.user", getWorkDirPath ());
        cache = AutoupdateCatalogCache.getDefault ();
    }
    
    public void testWriteToCache () throws IOException {
        URL catalogInCache = cache.writeCatalogToCache ("test-catalog", URL_TO_TEST_CATALOG);
        assertNotNull ("Cache exists!", catalogInCache);
    }
    
    public void testCompareOriginalAndCache () throws IOException, SAXException {
        assertEquals ("Number of items is same in both places.",
                AutoupdateCatalogParser.getUpdateItems(URL_TO_TEST_CATALOG, URL_TO_TEST_CATALOG).size (),
                AutoupdateCatalogParser.getUpdateItems (cache.writeCatalogToCache("test-catalog", URL_TO_TEST_CATALOG), URL_TO_TEST_CATALOG).size ());
    }
    
    public void testGetCatalogURL () throws IOException {
        URL stored1 = cache.writeCatalogToCache ("test-1-catalog", URL_TO_TEST_CATALOG);
        URL stored2 = cache.writeCatalogToCache ("test-2-catalog", URL_TO_TEST_CATALOG);
        assertNotNull (stored1);
        assertNotNull (stored2);
        assertEquals ("Get catalog URL as same as stored", stored1, cache.getCatalogURL ("test-1-catalog"));
        assertEquals ("Get catalog URL as same as stored", stored2, cache.getCatalogURL ("test-2-catalog"));
        assertFalse ("Stored URLs of two cache cannot be same.", stored2.equals(stored1));
        assertFalse ("Stored URLs of two cache cannot be same.", cache.getCatalogURL ("test-2-catalog").equals(cache.getCatalogURL ("test-1-catalog")));
    }
    
}
