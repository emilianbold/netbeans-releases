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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.VisualReplicator;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AddAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteColumnContentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteComponentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteRowAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteRowContentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.InsertColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.InsertRowAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.SplitColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.SplitRowAction;
import org.openide.nodes.Node;

/**
 * {@code GridManager} for {@code GrigBagLayout} layout manager.
 *
 * @author Jan Stola
 */
public class GridBagManager implements GridManager {
    private Container container;
    private GridBagInfoProvider info;
    private GridCustomizer customizer;
    private VisualReplicator replicator;
    private Map<Component,RADVisualComponent> componentMap = new IdentityHashMap<Component,RADVisualComponent>();

    public GridBagManager(VisualReplicator replicator) {
        this.replicator = replicator;
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        Object bean = replicator.getClonedComponent(metacont);
        this.container = metacont.getContainerDelegate(bean);
        if (!(container.getLayout() instanceof GridBagLayout)) {
            throw new IllegalArgumentException();
        }
        info = new GridBagInfoProvider(container);
        updateComponentMap();
    }

    private void updateComponentMap() {
        componentMap.clear();
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        for (RADVisualComponent metacomp : metacont.getSubComponents()) {
            componentMap.put((Component)replicator.getClonedComponent(metacomp), metacomp);
        }
    }
    
    private RADVisualComponent getMetaComponent(Component component) {
        RADVisualComponent metacomp = componentMap.get(component);
        if (metacomp == null) {
            updateComponentMap();
            metacomp = componentMap.get(component);
        }
        return metacomp;
    }

    private Node.Property getProperty(Component component, String propertyName) {
        RADVisualComponent metacomp = getMetaComponent(component);
        for (Node.Property property : metacomp.getConstraintsProperties()) {
            String name = property.getName();
            if (name.endsWith(propertyName)) {
                return property;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public GridBagInfoProvider getGridInfo() {
        return info;
    }

    private void setProperty(Component component, String propertyName, Object value) {
        Node.Property property = getProperty(component, propertyName);
        try {
            property.setValue(value);
        } catch (IllegalAccessException iaex) {
            FormUtils.LOGGER.log(Level.WARNING, iaex.getMessage(), iaex);
        } catch (InvocationTargetException itex) {
            FormUtils.LOGGER.log(Level.WARNING, itex.getMessage(), itex);
        }
    }

    @Override
    public void setGridX(Component component, int gridX) {
        setProperty(component, "gridx", gridX); // NOI18N
    }

    @Override
    public void setGridY(Component component, int gridY) {
        setProperty(component, "gridy", gridY); // NOI18N
    }

    @Override
    public void setGridWidth(Component component, int gridWidth) {
        setProperty(component, "gridwidth", gridWidth); // NOI18N
    }

    @Override
    public void setGridHeight(Component component, int gridHeight) {
        setProperty(component, "gridheight", gridHeight); // NOI18N
    }

    @Override
    public void addComponent(Component component, int gridX, int gridY, int gridWidth, int gridHeight) {
        if (!GridUtils.isPaddingComponent(component)) {
            throw new IllegalArgumentException();
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridX;
        c.gridy = gridY;
        c.gridwidth = gridWidth;
        c.gridheight = gridHeight;
        container.add(component, c);
        container.doLayout();
    }

    @Override
    public List<GridAction> designerActions(GridAction.Context context) {
        List<GridAction> actions = new ArrayList<GridAction>();
        if (context == GridAction.Context.COLUMN) {
            GridAction action = new InsertColumnAction(false);
            actions.add(action);
            action = new InsertColumnAction(true);
            actions.add(action);
            action = new DeleteColumnAction();
            actions.add(action);
            action = new DeleteColumnContentAction();
            actions.add(action);
            action = new SplitColumnAction();
            actions.add(action);
        } else if (context == GridAction.Context.ROW) {
            GridAction action = new InsertRowAction(false);
            actions.add(action);
            action = new InsertRowAction(true);
            actions.add(action);
            action = new DeleteRowAction();
            actions.add(action);
            action = new DeleteRowContentAction();
            actions.add(action);
            action = new SplitRowAction();
            actions.add(action);
        } else if (context == GridAction.Context.COMPONENT) {
            GridAction action = new DeleteComponentAction();
            actions.add(action);
        } else if (context == GridAction.Context.GRID) {
            GridAction action = new AddAction((RADVisualContainer)replicator.getTopMetaComponent());
            actions.add(action);
        }
        return actions;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void removeComponent(Component component) {
        if (!GridUtils.isPaddingComponent(component)) {
            // Padding components are not in the form model
            RADVisualComponent metacomp = getMetaComponent(component);
            metacomp.getFormModel().removeComponent(metacomp, true);
        }
        container.remove(component);
    }

    @Override
    public void insertColumn(int newColumnIndex) {
        for (Component component : getContainer().getComponents()) {
            int x = info.getGridX(component);
            int width = info.getGridWidth(component);
            if (x >= newColumnIndex) {
                setGridX(component, ++x);
            } else if (x+width > newColumnIndex) {
                setGridWidth(component, ++width);
            }
        }
    }

    @Override
    public void deleteColumn(int columnIndex) {
        for (Component component : getContainer().getComponents()) {
            int x = info.getGridX(component);
            int width = info.getGridWidth(component);
            if (x==columnIndex && width==1) {
                removeComponent(component);
            } else if (x > columnIndex) {
                setGridX(component, --x);
            } else if (x+width > columnIndex) {
                setGridWidth(component, --width);
            }
        }
    }

    @Override
    public void insertRow(int newRowIndex) {
        for (Component component : getContainer().getComponents()) {
            int y = info.getGridY(component);
            int height = info.getGridHeight(component);
            if (y >= newRowIndex) {
                setGridY(component, ++y);
            } else if (y+height > newRowIndex) {
                setGridHeight(component, ++height);
            }
        }
    }

    @Override
    public void deleteRow(int rowIndex) {
        for (Component component : getContainer().getComponents()) {
            int y = info.getGridY(component);
            int height = info.getGridHeight(component);
            if (y==rowIndex && height==1) {
                removeComponent(component);
            } else if (y > rowIndex) {
                setGridY(component, --y);
            } else if (y+height > rowIndex) {
                setGridHeight(component, --height);
            }
        }
    }

    @Override
    public void updateLayout() {
        replicator.updateContainerLayout((RADVisualContainer)replicator.getTopMetaComponent());
    }

    @Override
    public GridCustomizer getCustomizer(GridActionPerformer performer) {
        if (customizer == null) {
            customizer = createCustomizer(performer);
        }
        return customizer;
    }

    private GridCustomizer createCustomizer(GridActionPerformer performer) {
        return new GridBagCustomizer(this, performer);
    }

    public void setAnchor(Component component, int anchor) {
        setProperty(component, "anchor", anchor); // NOI18N
    }

    public void setFill(Component component, int fill) {
        setProperty(component, "fill", fill); // NOI18N
    }

}
