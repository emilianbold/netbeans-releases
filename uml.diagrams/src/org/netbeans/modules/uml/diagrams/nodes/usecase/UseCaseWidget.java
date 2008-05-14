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

import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import org.netbeans.api.visual.border.BorderFactory;
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
import org.netbeans.modules.uml.diagrams.nodes.ElementListWidget;
import org.netbeans.modules.uml.diagrams.nodes.OvalWidget;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
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
        }
    }

    private void initUseCaseWidget()
    {
        usecaseWidget = new Widget(scene);
        ovalWidget = new OvalWidget(scene, USECASE_DEFAULT_WIDTH, USECASE_DEFAULT_HEIGHT, getWidgetID(), bundle.getString("LBL_body"));
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
        detailWidget.addChild(new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL));
        UMLLabelWidget extPtLabel = new UMLLabelWidget(scene,
                NbBundle.getMessage(UseCaseWidget.class, "LBL_ExtensionPoints"),
                getWidgetID() + "." + "extensionPoint", "Extension Point Label");
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
    }

    public boolean isDetailVisible()
    {
        return detailWidget.isVisible();
    }

    public void setDetailVisible(boolean visible)
    {
        detailWidget.setVisible(visible);
    }

    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.USECASEWIDGET.toString();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        if (!event.getSource().equals(usecase))
        {
            return;
        }
        String propName = event.getPropertyName();

        if (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
        {
            if (nameWidget != null)
            {
                nameWidget.propertyChange(event);
            }
        } 
         
        else if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
        {
            updateDetails();
        }        
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
            extPtListWidget.addChild(widget);
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
    
}
