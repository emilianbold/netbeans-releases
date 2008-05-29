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
package org.netbeans.modules.uml.diagrams.nodes.activity;

import java.awt.BasicStroke;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.ResourceBundle;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.SeparatorWidget.Orientation;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.state.CompartmentSeparatorWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Thuy
 */
public class SubPartitionWidget extends Widget implements PropertyChangeListener
{
    private Scene scene;
    private IActivityPartition subPartition;
    private ActivityPartitionWidget parentWidget;
    private Widget nameLayer;
    private EditableCompartmentWidget nameWidget;
    private Widget containerLayer;
    private ContainerWidget containerWidget;
    private CompartmentSeparatorWidget separatorWidget;
    private boolean selected = false;
    private static final int STROKE_THICKNESS = 1;
    private static BasicStroke STROKE = new BasicStroke(STROKE_THICKNESS,
                                                          BasicStroke.CAP_BUTT,
                                                          BasicStroke.JOIN_ROUND,
                                                          1.0f,
                                                          new float[]{5, 5}, 0);
    private static ResourceBundle bundle = NbBundle.getBundle(EditableCompartmentWidget.class);

    public SubPartitionWidget(Scene scene, IActivityPartition subPart,
                               ActivityPartitionWidget actPartitionWidget)
    {
        super(scene);
        this.scene = scene;
        subPartition = subPart;
        parentWidget = actPartitionWidget;
        initializeNode();

        WidgetAction.Chain selectTool = createActions(DesignerTools.SELECT);
        selectTool.addAction(ActionFactory.createSelectAction(selectProvider, true));
        getActions().addAction(ActionFactory.createResizeAction(resizeStrategy,
                                                              new RegionResizeControlPointResolver(),
                                                              resizeProvider));
    }

    private void initializeNode()
    {
      
        Widget subPartitionPanel = new Widget(scene);
        subPartitionPanel.setLayout(LayoutFactory.createOverlayLayout());
        
        // name layer
        nameLayer = new Widget(scene);
        nameLayer.setBorder(BorderFactory.createEmptyBorder(2));
        nameLayer.setLayout(
                LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 0));
        nameWidget = new EditableCompartmentWidget(scene,
                                                   getWidgetID() + ".name",
                                                   bundle.getString("LBL_name"));
        nameWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
        nameWidget.setVerticalAlignment(UMLLabelWidget.VerticalAlignment.TOP);
        if (subPartition != null)
        {
            nameWidget.setLabel(subPartition.getNameWithAlias());
        }
        nameLayer.addChild(nameWidget);
        subPartitionPanel.addChild(nameLayer);

        // container layer
        containerLayer = new Widget(scene);
        containerLayer.setBorder(BorderFactory.createEmptyBorder(2));
        containerLayer.setLayout(LayoutFactory.createOverlayLayout());
        
        containerWidget = new ContainerWidget(scene);
        // hover action to highlight the container when mouse is over it
        WidgetAction hoverAction = ActionFactory.createHoverAction(hoverProvider);
        containerWidget.getActions().addAction(hoverAction);
        scene.getActions().addAction(hoverAction);
        
        containerLayer.addChild(containerWidget);
        subPartitionPanel.addChild(containerLayer);

        Orientation orientation = parentWidget.getOrientation();
        if (parentWidget.isVerticalLayout())
        {
            setLayout(LayoutFactory.createVerticalFlowLayout(
                      LayoutFactory.SerialAlignment.JUSTIFY, 0));
        } else
        {
            setLayout(LayoutFactory.createHorizontalFlowLayout(
                      LayoutFactory.SerialAlignment.JUSTIFY, 0));
        }
        separatorWidget = new CompartmentSeparatorWidget(scene,
                                                         orientation, STROKE,
                                                         STROKE_THICKNESS);
        addChild(subPartitionPanel, 1);
        addChild(separatorWidget, 0);
    }

    public void showPartitionDivider(boolean show)
    {
        if (separatorWidget != null)
        {
            separatorWidget.setVisible(show);
        }
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public String getLabel()
    {
        return nameWidget.getLabel();
    }

    public void enableShowName(boolean enable)
    {
        if (nameWidget != null)
        {
            nameWidget.setEnabled(enable);
        }
    }

    
    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.SUBPARTITIONWIDGET.toString();
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();

        if (element instanceof IActivityPartition)
        {
            IActivityPartition partitionElem = (IActivityPartition) element;
            if (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()) ||
               propName.equals(ModelElementChangedKind.ALIAS_MODIFIED.toString()) )
            {
                String newName = partitionElem.getNameWithAlias();
                String oldName = nameWidget.getLabel();
                if ( newName != null && !newName.equals(oldName) )
                {
                    nameWidget.setLabel(newName);
                    parentWidget.revalidate();
                    getScene().revalidate();
                }
            }
        }
    }

    private class RegionResizeControlPointResolver implements ResizeControlPointResolver
    {

        public ResizeProvider.ControlPoint resolveControlPoint(Widget widget,
                                                                Point point)
        {
            Rectangle bounds = widget.getBounds();
            if (point.y >= (bounds.y + bounds.height - 1 - STROKE_THICKNESS) && point.y < (bounds.y + bounds.height + 1))
            {
                if (point.x >= bounds.x && point.x < bounds.x + bounds.width)
                {
                    return ResizeProvider.ControlPoint.BOTTOM_CENTER;
                }
            } else
            {
                if (point.x >= (bounds.x + bounds.width - 1 - STROKE_THICKNESS) && point.x < (bounds.x + bounds.width + 1))
                {
                    if (point.y >= bounds.y && point.y < (bounds.y + bounds.height))
                    {
                        return ResizeProvider.ControlPoint.CENTER_RIGHT;
                    }
                }
            }
            
            return null;
        }
    }

    private Rectangle calculateMinimumBounds()
    {
        Insets insets = getBorder().getInsets();
        Rectangle clientArea = new Rectangle();

        clientArea.add(calculateMinimumBounds(nameLayer));
        clientArea.add(calculateMinimumBounds(containerLayer));
        clientArea.add(calculateMinimumBounds(separatorWidget));
        clientArea.x -= insets.left;
        clientArea.y -= insets.top;
        clientArea.width += insets.left + insets.right;
        clientArea.height += insets.top + insets.bottom;
        return clientArea;
    }

    private Rectangle calculateMinimumBounds(Widget widget)
    {
        Insets insets = widget.getParentWidget().getBorder().getInsets();
        Rectangle clientArea = new Rectangle();
        for (Widget child : widget.getChildren())
        {
            if (!child.isVisible())
            {
                continue;
            }

            Point location = child.getLocation();
            Rectangle bounds = child.getBounds();
            bounds.translate(location.x, location.y);
            clientArea.add(bounds);
        }
        clientArea.translate(widget.getLocation().x, widget.getLocation().y);
        clientArea.x -= insets.left;
        clientArea.y -= insets.top;
        clientArea.width += insets.left + insets.right;
        clientArea.height += insets.top + insets.bottom;
        return clientArea;
    }
    
    private ResizeStrategy resizeStrategy = new ResizeStrategy()
    {
        public Rectangle boundsSuggested(
                Widget widget,
                Rectangle originalBounds,
                Rectangle suggestedBounds,
                ResizeProvider.ControlPoint controlPoint)
        {
            Rectangle mininumBounds = calculateMinimumBounds();
            int width = Math.max(mininumBounds.width + 1, suggestedBounds.width);
            int height = Math.max(mininumBounds.height + 1,
                                  suggestedBounds.height);

            if (parentWidget.isVerticalLayout())
            {
                return new Rectangle(suggestedBounds.x, suggestedBounds.y,
                                     suggestedBounds.width, height);
            }

            return new Rectangle(suggestedBounds.x, suggestedBounds.y, width,
                                 suggestedBounds.height);
        }
    };
    
    private ResizeProvider resizeProvider = new ResizeProvider()
    {

        public void resizingStarted(Widget widget)
        {
        }

        public void resizingFinished(Widget widget)
        {
            parentWidget.revalidate();
            widget.getScene().validate();
        }
    };
    
    private TwoStateHoverProvider hoverProvider = new TwoStateHoverProvider()
    {
        public void unsetHovering(Widget widget)
        {
            if (widget != null)
            {
                widget.setBorder(BorderFactory.createEmptyBorder(1));
            }
        }

        public void setHovering(Widget widget)
        {
            if (widget != null)
            {
                widget.setBorder(BorderFactory.createLineBorder(1, UMLWidget.BORDER_HILIGHTED_COLOR));
            }

        }
    };
    
    private SelectProvider selectProvider = new SelectProvider()
    {

        public boolean isAimingAllowed(Widget widget, Point localLocation,
                                        boolean invertSelection)
        {
            return false;
        }

        public boolean isSelectionAllowed(Widget widget, Point localLocation,
                                           boolean invertSelection)
        {
            boolean retVal = widget instanceof SubPartitionWidget;
            return (retVal && ((ObjectScene) widget.getScene()).findObject(widget) != null);
        }

        public void select(Widget widget, Point localLocation,
                            boolean invertSelection)
        {
            if (widget instanceof SubPartitionWidget)
            {
                ObjectScene oScene = (ObjectScene) widget.getScene();
                SubPartitionWidget subPartWidget = (SubPartitionWidget) widget;
                ObjectState parentState = subPartWidget.parentWidget.getState();
                if (!parentState.isSelected())
                {
                    // select parent first
                    Object object = oScene.findObject(parentWidget);
                    oScene.setFocusedObject(object);
                    if (object != null)
                    {
                        if (invertSelection || !oScene.getSelectedObjects().contains(object))
                        {
                            oScene.userSelectionSuggested(Collections.singleton(object),
                                                          invertSelection);
                        }
                    } else
                    {
                        oScene.userSelectionSuggested(Collections.emptySet(),
                                                      invertSelection);
                    }
                }
                // deselect other SubPartitionWidgets
                subPartWidget.parentWidget.deselectSubPartitionWidgets();
                // flag this subpartitionWidget selected
                subPartWidget.setSelected(true);
            }
        }
    };
}
