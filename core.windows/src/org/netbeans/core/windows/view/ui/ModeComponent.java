/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view.ui;


import org.netbeans.core.windows.view.ModeView;

/**
 * Just a marker (originally,
 * the method was added due to TabbedHandler.ActivateTopComponentOnMouseClick)
 * interface for mode GUI component, to be recognized 
 * when traversing AWT hierarchy.
 *
 * @author  Peter Zavadsky
 */
public interface ModeComponent {
   
    /** Gets represented <code>ModeView</code>. */
    public ModeView getModeView();
    
    // XXX
    public int getKind();
}
