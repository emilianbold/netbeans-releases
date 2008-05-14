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
import javax.swing.BorderFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.DefaultWidgetContext;
import org.netbeans.modules.uml.diagrams.actions.ClassifierSelectAction;
import org.netbeans.modules.uml.diagrams.nodes.ActorSymbolWidget;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.SwitchableWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author jyothi
 */
public class ActorWidget extends SwitchableWidget
{
    protected static ResourceBundle bundle = NbBundle.getBundle(EditableCompartmentWidget.class);
    public static int DEFAULT_WIDTH = 60;
    public static int DEFAULT_HEIGHT = 90;
    Widget mainView = null;
    ActorSymbolWidget stickFigureWidget = null;
    UMLNameWidget nameWidget = null;
    
    public ActorWidget(Scene scene)
    {
        super(scene, "Actor", true);
        WidgetAction.Chain actions = createActions(DesignerTools.SELECT);

        addToLookup(initializeContextPalette());
        addToLookup(new DefaultWidgetContext("Actor"));
        addToLookup(new ClassifierSelectAction());
    }

    @Override
    public Widget createDefaultWidget(IPresentationElement element)
    {
        if (element != null)
        {
            IActor actorElt = (IActor) element.getFirstSubject();
            Scene scene = getScene();
            setOpaque(true);

            //create the main view
            mainView = new Widget(scene);
            mainView.setLayout(
                    LayoutFactory.createVerticalFlowLayout(
                    LayoutFactory.SerialAlignment.JUSTIFY, 0));
            mainView.setBorder(BorderFactory.createEmptyBorder());

            //create the stick figure widget
            stickFigureWidget = new ActorSymbolWidget(
                    scene, DEFAULT_WIDTH, DEFAULT_WIDTH, getWidgetID(),
                    bundle.getString("LBL_body"));
            stickFigureWidget.setOpaque(true);
            mainView.addChild(stickFigureWidget);
            mainView.setChildConstraint(stickFigureWidget, 100);
            
            //create the name widget
            nameWidget = new UMLNameWidget(scene, false, getWidgetID());
            setStaticText(nameWidget, element.getFirstSubject());
            nameWidget.initialize(actorElt);

            mainView.addChild(nameWidget);
            mainView.setChildConstraint(nameWidget, 1);

            setCurrentView(mainView);
        }
        return mainView;
    }

    @Override
    public void removingView()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.ACTORWIDGET.toString();
    }
    
    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/Actor");
        return paletteModel;
    }

    private void setStaticText(UMLNameWidget nameWidget, IElement element)
    {
        if (element instanceof IPartFacade)
        {
            String sStaticText = "<<role>>";
            nameWidget.setStaticText(sStaticText);            
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();
        if (element != null && element instanceof IActor)
        {
            nameWidget.propertyChange(event);
        }
    }
}
