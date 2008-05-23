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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.datatransfer.Transferable;
import java.util.HashMap;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.drawingarea.view.SwitchableWidget;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Paint; 
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.util.List;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.widgets.ListWidget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.diagrams.actions.ClassifierSelectAction;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.openide.util.NbBundle;


public class UMLClassWidget  extends SwitchableWidget
{
    private UMLNameWidget nameWidget = null;
    
    private ElementListWidget members;
    private ElementListWidget operations;
    private TemplateWidget parameterWidget = null;
    private Widget classView = null;
    private Widget attributeSection = null;
    
    private HashMap <String, ElementListWidget > operationRedefinedMap = 
            new HashMap <String, ElementListWidget >();
    
    private HashMap <String, ElementListWidget > attributeRedefinedMap = 
            new HashMap <String, ElementListWidget >();
    
    public UMLClassWidget(Scene scene)
    {
        this(scene, "Class");
    }
    
     public UMLClassWidget(Scene scene, IPresentationElement element)
    {
        this(scene, "Class");
        initializeNode(element);
    }
     
    public UMLClassWidget(Scene scene, String metatype)
    {
        super(scene, metatype, true);
        
        WidgetAction.Chain actions = createActions(DesignerTools.SELECT);
        
        addToLookup(initializeContextPalette());
        addToLookup(new DefaultWidgetContext("Class"));
//        addToLookup(new ClassifierSelectAction());
    }

    @Override
    public Widget createDefaultWidget(IPresentationElement element)
    {
        IClassifier clazz = (IClassifier) element.getFirstSubject();
        return initializeContents(clazz);
    }
    
    public void removingView()
    {
        if (members != null)
            members.removeFromParent();
        if (operations != null)
            operations.removeFromParent();
        if (parameterWidget != null)
            parameterWidget.removeFromParent();
        if (classView != null)
            classView.removeFromParent();
        
        classView = null;
        members = null;
        operations = null;
        parameterWidget = null;

        getScene().validate();
    }

    protected Widget initializeContents(IClassifier clazz)
    {
        Widget retVal = classView;
        setBackground(null);
        
        boolean viewRequireUpdate = initializeClassView(clazz);
        boolean paramRequireUpdate = initializeParameterWidget(clazz);

        if ((viewRequireUpdate == true) || (paramRequireUpdate == true))
        {
            if(classView.getParentWidget() != null)
            {
                classView.getParentWidget().removeChild(classView);
            }
            
            if((parameterWidget != null) && (parameterWidget.getParentWidget() != null))
            {
                parameterWidget.getParentWidget().removeChild(parameterWidget);
            }
            
            if (parameterWidget != null)
            {
                retVal = new Widget(getScene());
                retVal.setForeground((Color)null);
                retVal.setLayout(new TemplateWidgetLayout());
                retVal.addChild(classView);
                retVal.addChild(parameterWidget);
            }
            else
            {
                retVal = classView;
            }
        }
        else if(parameterWidget != null)
        {
            classView.removeFromParent();
            parameterWidget.removeFromParent();
            
            retVal = new Widget(getScene());
            retVal.setForeground((Color)null);
            retVal.setLayout(new TemplateWidgetLayout());
            retVal.addChild(classView);
            retVal.addChild(parameterWidget);
        }
        
        return retVal;
    }

    
    protected boolean initializeParameterWidget(IClassifier element)
    {
        boolean retVal = false;
        
        // When the user drops a template class from the palette it will have
        // a parameter, but the parameter name will be empty.  Therefore we
        // need to count the number of template parameters, not just use the 
        // parameter string.
        
        List < IParameterableElement > params = element.getTemplateParameters();
        if((params != null) && (params.size() > 0))
        {
            if(parameterWidget == null)
            {
                parameterWidget = new TemplateWidget(getScene());
                retVal = true;
            }
            parameterWidget.updateUI(element);
        }
        else if((parameterWidget != null) && (parameterWidget.getParentWidget() != null))
        {
            parameterWidget.getParentWidget().removeChild(parameterWidget);
            parameterWidget = null;
            retVal = true;
        }
        
        return retVal;
    }
    
    protected boolean initializeClassView(IClassifier element)
    {
        boolean retVal = false;
        
        if(classView == null)
        {
            retVal = true;
            ObjectScene scene = (ObjectScene) getScene();

//            classView = new CustomizableWidget(scene, getResourcePath(), "Default", getResourceTable()); 
            classView = new Widget(scene){
                @Override
                protected void paintBackground()
                {
                    if(classView != null)
                    {
                        Paint bg = getBackground();

                        // TODO: Need to test if gradient paint preference is set.
                        if((bg instanceof Color) && (useGradient == true))
                        {
                            Rectangle bounds = getClientArea();
                            float midX = bounds.width / 2;

                            Color bgColor = (Color)bg;
                            GradientPaint paint = new GradientPaint(midX, 0, Color.WHITE,
                                                                    midX, getBounds().height, 
                                                                    bgColor);
                            Graphics2D g = getGraphics();
                            g.setPaint(paint);
                            g.fillRect(0, 0, bounds.width, bounds.height);
                        }
                        else
                        {
                            super.paintBackground();
                        }
                    }
                }
            };  
            ResourceValue.initResources(getResourcePath(), classView);
            classView.setOpaque(true);
            
            classView.setLayout(LayoutFactory.createVerticalFlowLayout());
            classView.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK));
            classView.setCheckClipping(true);

            nameWidget = new UMLNameWidget(scene, getWidgetID());
            setStaticText(nameWidget, element);
            nameWidget.initialize(element);

            classView.addChild(nameWidget);
            classView.addChild(new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL));

            // It turns out that attributes can be redefined as well.  I do not
            // think that we have a UI to allow an attribute to be redefined,
            // but we had code to show redefined attributes before, so I am 
            // adding it here.  The attribute section allows the redefined
            // compartments to be grouped with the standard attributes 
            // compartment.
            attributeSection = new Widget(scene);
            attributeSection.setForeground((Color)null);
            attributeSection.setLayout(LayoutFactory.createVerticalFlowLayout());
            
            String attrsTitle = NbBundle.getMessage(UMLClassWidget.class, 
                                                    "LBL_AttributesCompartment"); 
            members = new ElementListWidget(scene);
            members.createActions(DesignerTools.SELECT).addAction(ActionFactory.createAcceptAction(new AcceptFeatureProvider()));
            ((ListWidget) members).setLabel(attrsTitle);
            attributeSection.addChild(members);
            classView.addChild(attributeSection);
            initializeAttributes(element);

            classView.addChild(new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL));

            String opsTitle = NbBundle.getMessage(UMLClassWidget.class, 
                                                    "LBL_OperationsCompartment");
            
            operations = new ElementListWidget(scene);
            operations.createActions(DesignerTools.SELECT).addAction(ActionFactory.createAcceptAction(new AcceptFeatureProvider()));
            ((ListWidget) operations).setLabel(opsTitle);
            classView.addChild(operations);
            initializeOperations(element);
        }
        
        return retVal;
    }

    protected void initializeAttributes(IClassifier clazz)
    {
        if(clazz != null)
        {
            for(IAttribute attr : clazz.getAttributes())
            {
                addAttribute(attr);
            }
        }
    }
    
    protected void initializeOperations(IClassifier clazz)
    {
        if(clazz != null)
        {
            for(IOperation op : clazz.getOperations())
            {
                addOperation(op);
            }
        }
    }
    
    protected void addRedefinedOperation(IOperation op)
    {
        List < IRedefinableElement > redefined = op.getRedefinedElements();
        for(IRedefinableElement element : redefined)
        {
            if (element instanceof IFeature)
            {
                IFeature feature = (IFeature) element;
                IClassifier classifier = feature.getFeaturingClassifier();
                Widget list = getRedefinedOperationsCompartment(classifier);
                
                OperationWidget widget = new OperationWidget(getScene());
                widget.initialize(op);
                ResourceValue.initResources(getWidgetID() + "." + DEFAULT, widget);
                list.addChild(widget);
            }

            
        }
       
    }
    
    protected void addRedefinedAttribute(IAttribute attr)
    {
        List < IRedefinableElement > redefined = attr.getRedefinedElements();
        for(IRedefinableElement element : redefined)
        {
            if (element instanceof IFeature)
            {
                IFeature feature = (IFeature) element;
                IClassifier classifier = feature.getFeaturingClassifier();
                Widget list = getRedefinedOperationsCompartment(classifier);
                
                AttributeWidget widget = new AttributeWidget(getScene());
                widget.initialize(attr);
                list.addChild(widget);
            }

            
        }
       
    }

    protected ElementListWidget getRedefinedOperationsCompartment(IClassifier classifier)
    {
        ElementListWidget retVal = operationRedefinedMap.get(classifier.getXMIID());
        if(retVal == null)
        {
            String title = NbBundle.getMessage(UMLClassWidget.class, 
                                               "LBL_RedefinedOperations",
                                               classifier.getNameWithAlias());

            retVal = new ElementListWidget(getScene());
            retVal.setLabel(title);

            classView.addChild(new SeparatorWidget(getScene(), 
                                                   SeparatorWidget.Orientation.HORIZONTAL));
            classView.addChild(retVal);
            
            operationRedefinedMap.put(classifier.getXMIID(), retVal);
        }
        
        return retVal;
    }
    
    protected ElementListWidget getRedefinedAttributesCompartment(IClassifier classifier)
    {
        ElementListWidget retVal = attributeRedefinedMap.get(classifier.getXMIID());
        if(retVal == null)
        {
            String title = NbBundle.getMessage(UMLClassWidget.class, 
                                               "LBL_RedefinedAttributes",
                                               classifier.getNameWithAlias());

            retVal = new ElementListWidget(getScene());
            retVal.setLabel(title);

            attributeSection.addChild(new SeparatorWidget(getScene(), 
                                                   SeparatorWidget.Orientation.HORIZONTAL));
            attributeSection.addChild(retVal);
            
            attributeRedefinedMap.put(classifier.getXMIID(), retVal);
        }
        
        return retVal;
    }
    
    protected void addOperation(IOperation op)
    {
        if(op.getIsRedefined() == false)
        {
            OperationWidget widget = new OperationWidget(getScene());
            widget.initialize(op);
            operations.addChild(widget);
        }
        else
        {
           addRedefinedOperation(op);
        }
        
    }
    
    protected void removeOperation(IOperation op)
    {
        operations.removeElement(op);
    }
    
    protected void addAttribute(IAttribute attr)
    {
        
        if(attr.getIsRedefined() == false)
        {
            AttributeWidget widget = new AttributeWidget(getScene());
            ResourceValue.initResources(getWidgetID() + "." + DEFAULT, widget);
            widget.initialize(attr);
            members.addChild(widget);
        }
        else
        {
           addRedefinedAttribute(attr);
        }
        
        
    }
    
    protected void removeAttribute(IAttribute attr)
    {
        members.removeElement(attr);
    }
    
    public String getKind()
    {
        return "UML_CLASS"; //no I18N
    }

    public boolean isSelected()
    {
        boolean selected = getState().isSelected();
        return selected;
    }

    public void setSelection(boolean select)
    {
        this.setState(getState().deriveSelected(select));
    }

    private void setStaticText(UMLNameWidget nameWidget, IClassifier element)
    {
        if (element instanceof IPartFacade 
            && element instanceof IParameterableElement)
        {
            IParameterableElement pe = (IParameterableElement)element;
            String sTypeConstraint = pe.getTypeConstraint();
                
            String sStaticText = "<<role>>";
            if (sTypeConstraint != null && sTypeConstraint.equals("Interface"))
            {
                sStaticText = "<<interface,role>>";
            }               
            nameWidget.setStaticText(sStaticText);            
        }
    }

    ///////////////////////////////////////////////////////////////
    // PropertyChangeListener Implementation
    
    protected boolean isParameter(IElement element)
    {
        boolean retVal = false;
        
        if (element instanceof IParameterableElement)
        {
            IParameterableElement param = (IParameterableElement) element;
        
            IPresentationElement modelElement = getObject();
            if (modelElement.getFirstSubject() instanceof IClassifier)
            {
                IClassifier clazz = (IClassifier) modelElement.getFirstSubject();
                retVal = clazz.getIsTemplateParameter(param);
            }    
        }

        
        return retVal;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        
        if(classView != null)
        {
            if((isParameter((IElement)event.getSource()) == true) && 
               (parameterWidget != null))
            {
                parameterWidget.propertyChange(event);
                return;
                
            }
            else if(!(event.getSource() instanceof IClassifier))
            {
                return;
            }

            String propName = event.getPropertyName();
            nameWidget.propertyChange(event);
            if(propName.equals(ModelElementChangedKind.FEATUREADDED.toString()))
            {
                if(event.getNewValue() instanceof IOperation)
                {
                    addOperation((IOperation)event.getNewValue());
                }
                else if(event.getNewValue() instanceof IAttribute)
                {
                    addAttribute((IAttribute)event.getNewValue());
                }
            }
            else if(propName.equals(ModelElementChangedKind.FEATUREMOVED.toString()) ||
                    propName.equals(ModelElementChangedKind.DELETE.toString()) ||
                    propName.equals(ModelElementChangedKind.PRE_DELETE.toString()))
            {
                if(event.getOldValue() instanceof IOperation)
                {
                    removeOperation((IOperation)event.getOldValue());
                }
                else if(event.getOldValue() instanceof IAttribute)
                {
                    removeAttribute((IAttribute)event.getOldValue());
                }
            }
            else if(propName.equals(ModelElementChangedKind.TEMPLATE_PARAMETER.toString()))
            {
                Widget result = initializeContents((IClassifier)event.getSource());
                result.removeFromParent();
                setCurrentView(result);
            }
            else if(propName.equals(ModelElementChangedKind.REDEFINED_OWNER_NAME_CHANGED.toString()))
            {
                updateRedefinesCompartment((IClassifier)event.getNewValue());
            }
        }
    }

    protected void updateRedefinesCompartment(IClassifier redefinedOwner)
    {
        String xmiid = redefinedOwner.getXMIID();
        ElementListWidget opList = operationRedefinedMap.get(xmiid);
        ElementListWidget attrList = attributeRedefinedMap.get(xmiid);
        
        if(opList != null)
        {
            String title = NbBundle.getMessage(UMLClassWidget.class, 
                                               "LBL_RedefinedOperations",
                                               redefinedOwner.getNameWithAlias());
            opList.setLabel(title);
        }
        
        if(attrList != null)
        {
            String title = NbBundle.getMessage(UMLClassWidget.class, 
                                               "LBL_RedefinedAttributes",
                                               redefinedOwner.getNameWithAlias());
            attrList.setLabel(title);
        }
        
        revalidate();
    }
    
    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Class");
        return paletteModel;
    }
    

    public class TemplateWidgetLayout implements Layout
    {
        private static final int TEMPLATE_EXTENDS = 10;
        public void layout(Widget widget)
        {
            Rectangle bounds = classView.getPreferredBounds();
            int viewY = 0;
            if(bounds != null)
            {
                int viewHalf = bounds.width / 2;
                Rectangle paramBounds = parameterWidget.getPreferredBounds();
                viewY = paramBounds.height / 2;

                if(paramBounds.width < (viewHalf + TEMPLATE_EXTENDS))
                {
                    paramBounds.width = viewHalf + TEMPLATE_EXTENDS;
                }

                parameterWidget.resolveBounds(new Point(bounds.width / 2, -paramBounds.y), paramBounds);
    
                Point bodyLocation = new Point(0, paramBounds.height - (paramBounds.height / 3));
                classView.resolveBounds(bodyLocation, new Rectangle(new Point(0, 0), bounds.getSize()));
            }
        }

        public boolean requiresJustification(Widget widget)
        {
            return true;
        }

        public void justify(Widget widget)
        {
            Rectangle clientArea = widget.getClientArea();
            
            int bodyWidth = clientArea.width - TEMPLATE_EXTENDS;
            int bodyHalf = bodyWidth / 2;
            
            Rectangle paramBounds = parameterWidget.getPreferredBounds();
            Dimension paramSize = new Dimension(clientArea.width - bodyHalf, paramBounds.height );
            
            int bodyY = paramSize.height - (paramSize.height / 3);
            Dimension bodySize = new Dimension(bodyWidth, clientArea.height - bodyY);

            Point paramLocation = new Point(bodyHalf, -paramBounds.y);
            Point bodyLocation = new Point(0, bodyY);
            
            parameterWidget.resolveBounds(paramLocation, new Rectangle(paramBounds.getLocation(), paramSize));
            classView.resolveBounds(bodyLocation, new Rectangle(new Point(0, 0), bodySize));
        }
        
    }
    
    public String getWidgetID() {
        return UMLWidgetIDString.UMLCLASSWIDGET.toString();
    }
    
    
    public class AcceptFeatureProvider implements AcceptProvider
    {
        public ConnectorState isAcceptable(Widget widget,
                                           Point point,
                                           Transferable transferable)
        {
            return ConnectorState.REJECT;
        }
        
        public void accept(Widget widget,
                           Point point,
                           Transferable transferable)
        {
            
        }
    }

    @Override
    protected void paintBackground()
    {
        if(classView != null)
        {
            Paint bg = classView.getBackground();

            // TODO: Need to test if gradient paint preference is set.
            if((bg instanceof Color) && (useGradient == true))
            {
                Rectangle bounds = getClientArea();
                float midX = bounds.width / 2;

                Color bgColor = (Color)bg;
                GradientPaint paint = new GradientPaint(midX, 0, Color.WHITE,
                                                        midX, getBounds().height, 
                                                        bgColor);
                Graphics2D g = getGraphics();
                g.setPaint(paint);
                g.fillRect(0, 0, bounds.width, bounds.height);
            }
            else
            {
                super.paintBackground();
            }
        }
    }

    protected void notifyFontChanged(Font font)
    {
        // Some of the widgets may be relative.  Therefore, notify them that 
        // the font changed.
    }
}
    
