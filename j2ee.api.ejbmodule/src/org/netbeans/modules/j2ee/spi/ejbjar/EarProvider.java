/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.spi.ejbjar;

import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.openide.filesystems.FileObject;

/**
 * Provider interface for Ear modules.
 * <p>
 * The <code>org.netbeans.modules.j2ee.api/ejbjar</code> module registers an
 * implementation of this interface to global lookup which looks for the
 * project which owns a file (if any) and checks its lookup for this interface,
 * and if it finds an instance, delegates to it. Therefore it is not normally
 * necessary for a project type provider to register its own instance just to
 * define the webmodule for files it owns, assuming it uses projects for 
 * implementation of ear module.
 * </p>
 * <p> If needed a new implementation of this interface can be registered in 
 * global lookup.
 * </p>
 * @see Ear#getEar
 * @author Pavel Buzek
 */
public interface EarProvider {
    
    /**
     * Find an Ear containing a given file.
     * @param file a file somewhere
     * @return an ear , or null for no answer
     * @see EjbJarFactory
     */
    Ear findEar(FileObject file);
    
}
