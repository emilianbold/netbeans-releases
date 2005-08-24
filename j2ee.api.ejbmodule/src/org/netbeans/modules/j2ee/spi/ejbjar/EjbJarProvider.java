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

import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.filesystems.FileObject;

/**
 * Provider interface for webmodules.
 * <p>
 * The <code>org.netbeans.modules.j2ee.ejbapi</code> module registers an
 * implementation of this interface to global lookup which looks for the
 * project which owns a file (if any) and checks its lookup for this interface,
 * and if it finds an instance, delegates to it. Therefore it is not normally
 * necessary for a project type provider to register its own instance just to
 * define the webmodule for files it owns, assuming it uses projects for 
 * implementation of webmodule.
 * </p>
 * <p> If needed a new implementation of this interface can be registered in 
 * global lookup.
 * </p>
 * @see EjbJar#getEjbJar
 * @author Pavel Buzek
 */
public interface EjbJarProvider {
    
    /**
     * Find a webmodule containing a given file.
     * @param file a file somewhere
     * @return a webmodule, or null for no answer
     * @see EjbJarFactory
     */
    EjbJar findEjbJar(FileObject file);
    
}
