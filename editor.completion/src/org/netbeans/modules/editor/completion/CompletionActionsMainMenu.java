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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.completion;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.MainMenuAction;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public abstract class CompletionActionsMainMenu extends MainMenuAction implements Action {

    private JMenuItem menuItem;
    private AbstractAction delegate;
        
    public CompletionActionsMainMenu() {
        super();
        menuItem = new JMenuItem(getMenuItemText());
        delegate = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // Does nothing;
            }
        };
        putValue(NAME, getActionName());
        setMenu();
    }
    
    public JMenuItem getMenuPresenter() {
        return menuItem;
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        delegate.removePropertyChangeListener(listener);
    }

    public void putValue(String key, Object newValue) {
        delegate.putValue(key, newValue);
    }

    public Object getValue(String key) {
        return delegate.getValue(key);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        delegate.addPropertyChangeListener(listener);
    }

    public void setEnabled(boolean newValue) {
        delegate.setEnabled(newValue);
    }

    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    /** Sets the state of JMenuItem*/
    protected void setMenu(){
        
        ActionMap am = getContextActionMap();
        Action action = null;
        if (am != null) {
            action = am.get(getActionName());
        }
        
        JMenuItem presenter = getMenuPresenter();
        Action presenterAction = presenter.getAction();
        if (presenterAction == null){
            presenter.setAction(this);
            presenter.setToolTipText(null); /* bugfix #62872 */ 
            menuInitialized = false;
        } 
        else {
            if (!this.equals(presenterAction)){
                presenter.setAction(this);
                presenter.setToolTipText(null); /* bugfix #62872 */
                menuInitialized = false;
            }
        }

        if (!menuInitialized){
            Mnemonics.setLocalizedText(presenter, getMenuItemText());
            menuInitialized = true;
        }

        presenter.setEnabled(action != null);
        JTextComponent comp = Utilities.getFocusedComponent();
        if (comp != null && comp instanceof JEditorPane){
            addAccelerators(this, presenter, comp);
        } else {
            presenter.setAccelerator(getDefaultAccelerator());
        }

    }
    
    
    public static final class CompletionShow extends CompletionActionsMainMenu {


        protected String getMenuItemText() {
            return NbBundle.getBundle(CompletionActionsMainMenu.class).getString(ExtKit.completionShowAction + "-main_menu_item"); //NOI18N
        }

        
        protected String getActionName() {
            return ExtKit.completionShowAction;
        }

        public void actionPerformed(ActionEvent e) {
            Completion.get().showCompletion();
        }
        
        
        
        
    } 
    
    public static final class DocumentationShow extends CompletionActionsMainMenu {

        protected String getMenuItemText() {
            return NbBundle.getBundle(CompletionActionsMainMenu.class).getString(ExtKit.documentationShowAction + "-main_menu_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.documentationShowAction;
        }

        public void actionPerformed(ActionEvent e) {
            Completion.get().showDocumentation();
        }
                
    }
    
    public static final class ToolTipShow extends CompletionActionsMainMenu {

        protected String getMenuItemText() {
            return NbBundle.getBundle(CompletionActionsMainMenu.class).getString(ExtKit.completionTooltipShowAction + "-main_menu_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.completionTooltipShowAction;
        }

        public void actionPerformed(ActionEvent e) {
            Completion.get().showToolTip();
        }
                
    }
    
}
