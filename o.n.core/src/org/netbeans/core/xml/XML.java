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

package org.netbeans.core.xml;

import java.util.*;

/** Global utils for XML related stuff.
 *
 * @author  Jaroslav Tulach
 */
public final class XML extends Object {
    /** Initializes the XML in the IDE. Currently registers all XMLDataObject.Processors.
     */
    public static void init () {
        org.netbeans.core.xml.EntityCatalogImpl.init ();
        org.netbeans.core.windows.toolbars.ToolbarConfiguration.init ();
    }

}
