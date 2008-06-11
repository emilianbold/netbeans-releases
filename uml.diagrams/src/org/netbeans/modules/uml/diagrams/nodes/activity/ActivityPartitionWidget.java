/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.nodes.activity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.diagrams.nodes.ContainerNode;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Thuy
 */
public class ActivityPartitionWidget extends UMLNodeWidget
{

    private Scene scene;
    private IActivityPartition parentPartition;
    private UMLNameWidget nameWidget;
    private Widget partitionPanel;
    private static ResourceBundle bundle = NbBundle.getBundle(ContainerNode.class);
    private SeparatorWidget.Orientation orientation = SeparatorWidget.Orientation.VERTICAL;

    public ActivityPartitionWidget(Scene scene)
    {
        super(scene);
        this.scene = scene;
        
        // initialize context palette
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/ActivityFinal");
        addToLookup(paletteModel);
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if (presentation != null)
        {
            parentPartition = (IActivityPartition) presentation.getFirstSubject();
            setCurrentView(createActivityParttionView(parentPartition));
        }
    }

    private Widget createActivityParttionView(IActivityPartition partitionElement)
    {
        //create main view 
        MainViewWidget mainView = new MainViewWidget(scene,
                                                     getWidgetID(),
                                                     bundle.getString("LBL_body"));
        
        mainView.setLayout(
                LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 1));

        mainView.setBorder(BorderFactory.createLineBorder(2));
        mainView.setUseGradient(useGradient);
        mainView.setCustomizableResourceTypes(
                new ResourceType[]{ResourceType.BACKGROUND});
        mainView.setOpaque(true);
        mainView.setCheckClipping(true);

        // element name widget (including stereotype and tagged value)
        nameWidget = new UMLNameWidget(scene, false, getWidgetID());
        nameWidget.initialize(partitionElement);
        mainView.addChild(nameWidget);

        SeparatorWidget hSeparator = new SeparatorWidget(scene,
                                                         SeparatorWidget.Orientation.HORIZONTAL);
        mainView.addChild(hSeparator);

        partitionPanel = new Widget(scene);
        partitionPanel.setMinimumSize(new Dimension(100, 85));
        // TODO: need to find a way to figure out the exisiting orientation of sub parttition
        //setOrientation(Orientation.VERTICAL);   
        populateAllPartitions(partitionElement);
        mainView.addChild(partitionPanel, 1);

        return mainView;
    }

    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.PARTITIONWIDGET.toString();
    }

    public SeparatorWidget.Orientation getOrientation()
    {
        return orientation;
    }

    public void setOrientation(SeparatorWidget.Orientation orientation)
    {
        this.orientation = orientation;
    }

    public boolean hasRowPartition()
    {
        return isVerticalLayout();
    }

    public boolean isVerticalLayout()
    {
        return (getOrientation() == SeparatorWidget.Orientation.HORIZONTAL);
    }

    public void addSubPartition(IActivityPartition subPart)
    {
        SubPartitionWidget subPartWidget = createSubPartitionWidget(subPart);
        addSubPartition(subPartWidget);
    }

    public void addSubPartition(SubPartitionWidget subPartWidget)
    {
        if (subPartWidget == null)
        {
            return;
        }

        if (isVerticalLayout())
        {
            partitionPanel.setLayout(
                    LayoutFactory.createVerticalFlowLayout(
                    LayoutFactory.SerialAlignment.JUSTIFY, 1));
        }
        else
        {
            partitionPanel.setLayout(
                    LayoutFactory.createHorizontalFlowLayout(
                    LayoutFactory.SerialAlignment.JUSTIFY, 1));
        }

        partitionPanel.addChild(subPartWidget);
        updateDividers();
        revalidate();
        scene.validate();
    }

    public int getSubPartitionCount()
    {
        if (parentPartition != null)
        {
            ETList<IActivityPartition> partitions = parentPartition.getSubPartitions();
            return (partitions != null ? partitions.size() : 0);
        }
        return 0;
    }

    public void removeSubPartition(SubPartitionWidget subPartWidget)
    {
        if (subPartWidget != null)
        {
            // get the representing object
            if (scene instanceof ObjectScene)
            {
                Object obj = ((ObjectScene) scene).findObject(subPartWidget);

                if (obj instanceof IPresentationElement)
                {
                    IElement elem = ((IPresentationElement) obj).getFirstSubject();
                    if (elem instanceof IActivityPartition)
                    {
                        // remove sub partition from the model
                        removeSubPartition((IActivityPartition) elem);
                    }
                }
            }
            // remove sub partition widget from parent widget.
            //partitionPanel.removeChild(subPartWidget);
            if (this.getSubPartitionCount() == 0)
            {
                SubPartitionWidget w = new SubPartitionWidget(scene, null, this);
                addSubPartition(w);
            } else
            {
                updateDividers();
                revalidate();
                scene.validate();
            }
        }
    }

    private void removeSubPartition(IActivityPartition subPart)
    {
        if (subPart != null)
        {
            // remove the elemen from model
            subPart.delete();
            // remove the element from parent 
            this.getParentPartition().removeSubPartition(subPart);
        }
    }

    public void removeSubPartitionWidgets()
    {
        List<Widget> children = partitionPanel.getChildren();
        if (children != null && children.size() > 0)
        {
            for (int i = 0; i < children.size(); i++)
            {
                Widget w = children.get(i);
                if (w instanceof SubPartitionWidget)
                {
                    partitionPanel.removeChild(w);
                }
            }
        }
    }

    public void deselectSubPartitionWidgets()
    {
        List<Widget> children = partitionPanel.getChildren();
        if (children != null && children.size() > 0)
        {
            for (int i = 0; i < children.size(); i++)
            {
                Widget w = children.get(i);
                if (w instanceof SubPartitionWidget)
                {
                    ((SubPartitionWidget) w).setSelected(false);
                }
            }
        }
    }

    public List<SubPartitionWidget> getSelectedSubPartition()
    {
        List<SubPartitionWidget> selectedWidgets = new ArrayList<SubPartitionWidget>();

        List<Widget> children = partitionPanel.getChildren();
        if (children != null && children.size() > 0)
        {
            for (int i = 0; i < children.size(); i++)
            {
                Widget w = children.get(i);
                if (w instanceof SubPartitionWidget)
                {
                    SubPartitionWidget subPartWidget = (SubPartitionWidget) w;
                    if (subPartWidget.isSelected())
                    {
                        selectedWidgets.add(subPartWidget);
                    }
                }
            }
        }
        return selectedWidgets;
    }

    private void updateDividers()
    {
        List<Widget> children = partitionPanel.getChildren();
        int count = 0;
        if (children != null && (count = children.size()) > 0)
        {
            for (int i = 0; i < count; i++)
            {
                Widget w = children.get(i);
                if (w instanceof SubPartitionWidget)
                {
                    partitionPanel.setChildConstraint(w, i == (count - 1) ? 1 : 0);
                    ((SubPartitionWidget) w).showPartitionDivider(i != (count - 1));
                }
            }
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();
        nameWidget.propertyChange(event);
    }

    private void populateAllPartitions(IActivityPartition parentPartition)
    {
        SubPartitionWidget subPartWidget = null;
        if (parentPartition != null)
        {
            ETList<IActivityPartition> partitions = parentPartition.getSubPartitions();
            if (partitions != null && partitions.size() > 0)
            {
                IActivityPartition subPart = null;
                for (int i = 0; i < partitions.size(); i++)
                {
                    subPart = partitions.get(i);
                    addSubPartition(subPart);
                }
            }
            else // there's no subPartition
            {
                subPartWidget = new SubPartitionWidget(scene, null, this);
                subPartWidget.enableShowName(false);
                addSubPartition(subPartWidget);
            }
        }
    }

    private SubPartitionWidget createSubPartitionWidget(IActivityPartition subPart)
    {
        SubPartitionWidget subPartWidget = new SubPartitionWidget(scene, subPart,
                                                                  this);

        IPresentationElement pe = Util.createNodePresentationElement();
        pe.addSubject(subPart);
        if (scene instanceof ObjectScene)
        {
            ((ObjectScene) scene).addObject(pe, subPartWidget);
        }
        return subPartWidget;
    }

    public IActivityPartition getParentPartition()
    {
        return parentPartition;
    }

    public SubPartitionWidget getSubPartitionWidget(IActivityPartition subPart)
    {
        if (subPart != null)
        {
            List<Widget> children = partitionPanel.getChildren();
            if (children != null && children.size() > 0)
            {
                for (int i = 0; i < children.size(); i++)
                {
                    Widget w = children.get(i);
                    if (w instanceof SubPartitionWidget)
                    {
                        if (PersistenceUtil.getModelElement(w).isSame(subPart))
                            return (SubPartitionWidget)w;
                    }
                }
            }
        }
        return null;
    }
    IElementLocator locator = new ElementLocator();
    
    @Override
    public void load(NodeInfo nodeReader)
    {
        IElement elt = nodeReader.getModelElement();
        if (elt == null)
        {
            elt = locator.findByID(nodeReader.getProject(), nodeReader.getMEID());
        }   
        if (elt != null && elt instanceof IActivityPartition)
        {
            if (elt.getOwner() instanceof IActivity)
            {
                super.load(nodeReader);                
            }
            SubPartitionWidget subPart = getSubPartitionWidget((IActivityPartition) elt);
            if (subPart != null)
            {
                //fix the size/location/properties
                subPart.setPreferredSize(nodeReader.getSize());
                IPresentationElement pElt = PersistenceUtil.getPresentationElement(subPart);
                nodeReader.setPresentationElement(pElt);
            }
        }
    }

    private class MainViewWidget extends CustomizableWidget
    {

        public MainViewWidget(Scene scene, String propID,
                               String propDisplayName)
        {
            super(scene, propID, propDisplayName);
        }

        @Override
        public void paintBackground()
        {
            Rectangle bounds = getBounds();
            Paint bgColor = getBackground();

            if (isGradient())
            {
                Color primeBgColor = (Color) bgColor;
                bgColor = new GradientPaint(
                        0, 0, Color.WHITE,
                        0, bounds.height, primeBgColor);
            }

            Graphics2D graphics = getGraphics();
            Paint previousPaint = graphics.getPaint();
            graphics.setPaint(bgColor);
            graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

            // reset to previous paint
            graphics.setPaint(previousPaint);
        }
    }
}
