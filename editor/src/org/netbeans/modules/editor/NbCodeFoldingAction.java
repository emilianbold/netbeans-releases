/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeymap;
import org.netbeans.editor.Utilities;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Martin Roskanin
 */
public abstract class NbCodeFoldingAction extends SystemAction {

    /** Creates a new instance of NbCodeFoldingAction */
    public NbCodeFoldingAction() {
    }
    
    public final HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /** Delegates action to specific folding action */
    protected abstract void delegateAction(ActionEvent evt, JTextComponent target);

    private static boolean isOpen(Document doc){
        if (doc==null) return false;
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj==null) return false;
        EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        if (ec==null) return false;
        JEditorPane jep[] = ec.getOpenedPanes();
        return (jep!=null && jep.length>0);
    }
    
    public boolean isEnabled() {
        JTextComponent component = Utilities.getLastActiveComponent();
        if (component!=null){
            Document doc = component.getDocument();
            return isOpen(doc);
        }
        return false;
    }

    private static JTextComponent getComponent(){
        return Utilities.getLastActiveComponent();
    }
    
    public void actionPerformed (java.awt.event.ActionEvent ev){
        JTextComponent component = Utilities.getLastActiveComponent();
        if (component !=null && isOpen(component.getDocument())) {
            delegateAction(ev, component);
        }
    }

    private static Action getDelegateAction(String actionName){
        BaseKit bk = BaseKit.getKit(BaseKit.class);
        if (bk!=null){
            return bk.getActionByName(actionName);
        }
        return null;
    }
    
    private static KeyStroke getKeystroke(Action a){
        BaseKit bk = BaseKit.getKit(BaseKit.class);
        if (bk!=null){
            MultiKeymap mkm = bk.getKeymap();
            KeyStroke[] ks = mkm.getKeyStrokesForAction(a);
            if (ks != null && ks.length > 0) {
                return ks[0];
            }            
        }
        return null;
    }

    
    public static class CollapseAll extends NbCodeFoldingAction{
        public CollapseAll(){
        }

        public boolean isEnabled(){
            putValue(Action.ACCELERATOR_KEY, getKeystroke(getDelegateAction(BaseKit.collapseAllFoldsAction)));
            return super.isEnabled();
        }
        
        public String getName() {
            return NbBundle.getBundle(NbCodeFoldingAction.class).getString(
                "collapse-all"); //NOI18N
        }        

        protected void delegateAction(ActionEvent evt, JTextComponent target){
            Action action = getDelegateAction(BaseKit.collapseAllFoldsAction);
            if (action instanceof BaseAction){
                ((BaseAction)action).actionPerformed(evt, target);
            }
        }
    }
    
    public static class CollapseFold extends NbCodeFoldingAction{
        public CollapseFold(){
        }

        public boolean isEnabled(){
            putValue(Action.ACCELERATOR_KEY, getKeystroke(getDelegateAction(BaseKit.collapseFoldAction)));
            return super.isEnabled();
        }
        
        public String getName() {
            return NbBundle.getBundle(NbCodeFoldingAction.class).getString(
                "collapse-fold"); //NOI18N
        }        

        protected void delegateAction(ActionEvent evt, JTextComponent target){
            Action action = getDelegateAction(BaseKit.collapseFoldAction);
            if (action instanceof BaseAction){
                ((BaseAction)action).actionPerformed(evt, target);
            }
        }
    }

    public static class ExpandAll extends NbCodeFoldingAction{
        public ExpandAll(){
        }

        public boolean isEnabled(){
            putValue(Action.ACCELERATOR_KEY, getKeystroke(getDelegateAction(BaseKit.expandAllFoldsAction)));
            return super.isEnabled();
        }
        
        public String getName() {
            return NbBundle.getBundle(NbCodeFoldingAction.class).getString(
                "expand-all"); //NOI18N
        }        

        protected void delegateAction(ActionEvent evt, JTextComponent target){
            Action action = getDelegateAction(BaseKit.expandAllFoldsAction);
            if (action instanceof BaseAction){
                ((BaseAction)action).actionPerformed(evt, target);
            }
        }
    }
    
    public static class ExpandFold extends NbCodeFoldingAction{
        public ExpandFold(){
        }

        public boolean isEnabled(){
            putValue(Action.ACCELERATOR_KEY, getKeystroke(getDelegateAction(BaseKit.expandFoldAction)));
            return super.isEnabled();
        }
        
        public String getName() {
            return NbBundle.getBundle(NbCodeFoldingAction.class).getString(
                "expand-fold"); //NOI18N
        }        

        protected void delegateAction(ActionEvent evt, JTextComponent target){
            Action action = getDelegateAction(BaseKit.expandFoldAction);
            if (action instanceof BaseAction){
                ((BaseAction)action).actionPerformed(evt, target);
            }
        }
    }
    
}
