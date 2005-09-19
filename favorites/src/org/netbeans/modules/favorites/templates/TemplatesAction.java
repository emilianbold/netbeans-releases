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

package org.netbeans.modules.favorites.templates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
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
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/** 
 *
 * @author Jiri Rechtacek
 */
public class TemplatesAction extends CallableSystemAction {

    public TemplatesAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }    
    
    public void performAction () {
        final TemplatesPanel tp = new TemplatesPanel ();
        JButton closeButton = new JButton ();
        Mnemonics.setLocalizedText (closeButton, NbBundle.getMessage (TemplatesAction.class, "BTN_TemplatesPanel_CloseButton")); // NOI18N
        JButton openInEditor = new JButton ();
        openInEditor.setEnabled (false);
        OpenInEditorListener l = new OpenInEditorListener (tp, openInEditor);
        openInEditor.addActionListener (l);
        tp.getExplorerManager ().addPropertyChangeListener (l);
        Mnemonics.setLocalizedText (openInEditor, NbBundle.getMessage (TemplatesAction.class, "BTN_TemplatesPanel_OpenInEditorButton")); // NOI18N
        DialogDescriptor dd = new DialogDescriptor (tp,
                                NbBundle.getMessage (TemplatesAction.class, "LBL_TemplatesPanel_Title"),  // NOI18N
                                false, // modal
                                new Object [] { openInEditor, closeButton },
                                closeButton,
                                DialogDescriptor.DEFAULT_ALIGN,
                                null,
                                null);
        dd.setClosingOptions (null);
        // set helpctx to null again, DialogDescriptor replaces null with HelpCtx.DEFAULT_HELP
        dd.setHelpCtx (null);
        DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
    }
    
    protected boolean asynchronous() {
        return true;
    }

    public String getName () {
        return NbBundle.getMessage (TemplatesAction.class, "LBL_TemplatesAction_Name"); // NOI18N
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
        putProperty (TemplatesAction.SHORT_DESCRIPTION, NbBundle.getMessage (TemplatesAction.class, "HINT_TemplatesAction")); // NOI18N
    }
    
    // helper classes
    private static class OpenInEditorListener implements ActionListener, PropertyChangeListener {
        TemplatesPanel tp;
        JButton b;
        public OpenInEditorListener (TemplatesPanel panel, JButton button) {
            tp = panel;
            b = button;
        }
        
        // ActionListener
        public void actionPerformed (ActionEvent ev) {
            Node [] nodes = (Node []) tp.getExplorerManager ().getSelectedNodes ();
            assert nodes != null && nodes.length > 0 : "Selected templates cannot be null or empty.";
            Set nodes2open = getNodes2Open (nodes);
            assert ! nodes2open.isEmpty () : "Selected templates to open cannot by empty for nodes " + Arrays.asList (nodes);
            Iterator/*<Node>*/ it = nodes2open.iterator ();
            while (it.hasNext ()) {
                Node n = (Node) it.next ();
                EditCookie ec = (EditCookie) n.getLookup ().lookup (EditCookie.class);
                if (ec != null) {
                    ec.edit ();
                } else {
                    OpenCookie oc = (OpenCookie) n.getLookup ().lookup (OpenCookie.class);
                    if (oc != null) {
                        oc.open ();
                    } else {
                        assert false : "Node " + n + " has to have a EditCookie or OpenCookie.";
                    }
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
                    EditCookie ec = (EditCookie) n.getLookup ().lookup (EditCookie.class);
                    OpenCookie oc = (OpenCookie) n.getLookup ().lookup (OpenCookie.class);
                    res = ec != null || oc != null;
                    i++;
                }
                b.setEnabled (res);
            }
        }
    }
    
    static private Set getNodes2Open (Node [] nodes) {
        Set/*<Node>*/ nodes2open = new HashSet (nodes.length);
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
