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

package org.netbeans.modules.uml.drawingarea.persistence.readers;

import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.drawingarea.persistence.api.GraphNodeReader;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;

/**
 *
 * @author jyothi
 */
public class GraphNodeReaderFactory {

    public static GraphNodeReader getReader(NodeInfo nodeInfo)
    {
        GraphNodeReader retVal = null;
        
        //try to get the IElement based on nodeInfo
        //Figure out what reader should be initalized from the layer file
        IProject project = nodeInfo.getProject();
        String meid = nodeInfo.getMEID();
        IElementLocator locator = new ElementLocator();
        IElement elt = locator.findByID(project, meid);
        if (elt != null)
        {
            //create appropriate Reader..
            if (elt instanceof IActivityPartition)
            {
                retVal = new ActivityPartitionReader(nodeInfo);
            }
            else if (elt instanceof ICombinedFragment || elt instanceof IInteractionOperand)
            {
                retVal = new CombinedFragmentReader(nodeInfo);
            }
            // we don't need special handling for packages
//            else if (elt instanceof IPackage)
//            {
//                retVal = new PackageReader(nodeInfo);
//            }
            else if (elt instanceof IState || elt instanceof IRegion)
            {
                retVal = new CompositeStateReader(nodeInfo);
            }
            else if (elt instanceof IOperation || elt instanceof IAttribute)
            {
                retVal = new FeatureStateReader(nodeInfo);
            }
            else
            {
                retVal = new DefaultReader(nodeInfo);
            }
            
        }
        
        
        
        return retVal;
    }
}
