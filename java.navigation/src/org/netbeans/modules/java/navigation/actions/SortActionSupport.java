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

package org.netbeans.modules.java.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.java.navigation.ClassMemberFilters;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/** "Radio button" type action, base class designed for subclassing
 *
 * @author Dafe Simonek
 */
public abstract class SortActionSupport extends AbstractAction implements Presenter.Popup {
    
    private JRadioButtonMenuItem menuItem;
    protected ClassMemberFilters filters;
    
    /** Creates a new instance of SortByNameAction */
    public SortActionSupport ( ClassMemberFilters filters ) {
        this.filters = filters;
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
        
        public SortByNameAction ( ClassMemberFilters filters) {
            super(filters);
            putValue(Action.NAME, NbBundle.getMessage(SortByNameAction.class, "LBL_SortByName")); //NOI18N
        }
    
        public void actionPerformed (ActionEvent e) {
            filters.setNaturalSort(false);
            updateMenuItem();
        }

        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(!filters.isNaturalSort());
        }
    } // end of SortByNameAction

    /** Enables sorting by names when selected
     */
    public static final class SortBySourceAction extends SortActionSupport {
        
        public SortBySourceAction ( ClassMemberFilters filters ) {
            super(filters);
            putValue(Action.NAME, NbBundle.getMessage(SortBySourceAction.class, "LBL_SortBySource")); //NOI18N
        }
    
        public void actionPerformed (ActionEvent e) {
            filters.setNaturalSort(true);
            updateMenuItem();
        }

        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(filters.isNaturalSort());
        }
    } // end of SortBySourceAction
    
    
}
