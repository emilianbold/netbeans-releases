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

package org.netbeans.modules.editor.impl;

import java.awt.event.*;
import java.awt.Dialog;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import java.util.HashMap;
import org.netbeans.spi.editor.DialogFactory;

/** 
 * 
 * @author Vita Stejskal
 */
public class NbDialogFactory implements DialogFactory {

    /**
     * Hash map containing string (ClassNames) <-> string (HelpID).
     */
    private static HashMap helpIDs;
    
    private static final String HELP_ID_MacroSavePanel = "editing.macros.recording"; // !!! NOI18N
    private static final String HELP_ID_FindPanel = "editing.find"; // !!! NOI18N
    private static final String HELP_ID_JavaFastImportPanel = "editing.fastimport"; // !!! NOI18N
    private static final String HELP_ID_ScrollCompletionPane = "editing.codecompletion"; // !!! NOI18N
    
    public NbDialogFactory()
    {
        if (helpIDs == null)
        {
            helpIDs = new HashMap(7);
            helpIDs.put("org.netbeans.editor.MacroSavePanel", HELP_ID_MacroSavePanel); // NOI18N
            helpIDs.put("org.netbeans.editor.ext.FindDialogSupport$FindPanel", HELP_ID_FindPanel); // NOI18N
            helpIDs.put("org.netbeans.editor.ext.ScrollCompletionPane", HELP_ID_ScrollCompletionPane); // NOI18N
            helpIDs.put("org.netbeans.editor.ext.java.JavaFastImportPanel", HELP_ID_JavaFastImportPanel); // NOI18N
        }
    }
    
    /**
     * The method for creating a dialog with specified properties.
     * @param title The title of created dialog.
     * @param panel The content of the dialog to be displayed.
     * @param modal Whether the dialog should be modal.
     * @param buttons The array of JButtons to be added to the dialog.
     * @param sideButtons The buttons could be placed under the panel (false),
     *     or on the right side of the panel (true).
     * @param defaultIndex The index of default button in the buttons array,
     *   if <CODE>index < 0</CODE>, no default button is set.
     * @param cancelIndex The index of cancel button - the button that will
     *   be <I>pressed</I> when closing the dialog.\
     * @param listener The listener which will be notified of all button
     *   events.
     */
    public Dialog createDialog(String title, JPanel panel,boolean modal,JButton[] buttons,boolean sideButtons,int defaultIndex,int cancelIndex,ActionListener listener) {
        String helpID = (String)helpIDs.get(panel.getClass().getName());
        Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(
                new DialogDescriptor( panel, title, modal, buttons,
                defaultIndex == -1 ? buttons[0] : buttons[defaultIndex],
                    sideButtons ? DialogDescriptor.RIGHT_ALIGN : DialogDescriptor.BOTTOM_ALIGN,
                    helpID != null ? new HelpCtx( helpID ) : null, listener
                )
        );

        // register the cancel button helpers
        if( cancelIndex >= 0 && d instanceof JDialog ) {
            final JButton cancelButton = buttons[cancelIndex];
            // register the Esc key to simulate Cancel click
            ((JDialog)d).getRootPane().registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) { //                    l.actionPerformed( new ActionEvent(buttons[cancelButtonIndex], 0, null));
                        cancelButton.doClick( 10 );
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                //JComponent.WHEN_IN_FOCUSED_WINDOW
                JComponent.WHEN_FOCUSED
            );

            //bugfix of #45552, 45555, 45556, 45558
            ((JDialog)d).getRootPane().setFocusable(false);    
                
            d.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing( WindowEvent evt ) {
                        cancelButton.doClick( 10 );
                    }
                }
            );
        }
                    
        return d;
    }
    
}
