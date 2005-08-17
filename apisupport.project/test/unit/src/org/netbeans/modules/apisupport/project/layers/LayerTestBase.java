/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.layers;

import java.io.IOException;
import junit.framework.Assert;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.core.XMLDataLoader;
import org.netbeans.modules.xml.core.XMLDataObjectLook;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Superclass for tests that work with XML layers.
 * Does some setup, since TAX requires some special infrastructure.
 * @author Jesse Glick
 * @see "#62363"
 */
public abstract class LayerTestBase extends NbTestCase {
    
    // Copied from org.netbeans.api.project.TestUtil:
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        Assert.assertEquals(Lkp.class, Lookup.getDefault().getClass());
        LayerUtils.LayerHandle.PROVIDER = new LayerUtils.LayerHandle.XmlDataObjectProvider() {
            public TreeEditorCookie cookieForDataObject(DataObject d) {
                return new TreeEditorCookieImpl((XMLDataObjectLook) d);
            }
        };
    }
    
    public static final class Lkp extends ProxyLookup {
        private static Lkp DEFAULT;
        public Lkp() {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            setLookup(new Object[0]);
        }
        public static void setLookup(Object[] instances) {
            ClassLoader l = Lkp.class.getClassLoader();
            DEFAULT.setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
            });
        }
    }
    
    protected LayerTestBase(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        Lkp.setLookup(new Object[] {
            SharedClassObject.findObject(XMLDataLoader.class, true),
            new TestCatalog(),
        });
    }
    
    /**
     * In the actual IDE, the default NetBeans Catalog will already be "mounted", so just for testing:
     */
    private static final class TestCatalog extends UserCatalog implements EntityResolver {
        
        public TestCatalog() {}
        
        public EntityResolver getEntityResolver() {
            return this;
        }
        
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if ("-//NetBeans//DTD Filesystem 1.1//EN".equals(publicId)) {
                return new InputSource("nbres:/org/openide/filesystems/filesystem1_1.dtd");
            } else {
                return null;
            }
        }
        
    }
    
}
