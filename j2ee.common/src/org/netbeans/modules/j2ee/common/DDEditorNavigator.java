/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common;

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.nodes.Node;

/**
 * A cookie to be provided by DD multiview DataObjects to enable navigation
 * to given element in visual DD editor.
 *
 * @author Martin Adamek
 */
public interface DDEditorNavigator extends Node.Cookie {
    
    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View.
     */
    void showElement(Object element);
    
}
