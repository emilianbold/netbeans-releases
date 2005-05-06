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

package org.openide.awt;

import java.awt.Component;


/**
 * Provides a component which will be added at east side of the status line.
 * The providers can be declared in module's MET-INF/services.
 * 
 * @since
 * @author Jiri Rechtacek
 */
public interface StatusLineElementProvider {
    
    /** Returns a component displayable in the status line. */
    public Component getStatusLineElement ();
    
}
