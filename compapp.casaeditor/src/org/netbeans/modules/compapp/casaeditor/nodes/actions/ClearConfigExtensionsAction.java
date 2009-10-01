/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/** 
 * Action to clear configuration extensions. 
 * 
 * This action only serves for presenting purpose. 
 * 
 * @see ClearConfigExtensionAction  
 * 
 * @author jqian
 */
public class ClearConfigExtensionsAction extends NodeAbstractAction
        implements Presenter.Popup {

    public ClearConfigExtensionsAction(String displayName, CasaNode node) {
        super(displayName, node);
    }

    public void actionPerformed(ActionEvent e) {
        // do nothing
    }

    public JMenuItem getPopupPresenter() {
        CasaComponent casaComponent = (CasaComponent) getData();
        List<QName> extensionQNames = getExtensionQNames(casaComponent);

        JMenu menu = new JMenu((String) getValue(Action.NAME));

        if (extensionQNames.size() > 1) {
            Action clearAllQoSExtensionsAction = 
                    new ClearConfigExtensionAction((CasaNode) getNode(), null);

            JMenuItem menuItem = new JMenuItem();
            Actions.connect(menuItem, clearAllQoSExtensionsAction, true);
            menu.add(menuItem);

            menu.add(new JSeparator());
        }

        for (QName extensionQName : extensionQNames) {
            Action clearQoSExtensionAction = new ClearConfigExtensionAction(
                    (CasaNode) getNode(), extensionQName);

            JMenuItem menuItem = new JMenuItem();
            Actions.connect(menuItem, clearQoSExtensionAction, true);
            menu.add(menuItem);
        }

        return menu;
    }

    private List<QName> getExtensionQNames(CasaComponent casaComponent) {
        List<QName> ret = new ArrayList<QName>();

        for (CasaExtensibilityElement ee : casaComponent.getExtensibilityElements()) {
            ret.add(ee.getQName());
        }

        return ret;
    }
}

/** 
 * Real action to clear one particular type of config configuration.   
 */
class ClearConfigExtensionAction extends NodeAbstractAction {

    private QName qName;

    /**
     * Constructs a ClearConfigExtensionAction.
     * 
     * @param node  a CASA node
     * @param qName QName for the configuration extension element; 
     *      or <code>null</code> for all extension elements.    
     *      For example: {http://www.sun.com/jbi/qos/redelivery}redelivery
     */
    public ClearConfigExtensionAction(CasaNode node, QName qName) {
        super(qName == null ? "All" : qName.getLocalPart(), node);
        this.qName = qName;
    }

    public void actionPerformed(ActionEvent e) {
        CasaComponent casaComponent = (CasaComponent) getData();

        CasaWrapperModel model = getModel();
        for (CasaExtensibilityElement ee : casaComponent.getExtensibilityElements()) {
            if (qName == null || ee.getQName().equals(qName)) {
                model.removeExtensibilityElement(casaComponent, ee);
                if (qName != null) {
                    break;
                }
            }
        }

        //((CasaNode) getNode()).refresh();
        Node[] selectedNodes = TopComponent.getRegistry().getActivatedNodes();
        
        if (selectedNodes != null && selectedNodes.length > 0 &&
                selectedNodes[0] instanceof CasaNode) {
            CasaNode connectionNode = (CasaNode) selectedNodes[0];
            connectionNode.refresh();
        }
    }
}

