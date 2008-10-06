/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeListener;
import org.openide.awt.Actions;
import org.openide.awt.JInlineMenu;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * 
 * 
 * @author trey spiva
 */
public class SubMenuAction extends AbstractAction 
        implements Presenter.Popup, ContextAwareAction
{
    private ArrayList < Action > subActions = new ArrayList < Action >();
    private Lookup context = null;
    
    public SubMenuAction(String name)
    {
        putValue(Action.NAME, name);
    }

    public void actionPerformed(ActionEvent event)
    {
    }
    
    public Action createContextAwareInstance(Lookup actionContext)
    {
        context = actionContext;
        return this;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // SubMenu Actions
    
    public void addAction(Action a)
    {
        subActions.add(a);
    }
    
    public List < Action > getSubActions()
    {
        return Collections.unmodifiableList(subActions);
    }

    public JMenuItem getPopupPresenter()
    {
        
        Action[] actions = new Action[subActions.size()];
        subActions.toArray(actions);
        JPopupMenu popup = Utilities.actionsToPopup(actions, context);
        
        
        JMenuItem retVal = null;
        if(popup.getComponentCount() > 0)
        {
            JMenu  menu = new JMenu((String)getValue(NAME));
            for(Component item : popup.getComponents())
            {
                menu.add(item);
            }

            retVal = menu;
        }
        else
        {
            // If the menu item is not valid we want to not display the menu item
            // If we return null a warning will be written to the log file by
            // org.openide.util.Utilities.  However if we return an empty
            // JInlineMenu the menu will not appear.
            retVal = new JInlineMenu();
        }
        return retVal;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    
    protected class SubMenuModel implements Actions.SubMenuModel
    {
        private Action[] actions = null;
        
        public int getCount()
        {
            return subActions.size();
        }

        public String getLabel(int index)
        {
            String retVal = "Extra";
            if(index < subActions.size())
            {
                retVal = (String) subActions.get(index).getValue(Action.NAME);
            }
            
            return retVal;
        }

        public HelpCtx getHelpCtx(int index)
        {
            HelpCtx retVal = null;
            
            if((index >= 0) && (subActions.size() > index))
            {
                if(subActions.get(index) instanceof SystemAction)
                {
                    SystemAction sysAction = (SystemAction)subActions.get(index);
                    retVal = sysAction.getHelpCtx();
                }
            }
            
            return retVal;
        }

        public void performActionAt(int index)
        {
            if((index > 0) && (subActions.size() > index))
            {
                subActions.get(index).actionPerformed(null);
            }
        }

        public void addChangeListener(ChangeListener l)
        {
        }

        public void removeChangeListener(ChangeListener l)
        {
        }
    }
}
