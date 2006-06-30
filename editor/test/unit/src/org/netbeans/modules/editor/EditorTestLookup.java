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
 * The Original Software is the LaTeX module.
 * The Initial Developer of the Original Software is Jan Lahoda.
 * Portions created by Jan Lahoda_ are Copyright (C) 2002-2004.
 * All Rights Reserved.
 *
 * Contributor(s): Jan Lahoda.
 */
package org.netbeans.modules.editor;


import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import junit.framework.Assert;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 * Inspired by org.netbeans.api.project.TestUtil.
 *
 * @author Miloslav Metelka, Jan Lahoda
 */
public class EditorTestLookup extends ProxyLookup {
    
    public static EditorTestLookup DEFAULT_LOOKUP = null;
    
    static {
        EditorTestLookup.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", EditorTestLookup.class.getName());
        Assert.assertEquals(EditorTestLookup.class, Lookup.getDefault().getClass());
    }
    
    public EditorTestLookup() {
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    /**
     * Set the global default lookup with the specified content.
     *
     * @param layers xml-layer URLs to be present in the system filesystem.
     * @param instances object instances to be present in the default lookup.
     */
    public static void setLookup(URL[] layers, Object[] instances, ClassLoader cl)
    throws IOException, PropertyVetoException {
        
        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(layers);
        Repository repository = new Repository(system);

        Object[] lookupContent = new Object[instances.length + 1];
        lookupContent[0] = repository;
        System.arraycopy(instances, 0, lookupContent, 1, instances.length);
        
        DEFAULT_LOOKUP.setLookup(lookupContent, cl);
    }
    
}
