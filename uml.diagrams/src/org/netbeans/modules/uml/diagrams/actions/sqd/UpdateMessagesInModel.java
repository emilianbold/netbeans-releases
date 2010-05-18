/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 * provider need to find out if message order was changed and update model
 * (consider: may also check owning by cf/operands instead of container inner logic)
 * @author sp153251
 */
public class UpdateMessagesInModel implements ActionProvider {

    private ArrayList<MessageWidget> messages;
    private DesignerScene scene;
    private IInteraction interaction;
    
    public UpdateMessagesInModel(ArrayList<MessageWidget> messages)
    {
        this.messages=messages;
//        Collections.sort(messages,new Comparator<MessageWidget>() {
//            public int compare(MessageWidget o1, MessageWidget o2) {
//                int diff=o1.getSourceAnchor().getRelatedSceneLocation().y-o2.getSourceAnchor().getRelatedSceneLocation().y;
//                return diff;
//            }
//        });
        if(messages.size()>0)
        {
            scene=(DesignerScene) messages.get(0).getScene();
            INamespace ns=scene.getDiagram().getNamespace();
            if(ns instanceof IInteraction)
            {
                interaction=(IInteraction) ns;
            }
       }
    }
    
    /**
     * need to find messages and if order do not fit order in model exchange
     * currently do not work 20080605
     */
    public void perfomeAction() {
        if(scene!=null && interaction!=null)
        {
            ArrayList<IPresentationElement> allEdges=new ArrayList<IPresentationElement>();
            allEdges.addAll(scene.getEdges());
            ArrayList<MessageWidget> allMessageWs=new ArrayList<MessageWidget>();
            for(int i=allEdges.size()-1;i>=0;i--)
            {
                IPresentationElement pe=allEdges.get(i);
                if(pe.getFirstSubject() instanceof IMessage)
                {
                    allMessageWs.add((MessageWidget) scene.findWidget(pe));
                }
                else
                {
                    //allEdges.remove(i);
                }
            }
            if(allMessageWs.size()<2)return;
            Collections.sort(allMessageWs,new Comparator<MessageWidget>() {
                public int compare(MessageWidget o1, MessageWidget o2) {
                    int diff=o1.getSourceAnchor().getRelatedSceneLocation().y-o2.getSourceAnchor().getRelatedSceneLocation().y;
                    return diff;
                }
            });
            ETList<IMessage> allMsgObjsInInteraction=interaction.getMessages();
            IMessage nxtDgrMsg=null;
            for(int i=allMessageWs.size()-1;i>=0;i--)
            {
                MessageWidget mW=allMessageWs.get(i);
                IPresentationElement pe=(IPresentationElement) scene.findObject(mW);
                if(pe!=null)
                {
                    if(pe.getFirstSubject()!=null)
                    {
                        IMessage msg=(IMessage) pe.getFirstSubject();
                        int index=allMsgObjsInInteraction.indexOf(msg);
                        IMessage nxtModMsg=null;
                        if(index<(allMsgObjsInInteraction.size()-1))
                        {
                            nxtModMsg=allMsgObjsInInteraction.get(index+1);
                        }
                        if((nxtModMsg!=null && nxtDgrMsg==null) || (nxtModMsg==null && nxtDgrMsg!=null) || (nxtModMsg!=null && !nxtModMsg.equals(nxtDgrMsg)))
                        {
                            interaction.removeMessage(msg);
                            interaction.insertMessageBefore(msg, nxtDgrMsg);
                        }
                        nxtDgrMsg=msg;
                    }
                }
            }
        }
    }
}
