/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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

package org.netbeans.modules.xml.xam;

import java.util.EventListener;

/**
 * A component listener provides a course grained event stream based on 
 * values or children of the source. This is not intended to replace
 * property change events and only serves as a way to differentiate between
 * children and non children related events. 
 * @author Rico Cruz
 * @author Nam Nguyen
 * @author Chris Webster
 */
public interface ComponentListener extends EventListener {
    /**
     * invoked if a value other than children is changed.
     */
    void valueChanged(ComponentEvent evt);
    /**
     * invoked if a child has been added.
     */
    void childrenAdded(ComponentEvent evt);
    /**
     * invoked if a child has been removed. 
     */
    void childrenDeleted(ComponentEvent evt);
}
