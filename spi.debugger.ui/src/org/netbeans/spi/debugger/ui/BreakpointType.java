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

package org.netbeans.spi.debugger.ui;

import javax.swing.JComponent;

/**
 * Support for "New Breakpoint" dialog and Breakpoint Customizer. Represents 
 * one breakpoint type.
 *
 * @author   Jan Jancura
 */
public abstract class BreakpointType {
    
    /**
     * Display name of cathegory of this breakpoint type. Cathegory typically
     * represents one debugger language.
     *
     * @return display name of cathegory of this breakpoint type
     */
    public abstract String getCategoryDisplayName ();

    /**
     * Return display name of this breakpoint type (like "Line Breakppoint").
     *
     * @return display name of this breakpoint type
     */
    public abstract String getTypeDisplayName ();

    /**
     * Returns visual customizer for this breakpoint type. Customizer can 
     * optionally implement {@link Controller} intarface.
     *
     * @return visual customizer for this breakpoint type
     */
    public abstract JComponent getCustomizer ();

    /**
     * Should return true of this breakpoint type should be default one in 
     * the current context. Default breakpoint type is selected one when the
     * New Breakpoint dialog is opened.
     *
     * @return true of this breakpoint type should be default
     */
    public abstract boolean isDefault ();
}