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

/*
 * File       : CallBehaviorAction.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.BehaviorInvocation;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IBehaviorInvocation;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IPin;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.PrimitiveAction;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class CallBehaviorAction
    extends PrimitiveAction
    implements ICallBehaviorAction
{
    private IBehaviorInvocation behaviorInvoc = new BehaviorInvocation();
    
    public CallBehaviorAction()
    {
        behaviorInvoc = new BehaviorInvocation();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        behaviorInvoc.setNode(n);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ICallBehaviorAction#getIsSynchronous()
     */
    public boolean getIsSynchronous()
    {
        return getBooleanAttributeValue("isSynchronous", true);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.ICallBehaviorAction#setIsSynchronous(boolean)
     */
    public void setIsSynchronous(boolean isSynchronous)
    {
        setBooleanAttributeValue("isSynchronous", isSynchronous);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:CallBehaviorAction", doc, node);
    }


    ///////// IBehaviorInvocation delegate methods /////////
    public IBehavior getBehavior()
    {
        return behaviorInvoc.getBehavior();
    }

    public ETList<IPin> getResults()
    {
        return behaviorInvoc.getResults();
    }

    public void addBehaviorArgument(IPin pin)
    {
        behaviorInvoc.addBehaviorArgument(pin);
    }

    public void addResult(IPin pin) 
    {
        behaviorInvoc.addResult(pin);
    }

    public void removeBehaviorArgument(IPin pin) 
    {
        behaviorInvoc.removeBehaviorArgument(pin);
    }
    
    public ETList<IPin> getBehaviorArguments()
    {
        return behaviorInvoc.getBehaviorArguments();
    }

    public void removeResult(IPin pin) 
    {
        behaviorInvoc.removeResult(pin);
    }

    public void setBehavior(IBehavior behavior) 
    {
        behaviorInvoc.setBehavior(behavior);
    }
}
