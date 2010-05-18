/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
