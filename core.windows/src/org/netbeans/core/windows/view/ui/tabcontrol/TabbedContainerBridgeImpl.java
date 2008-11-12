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
/*
 * TabbedContainerBridgeImpl.java
 *
 * Created on June 1, 2004, 6:42 PM
 */

package org.netbeans.core.windows.view.ui.tabcontrol;

import java.util.List;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.ComponentConverter;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.modules.openide.explorer.TabbedContainerBridge;

/**
 * Implementation of org.netbeans.modules.explorer.TabbedContainerBridge, as
 * used by the property sheet.  This class allows the property sheet to use
 * TabbedContainer without openide explicitly depending on the TabControl
 * library (currently impossible).
 *
 * @author  Tim Boudreau
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.openide.explorer.TabbedContainerBridge.class)
public class TabbedContainerBridgeImpl extends TabbedContainerBridge {
    
    /** Creates a new instance of TabbedContainerBridgeImpl */
    public TabbedContainerBridgeImpl() {
    }
    
    public void attachSelectionListener(JComponent jc, ChangeListener listener) {
        TabbedContainer cont = (TabbedContainer) jc;
        cont.getSelectionModel().addChangeListener(listener);
    }
    
    public JComponent createTabbedContainer() {
        return new TabbedContainer(TabbedContainer.TYPE_TOOLBAR);
    }
    
    public void detachSelectionListener(JComponent jc, ChangeListener listener) {
        TabbedContainer cont = (TabbedContainer) jc;
        cont.getSelectionModel().removeChangeListener(listener);
    }
    
    public Object[] getItems(JComponent jc) {
        TabbedContainer cont = (TabbedContainer) jc;
        List l = cont.getModel().getTabs();
        Object[] items = new Object[l.size()];
        for (int i=0; i < items.length; i++) {
            items[i] = ((TabData) l.get(i)).getUserObject();
        }
        return items;
    }
    
    public Object getSelectedItem(JComponent jc) {
        Object result = null;
        TabbedContainer cont = (TabbedContainer) jc;
        int i = cont.getSelectionModel().getSelectedIndex();
        if (i != -1) {
            result = cont.getModel().getTab(i).getUserObject();
        }
        return result;
    }

    public void setSelectedItem(JComponent jc, Object selection) {
        TabbedContainer cont = (TabbedContainer) jc;
        TabDataModel mdl = cont.getModel();
        int max = mdl.size();
        for (int i=0; i < max; i++) {
            TabData td = mdl.getTab(i);
            if (td.getUserObject() == selection) {
                cont.getSelectionModel().setSelectedIndex(i);
                break;
            }
        }
    }

    public boolean setSelectionByName(JComponent jc, String tabname) {
        TabbedContainer cont = (TabbedContainer) jc;
        TabDataModel mdl = cont.getModel();
        int max = mdl.size();
        for (int i=0; i < max; i++) {
            TabData td = mdl.getTab(i);
            if (tabname.equals(td.getText())) {
                cont.getSelectionModel().setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    public String getCurrentSelectedTabName(JComponent jc) {
        TabbedContainer cont = (TabbedContainer) jc;
        int sel = cont.getSelectionModel().getSelectedIndex();
        if (sel != -1) {
            TabData td = cont.getModel().getTab(sel);
            return td.getText();
        }
        return null;
    }

    public void setInnerComponent(JComponent jc, JComponent inner) {
        TabbedContainer cont = (TabbedContainer) jc;
        ComponentConverter cc = new ComponentConverter.Fixed (inner);
        cont.setComponentConverter(cc);
    }
    
    public JComponent getInnerComponent(JComponent jc) {
        TabbedContainer cont = (TabbedContainer) jc;
        return (JComponent) cont.getComponentConverter().getComponent(null);
    }
    
    public void setItems(JComponent jc, Object[] objects, String[] titles) {
        TabbedContainer cont = (TabbedContainer) jc;
        assert objects.length == titles.length;
        TabData[] td = new TabData [objects.length];
        for (int i=0; i < objects.length; i++) {
            td[i] = new TabData (objects[i], null, titles[i], null);
        }
        cont.getModel().setTabs(td);
    }
    
}
