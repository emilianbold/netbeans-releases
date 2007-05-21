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

package org.netbeans.modules.xml.catalog;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JButton;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.ViewCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/** 
 *
 * @author Pavel Buzek
 */
public class CatalogAction extends CallableSystemAction {

    /** Weak reference to the dialog showing singleton Template Manager. */
    private Reference<Dialog> dialogWRef = new WeakReference<Dialog> (null);
    
    public CatalogAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }    
    
    public void performAction () {
        
        Dialog dialog = dialogWRef.get ();

        if (dialog == null || ! dialog.isShowing ()) {

            final CatalogPanel cp = new CatalogPanel ();
            JButton closeButton = new JButton ();
            Mnemonics.setLocalizedText (closeButton,NbBundle.getMessage (CatalogAction.class, "BTN_CatalogPanel_CloseButton")); // NOI18N
            JButton openInEditor = new JButton ();
            openInEditor.setEnabled (false);
            OpenInEditorListener l = new OpenInEditorListener (cp, openInEditor);
            openInEditor.addActionListener (l);
            cp.getExplorerManager ().addPropertyChangeListener (l);
            Mnemonics.setLocalizedText (openInEditor,NbBundle.getMessage (CatalogAction.class, "BTN_CatalogPanel_OpenInEditorButton")); // NOI18N
            DialogDescriptor dd = new DialogDescriptor (cp,NbBundle.getMessage (CatalogAction.class, "LBL_CatalogPanel_Title"),  // NOI18N
                                    false, // modal
                                    new Object [] { openInEditor, closeButton },
                                    closeButton,
                                    DialogDescriptor.DEFAULT_ALIGN,
                                    null,
                                    null);
            dd.setClosingOptions (null);
            // set helpctx to null again, DialogDescriptor replaces null with HelpCtx.DEFAULT_HELP
            dd.setHelpCtx (null);
            
            dialog = DialogDisplayer.getDefault ().createDialog (dd);
            dialog.setVisible (true);
            dialogWRef = new WeakReference<Dialog> (dialog);
            
        } else {
            dialog.toFront ();
        }
        
    }
    
    protected boolean asynchronous() {
        return true;
    }

    public String getName () {
        return NbBundle.getMessage (CatalogAction.class, "LBL_CatalogAction_Name"); // NOI18N
    }

    protected String iconResource () {
        return null;
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Adding hint.
     */
    protected void initialize () {
	super.initialize ();
        putProperty (CatalogAction.SHORT_DESCRIPTION,org.openide.util.NbBundle.getMessage(CatalogAction.class, "HINT_CatalogAction")); // NOI18N
    }
    
    // helper classes
    private static class OpenInEditorListener implements ActionListener, PropertyChangeListener {
        CatalogPanel cp;
        JButton b;
        public OpenInEditorListener (CatalogPanel panel, JButton button) {
            cp = panel;
            b = button;
        }
        
        // ActionListener
        public void actionPerformed (ActionEvent ev) {
            Node [] nodes = (Node []) cp.getExplorerManager ().getSelectedNodes ();
            assert nodes != null && nodes.length > 0 : "Selected templates cannot be null or empty.";
            Set nodes2open = getNodes2Open (nodes);
            assert ! nodes2open.isEmpty () : "Selected templates to open cannot by empty for nodes " + Arrays.asList (nodes);
            Iterator/*<Node>*/ it = nodes2open.iterator ();
            while (it.hasNext ()) {
                Node n = (Node) it.next ();
                ViewCookie vc = (ViewCookie) n.getLookup ().lookup (ViewCookie.class);
                if (vc != null) {
                    vc.view();
                } else {
                    assert false : "Node " + n + " has to have a VewCookie.";
                }
            }
        }

        // PropertyChangeListener
        public void propertyChange (java.beans.PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
                Node [] nodes = (Node []) evt.getNewValue ();
                boolean res = nodes != null;
                int i = 0;
                while (res && i < nodes.length) {
                    Node n = nodes [i];
//                    EditCookie ec = (EditCookie) n.getLookup ().lookup (EditCookie.class);
//                    OpenCookie oc = (OpenCookie) n.getLookup ().lookup (OpenCookie.class);
                    ViewCookie vc = (ViewCookie) n.getLookup ().lookup (ViewCookie.class);
                    res = vc != null; //ec != null || oc != null;
                    
                    i++;
                }
                b.setEnabled (res);
            }
        }
    }
    
    static private Set<Node> getNodes2Open (Node [] nodes) {
        Set<Node> nodes2open = new HashSet<Node> (nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            if (nodes [i].isLeaf ()) {
                nodes2open.add (nodes [i]);
            } else {
                nodes2open.addAll (getNodes2Open (nodes [i].getChildren ().getNodes (true)));
            }
        }
        return nodes2open;
    }

}
