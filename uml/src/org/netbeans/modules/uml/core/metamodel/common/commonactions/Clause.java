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
 * File       : Clause.java
 * Created on : Sep 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Element;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class Clause extends Element implements IClause
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#addPredecessor(org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause)
     */
    public void addPredecessor(final IClause pClause)
    {        
        new ElementConnector<IClause>()
            .addChildAndConnect(this, true, "predecessorClause", "predecessorClause",
                                pClause,
                                new IBackPointer<IClause>() {
                                    public void execute(IClause obj) {
                                        pClause.addSuccessor(obj);
                                    }
                                }
        );        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#addSuccessor(org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause)
     */
    public void addSuccessor(final IClause pClause)
    {
        new ElementConnector<IClause>()
            .addChildAndConnect(this, true, "successorClause", "successorClause",
                                pClause,
                                new IBackPointer<IClause>() {
                                    public void execute(IClause obj) {
                                        pClause.addPredecessor(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#addToBody(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addToBody(IAction pAction)
    {
        addChild("UML:Clause.body","UML:Clause.body", pAction);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#addToTest(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addToTest(IAction pAction)
    {
        addChild("UML:Clause.test","UML:Clause.test", pAction);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#getBody()
     */
    public ETList<IAction> getBody()
    {
        return new ElementCollector< IAction >()
            .retrieveElementCollection((IClause)this, "UML:Clause.body/*", IAction.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#getPredecessors()
     */
    public ETList<IClause> getPredecessors()
    {
        return new ElementCollector< IClause >()
            .retrieveElementCollectionWithAttrIDs(this, "predecessorClause", IClause.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#getSuccessors()
     */
    public ETList<IClause> getSuccessors()
    {
        return new ElementCollector< IClause >()
            .retrieveElementCollectionWithAttrIDs(this, "successorClause", IClause.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#getTest()
     */
    public ETList<IAction> getTest()
    {
        return new ElementCollector< IAction >()
            .retrieveElementCollection((IClause)this, "UML:Clause.test/*", IAction.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#getTestOutput()
     */
    public IValueSpecification getTestOutput()
    {
        return new ElementCollector< IValueSpecification >()
            .retrieveSingleElementWithAttrID(this, "testOutput", IValueSpecification.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#removeFromBody(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeFromBody(IAction pAction)
    {
        UMLXMLManip.removeChild(m_Node, pAction );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#removeFromTest(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeFromTest(IAction pAction)
    {
        UMLXMLManip.removeChild(m_Node, pAction );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#removePredecessor(org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause)
     */
    public void removePredecessor(final IClause pClause)
    {
        new ElementConnector<IClause>()
            .removeByID(this, pClause, "predecessorClause",
                                new IBackPointer<IClause>() {
                                    public void execute(IClause obj) {
                                        pClause.removeSuccessor(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#removeSuccessor(org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause)
     */
    public void removeSuccessor(final IClause pClause)
    {
        new ElementConnector<IClause>()
            .removeByID(this, pClause, "successorClause",
                                new IBackPointer<IClause>() {
                                    public void execute(IClause obj) {
                                        pClause.removePredecessor(obj);
                                    }
                                }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IClause#setTestOutput(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
     */
    public void setTestOutput(IValueSpecification pValSpec)
    {
        addElementByID(pValSpec, "testOutput");
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Clause", doc, node);
    }     

}
