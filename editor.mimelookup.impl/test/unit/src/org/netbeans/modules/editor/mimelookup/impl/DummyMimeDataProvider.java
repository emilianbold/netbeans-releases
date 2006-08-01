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

package org.netbeans.modules.editor.mimelookup.impl;

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
