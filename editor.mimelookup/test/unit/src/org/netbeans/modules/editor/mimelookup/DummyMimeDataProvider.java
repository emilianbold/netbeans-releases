/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.mimelookup;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author vita
 */
public class DummyMimeDataProvider implements MimeDataProvider {
    
    /** Creates a new instance of DummyMimeDataProvider */
    public DummyMimeDataProvider() {
//        System.out.println("Creating DummyMimeDataProvider");
    }

    public Lookup getLookup(MimePath mimePath) {
//        System.out.println("MimeDataProvider creating Marker for " + mimePath.getPath());
        return Lookups.fixed(new Object [] { new Marker(), mimePath });
    }
    
    public static final class Marker {
        
    } // End of Marker class
}
