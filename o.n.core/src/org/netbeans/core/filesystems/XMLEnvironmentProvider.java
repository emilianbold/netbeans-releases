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

package org.netbeans.core.filesystems;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.xml.*;
import org.openide.*;

/**
 * Default implementation providing Environment for XMLDataObject according to its
 * public ID.
 *
 * <p>
 * Subclass instance of this must be registered as <tt>FileEntityResolver</tt>
 * dictates and must implement <tt>Environment.Provider</tt> interface.
 *
 * <p>
 * It forever caches provided environment!
 *
 * @see     org.netbeans.core.xml.FileEntityResolver
 * @author  Petr Kuzel
 * @version 
 */
class XMLEnvironmentProvider extends SharedClassObject {

    private static final long serialVersionUID = 18947L;
    
    private static transient Map<FileObject, Lookup> envs = new HashMap<FileObject, Lookup>(11); // 11 expected number of modules using this
    
    // Environment.Provider ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Returns a lookup that represents environment.
     * @return the lookup
     */
    public final Lookup getEnvironment(DataObject obj){
        
        // the obj check is done by core FileEntityResolver that calls us
        
        // we want to create just one instance per FileObject

        FileObject file = obj.getPrimaryFile();
        Lookup lookup = envs.get(file);
        if (lookup == null) {
            lookup = createLookup(obj);
            envs.put(file, lookup);
        }
        return lookup;
        
    }
    
    /**
     * It is called exactly once per DataObject.
     *
     * @return content of assigned Lookup
     */
    protected InstanceContent createInstanceContent(DataObject obj) {
        return new InstanceContent();
    }
    
    /**
     * It is called exactly once per DataObject.
     *
     * @return Lookup containing <tt>createInstanceContent()</tt>
     */
    protected Lookup createLookup(DataObject obj) {
        InstanceContent ic = createInstanceContent(obj);
        Lookup lookup = new AbstractLookup(ic);
        if (lookup.lookup(InstanceCookie.class) == null) {
            Exceptions.printStackTrace(new IllegalStateException());  // instance cookie required
        }
        return lookup;
    }
            
}
