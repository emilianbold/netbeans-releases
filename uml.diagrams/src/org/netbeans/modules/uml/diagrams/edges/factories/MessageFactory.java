/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.edges.factories;

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.dynamics.DynamicsRelationFactory;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 * @author psb
 */
public class MessageFactory extends AbstractRelationshipFactory
{
    private DynamicsRelationFactory factory=new DynamicsRelationFactory();


    /**
    * Creates connector between source and target lifelines
    *
     * @param source
     * @param target
     * @return
     */
    public IRelationship create(IElement source, IElement target)
    {

        if(target instanceof ILifeline && source instanceof ILifeline)
        {
            IMessageConnector connector=factory.createMessageConnector((ILifeline) source, (ILifeline) target);
            return connector;
        }
        else return null;
    }
    /**
    * Create message, insert before messages after y position
    *
     * @param scene
     * @param source
     * @param target
     * @param kind
     * @param y
     * @return
     */
    public IMessage createMessage(ObjectScene scene,IElement source, IElement target,int kind,int y)
    {
        IInteraction fromOwner=(IInteraction) OwnerRetriever.getOwnerByType(source,IInteractionFragment.class);//TBD, may be different for "inner" messages
        if(fromOwner==null && source instanceof IInteraction)fromOwner=(IInteraction) source;
        IMessage messageAfter=getMessageNextToPoint(scene, fromOwner, y);
        return createMessage(source,target,kind,messageAfter);
    }
    
    
    /**
     * find message after suggested point within interaction
     * 
     * 
     * 
     * @param scene
     * @param fromOwner interaction for from lifeline (usually sqd itself)
     * @param y point
     * @return messae next to point or null
     */
    public IMessage getMessageNextToPoint(ObjectScene scene,IInteraction fromOwner,int y)
    {
         ETList<IMessage> messages=fromOwner.getMessages();
        IMessage messageAfter=null;
        for(int i=messages.size()-1;i>=0;i--)//TBD check if not sorted, and correclt for better perfomance if possible
        {
            IMessage tmp=messages.get(i);
            ETList<IPresentationElement> preselements=tmp.getPresentationElements();
            for(int j=0;j<preselements.size();j++)
            {
                Widget w=scene.findWidget(preselements.get(j));
                if(w instanceof ConnectionWidget)
                {
                    ConnectionWidget con=(ConnectionWidget) w;
                    //check only source anchor, in most cases source has the same level
                    if(con.getSourceAnchor().getRelatedSceneLocation().y>y)messageAfter=tmp;
                    else return messageAfter;
                }
            }
        }
        return messageAfter;
   }
    
    /**
     * 
     * @param beforeMessage message located next to newly created message
     *
     */
    public IMessage createMessage(IElement source, IElement target,int kind,IMessage beforeMessage)
    {
        //create new message between source and target
        IMessage message=factory.insertMessage(beforeMessage,source,(IInteractionFragment) OwnerRetriever.getOwnerByType(source,IInteractionFragment.class), target,(IInteractionFragment) OwnerRetriever.getOwnerByType(target,IInteractionFragment.class), null, kind);
        return message;
    }

    /**
     * 
     * @param relationship
     * @param source
     */
    public void reconnectSource(IElement element, IElement source)
    {
        //direct sourece reconnection isn't supported but no need to thow exception, may be used for child messages reconnection
        IMessage msg=(IMessage) element;
        msg.changeSendingLifeline(msg.getSendingLifeline(), (ILifeline) source);
    }

    /**
     * 
     * @param relationship
     * @param target
     */
    public void reconnectTarget(IElement element, IElement target)
    {
        IMessage msg=(IMessage) element;
        msg.changeReceivingLifeline(msg.getReceivingLifeline(), (ILifeline)target);
    }
    
    public String getElementType()
    {
        return "Message";
    }
}
