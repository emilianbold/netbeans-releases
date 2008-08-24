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

import java.awt.Color;
import java.awt.Paint;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.ActorSymbolWidget;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.Customizable;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author jyothi
 */
public class ActorWidget extends UMLNodeWidget
{

    protected static ResourceBundle bundle = NbBundle.getBundle(EditableCompartmentWidget.class);
    public static int DEFAULT_WIDTH = 60;
    public static int DEFAULT_HEIGHT = 90;
    ActorSymbolWidget stickFigureWidget = null;
    UMLNameWidget nameWidget = null;
    Scene scene;
    IActor actor;
    private Widget currentView;
    private Widget actorWidget;

    public ActorWidget(Scene scene)
    {
        super((Scene) scene);
        this.scene = scene;
        addToLookup(initializeContextPalette());
    }

    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Actor");
        return paletteModel;
    }

    @Override
    public void initializeNode(IPresentationElement element)
    {
        if (element != null)
        {
            IElement pElt = element.getFirstSubject();
            if (pElt instanceof IActor)
            {
                actor = (IActor) pElt;
                currentView = initActorWidget(actor);
                setCurrentView(currentView);
                setFont(getCurrentView().getFont());
            }
        }
        super.initializeNode(element);
    }

    private Widget initActorWidget(IActor actor)
    {
        actorWidget = new Widget(scene);
        actorWidget.setLayout(
                LayoutFactory.createVerticalFlowLayout(
                LayoutFactory.SerialAlignment.JUSTIFY, 0));
//            actorWidget.setBorder(BorderFactory.createEmptyBorder());

        //create the stick figure widget
        // The stick figure will override the customizable display name.
        stickFigureWidget = new ActorSymbolWidget(
                scene, DEFAULT_WIDTH, DEFAULT_WIDTH, getStickFigureId(),
                "");
        stickFigureWidget.setUseGradient(useGradient);
        stickFigureWidget.setOpaque(false);

        actorWidget.addChild(stickFigureWidget);
            actorWidget.setChildConstraint(stickFigureWidget, 100);

        actorWidget.setForeground(null);
        actorWidget.setBackground(null);
            
        //create the name widget
        nameWidget = new UMLNameWidget(scene, false, getWidgetID());
        setStaticText(nameWidget, actor);
        nameWidget.initialize(actor);

        actorWidget.addChild(nameWidget);
            actorWidget.setChildConstraint(nameWidget, 1);

        return actorWidget;
    }

    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.ACTORWIDGET.toString();
    }

    private void setStaticText(UMLNameWidget nameWidget, IElement element)
    {
        if (element instanceof IPartFacade)
        {
            String sStaticText = "<<role>>"; // NOI18N
            nameWidget.setStaticText(sStaticText);
        }
    }

    @Override
    protected void notifyBackgroundChanged(Paint paint)
    {
        String id =  getStickFigureId() + "." + ResourceValue.BGCOLOR; // NOI18N
        getResourceTable().addProperty(id, paint);
    }

//    @Override
//    protected void notifyForegroundChanged(Color newColor)
//    {
//        String id =  getStickFigureId() + "." + ResourceValue.FGCOLOR; // NOI18N
//        getResourceTable().addProperty(id, newColor);
//    }

    protected String getStickFigureId()
    {
        return getWidgetID() + ".stickfigure"; // NOI18N
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        if (!event.getSource().equals(actor))
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
    }
}
