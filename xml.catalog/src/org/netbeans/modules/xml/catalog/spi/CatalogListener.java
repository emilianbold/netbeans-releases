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
package org.netbeans.modules.xml.catalog.spi;

import java.util.EventListener;

/**
 * A callback interface notifying catalog content changes.
 * <p>
 * It is <b>a callback interface</b> so invokers must be aware of consequences.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public interface CatalogListener extends EventListener {
    
    /** Given public ID has changed - created. */
    public void notifyNew(String publicID);
    
    /** Given public ID has changed - disappeared. */
    public void notifyRemoved(String publicID);
    
    /** Given public ID has changed. */
    public void notifyUpdate(String publicID);
    
    /** All entries are invalidated. */
    public void notifyInvalidate();
    
}
