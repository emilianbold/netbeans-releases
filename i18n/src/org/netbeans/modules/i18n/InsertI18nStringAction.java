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


package org.netbeans.modules.i18n;


import java.util.ResourceBundle;
import java.io.IOException;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.cookies.SourceCookie;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;


/**
* Insert internationalized string at caret position (if it is not in guarded block).
*
* @author   Petr Jiricka
*/
public class InsertI18nStringAction extends CookieAction {

    static final long serialVersionUID =-7002111874047983222L;
    /** Actually performs InsertI18nStringAction
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        /*javax.swing.text.Keymap km= TopManager.getDefault ().getGlobalKeymap ();
        javax.swing.KeyStroke str = org.openide.util.Utilities.stringToKey ("C-I");
        System.out.println("keystroke " + str);
        System.out.println(km.getAction(str));*/

        final SourceCookie.Editor sec = (SourceCookie.Editor)(activatedNodes[0]).getCookie(SourceCookie.Editor.class);
        if (sec == null) return;
        
        sec.open(); 

        DataObject dobj = (DataObject)sec.getSource().getCookie(DataObject.class);
        if (dobj == null) return; 


        final Dialog[] dial = new Dialog[1];
        final int position = sec.getOpenedPanes()[0].getCaret().getDot();
        final StyledDocument doc = sec.getDocument();

        final ResourceBundleStringEditor rbStringEditor = new ResourceBundleStringEditor();
        ResourceBundleString rbString = (ResourceBundleString)rbStringEditor.getValue();
        rbString.setClassElement(I18nSupport.getSourceClassElement(sec.getSource())); // Set ClassElement.
        
        final ResourceBundlePanel rbPanel = (ResourceBundlePanel)rbStringEditor.getCustomEditor();
        rbPanel.setValue(rbString);
        
        DialogDescriptor dd = new DialogDescriptor(
            rbPanel,
            NbBundle.getBundle(I18nModule.class).getString("CTL_InsertI18nString"),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                        try {
                            ResourceBundleString newRbs = (ResourceBundleString)rbPanel.getPropertyValue();
                            rbStringEditor.setValue(newRbs);
                            
                            doc.insertString(position, rbStringEditor.getJavaInitializationString(), null);
                            
                            dial[0].setVisible(false);
                            dial[0].dispose();
                        } catch (IllegalStateException e) {
                            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                NbBundle.getBundle(I18nModule.class).getString("EXC_BadKey"),
                                NotifyDescriptor.ERROR_MESSAGE);
                            TopManager.getDefault().notify(msg);
                        } catch (BadLocationException e) {
                            // Note: this shouldn't happen, the code should be not in guarded block.
                            if(Boolean.getBoolean("netbeans.debug.exception")) // NOI18N
                                System.err.println("I18N module: BadLocationException thrown when trying to insert i18n string."); // NOI18N
                        }
                    } else if (ev.getSource() == DialogDescriptor.CANCEL_OPTION) {
                        dial[0].setVisible(false);
                        dial[0].dispose();
                    }
                }
            }
        );
        dial[0] = TopManager.getDefault().createDialog(dd);
        dial[0].setVisible(true);
    }

    /** Overrides superclass method.
    * @return true if action will be present 
    */
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes))
            return false;
        
        // if has an open editor pane must not be in a guarded block
        final SourceCookie.Editor sec = (SourceCookie.Editor)(activatedNodes[0]).getCookie(SourceCookie.Editor.class);        
        if (sec != null) {
            JEditorPane[] edits = sec.getOpenedPanes();
            if (edits != null && edits.length > 0) {
                int position = edits[0].getCaret().getDot();
                StyledDocument doc = sec.getDocument();
                DataObject obj = (DataObject)sec.getSource().getCookie(DataObject.class);
                if(I18nSupport.getI18nSupport(doc, obj).isGuardedPosition(position))
                    return false;
            }
        }
        return true;
    }

    /**
    * @return MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /**
    * @return ThreadCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   SourceCookie.Editor.class
               };
    }

    /** 
    * @return the action's icon 
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle(I18nModule.class).getString("CTL_InsertI18nString");
    }

    /** 
    * @return the action's help context 
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (InsertI18nStringAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/insertI18nStringAction.gif"; // NOI18N
    }
}