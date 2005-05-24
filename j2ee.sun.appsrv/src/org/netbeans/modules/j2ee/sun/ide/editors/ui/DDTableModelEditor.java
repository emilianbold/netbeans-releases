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
/*
 * DDTableModelEditor.java
 */ 
package org.netbeans.modules.j2ee.sun.ide.editors.ui;

/**
 * This interface represents a row editor. The editor can at least operate
 * on the data provided by model returning this interface. 
 * 
 * @author  cwebster
 * @version 1.0
 */

import javax.swing.*;

public interface DDTableModelEditor {

    /**
     * Return value from the editor. This method will not return null. The 
     * following pre-condition must always hold setValue(oref); 
     * oref != getValue().
     * @return object representing the newly edited value. 
     */
    public Object getValue();
    
    /**
     * set the displayed value. This method must handle oref == null. If the 
     * oref cannot be displayed by this editor, the display value is 
     * unspecified.
     */
    public void setValue(java.lang.Object oref);
    
    /**
     * provide display instance.
     * @return panel suitable for row editing
     */
    public JPanel getPanel();
    
}

