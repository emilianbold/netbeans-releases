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
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.diagrams.UMLRelationshipDiscovery;
import org.netbeans.modules.uml.diagrams.nodes.CompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.CompositeNodeWidget;
import org.netbeans.modules.uml.diagrams.nodes.ContainerNode;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Thuy
 */
public class ActivityPartitionWidget extends CompositeNodeWidget
{

    private Scene scene;
    private IActivityPartition parentPartition;
    private UMLNameWidget nameWidget;
    private Widget partitionPanel;
    private static ResourceBundle bundle = NbBundle.getBundle(ContainerNode.class);
    private SeparatorWidget.Orientation orientation = SeparatorWidget.Orientation.HORIZONTAL;
    private ArrayList<IElement> elements = new ArrayList<IElement>();
    private ArrayList<CompartmentWidget> compartmentWidgets = new ArrayList<CompartmentWidget>();

    public ActivityPartitionWidget(Scene scene)
    {
        super(scene);
        this.scene = scene;
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        assert presentation != null;

        parentPartition = (IActivityPartition) presentation.getFirstSubject();
        if (!isInitialized())
        {
            setCurrentView(createActivityPartitionView(parentPartition));
        } else   // sync diagram is invoked
        {
            populatePartitions(parentPartition);
        }

        setFont(getCurrentView().getFont());
        super.initializeNode(presentation);
    }

    private Widget createActivityPartitionView(IActivityPartition partitionElement)
    {
        //create main view 
        MainViewWidget mainView = new MainViewWidget(scene,
                                                     getResourcePath(),
                                                     bundle.getString("LBL_body"));
        
        mainView.setLayout(
                LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 1));

        mainView.setBorder(BorderFactory.createLineBorder(2));
        mainView.setUseGradient(useGradient);
        mainView.setCustomizableResourceTypes(
                new ResourceType[]{ResourceType.BACKGROUND, ResourceType.FONT});
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
        partitionPanel.setForeground(null);
        
        setOrientation(orientation); 
        if (!PersistenceUtil.isDiagramLoading())
        {
            initializeSubPartitions(partitionElement);
            populatePartitions(partitionElement);
        }
        mainView.addChild(partitionPanel, 1);

        setIsInitialized(true);
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
        if (orientation == SeparatorWidget.Orientation.HORIZONTAL)
        {
            partitionPanel.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        } else
        {
            partitionPanel.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        }
        for (CompartmentWidget widget : compartmentWidgets)
        {
            widget.updateOrientation(orientation == SeparatorWidget.Orientation.HORIZONTAL);
        }
    }


    public void addSubPartition(SubPartitionWidget subPartWidget)
    {
        if (subPartWidget == null)
        {
            return;
        }

        compartmentWidgets.add(subPartWidget);
        partitionPanel.addChild(subPartWidget);
        updateDividers();
        setFont(getFont());
        revalidate();
        scene.validate();
    }


    public void removeCompartment(CompartmentWidget subPartWidget)
    {
        if (subPartWidget != null)
        {
            if (scene instanceof ObjectScene)
            {
                compartmentWidgets.remove(subPartWidget);
                updateDividers();
            }
            if ( compartmentWidgets.size() == 0)
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
                    ((SubPartitionWidget) w).showSeparator(i != (count - 1));
                    ((SubPartitionWidget) w).getNameWidget().setVisible(count > 1);
                }
            }
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event.getSource() instanceof IActivityPartition)
        {
            nameWidget.propertyChange(event);
            getScene().validate();
            setPreferredBounds(getBounds().union(calculateMinimumBounds()));
            revalidate();
        }
    }

    private void initializeSubPartitions(IActivityPartition parentPartition)
    {
        if (parentPartition != null)
        {
            ETList<IActivityPartition> partitions = parentPartition.getSubPartitions();
            if (partitions != null && partitions.size() > 0)
            {
                for (int i = 0; i < partitions.size(); i++)
                {
                    addCompartment(partitions.get(i));
                }
            }
            else // there's no subPartition, create one
            {
                TypedFactoryRetriever<IActivityPartition> ret = new TypedFactoryRetriever<IActivityPartition>();
                IActivityPartition subPartition = ret.createType("ActivityPartition");
                parentPartition.addSubPartition(subPartition);
                addCompartment(subPartition);              
            }
        }
    }
    
    private void populatePartitions(IActivityPartition parentPartition)
    {
        if (parentPartition != null)
        {                        
            for (CompartmentWidget widget: compartmentWidgets)
            {
                widget.initContainedElements();
            }
        }
        for (IActivityPartition par: parentPartition.getSubPartitions())
        {
            elements.addAll(par.getNodeContents());
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

    public IActivityPartition getElement()
    {
        return parentPartition;
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
        if ((elt != null) && (elt instanceof IActivityPartition)) 
        {
            if ((elt.getOwner() instanceof IActivity
                    || elt.getOwner() instanceof IPackage)
                     && (findCompartmentWidget(elt) == null))//last condition is for partition with NO sub-partitions
            {
                String or = nodeReader.getProperties().get("Orientation").toString();
                this.setOrientation(SeparatorWidget.Orientation.valueOf(or));
                initializeSubPartitions((IActivityPartition)elt);
                super.load(nodeReader);                
            }
            CompartmentWidget subPart = findCompartmentWidget(elt);
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
            setForeground(null);
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
    
    
        @Override
        public void notifyAdded()
        {
            if(!PersistenceUtil.isDiagramLoading())
            {
                UMLRelationshipDiscovery relationshipD = new UMLRelationshipDiscovery((GraphScene) scene);
                relationshipD.discoverCommonRelations(elements);
            }
        }
    }

    public Collection<CompartmentWidget> getCompartmentWidgets()
    {
        return compartmentWidgets;
    }


    @Override
    public String getContextPalettePath()
    {
        return "UML/context-palette/Activity";
    }

    @Override
    public UMLNameWidget getNameWidget()
    {
        return nameWidget;
    }

    @Override
    public CompartmentWidget addCompartment(IElement element)
    {
        assert element instanceof IActivityPartition;
        IActivityPartition subPart = (IActivityPartition)element;
        SubPartitionWidget subPartWidget = createSubPartitionWidget(subPart);
        addSubPartition(subPartWidget);
        return subPartWidget;
    }
}
