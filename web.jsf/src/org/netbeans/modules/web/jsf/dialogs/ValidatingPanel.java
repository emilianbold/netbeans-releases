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
