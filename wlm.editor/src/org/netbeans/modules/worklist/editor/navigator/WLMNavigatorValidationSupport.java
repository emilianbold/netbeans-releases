/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.navigator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.validation.core.Controller;
import org.netbeans.modules.xml.validation.core.Listener;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.worklist.dataloader.ValidationControllerCookie;
import org.netbeans.modules.worklist.editor.nodes.WLMNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public class WLMNavigatorValidationSupport implements Listener {

    private WLMNavigatorPanel navigatorPanel;
    private Controller validationController = null;

    private Map<WLMComponent, WLMValidationItems> dataMap;

    private UpdateViewRunnable updateViewRunnable = null;
    private final Object sync = new Object();

    public WLMNavigatorValidationSupport(WLMNavigatorPanel navigatorPanel) {
        this.navigatorPanel = navigatorPanel;
    }

    public void install(Lookup lookup) {
        assert (validationController == null);

        System.out.println("*** INSTALL");

        ValidationControllerCookie cookie = lookup
                .lookup(ValidationControllerCookie.class);
        if (cookie != null) {
            validationController = cookie.getValidationController();
        }

        System.out.println("*** ValidationController=" + validationController);

        if (validationController != null) {
            validationController.addListener(this);
            updateView(validationController.getResult());
        }
    }

    public void uninstall() {
        System.out.println("*** UNINSTALL");
        
        if (validationController == null) {
            return;
        }

        validationController.removeListener(this);
        validationController = null;
    }

    // org.netbeans.modules.soa.validation.core.Listener method implementation
    public void validationUpdated(List<ResultItem> result) {
        System.out.println("*** EVENT");

        updateView(result);
    }

    public WLMValidationItems getValidationItems(WLMNode node) {
        WLMComponent nodeSourceComponent = node.getGoToSourceWLMComponent();

        if (WLMNodeTypeResolver.getNodeType(nodeSourceComponent) 
                != node.getType())
        {
            return null;
        }

        return (this.dataMap == null) ? null : this.dataMap
                .get(nodeSourceComponent);
    }

    private void updateView(List<ResultItem> resultsList) {
        synchronized (sync) {
            if (updateViewRunnable == null) {
                updateViewRunnable = new UpdateViewRunnable();
                SwingUtilities.invokeLater(updateViewRunnable);
            }
            updateViewRunnable.setResultItems(resultsList);
        }
    }

    private Map<WLMComponent, WLMValidationItems> createDataMap(
            List<ResultItem> resultsList)
    {
        Map<WLMComponent, WLMValidationItems> result
                = new HashMap<WLMComponent, WLMValidationItems>();

        if (resultsList != null) {
            for (ResultItem resultItem : resultsList) {
                WLMComponent component = findWLMComponent(
                        resultItem.getComponents());

                if (component == null) {
                    continue;
                }

                WLMValidationItems validationItems = result.get(component);
                if (validationItems == null) {
                    validationItems = new WLMValidationItems();
                    result.put(component, validationItems);
                }
                validationItems.add(resultItem);
            }
        }

        return result;
    }

    private WLMComponent findWLMComponent(Component component) {
        if (!(component instanceof WLMComponent)) {
            return null;
        }

        WLMComponent wlmComponent = (WLMComponent) component;
        WLMNodeType type = WLMNodeTypeResolver.getNodeType(wlmComponent);
        if (type == null) {
            WLMComponent parent = wlmComponent.getParent();
            while (type == null && parent != null) {
                wlmComponent = parent;
                type = WLMNodeTypeResolver.getNodeType(wlmComponent);
                parent = wlmComponent.getParent();
            }
        }

        return (type == null) ? null : wlmComponent;
    }

    private class UpdateViewRunnable implements Runnable {
        private List<ResultItem> resultItems;

        void setResultItems(List<ResultItem> resultItems) {
            this.resultItems = resultItems;
        }

        public void run() {
            synchronized (sync) {
                updateViewRunnable = null;
            }

            if (validationController == null) {
                return;
            }

            dataMap = createDataMap(resultItems);
            navigatorPanel.updateValidationItems();
        }
    }
}
