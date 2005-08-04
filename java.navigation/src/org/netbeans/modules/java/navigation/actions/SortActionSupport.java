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

package org.netbeans.modules.java.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.java.navigation.ClassMemberModel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.Presenter;

/** "Radio button" type action, base class designed for subclassing
 *
 * @author Dafe Simonek
 */
public abstract class SortActionSupport extends AbstractAction implements Presenter.Popup {
    
    private JRadioButtonMenuItem menuItem;
    protected ClassMemberModel mdl;
    
    /** Creates a new instance of SortByNameAction */
    public SortActionSupport (ClassMemberModel mdl) {
        this.mdl = mdl;
    }
    
    public final JMenuItem getPopupPresenter() {
        JMenuItem result = obtainMenuItem();
        updateMenuItem();
        return result;
    }
    
    protected final JRadioButtonMenuItem obtainMenuItem () {
        if (menuItem == null) {
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
        return menuItem;
    }
    
    protected abstract void updateMenuItem ();
    
    
    /** Enables sorting by names when selected
     */
    public static final class SortByNameAction extends SortActionSupport {
        
        public SortByNameAction (ClassMemberModel mdl) {
            super(mdl);
            putValue(Action.NAME, NbBundle.getMessage(SortByNameAction.class, "LBL_SortByName")); //NOI18N
        }
    
        public void actionPerformed (ActionEvent e) {
            mdl.setNaturalSort(false);
            updateMenuItem();
        }

        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(!mdl.isNaturalSort());
        }
    } // end of SortByNameAction

    /** Enables sorting by names when selected
     */
    public static final class SortBySourceAction extends SortActionSupport {
        
        public SortBySourceAction (ClassMemberModel mdl) {
            super(mdl);
            putValue(Action.NAME, NbBundle.getMessage(SortBySourceAction.class, "LBL_SortBySource")); //NOI18N
        }
    
        public void actionPerformed (ActionEvent e) {
            mdl.setNaturalSort(true);
            updateMenuItem();
        }

        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(mdl.isNaturalSort());
        }
    } // end of SortBySourceAction
    
    
}
