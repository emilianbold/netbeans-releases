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

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author treyspiva
 */
public class UMLNameWidget extends Widget implements PropertyChangeListener,org.netbeans.modules.uml.drawingarea.widgets.NameFontHandler
{

    public static final int BORDER_SIZE = 4;
    private EditableCompartmentWidget className = null;
    private ImageWidget nodeIconWidget = null;
    private UMLLabelWidget stereotypeWidget = null;
    private UMLLabelWidget staticTextWidget = null;
    private MultiLineTaggedValueWidget taggedValuesWidget = null;
    private boolean showIcon = true;
    private boolean isAbstract = false;
    private ArrayList<String> hiddenStereotypes = new ArrayList<String>();
    private String nodeWidgetID;
    private String staticText = null;
    public static final String stereotypeID = "stereotype";
    public static final String staticTextID = "statictext";
    public static final String nameCompartmentWidgetID = "name";
    public static final String taggedValueID = "taggedValue";
    public static final String ID = "commonNameCompartment";

    public UMLNameWidget(Scene scene, String propertyId)
    {
        this(scene, true, propertyId);
    }

    public UMLNameWidget(Scene scene, boolean showIcon, String propertyId)
    {
        super(scene);
        setBackground((Paint) null);
        setForeground((Color) null);
        nodeWidgetID = propertyId;
        setShowIcon(showIcon);
    }

    public void initialize(IElement data)
    {
        setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, 0));
        setBorder(BorderFactory.createOpaqueBorder(BORDER_SIZE,
                BORDER_SIZE,
                0,
                BORDER_SIZE));

        if (data instanceof INamedElement)
        {
            if (staticText != null) 
            {
                staticTextWidget = new UMLLabelWidget(getScene(), 
                    nodeWidgetID + "." + staticTextID,
                    loc("LBL_staticText"));
                staticTextWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
                staticTextWidget.setLabel(staticText);
                staticTextWidget.setVisible(true);
                staticTextWidget.setForeground(null);
                addChild(staticTextWidget);
            }

            INamedElement namedElement = (INamedElement) data;

            stereotypeWidget = new UMLLabelWidget(getScene(),
                    nodeWidgetID + "." + stereotypeID,
                    loc("LBL_stereotype"));
            stereotypeWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);

            updateStereotypes(data.getAppliedStereotypesAsString());
            addChild(stereotypeWidget);

            Widget classNameContainer = new Widget(getScene());
            classNameContainer.setForeground((Color) null);
            classNameContainer.setBackground((Paint) null);
            classNameContainer.setLayout(LayoutFactory.createHorizontalFlowLayout(SerialAlignment.CENTER, 0));

            if (isShowIcon() == true)
            {
                Icon icon = getIcon(data);
                if (icon != null)
                {
                    nodeIconWidget = new ImageWidget(getScene());
                    nodeIconWidget.setImage(ImageUtilities.icon2Image(icon));
                    nodeIconWidget.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
                    classNameContainer.addChild(nodeIconWidget);
                }
            }

            className = new EditableCompartmentWidget(getScene(),
                    nodeWidgetID + "." + nameCompartmentWidgetID,
                    loc("LBL_name"));
            className.setCustomizableResourceTypes(new ResourceType[]{ResourceType.FOREGROUND});//name should use inherited always
            className.setLabel(namedElement.getNameWithAlias());
            classNameContainer.addChild(className);

            addChild(classNameContainer);

            List<String> taggedValues = data.getTaggedValuesAsList();
            taggedValuesWidget = new MultiLineTaggedValueWidget(getScene(),
                    nodeWidgetID + "." + taggedValueID,
                    loc("LBL_taggedValue"));
            taggedValuesWidget.updateTaggedValues(taggedValues);
            addChild(taggedValuesWidget);

            if (data instanceof IClassifier)
            {
                IClassifier classifier = (IClassifier) data;
                isAbstract = classifier.getIsAbstract();
            } else if (data instanceof IBehavioralFeature)
            {
                IBehavioralFeature feature = (IBehavioralFeature) data;
                isAbstract = feature.getIsAbstract();
            }
            
            // Set the label widgets to use the parents foreground colors.
            className.setForeground(null);
            stereotypeWidget.setForeground(null);
            taggedValuesWidget.setForeground(null);
        }
    }

    public void hideStereotype(String stereotype)
    {
        hiddenStereotypes.add(stereotype);
    }

    protected void updateStereotypes(List<String> stereotypes)
    {
        String stereotypeStr = "";
        for (String stereotype : stereotypes)
        {
            if (hiddenStereotypes.contains(stereotype) == false)
            {
                if (stereotypeStr.length() > 0)
                {
                    stereotypeStr += ", ";
                }
                stereotypeStr += stereotype;
            }
        }

        if (stereotypeStr.length() > 0)
        {
            stereotypeWidget.setLabel("<<" + stereotypeStr + ">>");
            stereotypeWidget.setVisible(true);
        } else
        {
            stereotypeWidget.setLabel("");
            stereotypeWidget.setVisible(false);
        }
    }

    public void setStaticText(String text)
    {
        staticText = text;
    }

    public void setNameFont(Font font)
    {
        font = updateforAbstract(font);
        if(className!=null)className.setFont(font);
    }

    public void setStereotypeFont(Font font)
    {
        stereotypeWidget.setFont(font);
    }
    
    protected Icon getIcon(IElement data)
    {
        return CommonResourceManager.instance().getIconForElementType(data.getElementType());
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();
        if (element instanceof INamedElement &&
            (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()) ||
            propName.equals(ModelElementChangedKind.ALIAS_MODIFIED.toString()) ) )
        {
            if (getScene() instanceof ObjectScene)
            {
                ObjectScene scene = (ObjectScene) getScene();
                Object obj = scene.findObject(getParentWidget());
                if (obj instanceof IPresentationElement)
                {
                    IPresentationElement presentation = (IPresentationElement) obj;
                    if(element.isSame(presentation.getFirstSubject()) == true)
                    {   
                        INamedElement nameElement = (INamedElement) element;
                        className.setLabel(nameElement.getNameWithAlias());
                    }
                }
            }
        } else if (propName.equals(ModelElementChangedKind.STEREOTYPE.toString()))
        {
            INamedElement nameElement = (INamedElement) element;
            className.setLabel(nameElement.getNameWithAlias());
            updateStereotypes(element.getAppliedStereotypesAsString());
        } else if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
        {
            // There is a specific tagged value event.  Therefore we have to 
            // check everytime the element is modified.
            List<String> taggedValues = element.getTaggedValuesAsList();
            taggedValuesWidget.updateTaggedValues(taggedValues);
        }

        // If the abstract property changed, then update the font.
        if (element instanceof IClassifier)
        {
            IClassifier classifier = (IClassifier) element;
            if (isAbstract != classifier.getIsAbstract())
            {
                isAbstract = classifier.getIsAbstract();
                Font font = updateforAbstract(className.getFont());
                className.setFont(font);
            }
        } else if (element instanceof IBehavioralFeature)
        {
            IBehavioralFeature feature = (IBehavioralFeature) element;
            if (isAbstract != feature.getIsAbstract())
            {
                isAbstract = feature.getIsAbstract();
                Font font = updateforAbstract(className.getFont());
                className.setFont(font);
            }
        }
    }


    public boolean isShowIcon()
    {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon)
    {
        this.showIcon = showIcon;
    }

    private Font updateforAbstract(Font font)
    {
        Font retVal = font;

        if(font!=null)
        {
            if (isAbstract == true)
            {
                retVal = font.deriveFont(Font.BOLD | Font.ITALIC);
            } else
            {
                retVal = font.deriveFont(Font.BOLD);
            }
        }

        return retVal;
    }

    private String loc(String msg)
    {
        return NbBundle.getMessage(UMLNameWidget.class, msg);
    }

    public void showAllWidgets()
    {
        updateStereotypes(Arrays.asList(new String[]{ "manager" }));
        taggedValuesWidget.updateTaggedValues("Extend, Import");
    }
}
