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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.api.model.presenters.actions;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Karol Harezlak
 */

public final class AddAction extends AbstractAction  implements ActionContext , Presenter.Popup  {
    
    public static final String DISPLAY_NAME = NbBundle.getMessage(AddAction.class, "Name_AddAction"); //NOI18N
    
    private static Map<Collection<TypeID>, AddAction> instances = new HashMap<Collection<TypeID>, AddAction>(); 
    
    public static final AddAction getInstance(TypeID... filters) {
        Collection<TypeID> filtersList = Arrays.asList(filters);
        
        for (Collection<TypeID> key : instances.keySet()) {
            if (key.size() == filtersList.size() && key.containsAll(filtersList))
                return instances.get(key);
        }
        instances.put(filtersList, new AddAction(filters));
        return instances.get(filtersList);
    } 
    
    private JMenu menu;
    private WeakReference<DesignComponent> component;
    private boolean enabled;
    private TypeID[] filtersTypeID =  new TypeID[0];

    private AddAction(TypeID... filtersTypeID) {
        this.putValue(Action.NAME, DISPLAY_NAME);
        if (filtersTypeID != null)
            this.filtersTypeID = filtersTypeID;
    }
    
    public void actionPerformed(ActionEvent e) {
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public JMenu getPopupPresenter() {
        return getMenu();
    }
    
    private JMenu getMenu() {
        if (component == null || component.get() == null)
            throw new IllegalStateException("This action has to be attached to exisitng DesignComponent");//NOI18N
        
        if (menu == null)
            menu = new JMenu(DISPLAY_NAME);
        else
            menu.removeAll();
        
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                menu.setEnabled(isEnabled());
            }
        });
        
        for (Action addAction : ActionsSupport.createAddActionArray(component.get(), filtersTypeID)) {
            if (addAction.getValue(ActionsSupport.SEPERATOR_KEY) == null)
                menu.add(addAction);
            else
                menu.add(getSeparator( (String) addAction.getValue(Action.NAME)));
        }
        return menu;
    }
    
    @Override
    public boolean isEnabled() {
        if (component == null || component.get() == null)
            throw new IllegalStateException("This action has to be attache to DesignComponent, component can not be null");//NOI18N
        
        if (component.get().getDocument().getSelectedComponents().size() > 1 || component.get().getPresenters(AddActionPresenter.class).isEmpty())
            enabled = false;
        for (AddActionPresenter presenter : component.get().getPresenters(AddActionPresenter.class)) {
            AddActionItem[] addActionItems = presenter.getAddActionItems();
            if (addActionItems != null && addActionItems.length > 0)
                enabled = true;
        }
        return enabled;
    }
    
    private JComponent getSeparator(String name){
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(name);
        
        //panel.setBackground(new Color(230,230,230));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        //label.setFont(label.getFont().deriveFont(Font.PLAIN, 12));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public void setComponent(DesignComponent component) {
        this.component = new WeakReference<DesignComponent>(component);
    }
   
}
