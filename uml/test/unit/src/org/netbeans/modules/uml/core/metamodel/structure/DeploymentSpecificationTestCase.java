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


package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 */
public class DeploymentSpecificationTestCase extends AbstractUMLTestCase
{
    private IDeploymentSpecification depSpec = null;
    
    public DeploymentSpecificationTestCase()
    {
        super();
    }
    
    protected void setUp()
    {
        depSpec = factory.createDeploymentSpecification(null);
        project.addElement(depSpec);
    }
    
    public void testAddDeploymentDescriptor()
    {
        IArtifact arti = factory.createArtifact(null);
        project.addElement(arti);
        depSpec.addDeploymentDescriptor(arti);
        ETList<IArtifact> elems = depSpec.getDeploymentDescriptors();
        assertNotNull(elems);
        
        IArtifact artiGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                artiGot = elems.get(i);
            }
        }
        assertEquals(arti, artiGot);
    }
    
    public void testSetContainer()
    {
        INode node = factory.createNode(null);
        project.addElement(node);
        depSpec.setContainer(node);
        assertEquals(node,depSpec.getContainer());
    }
    
    public void testSetDeploymentLocation()
    {
        String loc = "newDepLoc";
        depSpec.setDeploymentLocation(loc);
        assertEquals( loc, depSpec.getDeploymentLocation());
    }
    
    
    public void testSetExecutionLocation()
    {
        String loc = "newExecLoc";
        depSpec.setExecutionLocation(loc);
        assertEquals(loc, depSpec.getExecutionLocation());
    }
    
    public void testAddDeployment()
    {
        IDeployment dep = factory.createDeployment(null);
        project.addElement(dep);
        depSpec.addDeployment(dep);
        ETList<IDeployment> elems = depSpec.getDeployments();
        assertNotNull(elems);
        
        IDeployment depGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                depGot = elems.get(i);
            }
        }
        assertEquals(dep, depGot);
    }
    
    public void testSetConfiguredComponent()
    {
        IComponent comp = factory.createComponent(null);
        project.addElement(comp);
        depSpec.setConfiguredComponent(comp);
        assertEquals(comp,depSpec.getConfiguredComponent());
    }
    
    public void testSetConfiguredAssembly()
    {
        IComponentAssembly ca = (IComponentAssembly)FactoryRetriever.instance().createType("ComponentAssembly", null);
        //ca.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(ca);
        depSpec.setConfiguredAssembly(ca);
        assertEquals(ca.getXMIID(), depSpec.getConfiguredAssembly().getXMIID());
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(DeploymentSpecificationTestCase.class);
    }
}


