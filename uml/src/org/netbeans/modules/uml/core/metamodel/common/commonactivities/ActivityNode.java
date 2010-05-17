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
 * File       : ActivityNode.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ActivityNode extends RedefinableElement implements IActivityNode
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#addGroup(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup)
     */
    public void addGroup(final IActivityGroup pGroup)
    {
              
        new ElementConnector<IActivityNode>()
            .addChildAndConnect(this, true, "group", "group",
                                pGroup,
                                new IBackPointer<IActivityNode>() {
                                    public void execute(IActivityNode obj) {
                                        pGroup.addNodeContent(obj);
                                    }
                                }
        );             

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#addIncomingEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void addIncomingEdge(final IActivityEdge pEdge)
    {
        new ElementConnector<IActivityNode>()
            .addChildAndConnect(this, true, "incoming", "incoming",
                                pEdge,
                                new IBackPointer<IActivityNode>() {
                                    public void execute(IActivityNode obj) {
                                        pEdge.setTarget(obj);
                                    }
                                }
        );    

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#addOutgoingEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void addOutgoingEdge(final IActivityEdge pEdge)
    {
        new ElementConnector<IActivityNode>()
            .addChildAndConnect(this, true, "outgoing", "outgoing",
                                pEdge,
                                new IBackPointer<IActivityNode>() {
                                    public void execute(IActivityNode obj) {
                                        pEdge.setSource(obj);
                                    }
                                }
        );    

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#getActivity()
     */
    public IActivity getActivity()
    {
		return OwnerRetriever.getOwnerByType(this, IActivity.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#getGroups()
     */
    public ETList<IActivityGroup> getGroups()
    {
        return new ElementCollector< IActivityGroup >().
            retrieveElementCollectionWithAttrIDs(this, "group", IActivityGroup.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#getIncomingEdges()
     */
    public ETList<IActivityEdge> getIncomingEdges()
    {
        return new ElementCollector< IActivityEdge >().
            retrieveElementCollectionWithAttrIDs(this, "incoming", IActivityEdge.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#getOutgoingEdges()
     */
    public ETList<IActivityEdge> getOutgoingEdges()
    {
        return new ElementCollector< IActivityEdge >().
            retrieveElementCollectionWithAttrIDs(this, "outgoing", IActivityEdge.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#removeGroup(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup)
     */
    public void removeGroup(final IActivityGroup pGroup)
    {
        new ElementConnector< IActivityNode >().
            removeByID( this, 
                        pGroup, 
                        "group", 
                        new IBackPointer<IActivityNode>() {
                            public void execute(IActivityNode obj) {
                                pGroup.removeNodeContent(obj);
                            }
                        }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#removeIncomingEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void removeIncomingEdge(final IActivityEdge pEdge)
    {
        new ElementConnector< IActivityNode >().
            removeByID( this, 
                        pEdge, 
                        "incoming", 
                        new IBackPointer<IActivityNode>() {
                            public void execute(IActivityNode obj) {
                                pEdge.setTarget(obj);
                            }
                        }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#removeOutgoingEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void removeOutgoingEdge(final IActivityEdge pEdge)
    {
        new ElementConnector< IActivityNode >().
            removeByID( this, 
                        pEdge, 
                        "outgoing", 
                        new IBackPointer<IActivityNode>() {
                            public void execute(IActivityNode obj) {
                                pEdge.setSource(obj);
                            }
                        }
        );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode#setActivity(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity)
     */
    public void setActivity(IActivity value)
    {
       setOwner(value);
    }

}
