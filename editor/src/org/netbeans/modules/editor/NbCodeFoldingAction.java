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

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 *  Code Folding action displayed under Menu/View/
 *
 *  @author  Martin Roskanin
 */
public  class NbCodeFoldingAction extends SystemAction implements Presenter.Menu{

    
    /** Creates a new instance of NbCodeFoldingAction */
    public NbCodeFoldingAction() {
    }
    
    public final HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getBundle(NbCodeFoldingAction.class).getString(
            "generate-fold-popup"); //NOI18N
    }        

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

    /** Get a menu item that can present this action in a {@link javax.swing.JMenu}.
    * @return the representation for this action
    */
    public JMenuItem getMenuPresenter(){
        return new CodeFoldsMenu(getName());
    }
    
    private static JTextComponent getComponent(){
        return Utilities.getLastActiveComponent();
    }
    
    public void actionPerformed (java.awt.event.ActionEvent ev){
    }
    
    private BaseKit getKit(){
        JTextComponent component = getComponent();
        return (component == null) ? BaseKit.getKit(NbEditorKit.class) : Utilities.getKit(component);
    }

    
    public class CodeFoldsMenu extends JMenu{
        public CodeFoldsMenu(){
            super();
        }
        
        public CodeFoldsMenu(String s){
            super(s);
            //#40585 fix start - setting the empty, transparent icon for the menu item to align it correctly with other items
            setIcon(new ImageIcon(org.openide.util.Utilities.loadImage("org/openide/resources/actions/empty.gif"))); //NOI18N
            //#40585 fix end
            
        }
        
        public JPopupMenu getPopupMenu(){
            JPopupMenu pm = super.getPopupMenu();
            pm.removeAll();
            Action action = getKit().getActionByName(NbEditorKit.generateFoldPopupAction);
            if (action instanceof BaseAction){
                JTextComponent component = Utilities.getLastActiveComponent();
                JMenu menu = (JMenu)((BaseAction)action).getPopupMenuItem(component);
                if (menu!=null){
                    Component comps[] = menu.getMenuComponents();
                    for (int i=0; i<comps.length; i++){
                        pm.add(comps[i]);
                    }
                }
            }
            pm.pack();
            return pm;
        }
    }
    
}
