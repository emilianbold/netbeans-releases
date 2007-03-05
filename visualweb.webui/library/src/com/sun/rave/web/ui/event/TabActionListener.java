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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.event;

import java.util.Iterator;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.component.UIComponent;

import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.component.Tab;
import com.sun.rave.web.ui.component.TabSet;

/**
 * <p>Defines an ActionListener for Tab click events.</p>
 *
 * <p>This listener updates the selected value of the enclsing TabSet with the
 * id of the Tab that generated the event. It also ensures that the
 * selectedChildId of the clicked Tab's parent Tab is updated.</p>
 *
 * @author Sean Comerford
 */
public class TabActionListener implements ActionListener {

    /**
     *	<p> Creates a new instance of <code>TabActionListener</code>.</p>
     */
    public TabActionListener() {
    }
    
    /**
     * <p>Perform the processing necessary to handle a Tab click event.</p>
     *
     * <p>The clicked Tab will be the source component that generated the given
     * ActionEvent. This method should ensure that id of the source component 
     * (or one of its children) is set as the selected tab id for the
     * enclosing TabSet. This method should also ensure that the selectedChildId
     * of the source tab's parent Tab (if one exists) is also updated.
     *
     * @param event The ActionEvent generated
     */
    public void processAction(ActionEvent event) {
        UIComponent eventComponent = event.getComponent();
        if (!(eventComponent instanceof Tab)) {
            // we don't care about non Tab child actions
            return;
        }
        
        // get the tab that was clicked
        Tab tabToSelect = (Tab) eventComponent;
        
        while (tabToSelect.getChildCount() > 0) {
            // the clicked tab has children - one of them may need to be set as
            // the selected tab
            String selectedChildId = tabToSelect.getSelectedChildId();
            
            Iterator i = tabToSelect.getChildren().iterator();
            
            // test if a previous child tab was set as the selected child
            if (selectedChildId != null) {                
                // set the last selected child tab as selected
                while(i.hasNext()) {
                    Tab child = null;
                    
                    try {
                        child = (Tab) i.next();
                    } catch (ClassCastException cce) {
                        // this child not a Tab
                        continue;
                    }
                    
                    if (selectedChildId.equals(child.getId())) {
                        // set this child as the tab to select
                        tabToSelect = child;
                        break;
                    }
                }                
            } else {                
                // no prior child selection -  select the 1st child tab but be
                // aware there may not actually be any Tab children
                while (i.hasNext()) {
                    Object kid = i.next();
                    
                    if (kid instanceof Tab) {
                        tabToSelect = (Tab) kid;
                        break;
                    }
                }
                // there must not be any Tab children - break outer while
                break;
            }
        }
        
        String newSelection = tabToSelect.getId();
        UIComponent parent = eventComponent.getParent();
        
        if (parent instanceof Tab) {
            // updated the parent's selectedChildId
            ((Tab) parent).setSelectedChildId(newSelection);
        }
        
        // back up the component tree until we find the TabSet ancestor
        while (parent != null) {
            if (parent instanceof TabSet) {
                break;
            } else {
                parent = parent.getParent();
            }
        }
                
        try {
            ((TabSet) parent).setSelected(newSelection);            
        } catch (NullPointerException npe) {
            if (LogUtil.infoEnabled()) {
                LogUtil.info(TabActionListener.class, "WEBUI0006",
                    new String[] { eventComponent.getId() });
            }
        }
    }
}
