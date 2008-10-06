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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;

/**
 *
 * @author Sheryl Su
 */
public abstract class CompartmentWidget extends Widget implements PropertyChangeListener, DiagramNodeWriter, DiagramNodeReader
{

    private UMLNameWidget nameWidget;
    private Scene scene;
    private static final int STROKE_THICKNESS = 1;
    private BasicStroke stroke =
            new BasicStroke(STROKE_THICKNESS, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
            1.0f, new float[]{5, 5}, 0);
    private boolean selected;
    private CompartmentSeparatorWidget separatorWidget;

    private Rectangle mininumBounds;
    private int BORDER_THICKNESS = 1;
    private ResizeStrategy RESIZE_STRATEGY;
    private ContainerWidget containerWidget;
    private IElement element;
    private CompositeNodeWidget compositeWidget;
    
    public CompartmentWidget(Scene scene, IElement element, CompositeNodeWidget compositeWidget)
    {
        super(scene);
        setForeground(null);
        setBackground(null);
        this.scene = scene;
        this.element = element;
        this.compositeWidget = compositeWidget;
        init(element);
        WidgetAction.Chain selectTool = createActions(DesignerTools.SELECT);
        selectTool.addAction(ActionFactory.createResizeAction(RESIZE_STRATEGY,
                new RegionResizeControlPointResolver(), RESIZE_PROVIDER));
    }

    protected void init(IElement element)
    {
        Widget layer = new Widget(scene);
        layer.setForeground(null);
        layer.setBackground(null);

        layer.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        nameWidget = new UMLNameWidget(scene, false, getWidgetID());
        nameWidget.initialize(element);

        layer.addChild(nameWidget, 0);
        containerWidget = getContainerWidget();

        layer.addChild(containerWidget, 1);

        boolean horizontal = isHorizontalLayout();
        if (horizontal)
        {
            setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        } else
        {
            setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        }
        separatorWidget = new CompartmentSeparatorWidget(scene,
                horizontal ? SeparatorWidget.Orientation.VERTICAL : SeparatorWidget.Orientation.HORIZONTAL, stroke, BORDER_THICKNESS);
        addChild(layer, 1);
        addChild(separatorWidget, 0);
        
        setFont(getFont());

        RESIZE_STRATEGY = new ResizeStrategy()
        {

            public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ResizeProvider.ControlPoint controlPoint)
            {
                int width = Math.max(mininumBounds.width + separatorWidget.calculateClientArea().width, suggestedBounds.width );
                int height = Math.max(mininumBounds.height + separatorWidget.calculateClientArea().height , suggestedBounds.height);

                if (isHorizontalLayout())
                {
                    return new Rectangle(suggestedBounds.x, suggestedBounds.y, width, suggestedBounds.height);
                }
                return new Rectangle(suggestedBounds.x, suggestedBounds.y, suggestedBounds.width, height);
            }
        };
    }

    public abstract List<IElement> getContainedElements();

    public abstract String getWidgetID();

    public abstract ContainerWidget getContainerWidget();

    private boolean isHorizontalLayout()
    {
        return compositeWidget.getOrientation() == SeparatorWidget.Orientation.HORIZONTAL;
    }

    public void updateOrientation(boolean horizontal)
    {
        if (separatorWidget != null)
        {
            separatorWidget.removeFromParent();
        }
        if (horizontal)
        {
            setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        } else
        {
            setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        }
        separatorWidget = new CompartmentSeparatorWidget(scene,
                horizontal ? SeparatorWidget.Orientation.VERTICAL : SeparatorWidget.Orientation.HORIZONTAL, stroke, BORDER_THICKNESS);
        addChild(separatorWidget, 0);
        scene.validate();
    }

    public void setSelected(boolean val)
    {
        selected = val;
    }

    public boolean isSelected()
    {
        return selected;
    }

    private class RegionResizeControlPointResolver implements ResizeControlPointResolver
    {

        public ResizeProvider.ControlPoint resolveControlPoint(Widget widget, Point point)
        {
            Rectangle bounds = widget.getBounds();

            if (point.y >= bounds.y + bounds.height - BORDER_THICKNESS - STROKE_THICKNESS - 2 && point.y < bounds.y + bounds.height + 2)
            {
                if (point.x >= bounds.x && point.x < bounds.x + bounds.width)
                {
                    return ResizeProvider.ControlPoint.BOTTOM_CENTER;
                }
            } else if (point.x >= bounds.x + bounds.width - BORDER_THICKNESS - STROKE_THICKNESS - 2 && point.x < bounds.x + bounds.width + 2)
            {
                if (point.y >= bounds.y && point.y < bounds.y + bounds.height)
                {
                    return ResizeProvider.ControlPoint.CENTER_RIGHT;
                }
            }
            return null;
        }
    }
    
    public Rectangle calculateMinimumBounds()
    {
        Insets insets = getBorder().getInsets();
        Rectangle clientArea = new Rectangle();

        clientArea.add(getNameWidget().getPreferredBounds());
        clientArea.add(calculateMinimumBounds(getContainerWidget()));

        clientArea.x -= insets.left;
        clientArea.y -= insets.top;
        clientArea.width += insets.left + insets.right;
        clientArea.height += insets.top + insets.bottom;

        return clientArea;
    }

    private Rectangle calculateMinimumBounds(Widget widget)
    {
        Insets insets = getBorder().getInsets();
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

    public UMLNameWidget getNameWidget()
    {
        return nameWidget;
    }

    public IElement getElement()
    {
        return element;
    }

    public void showSeparator(boolean show)
    {
        if (separatorWidget != null)
        {
            separatorWidget.setVisible(show);
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
        {
            nameWidget.propertyChange(evt);
            updateSize();
        } else if (evt.getPropertyName().equals(ModelElementChangedKind.DELETE.toString()) ||
                evt.getPropertyName().equals(ModelElementChangedKind.PRE_DELETE.toString()))
        {
            compositeWidget.removeCompartment(this);
            updateSize();
        }
    }
    
    private void updateSize()
    {
        setPreferredBounds(null);
        setPreferredSize(null);
        setMinimumSize(null);

        compositeWidget.setPreferredBounds(null);
        compositeWidget.setPreferredSize(null);
        compositeWidget.revalidate();
    }

    private UMLNodeWidget getParentNodeWidget(Widget widget)
    {
        Widget parent = widget.getParentWidget();
        if (parent == null)
            return null;
        if (parent instanceof UMLNodeWidget)
        {
            return (UMLNodeWidget)parent;
        }
        return getParentNodeWidget(parent);
    }
    

    public void remove(Widget w)
    {
        List<Widget> children = new ArrayList<Widget>();
        children.addAll(w.getChildren());
        for (Widget child: children)
        {
            if (scene instanceof GraphScene)
            {
                Object obj = ((GraphScene)scene).findObject(child);
                if (((GraphScene)scene).isNode(obj))
                {
                    ((GraphScene)scene).removeNodeWithEdges(obj);
                }              
            }
            remove(child);
        }
        w.removeFromParent();
    }
    
    public void save(NodeWriter nodeWriter)
    {
        setNodeWriterValues(nodeWriter, this);
        nodeWriter.beginGraphNodeWithModelBridge();
        nodeWriter.beginContained();
        //write contained
        saveChildren(this, nodeWriter);
        nodeWriter.endContained();
        nodeWriter.endGraphNode();
    }

    public void saveChildren(Widget widget, NodeWriter nodeWriter)
    {
        if (widget == null || nodeWriter == null)
        {
            return;
        }
        List<Widget> widList = widget.getChildren();
        for (Widget child : widList)
        {
            if ((child instanceof DiagramNodeWriter) && !(child instanceof Widget.Dependency))
            { // we write dependencies in another section

                ((DiagramNodeWriter) child).save(nodeWriter);
            } else
            {
                saveChildren(child, nodeWriter);
            }
        }
    }

    protected void setNodeWriterValues(NodeWriter nodeWriter, Widget widget)
    {
        nodeWriter = PersistenceUtil.populateNodeWriter(nodeWriter, widget);
        nodeWriter.setHasPositionSize(true);
        PersistenceUtil.populateProperties(nodeWriter, widget);
    }

    public void addContainedChild(Widget widget)
    {
        widget.removeFromParent();
        getContainerWidget().addChild(widget);
    }

    public void load(NodeInfo nodeReader)
    {
        //we don't have anything to do here..
    }

    public void loadDependencies(NodeInfo nodeReader)
    {
        // do nothing
    }
    private final ResizeProvider RESIZE_PROVIDER = new ResizeProvider()
    {

        public void resizingStarted(Widget widget)
        {
            mininumBounds = calculateMinimumBounds();
        }

        public void resizingFinished(Widget widget)
        {
            compositeWidget.setPreferredBounds(null);
            compositeWidget.setPreferredSize(null);         
            compositeWidget.setMinimumSize(compositeWidget.getBounds().getSize());
            compositeWidget.revalidate();
        }
        
    };

    @Override
    protected void notifyFontChanged(Font font) 
    {
        if(font==null || nameWidget==null)return;
        nameWidget.setNameFont(font);
        revalidate();
    }
    
    
    public void initContainedElements()
    {
        if (!(getScene() instanceof GraphScene))
        {
            return;
        }
 
        Point point = new Point(10,10);
        for (IElement e : getContainedElements())
        {
            boolean found = false;
            List<Widget> list = getContainerWidget().getChildren();
            List<Widget> children = new ArrayList<Widget>(list);
            for (Widget child: children)
            {
                Object object = ((DesignerScene)getScene()).findObject(child);
                assert object instanceof IPresentationElement;
                if (((IPresentationElement)object).getFirstSubject() == e)
                {
                    ((UMLNodeWidget)child).initializeNode((IPresentationElement)object);
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                IPresentationElement presentation = Util.createNodePresentationElement();
                presentation.addSubject(e);

                Widget w = ((DesignerScene) getScene()).addNode(presentation);
                if (w != null)
                {
                    w.removeFromParent();
                    getContainerWidget().addChild(w);
                    w.setPreferredLocation(point);
                    point = new Point(point.x + 50, point.y + 50);
                }
            }
        }
    }

    
}
