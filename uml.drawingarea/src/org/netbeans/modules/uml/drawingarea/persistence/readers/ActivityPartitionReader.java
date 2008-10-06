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

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.GraphNodeReader;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.util.Exceptions;

/**
 *
 * @author jyothi
 */
class ActivityPartitionReader implements GraphNodeReader {

    DesignerScene scene;
        private IElementLocator locator = new ElementLocator();
        private NodeInfo nodeInfo;
    
        
    public ActivityPartitionReader(NodeInfo nodeInfo)
    {    
        this.nodeInfo = nodeInfo;
    }

    public void finalizeReader()
    {
        try
        {
            this.finalize();
        } catch (Throwable ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    public GraphNodeReader foundChild()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GraphNodeReader initializeReader(Scene scene, NodeInfo nodeInfo)
    {
        this.scene = (DesignerScene) scene;
        return this;
        
    }

    public void processGraphNode(GraphNodeReader peek, NodeInfo nodeInfo)
    {
        if (peek == null)
            addNodeToScene(nodeInfo);
        else if (peek instanceof ActivityPartitionReader)
        {
            //we know it is a sub-partition.. 
            //do not add subpartitions it is already added as a part of partition..
            //just get size, orientation, position info and apply it
            Widget widget = scene.findWidget(peek.getNodeInfo().getPresentationElement());
            if (widget != null && widget instanceof DiagramNodeReader)
            {
                ((DiagramNodeReader)widget).load(nodeInfo);
            }
        }
        else
        {
            addChild(nodeInfo);
        }
    }

    private void addChild(NodeInfo nodeInfo)
    {
        IElement elt = getElement(nodeInfo.getProject(), nodeInfo.getMEID());
        if (elt != null)
        {
            addNodeToScene(nodeInfo);
        }
        else
        {
            //it is just a visual elt.. no associated model elt
        }
    }

     private IElement getElement(IProject project, String sModelElementID)
    {
        IElement element = null;
        if (project != null)
        {
            element = locator.findElementByID(project, sModelElementID);
        }
        return element;
    }

     
    private void addNodeToScene(NodeInfo nodeInfo)
    {
        try
        {
            IElement elt = getElement(nodeInfo.getProject(), nodeInfo.getMEID());
            if (elt == null)
            {
                //there is nothing to add.. so return..
                return;
            }
            IPresentationElement pE = elt.getPresentationElementById(nodeInfo.getPEID());
            if (pE == null)
            {
                pE = Util.createNodePresentationElement();
            }
            pE.setXMIID(nodeInfo.getPEID());
            pE.addSubject(elt);
            nodeInfo.setPresentationElement(pE);
            nodeInfo.setModelElement(elt);
            
            DiagramEngine engine = scene.getEngine();                    
           
            if (engine.createWidget(pE) != null)
            {
                Widget widget = engine.addWidget(pE, nodeInfo.getPosition());
                //
                if (widget instanceof DiagramNodeReader)
                {
                    nodeInfo.setPresentationElement(pE);
                    //load all the properties
                    ((DiagramNodeReader) widget).load(nodeInfo);
                }
            }
            else
            {
//                System.out.println("  engine.createWidget is returning null.... ");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public NodeInfo getNodeInfo()
    {
        return nodeInfo;
    }
    
    public void processDependencies()
    {
        Widget widget = scene.findWidget(nodeInfo.getPresentationElement());
        if (widget != null && widget instanceof DiagramNodeReader)
        {
            ((DiagramNodeReader)widget).loadDependencies(nodeInfo);
        }
    }
    
}

