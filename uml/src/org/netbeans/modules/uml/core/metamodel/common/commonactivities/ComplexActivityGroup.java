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
 * File       : ComplexActivityGroup.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ComplexActivityGroup extends IterationActivityGroup implements IComplexActivityGroup
{
    private IInterruptibleActivityRegion interActRegion = null;
    public ComplexActivityGroup()
    {
        interActRegion = new InterruptibleActivityRegion();
    }
    
//    public int getKind()
//    {
//        return getActivityGroupKind("groupKind");
//    }
//    public void setKind(int nKind)
//    {
//        setActivityGroupKind("groupKind", nKind);
//    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:ComplexActivityGroup", doc, node);
    }       
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInterruptibleActivityRegion#addInterruptingEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void addInterruptingEdge(IActivityEdge pEdge)
    {
        interActRegion.addInterruptingEdge(pEdge);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInterruptibleActivityRegion#getInterruptingEdges()
     */
    public ETList<IActivityEdge> getInterruptingEdges()
    {
        return interActRegion.getInterruptingEdges();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInterruptibleActivityRegion#removeInterruptingEdge(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge)
     */
    public void removeInterruptingEdge(IActivityEdge pEdge)
    {
        interActRegion.removeInterruptingEdge(pEdge);
    }

	public int getGroupKind()
	{
		return getActivityGroupKind("groupKind");
	}
	
	public void setGroupKind(int kind )
	{
		setActivityGroupKind("groupKind", kind);
	}
}
