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

/*
 * JavadocSearchType.java
 *
 * Created on 19. únor 2001, 16:27
 */

package org.netbeans.modules.javadoc.search;

import java.util.*;
import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Petr Suchomel
 * @version 1.0
 */
public abstract class JavadocSearchType extends ServiceType {

    /** generated Serialized Version UID */
    static final long serialVersionUID =-7643543247564581246L;

    /** default returns null, must be overriden
     * @param fs File system where to find index files
     * @param rootOffset offset , position of index files in file system, normally null
     * @return File object containing index-files
    */
    public abstract FileObject getDocFileObject( FileSystem fs , String rootOffset );
    
    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     * @see IndexSearchThread
     */    
    public abstract IndexSearchThread getSearchThread( String toFind, FileObject fo, IndexSearchThread.DocIndexItemConsumer diiConsumer );
}
