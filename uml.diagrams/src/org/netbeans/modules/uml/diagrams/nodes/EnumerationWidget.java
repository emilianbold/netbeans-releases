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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
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
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.widgets.ListWidget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.openide.util.NbBundle;


public class EnumerationWidget extends SwitchableWidget implements ICommonFeature
{
    private UMLNameWidget nameWidget = null;
    
    private ElementListWidget literals;
    private ElementListWidget members;
    private ElementListWidget operations;
    private TemplateWidget parameterWidget = null;
    private Widget classView = null;
    private Widget attributeSection = null;
    private IAttribute attributeToSelect;
    private IOperation operationToSelect;
    
    private HashMap <String, ElementListWidget > operationRedefinedMap = 
            new HashMap <String, ElementListWidget >();
    
    private HashMap <String, ElementListWidget > attributeRedefinedMap = 
            new HashMap <String, ElementListWidget >();
    
    public EnumerationWidget(Scene scene)
    {
        this(scene, "Class");
    }
    
     public EnumerationWidget(Scene scene, IPresentationElement element)
    {
        this(scene, "Class");
        initializeNode(element);
    }
     
    public EnumerationWidget(Scene scene, String metatype)
    {
        super(scene, metatype, true);
        
        WidgetAction.Chain actions = createActions(DesignerTools.SELECT);
        
        addToLookup(initializeContextPalette());
        addToLookup(new DefaultWidgetContext("Enumeration"));
//        addToLookup(new ClassifierSelectAction());
    }

    @Override
    public Widget createDefaultWidget(IPresentationElement element)
    {
        IClassifier clazz = (IClassifier) element.getFirstSubject();
        Widget retVal = initializeContents(clazz);
        
        setOpaque(true);
        ResourceValue.initResources(getResourcePath(), this);
        return retVal;
    }
    
    public void removingView()
    {
        if (members != null)
        {
            members.removeFromParent();
        }
        
        if (operations != null)
        {
            operations.removeFromParent();
        }
        
        if (parameterWidget != null)
        {
            parameterWidget.removeFromParent();
        }
        
        if(literals != null)
        {
            literals.removeFromParent();
        }
        
        if (classView != null)
        {
            classView.removeFromParent();
        }
        
        classView = null;
        members = null;
        operations = null;
        parameterWidget = null;
        literals = null;

        getScene().validate();
    }

    protected Widget initializeContents(IClassifier clazz)
    {
        Widget retVal = classView;
        setBackground(null);
        
        boolean viewRequireUpdate = initializeClassView(clazz);
        boolean paramRequireUpate = initializeParameterWidget(clazz);

        if ((viewRequireUpdate == true) || (paramRequireUpate == true))
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

            String literalsTitle = NbBundle.getMessage(EnumerationWidget.class, 
                                                    "LBL_LiteralsCompartment"); 
            literals = new ElementListWidget(scene);
            ((ListWidget) literals).setLabel(literalsTitle);
            CollapsibleWidget cwl = new CollapsibleWidget(scene, literals);
            classView.addChild(cwl);
            initializeLiterals(element);
            cwl.setCompartmentName(LITERALS_COMPARTMENT);//NOI18N
            addToLookup(cwl);
            
            // It turns out that attributes can be redefined as well.  I do not
            // think that we have a UI to allow an attribute to be redefined,
            // but we had code to show redefined attributes before, so I am 
            // adding it here.  The attribute section allows the redefined
            // compartments to be grouped with the standard attributes 
            // compartment.
            attributeSection = new Widget(scene);
            attributeSection.setForeground((Color)null);
            attributeSection.setLayout(LayoutFactory.createVerticalFlowLayout());
            
            String attrsTitle = NbBundle.getMessage(EnumerationWidget.class, 
                                                    "LBL_AttributesCompartment"); 
            members = new ElementListWidget(scene);
            members.createActions(DesignerTools.SELECT).addAction(ActionFactory.createAcceptAction(new AcceptFeatureProvider()));
            ((ListWidget) members).setLabel(attrsTitle);
            
            CollapsibleWidget membersSection = new CollapsibleWidget(scene, members);
            membersSection.setVisible(false);
            attributeSection.addChild(membersSection);
            classView.addChild(attributeSection);
            initializeAttributes(element);
            membersSection.setCompartmentName(ATTRIBUTES_COMPARTMENT);//NOI18N
            addToLookup(membersSection);

            String opsTitle = NbBundle.getMessage(EnumerationWidget.class, 
                                                    "LBL_OperationsCompartment");
            
            operations = new ElementListWidget(scene);
            operations.createActions(DesignerTools.SELECT).addAction(ActionFactory.createAcceptAction(new AcceptFeatureProvider()));
            ((ListWidget) operations).setLabel(opsTitle);
            
            CollapsibleWidget operationSection = new CollapsibleWidget(scene, operations);
            operationSection.setVisible(false);
            classView.addChild(operationSection);
            initializeOperations(element);
            operationSection.setCompartmentName(OPERATIONS_COMPARTMENT);//NOI18N
            addToLookup(operationSection);
            setFont(classView.getFont());
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
    
    protected void initializeLiterals(IClassifier clazz)
    {
        if(clazz instanceof IEnumeration)
        {
            IEnumeration enumeration = (IEnumeration)clazz;
            for(IEnumerationLiteral literal : enumeration.getLiterals())
            {
                addLiteral(literal);
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
            String title = NbBundle.getMessage(EnumerationWidget.class, 
                                               "LBL_RedefinedOperations",
                                               classifier.getNameWithAlias());

            retVal = new ElementListWidget(getScene());
            retVal.setLabel(title);
            
            CollapsibleWidget cw = new CollapsibleWidget(getScene(), retVal);
            classView.addChild(cw);
            cw.setCompartmentName(REDEFINED_OPER_COMPARTMENT);//NOI18N
            addToLookup(cw);
            operationRedefinedMap.put(classifier.getXMIID(), retVal);
        }
        
        return retVal;
    }
    
    protected ElementListWidget getRedefinedAttributesCompartment(IClassifier classifier)
    {
        ElementListWidget retVal = attributeRedefinedMap.get(classifier.getXMIID());
        if(retVal == null)
        {
            String title = NbBundle.getMessage(EnumerationWidget.class, 
                                               "LBL_RedefinedAttributes",
                                               classifier.getNameWithAlias());

            retVal = new ElementListWidget(getScene());
            retVal.setLabel(title);
            
            CollapsibleWidget cw = new CollapsibleWidget(getScene(), retVal);
            attributeSection.addChild(cw);
            attributeSection.addChild(retVal);
            cw.setCompartmentName(REDEFINED_ATTR_COMPARTMENT);//NOI18N
            addToLookup(cw);
            attributeRedefinedMap.put(classifier.getXMIID(), retVal);
        }
        
        return retVal;
    }
    
    protected OperationWidget addOperation(IOperation op)
    {
        OperationWidget widget = null;
        if(op.getIsRedefined() == false)
        {
            widget = new OperationWidget(getScene());
            widget.initialize(op);
            operations.addChild(widget);
            
            if(operations.isVisible() == false)
            {
                operations.setVisible(true);
            }
            
            if(operations.getParentWidget().isVisible() == false)
            {
                operations.getParentWidget().setVisible(true);
            }
        }
        else
        {
           addRedefinedOperation(op);
        }
        return widget;
    }
    
    protected void removeOperation(IOperation op)
    {
        operations.removeElement(op);
        
        if(operations.getSize() == 0)
        {
            operations.getParentWidget().setVisible(false);
        }
    }
    
    protected AttributeWidget addAttribute(IAttribute attr)
    {
        AttributeWidget widget = null;
        if(attr.getIsRedefined() == false)
        {
            widget = new AttributeWidget(getScene());
            ResourceValue.initResources(getWidgetID() + "." + DEFAULT, widget);
            widget.initialize(attr);
            members.addChild(widget);
        }
        else
        {
           addRedefinedAttribute(attr);
        }
        
        if(members.isVisible() == false)
        {
            members.setVisible(true);
        }

        if(members.getParentWidget().isVisible() == false)
        {
            members.getParentWidget().setVisible(true);
        }
        
        return widget;
    }
    
    protected EnumerationLiteralWidget addLiteral(IEnumerationLiteral literal)
    {
        EnumerationLiteralWidget widget = new EnumerationLiteralWidget(getScene());
        ResourceValue.initResources(getWidgetID() + "." + DEFAULT, widget);
        widget.initialize(literal);
        literals.addChild(widget);
        return widget;
    }
    
    protected void removeAttribute(IAttribute attr)
    {
        members.removeElement(attr);
        
        if(members.getSize() == 0)
        {
            members.getParentWidget().setVisible(false);
        }
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
        Object eventSrc = event.getSource();
        if(classView != null)
        {
            if((isParameter((IElement)eventSrc) == true) && 
               (parameterWidget != null))
            {
                parameterWidget.propertyChange(event);
                return;
                
            }
            else if(!(eventSrc instanceof IClassifier))
            {
                return;
            }

            String propName = event.getPropertyName();
            Object newVal = event.getNewValue();
            Object oldVal = event.getOldValue();
            nameWidget.propertyChange(event);
            if(propName.equals(ModelElementChangedKind.FEATUREADDED.toString()))
            {
                if(newVal instanceof IOperation)
                {
                    IOperation op = (IOperation)newVal;
                    OperationWidget operW = addOperation(op);
                    if(operW != null && op == getSelectedOperation())
                    {
                        operW.select();
                        setSelectedOperation(null);
                    }
                }
                else if(newVal instanceof IAttribute)
                {
                    IAttribute attr = (IAttribute)newVal;
                    AttributeWidget attrW = addAttribute(attr);
                    if(attrW != null && attr == getSelectedAttribute())
                    {
                        attrW.select();
                        setSelectedAttribute(null);
                    }
                }
                else if(newVal instanceof IEnumerationLiteral)
                {
                    IEnumerationLiteral enumeration = (IEnumerationLiteral)newVal;
                    EnumerationLiteralWidget literalW = addLiteral(enumeration);
                    if ( literalW != null) 
                    {
                        literalW.select();
                    }
                }
            }
            else if(propName.equals(ModelElementChangedKind.FEATUREMOVED.toString()) ||
                    propName.equals(ModelElementChangedKind.DELETE.toString()) ||
                    propName.equals(ModelElementChangedKind.PRE_DELETE.toString()))
            {
                if(oldVal instanceof IOperation)
                {
                    removeOperation((IOperation)oldVal);
                }
                else if(oldVal instanceof IAttribute)
                {
                    removeAttribute((IAttribute)oldVal);
                }
            }
            else if(propName.equals(ModelElementChangedKind.TEMPLATE_PARAMETER.toString()))
            {
//                Widget result = initializeContents((IClassifier)event.getSource());
//                
//                result.removeFromParent();
//                setCurrentView(result);
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
            String title = NbBundle.getMessage(EnumerationWidget.class, 
                                               "LBL_RedefinedOperations",
                                               redefinedOwner.getNameWithAlias());
            opList.setLabel(title);
        }
        
        if(attrList != null)
        {
            String title = NbBundle.getMessage(EnumerationWidget.class, 
                                               "LBL_RedefinedAttributes",
                                               redefinedOwner.getNameWithAlias());
            attrList.setLabel(title);
        }
        
        revalidate();
    }
    
    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Enumeration");
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
        return UMLWidgetIDString.ENUMERATION_WIDGET.toString();
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

    @Override
    protected void notifyFontChanged(Font font)
    {
        //same as in class widget
        if(font==null)return;
        //
        if(nameWidget!=null)
        {
            if(classView!=null)nameWidget.setNameFont(font);//it works in classview only
        }
        //all other views are iconic, shuldn't have much widgets, so finding for UMLNameWidget without additional api.
        if(classView==null || classView!=getCurrentView())
        {
            if(getCurrentView()!=null)
            {
                org.netbeans.modules.uml.drawingarea.widgets.NameFontHandler nameW=findNameWidget(getCurrentView());
                if(nameW!=null)nameW.setNameFont(font);
            }
        }
        //need to update operations, attributes, titles
        if(classView!=null)
        {
            ObjectScene scene=(ObjectScene) getScene();
            operations.setFont(font.deriveFont(font.getStyle(), font.getSize()*.9f));
            members.setFont(font.deriveFont(font.getStyle(), font.getSize()*.9f));//? may it have sense to force plain for attributes?
            //
            for(Widget w:operations.getChildren())
            {
                if(w instanceof OperationWidget)
                {
                    w.setFont(operations.getFont());//update will be handled by hendler in operation widget
                }
            }
            classView.revalidate();
        }
        revalidate();
    }

    public void setSelectedAttribute(IAttribute attr)
    {
        this.attributeToSelect = attr;
    }

    public void setSelectedOperation(IOperation op)
    {
        this.operationToSelect = op;
    }

    public IAttribute getSelectedAttribute()
    {
        return this.attributeToSelect;
    }

    public IOperation getSelectedOperation()
    {
        return this.operationToSelect;
    }
}

