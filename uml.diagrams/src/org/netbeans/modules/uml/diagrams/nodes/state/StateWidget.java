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
package org.netbeans.modules.uml.diagrams.nodes.state;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.LayoutFactory.SerialAlignment;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.State;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.diagrams.Util;
import org.netbeans.modules.uml.diagrams.border.UMLRoundedBorder;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.diagrams.nodes.state.ProcedureWidget.DoEventWidget;
import org.netbeans.modules.uml.diagrams.nodes.state.ProcedureWidget.EntryEventWidget;
import org.netbeans.modules.uml.diagrams.nodes.state.ProcedureWidget.ExitEventWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class StateWidget extends UMLNodeWidget
{
    private Scene scene;
    private Widget stateWidget;
    private Widget detailWidget;
    private Widget eventsWidget;
    private UMLNameWidget nameWidget;
    private ProcedureWidget entryWidget;
    private ProcedureWidget exitWidget;
    private ProcedureWidget doWidget;
    private State state;
    public static String SHOW_TRANSITIONS = "ShowTransitions";//do not change, the same value is used in 6.1 importing

    public StateWidget(Scene scene)
    {
        super((Scene) scene);
        this.scene = scene;
        addToLookup(initializeContextPalette());
    }

    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize("UML/context-palette/State");
        return paletteModel;
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        initStateWidget();
        IElement element = presentation.getFirstSubject();
        if (element instanceof State)
        {
            state = (State) presentation.getFirstSubject();
            setCurrentView(createStateView(state));
        }
        super.initializeNode(presentation);
    }

    private void initStateWidget()
    {
        stateWidget = new BackgroundWidget(
                scene, getResourcePath(), 
                NbBundle.getMessage(UMLNodeWidget.class, "LBL_Default"), 15, 15);
        stateWidget.setOpaque(true);
        stateWidget.setCheckClipping(true);
        detailWidget = new Widget(getScene());
        detailWidget.setForeground(null);
        detailWidget.setBackground(null);
        eventsWidget = new Widget(getScene());
        eventsWidget.setForeground(null);
        eventsWidget.setBackground(null);
        setIsInitialized(true);
    }

    private Widget createStateView(State state)
    {
        if (state.getIsSubmachineState())   
        {
            return createSubMachineStateView(state);
        }
        return createSimpleStateView(state);
    }

    private Widget createSimpleStateView(State state)
    {
        createSubMachineStateView(state);
        showDetail(false);
        return stateWidget;
    }

    private Widget createSubMachineStateView(State state)
    {
        nameWidget = new UMLNameWidget(scene, false, getWidgetID());
        nameWidget.initialize(state);

        UMLRoundedBorder roundedBorder = new UMLRoundedBorder(15, 15, 10, 10, null, Color.BLACK);
        stateWidget.setBorder(roundedBorder);
        stateWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
        stateWidget.addChild(nameWidget);

        detailWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
        detailWidget.addChild(new SeparatorWidget(scene, SeparatorWidget.Orientation.HORIZONTAL));

        UMLLabelWidget transitionLabel = new UMLLabelWidget(scene,
                loc("LBL_Transitions"), getWidgetID() + "." + "transition", "Transition Label");
        transitionLabel.setAlignment(LabelWidget.Alignment.CENTER);
        transitionLabel.setBorder(BorderFactory.createEmptyBorder(5));
        transitionLabel.setForeground(null);
        detailWidget.addChild(transitionLabel);

        updateDetails();

        detailWidget.addChild(eventsWidget);
        stateWidget.addChild(detailWidget);

        return stateWidget;
    }


    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.STATEWIDGET.toString();
    }

    private void updateDetails()
    {
        eventsWidget.removeChildren();
        eventsWidget.setLayout(LayoutFactory.createVerticalFlowLayout(SerialAlignment.CENTER, 0));
        IProcedure entry = state.getEntry();
        IProcedure exit = state.getExit();
        IProcedure doActivity = state.getDoActivity();

        if (entry != null)
        {
            entryWidget = new EntryEventWidget(getScene(), entry);
            eventsWidget.addChild(entryWidget);
        }

        if (doActivity != null)
        {
            doWidget = new DoEventWidget(getScene(), doActivity);
            eventsWidget.addChild(doWidget);
        }

        if (exit != null)
        {
            exitWidget = new ExitEventWidget(getScene(), exit);
            eventsWidget.addChild(exitWidget);
        }

        updateTransitions();       
    }

    private void updateTransitions()
    {
        ETList<ITransition> incomings = state.getIncomingTransitions();
        ETList<ITransition> outgoings = state.getOutgoingTransitions();
        for (ITransition transition : incomings)
        {
            if (transition.getIsInternal())
            {
                TransitionWidget w = new TransitionWidget.IncomingTransitionWidget(scene, transition);
                eventsWidget.addChild(w);
            }
        }
        for (ITransition transition : outgoings)
        {
            if (transition.getIsInternal())
            {
                TransitionWidget w = new TransitionWidget.OutgoingTransitionWidget(scene, transition);
                eventsWidget.addChild(w);
            }
        }
    }

    private void showDetail(boolean show)
    {
        detailWidget.setVisible(show);
    }

    public boolean isDetailVisible()
    {
        return state!=null && detailWidget!=null && state.getIsSubmachineState() && detailWidget.isVisible();
    }

    public void setDetailVisible(boolean visible)
    {
        if(detailWidget!=null)
        {
            detailWidget.setVisible(visible);
        }
        if (visible)
        {
            state.setIsSubmachineState(true);
        }
    }

    private UMLNameWidget getNameWidget()
    {
        return nameWidget;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        if (!event.getSource().equals(state))
        {
            return;
        }
        String propName = event.getPropertyName();

        if (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()))
        {
            if (getNameWidget() instanceof PropertyChangeListener)
            {
                PropertyChangeListener listener = (PropertyChangeListener) getNameWidget();
                listener.propertyChange(event);
            }
        } else if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
        {         
            updateDetails();
        }
        updateSizeWithOptions();
    }

    private String loc(String key)
    {
        return NbBundle.getMessage(StateWidget.class, key);
    }

    public void initializeNode(IPresentationElement pe, boolean show)
    {
        initStateWidget();
        state = (State) pe.getFirstSubject();
        setCurrentView(createFullView((State) pe.getFirstSubject()));
    }

    private Widget createFullView(State state)
    {
        createSubMachineStateView(state);
        nameWidget.showAllWidgets();

        String unnamed = NbBundle.getMessage (org.netbeans.modules.uml.common.Util.class, "UNNAMED");
        
        // create dummy procedure
        IProcedure pro = (IProcedure) Util.retrieveModelElement("Procedure");
        pro.setName(unnamed);

        eventsWidget.removeChildren();
        entryWidget = new EntryEventWidget(getScene(), pro);
        eventsWidget.addChild(entryWidget);

        // create dummy transition
        ITransition transition = (ITransition) Util.retrieveModelElement("Transition");
        transition.setIsInternal(true);
        transition.setName(unnamed);
        transition.setContainer(state.getFirstContent());

        TransitionWidget w = new TransitionWidget.IncomingTransitionWidget(scene, transition);
        eventsWidget.addChild(w);

        return stateWidget;
    }

    @Override
    public void save(NodeWriter nodeWriter)
    {
        //we need to save the property for transition visibility
        HashMap map = nodeWriter.getProperties();
        map.put(SHOW_TRANSITIONS, isDetailVisible());
        nodeWriter.setProperties(map);
        super.save(nodeWriter);
    }
    
    @Override
    public void load(NodeInfo nodeReader)
    {
        if (nodeReader != null)
        {
            Object showTransitions = nodeReader.getProperties().get(SHOW_TRANSITIONS);
            if (showTransitions != null)
            {
                setDetailVisible(Boolean.parseBoolean(showTransitions.toString()));
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
        assert target instanceof StateWidget;
        
        super.duplicate(setBounds, target);
        ((StateWidget)target).showDetail(isDetailVisible());
    }
}