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

package org.netbeans.spi.debugger.ui;

import javax.swing.JComponent;


/**
 * Support for "Attach ..." dialog. Represents one type of attaching.
 *
 * @author   Jan Jancura
 */
public abstract class AttachType {

    /**
     * Provides display name of this Attach Type. Is used as one choice in
     * ComboBox.
     *
     * @return display name of this Attach Type
     */
    public abstract String getTypeDisplayName ();

    /**
     * Returns visual customizer for this Attach Type. Customizer can 
     * optionally implement {@link Controller} intarface.
     *
     * @return visual customizer for this Attach Type
     */
    public abstract JComponent getCustomizer ();
}