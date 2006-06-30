/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

