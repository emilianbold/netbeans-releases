/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
    
    // Map<FileObject, Lookup>
    private static transient Map envs = new HashMap(11); // 11 expected number of modules using this
    
    // Environment.Provider ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Returns a lookup that represents environment.
     * @return the lookup
     */
    public final Lookup getEnvironment(DataObject obj){
        
        // the obj check is done by core FileEntityResolver that calls us
        
        // we want to create just one instance per FileObject

        FileObject file = obj.getPrimaryFile();
        Lookup lookup = (Lookup) envs.get(file);
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
            ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);
            emgr.notify(new IllegalStateException());  // instance cookie required
        }
        return lookup;
    }
            
}
