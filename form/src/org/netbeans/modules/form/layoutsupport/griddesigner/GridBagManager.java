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
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.MetaComponentCreator;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.VisualReplicator;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AddAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteColumnContentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteComponentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteRowAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteRowContentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.EncloseInContainerAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.InsertColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.InsertRowAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.SplitColumnAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.SplitRowAction;
import org.netbeans.modules.form.project.ClassSource;
import org.openide.nodes.Node;

/**
 * {@code GridManager} for {@code GrigBagLayout} layout manager.
 *
 * @author Jan Stola, Petr Somol
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

    public void updateGridX(Component component, int gridXDiff) {
        int oldGridX = info.getGridX(component);
        if(oldGridX + gridXDiff > 0) {
            setProperty(component, "gridx", oldGridX + gridXDiff); // NOI18N
        } else {
            setProperty(component, "gridx", 0); // NOI18N
        }
    }
    
    public void updateGridY(Component component, int gridYDiff) {
        int oldGridY = info.getGridY(component);
        if(oldGridY + gridYDiff > 0) {
            setProperty(component, "gridy", oldGridY + gridYDiff); // NOI18N
        } else {
            setProperty(component, "gridy", 0); // NOI18N
        }
    }

    @Override
    public void setGridWidth(Component component, int gridWidth) {
        setProperty(component, "gridwidth", gridWidth); // NOI18N
    }

    @Override
    public void setGridHeight(Component component, int gridHeight) {
        setProperty(component, "gridheight", gridHeight); // NOI18N
    }

    public void updateGridWidth(Component component, int gridWDiff) {
        int oldGridW = info.getGridWidth(component);
        if(oldGridW + gridWDiff > 1) {
            setProperty(component, "gridwidth", oldGridW + gridWDiff); // NOI18N
        } else {
            setProperty(component, "gridwidth", 1); // NOI18N
        }
    }
    
    public void updateGridHeight(Component component, int gridHDiff) {
        int oldGridH = info.getGridHeight(component);
        if(oldGridH + gridHDiff > 1) {
            setProperty(component, "gridheight", oldGridH + gridHDiff); // NOI18N
        } else {
            setProperty(component, "gridheight", 1); // NOI18N
        }
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
            action = new EncloseInContainerAction();
            actions.add(action);
        } else if (context == GridAction.Context.CELL) {
            GridAction action = new AddAction(replicator);
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
    public void updateLayout(boolean includingSubcontainers) {
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        if (includingSubcontainers) {
            for (RADVisualComponent metacomp : metacont.getSubComponents()) {
                if (metacomp instanceof RADVisualContainer) {
                    replicator.updateContainerLayout((RADVisualContainer)metacomp);
                }
            }
        }
        replicator.updateContainerLayout(metacont);
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
    
    public void setHorizontalFill(Component component, boolean fill) {
        int oldFill = info.getFill(component);
        if(fill) switch(oldFill) {
            case GridBagConstraints.NONE: oldFill = GridBagConstraints.HORIZONTAL; break;
            case GridBagConstraints.VERTICAL: oldFill = GridBagConstraints.BOTH; break;
        } else switch(oldFill) {
            case GridBagConstraints.HORIZONTAL: oldFill = GridBagConstraints.NONE; break;
            case GridBagConstraints.BOTH: oldFill = GridBagConstraints.VERTICAL; break;
        }
        setProperty(component, "fill", oldFill); // NOI18N
    }
    
    public void setVerticalFill(Component component, boolean fill) {
        int oldFill = info.getFill(component);
        if(fill) switch(oldFill) {
            case GridBagConstraints.NONE: oldFill = GridBagConstraints.VERTICAL; break;
            case GridBagConstraints.HORIZONTAL: oldFill = GridBagConstraints.BOTH; break;
        } else switch(oldFill) {
            case GridBagConstraints.VERTICAL: oldFill = GridBagConstraints.NONE; break;
            case GridBagConstraints.BOTH: oldFill = GridBagConstraints.HORIZONTAL; break;
        }
        setProperty(component, "fill", oldFill); // NOI18N
    }
    
    public void updateIPadX(Component component, int iPadXDiff) {
        int oldIPadX = info.getIPadX(component);
        if(oldIPadX + iPadXDiff > 0) {
            setProperty(component, "ipadx", oldIPadX + iPadXDiff); // NOI18N
        } else {
            setProperty(component, "ipadx", 0); // NOI18N
        }
    }
    
    public void updateIPadY(Component component, int iPadYDiff) {
        int oldIPadY = info.getIPadY(component);
        if(oldIPadY + iPadYDiff > 0) {
            setProperty(component, "ipady", oldIPadY+iPadYDiff); // NOI18N
        } else {
            setProperty(component, "ipady", 0); // NOI18N
        }
    }

    public void setIPadX(Component component, int iPadX) {
        setProperty(component, "ipadx", iPadX); // NOI18N
    }

    public void setIPadY(Component component, int iPadY) {
        setProperty(component, "ipady", iPadY); // NOI18N
    }
    
    public void updateInsets(Component component, Insets diff) {
        Insets insets = info.getInsets(component);
        insets.top = insets.top + diff.top >= 0 ? insets.top + diff.top : 0;
        insets.left = insets.left + diff.left >= 0 ? insets.left + diff.left : 0;
        insets.bottom = insets.bottom + diff.bottom >= 0 ? insets.bottom + diff.bottom : 0;
        insets.right = insets.right + diff.right >= 0 ? insets.right + diff.right : 0;
        setProperty(component, "insets", insets); // NOI18N
    }

    public void resetInsets(Component component, boolean top, boolean left, boolean bottom, boolean right) {
        Insets insets = info.getInsets(component);
        if (top) insets.top = 0;
        if (left) insets.left = 0;
        if (bottom) insets.bottom = 0;
        if (right) insets.right = 0;
        setProperty(component, "insets", insets); // NOI18N
    }
    
    public void setWeightX(Component component, double weight) {
        setProperty(component, "weightx", weight); // NOI18N
    }

    public void setWeightY(Component component, double weight) {
        setProperty(component, "weighty", weight); // NOI18N
    }

    public void updateWeightX(Component component, double diff) {
        double oldWeight = info.getWeightX(component);
        if(oldWeight + diff > 0.0d) {
            setProperty(component, "weightx", oldWeight + diff); // NOI18N
        } else {
            setProperty(component, "weightx", 0.0d); // NOI18N
        }
    }

    public void updateWeightY(Component component, double diff) {
        double oldWeight = info.getWeightY(component);
        if(oldWeight + diff > 0.0d) {
            setProperty(component, "weighty", oldWeight + diff); // NOI18N
        } else {
            setProperty(component, "weighty", 0.0d); // NOI18N
        }
    }

    @Override
    public Container encloseInContainer(Set<Component> components) {
        GridBagLayout layout = (GridBagLayout)container.getLayout();
        RADVisualContainer parent = null;
        FormModel formModel = null;
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = 0;
        int maxy = 0;
        boolean horizontalFill = false;
        boolean verticalFill = false;
        boolean weightx = false;
        boolean weighty = false;
        for (Component comp : components) {
            RADVisualComponent metaComp = componentMap.get(comp);
            parent = metaComp.getParentContainer();
            formModel = metaComp.getFormModel();
            int gridx = info.getGridX(comp);
            int gridy = info.getGridY(comp);
            int gridwidth = info.getGridWidth(comp);
            int gridheight = info.getGridHeight(comp);            
            minx = Math.min(minx, gridx);
            miny = Math.min(miny, gridy);
            maxx = Math.max(maxx, gridx+gridwidth);
            maxy = Math.max(maxy, gridy+gridheight);
            int fill = info.getFill(comp);
            if (fill == GridBagConstraints.BOTH) {
                horizontalFill = true;
                verticalFill = true;
            } else if (fill == GridBagConstraints.HORIZONTAL) {
                horizontalFill = true;
            } else if (fill == GridBagConstraints.VERTICAL) {
                verticalFill = true;
            }
            double wx = info.getWeightX(comp);
            if (wx != 0) {
                weightx = true;
            }
            double wy = info.getWeightY(comp);
            if (wy != 0) {
                weighty = true;
            }
        }
        double[][] weights = layout.getLayoutWeights();
        MetaComponentCreator creator = formModel.getComponentCreator();
        RADVisualContainer panel = (RADVisualContainer)creator.createComponent(
                new ClassSource("javax.swing.JPanel"), parent, null); // NOI18N
        // Set the layout of the panel
        boolean recording = formModel.isUndoRedoRecording();
        try {
            // Issue 190882. No need to undo this layout change.
            // The panel is newly created. Hence, switching undo off makes no harm.
            formModel.setUndoRedoRecording(false);
            creator.createComponent(new ClassSource("java.awt.GridBagLayout"), panel, null); // NOI18N
        } finally {
            formModel.setUndoRedoRecording(recording);
        }
        for (Component comp : components) {
            RADVisualComponent metaComp = componentMap.get(comp);
            creator.moveComponent(metaComp, panel);
        }
        if (minx != 0) {
            for (Component comp : components) {
                int gridx = info.getGridX(comp);
                setGridX(comp, gridx-minx);
            }
        }
        if (miny != 0) {
            for (Component comp : components) {
                int gridy = info.getGridY(comp);
                setGridY(comp, gridy-miny);
            }
        }
        Container clone = (Container)replicator.getClonedComponent(panel);
        if (clone == null) {
            clone = (Container)replicator.createClone(panel);
        }
        componentMap.put(clone, panel);
        setGridX(clone, minx);
        setGridY(clone, miny);
        setGridWidth(clone, maxx-minx);
        setGridHeight(clone, maxy-miny);
        if (horizontalFill && weightx) {
            double totalWeightX = 0;
            for (int i=minx; i<maxx; i++) {
                totalWeightX += weights[0][i];
            }
            setWeightX(clone, totalWeightX);
        }
        if (verticalFill && weighty) {
            double totalWeightY = 0;
            for (int i=miny; i<maxy; i++) {
                totalWeightY += weights[1][i];
            }
            setWeightY(clone, totalWeightY);
        }
        int fill = (horizontalFill
                ? (verticalFill ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL)
                : (verticalFill ? GridBagConstraints.VERTICAL : GridBagConstraints.NONE));
        setFill(clone, fill);
        return clone;
    }

}
