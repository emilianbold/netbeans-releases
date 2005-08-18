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

package org.netbeans.modules.web.jsf.dialogs;

import javax.swing.AbstractButton;
import javax.swing.text.JTextComponent;

/**
 * ValidatingPanel.java
 *
 * @author mkuchtiak
 */
public interface ValidatingPanel {
    
    /** Returns error message or null according to component values
     * @return error message or null (if values are correct)
     */
    public String validatePanel();
    
    /** Returns the array of components (radio buttons, check boxes) whose state-change can influence
     * the data correctness
     */
    public AbstractButton[] getStateChangeComponents();
    
    /** Returns the array of text components (text fields, text areas) whose document-change can influence
     * the data correctness
     */
    public JTextComponent[] getDocumentChangeComponents();
}
