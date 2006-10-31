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

package org.netbeans.modules.apisupport.project.layers;

import java.io.IOException;
import junit.framework.Assert;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
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
    
    public static final class Lkp extends ProxyLookup {
        // Copied from org.netbeans.api.project.TestUtil:
        static {
            // XXX replace with MockServices
            System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
            Assert.assertEquals(Lkp.class, Lookup.getDefault().getClass());
        }
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
                return new InputSource(LayerTestBase.class.getClassLoader().getResource("org/openide/filesystems/filesystem1_1.dtd").toExternalForm());
            } else if ("-//NetBeans//DTD Filesystem 1.0//EN".equals(publicId)) {
                return new InputSource(LayerTestBase.class.getClassLoader().getResource("org/openide/filesystems/filesystem.dtd").toExternalForm());
            } else {
                return null;
            }
        }
        
    }
    
}
