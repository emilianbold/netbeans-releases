/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import org.netbeans.tax.event.TreeEventManager;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface TreeDocumentRoot {
    
    /**
     */
    public TreeEventManager getRootEventManager ();
    
    
    /**
     */
    public String getVersion ();
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public void setVersion (String version) throws ReadOnlyException, InvalidArgumentException;
    
    /**
     */
    public String getEncoding ();
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public void setEncoding (String encoding) throws ReadOnlyException, InvalidArgumentException;
    
    
    /**
     */
    public TreeObjectList getChildNodes ();
    
}
