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
package org.netbeans.api.visual.vmd;

/**
 * This inteface represents an ability to collapse and expand a widget.
 *
 * @author David Kaspar
 */
public interface VMDMinimizeAbility {

    /**
     * Collapses the widget.
     */
    public void collapseWidget ();

    /**
     * Expands the widget.
     */
    public void expandWidget ();

}
