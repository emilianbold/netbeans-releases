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

package org.netbeans.spi.debugger;

import java.util.List;


/**
 * Abstract ancestor of classes providing lookup.
 *
 * @author Jan Jancura
 */
public interface ContextProvider {
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param folder a folder name or null
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public abstract List lookup (String folder, Class service);
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param folder a folder name or null
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public abstract Object lookupFirst (String folder, Class service);
}

