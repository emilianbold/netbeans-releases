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
package org.netbeans.modules.uml.diagrams.actions.state;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.State;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.diagrams.Util;
import org.netbeans.modules.uml.diagrams.nodes.state.StateWidget;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class EventsAndTransitionsAction extends SceneNodeAction implements ContextAwareAction
{

    private JMenu popupMenu;
    private JMenuItem entry;
    private JMenuItem exit;
    private JMenuItem doActivity;
    private JMenuItem incoming;
    private JMenuItem outgoing;
    private JMenuItem deleteInternal;
    private DesignerScene scene;
    private State state;
    private IPresentationElement pe;

    public Action createContextAwareInstance(Lookup actionContext)
    {
        scene = actionContext.lookup(DesignerScene.class);
        pe = actionContext.lookup(IPresentationElement.class);
        
        if(pe != null)
        {
            IElement e = pe.getFirstSubject();
            if (e instanceof State)
            {
                state = (State) e;
                if (state.getIsSubmachineState())
                    return this;
            }
        }
        return this;
    }

    @Override
    protected void performAction(Node[] activatedNodes)
    {
    }

    @Override
    protected boolean enable(Node[] activatedNodes)
    {
        StateWidget widget = getStateWidget(scene, pe);
        
        return activatedNodes.length == 1 && state.getIsSubmachineState() && widget != null && widget.isDetailVisible() ;
    }

    @Override
    public String getName()
    {
        return loc("ACT_EventsAndTransitions");
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    private String loc(String key)
    {
        return NbBundle.getMessage(EventsAndTransitionsAction.class, key);
    }

    @Override
    public JMenuItem getMenuPresenter()
    {
        return getPopupPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter()
    {
        StateWidget widget = getStateWidget(scene, pe);
        
        boolean enable = false;
        if(state != null)
        {
            enable = state.getIsSubmachineState() &&
                         widget != null && 
                         widget.isDetailVisible() &&
                         scene.isReadOnly() == false;
        }
        
        popupMenu = new JMenu(loc("ACT_EventsAndTransitions")); // NOI18N

        popupMenu.setEnabled(enable);

        entry = new JMenuItem(state.getEntry() == null ? loc("CTL_SetEntry") : loc("CTL_DeleteEntry")); // NOI18N

        exit = new JMenuItem(state.getExit() == null ? loc("CTL_SetExit") : loc("CTL_DeleteExit")); // NOI18N

        doActivity = new JMenuItem(state.getDoActivity() == null ? loc("CTL_SetDo") : loc("CTL_DeleteDo")); // NOI18N

        incoming = new JMenuItem(loc("CTL_Incoming")); // NOI18N

        outgoing = new JMenuItem(loc("CTL_Outgoing")); // NOI18N

        deleteInternal = new JMenuItem(loc("CTL_DeleteInternal")); // NOI18N

        deleteInternal.setEnabled(hasInternalTransitions());
        JMenuItem[] items = new JMenuItem[]
        {
            entry, doActivity, exit, incoming, outgoing, deleteInternal
        };
        for (int i = 0; i < items.length; i++)
        {
            items[i].addActionListener(new MenuItemListener());
            popupMenu.add(items[i]);
        }
        return popupMenu;
    }

    private boolean hasInternalTransitions()
    {
        return getInternalTransitions().size() > 0;
    }

    private List<ITransition> getInternalTransitions()
    {
        List<ITransition> ret = new ArrayList<ITransition>();

        ETList<ITransition> incomings = state.getIncomingTransitions();
        ETList<ITransition> outgoings = state.getOutgoingTransitions();
        for (ITransition transition : incomings)
        {
            if (transition.getIsInternal())
            {
                ret.add(transition);
            }
        }
        for (ITransition transition : outgoings)
        {
            if (transition.getIsInternal())
            {
                ret.add(transition);
            }
        }
        return ret;
    }

    private class MenuItemListener implements ActionListener
    {

        public void actionPerformed(ActionEvent evt)
        {
            Object source = evt.getSource();
            if (source == entry)
            {
                if (state.getEntry() != null)
                {
                state.getEntry().delete();
                } else
                {
                    IProcedure proc = (IProcedure) Util.retrieveModelElement("Procedure");
                    state.setEntry(proc);
                }
            } else if (source == exit)
            {
                if (state.getExit() != null)
                {
                    state.getExit().delete();
                } else
                {
                    IProcedure proc = (IProcedure) Util.retrieveModelElement("Procedure");
                    state.setExit(proc);
                }
            } else if (source == doActivity)
            {
                if (state.getDoActivity() != null)
                {
                    state.getDoActivity().delete();
                } else
                {
                    IProcedure proc = (IProcedure) Util.retrieveModelElement("Procedure");
                    state.setDoActivity(proc);
                }
            } else if (source == incoming)
            {
                state.addIncomingTransition(createInternalTransition());
            } else if (source == outgoing)
            {
                state.addOutgoingTransition(createInternalTransition());
            } else if (source == deleteInternal)
            {
                List<ITransition> trans = getInternalTransitions();
                for (ITransition t : trans)
                {
                    t.delete();
                }
            }
        }
    }

    
    private ITransition createInternalTransition()
    {
        ITransition transition = (ITransition) Util.retrieveModelElement("Transition");
        transition.setIsInternal(true);
        transition.setName(NbBundle.getMessage (org.netbeans.modules.uml.common.Util.class, "UNNAMED"));
        transition.setContainer(state.getFirstContent());
        return transition;
    }
    
    
    private StateWidget getStateWidget(ObjectScene scene, IPresentationElement pe)
    {
        Widget widget = scene.findWidget(pe);
        if (widget instanceof StateWidget)
        {
            return (StateWidget) widget;
        }
        return null;
    }
}
