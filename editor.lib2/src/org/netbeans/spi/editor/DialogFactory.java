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

package org.netbeans.spi.editor;

import java.awt.Dialog;
import java.awt.event.*;
import javax.swing.*;

/**
 * DialogFactory implementation is a class responsible for providing
 * proper implementation of Dialog containing required widgets.
 * It can provide the dialog itself or delegate the functionality
 * to another piece of code, e.g some windowing system. 
 *
 * @author  pnejedly
 * @version 1.0
 */
public interface DialogFactory {
    
    /** 
     * The method for creating a dialog with specified properties.
     * 
     * @param title The title of created dialog.
     * @param panel The content of the dialog to be displayed.
     * @param modal Whether the dialog should be modal.
     * @param buttons The array of JButtons to be added to the dialog.
     * @param sidebuttons The buttons could be placed under the panel (false),
     *      or on the right side of the panel (true).
     * @param defaultIndex The index of default button in the buttons array,
     *    if <CODE>index < 0</CODE>, no default button is set.
     * @param cancelIndex The index of cancel button - the button that will
     *    be <I>pressed</I> when closing the dialog.
     * @param listener The listener which will be notified of all button
     *    events.
     * 
     * @return newly created <CODE>Dialog</CODE>
     */
    public Dialog createDialog( String title, JPanel panel, boolean modal,
            JButton[] buttons, boolean sidebuttons, int defaultIndex,
            int cancelIndex, ActionListener listener );
}
