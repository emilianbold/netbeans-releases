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

package org.netbeans.modules.uml.diagrams.edges.sqd;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.edges.AbstractUMLConnectionWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.ExecutionSpecificationThinWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.WidgetContext;
import org.netbeans.modules.uml.drawingarea.actions.WidgetContextFactory;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import java.util.TreeSet;
import java.util.List;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;

/**
 * just a root for all messages
 * @author sp153251
 */
abstract public class MessageWidget extends AbstractUMLConnectionWidget implements PropertyChangeListener {

    public MessageWidget(Scene scene)
    {
        super(scene);
        addToLookup(new MessageWidgetContext());
     }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LabelManager manager = getLookup().lookup(MessageLabelManager.class);//TBD, can it be cached?
        if(manager != null)
        {
            manager.propertyChange(evt);
        }
    }
    private class MessageWidgetContext implements WidgetContextFactory
    {
        public WidgetContext findWidgetContext(Point localLocation)
        {
            WidgetContext retVal = null;
            retVal = new MessageEdgeContext(LabelManager.LabelType.EDGE);
            return retVal;
        }
    }
   private class MessageEdgeContext implements WidgetContext
    {
        private LabelManager.LabelType contextName = LabelManager.LabelType.EDGE;
        
        public MessageEdgeContext(LabelManager.LabelType context)
        {
            contextName = context;
        }
        
        public String getContextName()
        {
            return contextName.toString();
        }

        public Object[] getContextItems()
        {
            Object[] retVal = null;
            
                retVal = new Object[0];
             
            return retVal;
        }
    }
    @Override
    public void remove()
    {
        DesignerScene scene=(DesignerScene) getScene();
        TreeSet<MessagePinWidget> pins=new TreeSet<MessagePinWidget>();
        MessagePinWidget sourcePin=(MessagePinWidget) getSourceAnchor().getRelatedWidget();
        MessagePinWidget targetPin=(MessagePinWidget) getTargetAnchor().getRelatedWidget();
        pins.add(sourcePin);
        pins.add(targetPin);
        //
        IPresentationElement edge= (IPresentationElement) scene.findObject(this);
        IMessage firstMsg=(IMessage) edge.getFirstSubject();
        //
        if(targetPin.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_RETURN_IN)
        {
            //need to find call message and delete instead
            IMessage call=firstMsg.getSendingMessage();
            Widget par=sourcePin.getParentWidget();
            List<Widget> children=par.getChildren();
            //int index=children.indexOf(sourcePin);
            MessagePinWidget callTargetPin=null;
            MessageWidget callMessageW=null;
            IPresentationElement callPE=null;
            //
            int resultIndex=children.indexOf(sourcePin);
            //
            for(int i=resultIndex-1;i>=0;i--)
            {
                Widget w=children.get(i);
                if(w instanceof MessagePinWidget)
                {
                    MessagePinWidget callTargetPinTmp=(MessagePinWidget) w;
                    MessageWidget callMessageWTmp=(MessageWidget) callTargetPinTmp.getConnection(0);
                    IPresentationElement callPETmp=(IPresentationElement) scene.findObject(callMessageWTmp);
                    if(callPETmp!=null && callPETmp.getFirstSubject()!=null && callPETmp.getFirstSubject()==call)
                    {
                        callTargetPin=callTargetPinTmp;
                        callMessageW=callMessageWTmp;
                        callPE=callPETmp;
                        break;
                    }
                    else if((callPETmp==null || callPETmp.getFirstSubject()==null) && callTargetPinTmp!=null && callTargetPin==null && callTargetPinTmp.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_IN)
                    {
                        //some fail case when call is deleted or model element is deleted but pins remains and result remains
                        callTargetPin=callTargetPinTmp;
                        callMessageW=callMessageWTmp;
                        if(callTargetPin.getNumbetOfConnections()>0)
                        {
                            callMessageW=(MessageWidget) callTargetPin.getConnection(0);
                        }
                        //do not break, but assign only once, we still will look for first case in this else if as preferred
                    }
                }
            }
            if(callMessageW!=null)
            {
                sourcePin=(MessagePinWidget) callMessageW.getSourceAnchor().getRelatedWidget();
                pins.add(sourcePin);
                targetPin=(MessagePinWidget) callMessageW.getTargetAnchor().getRelatedWidget();
                pins.add(targetPin);
            }
            else if(callTargetPin!=null)//do not found pins, but found remains of message
            {
                pins.add(callTargetPin);
                //and need to find callSourcePin
                par=targetPin.getParentWidget();
                children=par.getChildren();
                //int index=children.indexOf(sourcePin);
                MessagePinWidget callSourcePin=null;
                resultIndex=children.indexOf(targetPin);
                for(int i=resultIndex-1;i>=0;i--)
                {
                    Widget w=children.get(i);
                    if(w instanceof MessagePinWidget)
                    {
                        MessagePinWidget callSourcePinTmp=(MessagePinWidget) w;
                        if(callSourcePinTmp.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_OUT)
                        {
                            callSourcePin=callSourcePinTmp;
                            pins.add(callSourcePin);
                            break;
                        }
                    }
                }
            }
            if(callPE!=null)
            {
                 scene.removeEdge(callPE);
            }
            else if(callMessageW!=null)//presentation element/element is deleted from model
            {
                if(callMessageW.getParentWidget()!=null)
                {
                    callMessageW.getParentWidget().removeChild(callMessageW);
                }
            }
        }
        else if(targetPin.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_IN)
        {
            //need to find return message
            //need to find call message and delete instead
            Widget par=sourcePin.getParentWidget();
            List<Widget> children=par.getChildren();
            //int index=children.indexOf(sourcePin);
            MessagePinWidget resultTargetPin=null;
            MessageWidget resultMessageW=null;
            IPresentationElement resultPE=null;
            for(Widget w:children)
            {
                if(w instanceof MessagePinWidget)
                {
                    MessagePinWidget resultTargetPinTmp=(MessagePinWidget) w;
                    MessageWidget resultMessageWTmp=(MessageWidget) resultTargetPinTmp.getConnection(0);
                    IPresentationElement resultPETmp=(IPresentationElement) scene.findObject(resultMessageWTmp);
                    if(resultPETmp!=null && ((IMessage) resultPETmp.getFirstSubject()).getSendingMessage()==firstMsg)
                    {
                        resultTargetPin=resultTargetPinTmp;
                        resultMessageW=resultMessageWTmp;
                        resultPE=resultPETmp;
                        break;
                    }
                    else if(resultPETmp==null && resultTargetPinTmp!=null && resultTargetPin==null && resultTargetPinTmp.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_RETURN_IN)
                    {
                        //some fail case when call is deleted but pins remains and result remains
                        resultTargetPin=resultTargetPinTmp;
                        resultMessageW=resultMessageWTmp;
                        //do not break, but assign only once, we still will look for first case in this else if as preferred
                    }
                }
            }
            if(resultMessageW!=null)
            {
                sourcePin=(MessagePinWidget) resultMessageW.getSourceAnchor().getRelatedWidget();
                targetPin=(MessagePinWidget) resultMessageW.getTargetAnchor().getRelatedWidget();
                pins.add(sourcePin);
                pins.add(targetPin);
                scene.removeEdge(resultPE);
            }
       }
        
         //
        scene.removeEdge (edge);//all "bounds" found, may remove now
        //
        //now remove pins and handle possible ex spec relocation
        for(MessagePinWidget pin:pins)
        {
            Widget par=pin.getParentWidget();
            if(par!=null)
            {
                par.removeChild(pin);
                if(par instanceof ExecutionSpecificationThinWidget)
                {
                    Widget parpar=par.getParentWidget();
                    if(parpar!=null)
                    {
                        if(par.getChildren().size()==0)
                        {
                            parpar.removeChild(par);//remove empty execution specification
                        }
                    }
                }
                else
                {
                   //just remove pin and do nothing else for former createdlifeline 
                    //or for combined fragments
                }
            }
        }
    }

    @Override
    public void load(EdgeInfo edgeReader)
    {
        LabelManager manager = getLabelManager();
        if (manager != null)
        {
            List<EdgeInfo.EdgeLabel> edgeLabels = edgeReader.getLabels();
            for (Iterator<EdgeInfo.EdgeLabel> it = edgeLabels.iterator(); it.hasNext();)
            {
                EdgeInfo.EdgeLabel edgeLabel = it.next();
                if (edgeLabel.getLabel().equalsIgnoreCase(NAME))
                {
                    manager.showLabel(NAME, null, edgeLabel.getPosition());
                } 
                else if (edgeLabel.getLabel().equalsIgnoreCase(STEREOTYPE))
                {
                    manager.showLabel(STEREOTYPE, null, edgeLabel.getPosition());
                } 
                else if (edgeLabel.getLabel().equalsIgnoreCase(TAGGEDVALUE))
                {
                    manager.showLabel(TAGGEDVALUE, null, edgeLabel.getPosition());
                }
                else if (edgeLabel.getLabel().equalsIgnoreCase(OPERATION))
                {
                    manager.showLabel(OPERATION, null, edgeLabel.getPosition());
                }
            }
        }
    }

}
