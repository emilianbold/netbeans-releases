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

package org.netbeans.modules.editor;

import java.awt.event.*;
import java.awt.Dialog;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.netbeans.editor.DialogSupport;
import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import java.util.HashMap;

/** The NetBeans way of handling Dialogs is through TopManager,
 * prividing it with DialogDescriptor.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class NbDialogSupport implements DialogSupport.DialogFactory {

    /**
     * Hash map containing string (ClassNames) <-> string (HelpID).
     */
    private static HashMap helpIDs;
    
    private static final String HELP_ID_MacroSavePanel = "editing.macros.recording"; // !!! NOI18N
    private static final String HELP_ID_KeySequenceInputPanel = "editing.csh.shortcutseq"; // !!! NOI18N
    private static final String HELP_ID_FindPanel = "editing.find"; // !!! NOI18N
    private static final String HELP_ID_GotoDialogPanel = "editing.goto"; // !!! NOI18N
    private static final String HELP_ID_JavaFastImportPanel = "editing.fastimport"; // !!! NOI18N
    private static final String HELP_ID_JavaFastOpenPanel = "editing.fastopen"; // !!! NOI18N
    private static final String HELP_ID_ScrollCompletionPane = "editing.codecompletion"; // !!! NOI18N
    
    public NbDialogSupport()
    {
        if (helpIDs == null)
        {
            helpIDs = new HashMap(7);
            helpIDs.put("org.netbeans.editor.MacroSavePanel", HELP_ID_MacroSavePanel);
            helpIDs.put("org.netbeans.editor.KeySequenceInputPanel", HELP_ID_KeySequenceInputPanel);
            helpIDs.put("org.netbeans.editor.ext.FindDialogSupport.FindPanel", HELP_ID_FindPanel);
            helpIDs.put("org.netbeans.editor.ext.GotoDialogPanel", HELP_ID_GotoDialogPanel);
            helpIDs.put("org.netbeans.editor.ext.ScrollCompletionPane", HELP_ID_ScrollCompletionPane);
            helpIDs.put("org.netbeans.editor.ext.java.JavaFastImportPanel", HELP_ID_JavaFastImportPanel);
            helpIDs.put("org.netbeans.editor.ext.java.JavaFastOpenPanel", HELP_ID_JavaFastOpenPanel);
        }
    }
    
    /**
     * The method for creating a dialog with specified properties.
     * @param title The title of created dialog.
     * @param panel The content of the dialog to be displayed.
     * @param modal Whether the dialog should be modal.
     * @param buttons The array of JButtons to be added to the dialog.
     * @param sidebuttons The buttons could be placed under the panel (false),
     *     or on the right side of the panel (true).
     * @param defaultIndex The index of default button in the buttons array,
     *   if <CODE>index < 0</CODE>, no default button is set.
     * @param cancelIndex The index of cancel button - the button that will
     *   be <I>pressed</I> when closing the dialog.\
     * @param listener The listener which will be notified of all button
     *   events.
     */
    public Dialog createDialog(String title, JPanel panel,boolean modal,JButton[] buttons,boolean sideButtons,int defaultIndex,int cancelIndex,ActionListener listener) {
        Dialog d = TopManager.getDefault().createDialog(
                new DialogDescriptor( panel, title, modal, buttons,
                defaultIndex == -1 ? buttons[0] : buttons[defaultIndex],
                    sideButtons ? DialogDescriptor.RIGHT_ALIGN : DialogDescriptor.BOTTOM_ALIGN,
                    new HelpCtx( (String)helpIDs.get(panel.getClass().getName()) ), listener
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
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );

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
