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

package org.netbeans.modules.uml.diagrams.nodes.usecase;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.OvalWidget;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.Selectable;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author jyothi
 */
public class UseCaseWidget extends UMLNodeWidget
{

    protected static ResourceBundle bundle = NbBundle.getBundle(UseCaseWidget.class);
    public static int USECASE_DEFAULT_WIDTH = 90;
    public static int USECASE_DEFAULT_HEIGHT = 60;
    public static String SHOW_EXTENSION_POINTS = "ShowExtensionPoints";
    private Widget currentView;
    private Scene scene;
    private Widget usecaseWidget;
    private OvalWidget ovalWidget;
    private Widget detailWidget;
    private Widget extPtListWidget;
    private UMLNameWidget nameWidget;
    private IUseCase usecase;

    public UseCaseWidget(Scene scene)
    {
        super((Scene) scene);
        this.scene = scene;
        addToLookup(initializeContextPalette());
    }

    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/UseCase");
        return paletteModel;
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        initUseCaseWidget();
        IElement element = presentation.getFirstSubject();
        if (element instanceof IUseCase)
        {
            usecase = (IUseCase) presentation.getFirstSubject();
            currentView = createSimpleUseCaseView(usecase);
            setCurrentView(currentView);
//            setFont(getCurrentView().getFont());
        }
        super.initializeNode(presentation);
    }

    private void initUseCaseWidget()
    {
        usecaseWidget = new Widget(scene);
        ovalWidget = new OvalWidget(scene, USECASE_DEFAULT_WIDTH, USECASE_DEFAULT_HEIGHT, getResourcePath(), bundle.getString("LBL_body"));
        detailWidget = new Widget(getScene());
        detailWidget.setForeground(null);
        detailWidget.setBackground(null);
        extPtListWidget = new Widget(getScene());
        extPtListWidget.setForeground(null);
        extPtListWidget.setBackground(null);
    }

    private Widget createSimpleUseCaseView(IUseCase usecase)
    {
        createFullUseCaseView(usecase);
        if (getExtensionPointCount() > 0)
            showDetail(true);
        else
            showDetail(false);
        return usecaseWidget;
    }

    private Widget createFullUseCaseView(IUseCase usecase)
    {
        usecaseWidget.setLayout(LayoutFactory.createOverlayLayout());

        ovalWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        ovalWidget.setUseGradient(useGradient);
        ovalWidget.setOpaque(true);

        nameWidget = new UMLNameWidget(scene, false, getWidgetID());
//        setStaticText(nameWidget, pElement.getFirstSubject());
        setStaticText(nameWidget, usecase);
        nameWidget.initialize(usecase);

        ovalWidget.addChild(nameWidget);

        detailWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
        detailWidget.addChild(new ExtensionPointSeparator(scene));
        UMLLabelWidget extPtLabel = new UMLLabelWidget(scene,
                NbBundle.getMessage(UseCaseWidget.class, "LBL_ExtensionPoints"), // NOI18N
                getWidgetID() + "." + "extensionPoint", 
                NbBundle.getMessage(UseCaseWidget.class, "LBL_ExtensionPoint_Label")); // NOI18N
//        extPtLabel.setForeground(null);
        extPtLabel.setAlignment(LabelWidget.Alignment.CENTER);
        extPtLabel.setBorder(BorderFactory.createEmptyBorder(5));

        detailWidget.addChild(extPtLabel);

        updateDetails();

        detailWidget.addChild(extPtListWidget);
        ovalWidget.addChild(detailWidget);
        usecaseWidget.addChild(ovalWidget);

        return usecaseWidget;
    }

    private void setStaticText(UMLNameWidget nameWidget, IElement element)
    {
        if (element instanceof IPartFacade)
        {
            String sStaticText = "<<role>>";
            nameWidget.setStaticText(sStaticText);            
        }
    }

    private void showDetail(boolean show)
    {
        detailWidget.setVisible(show);
        if(show == true)
        {
            ovalWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        }
        else
        {
            ovalWidget.setLayout(new CenterWidgetLayout());
        }
    }

    public boolean isDetailVisible()
    {
        return detailWidget.isVisible();
    }

    public void setDetailVisible(boolean visible)
    {
        showDetail(visible);
    }

    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.USECASEWIDGET.toString();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        if (!event.getSource().equals(usecase))
        {
            return;
        }
        String propName = event.getPropertyName();
        //usecase name / stereotype change
        if (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString())
                || (propName.equals(ModelElementChangedKind.STEREOTYPE.toString())))
        {
            if (nameWidget != null)
            {
                nameWidget.propertyChange(event);
            }
        }    //taggedvalue change    
        else if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
        {
            String taggedValues = usecase.getTaggedValuesAsString();
            if (taggedValues.length() > 0)
            {
                nameWidget.propertyChange(event);
            }
            //extension points        
            updateDetails();
        }
        updateSizeWithOptions();
    }

    private void updateDetails()
    {
        
        extPtListWidget.removeChildren();
        extPtListWidget.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, 0));
        if (usecase != null)
        {
            if (usecase.getExtensionPoints().size() > 0)
            {
                showDetail(true);
            }
            else 
            {
                showDetail(false);
            }
            for (IExtensionPoint extPt : usecase.getExtensionPoints())
            {
                addExtensionPoint(extPt);     
            }
        }
    }

    private void addExtensionPoint(IExtensionPoint extPt)
    {
        if (extPt != null)
        {
            ExtensionPointWidget widget = new ExtensionPointWidget(getScene(), extPt);
            widget.setForeground(null);
            extPtListWidget.addChild(widget);
            Selectable selectable = widget.getLookup().lookup(Selectable.class);
            if (selectable != null) 
            {
                selectable.select(widget);
            }
        }
    }
    
    public int getExtensionPointCount()
    {
        int retVal = -1;
        if (usecase != null)
        {
            retVal = usecase.getExtensionPoints().size();
        }
        return retVal;
    }

    @Override
    public void save(NodeWriter nodeWriter)
    {
        //we need to save the property for ext pt visibility
        HashMap map = nodeWriter.getProperties();
        map.put(SHOW_EXTENSION_POINTS, isDetailVisible());
        nodeWriter.setProperties(map);
        super.save(nodeWriter);
    }

    @Override
    public void load(NodeInfo nodeReader)
    {
        if (nodeReader != null)
        {
            Object showExtPt = nodeReader.getProperties().get(SHOW_EXTENSION_POINTS);
            if (showExtPt != null)
            {
                setDetailVisible(Boolean.parseBoolean(showExtPt.toString()));
                if (isDetailVisible())
                {
                    showDetail(true);
                }
                else
                {
                    showDetail(false);
                }
            }
        }
        super.load(nodeReader);
    }
    
    
    public void duplicate(boolean setBounds, Widget target)
    {
        assert target instanceof UseCaseWidget;
        
        super.duplicate(setBounds, target);
        ((UseCaseWidget)target).showDetail(isDetailVisible());
    }
    
    public class ExtensionPointSeparator extends SeparatorWidget
    {
        public ExtensionPointSeparator(Scene scene)
        {
            super(scene, SeparatorWidget.Orientation.HORIZONTAL);
        }

        @Override
        protected void paintWidget()
        {
            // I need to use the oval bounds to calculate the ellipse that will
            // correctly bound the ellipse.  
            // 
            // Since the widget paint method first transforms the coordinate system
            // to be expressed in the separator coordinates, I need the x, and y
            // to be in the separators coordinate system.  Therefore the x, and 
            // y will be negitive numbers.
            Rectangle bounds = ovalWidget.getClientArea();
            Point ovalScreenLocation = ovalWidget.convertLocalToScene(ovalWidget.getClientArea()).getLocation();
            Point myScreenLocation = convertLocalToScene(getClientArea()).getLocation();
            Ellipse2D.Float ellipse = new Ellipse2D.Float(ovalScreenLocation.x - myScreenLocation.x,
                                                          ovalScreenLocation.y - myScreenLocation.y,
                                                          bounds.width,
                                                          bounds.height);
            
            
            
            Graphics2D graphics = getGraphics();
            
            Shape curClip = graphics.getClip();
            
            graphics.setClip(ellipse);
            super.paintWidget();
            
            graphics.setClip(curClip);
        }
        
        
    }
    
    public class CenterWidgetLayout implements Layout
    {

        public void layout(Widget widget)
        {
            List<Widget> children = widget.getChildren();

            int y = 0;
            for (Widget child : children)
            {
                Rectangle childBounds = child.getPreferredBounds();
                if(child.isVisible() == true)
                {
                    child.resolveBounds(new Point(0, y), childBounds);
                    y += childBounds.height;
                }
                else
                {
                    child.resolveBounds(new Point(0, y), new Rectangle(childBounds.x, childBounds.y, 0, 0));
                }
            }
        }

        public boolean requiresJustification(Widget widget)
        {
            return true;
        }

        public void justify(Widget widget)
        {
            List<Widget> children = widget.getChildren();

            int totalHeight = 0;
            for (Widget child : children)
            {
                if(child.isVisible() == true)
                {
                    Rectangle childBounds = child.getClientArea();
                    totalHeight += childBounds.height;
                }
            }

            Rectangle bounds = widget.getClientArea();

            // For some reason the top left corner is adjusted is adjusted 
            // to negitive numbers.
            // 
            // Therefore counter act that value.
            int x = bounds.x;
            int y = bounds.y;
            
            // Get the top location.
            y += (bounds.height - totalHeight) / 2;
            
            for (Widget child : children)
            {

                Rectangle childBounds = child.getPreferredBounds();
                Rectangle newBounds = new Rectangle(childBounds);
                newBounds.width = bounds.width;
                if(child.isVisible() == true)
                {
                    child.resolveBounds(new Point(x, y), 
                                        new Rectangle(new Point(0,0), 
                                                      bounds.getSize())); 
                    y += childBounds.height;
                }
                else
                {
                    newBounds = new Rectangle(newBounds.x, newBounds.y, 0, 0);
                    child.resolveBounds(new Point(-childBounds.x, y), newBounds);
                }
            }
        }
    }
    
}
