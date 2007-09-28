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



package org.netbeans.modules.uml.ui.products.ad.diagramengines.diagramComponentEngine;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IADInterfaceEventManager;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETComponentDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IPortDrawEngine;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.jnilayout.TSSide;
import org.netbeans.modules.uml.ui.support.TSSide;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * @author josephg
 *
 */
public class PortDrawEngine extends ADNodeDrawEngine implements IPortDrawEngine
{
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
         */
    public String getDrawEngineID()
    {
        return "PortDrawEngine";
    }
    
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
         */
    public void createCompartments() throws ETException
    {
        clearCompartments();
        
        createAndAddCompartment("BoxCompartment",0);
    }
    
    public boolean isDrawEngineValidForModelElement()
    {
        boolean result = false;
        
        if(getMetaTypeOfElement().equals("Port"))
        {
            result = true;
        }
        
        return result;
    }
    
    public final static int NODE_WIDTH = 10;
    public final static int NODE_HEIGHT = 10;
    
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
         */
    public void sizeToContents()
    {
        // Originally the last parameter was set to true.  However, this did not work when
        // the diagram was zoomed in and the user created an interface.  The port was too big.
        sizeToContentsWithMin( NODE_WIDTH, NODE_HEIGHT, false, false );
    }
    
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
         */
    public void onGraphEvent(int nKind)
    {
        switch(nKind)
        {
            case IGraphEventKind.GEK_POST_MOVE:
            case IGraphEventKind.GEK_POST_RESIZE:
            case IGraphEventKind.GEK_POST_CREATE:
                IComponentDrawEngine componentDE = getComponentDrawEngine();
                if(componentDE != null)
                {
                    componentDE.restoreAllPortPositions();
                }
                break;
            case IGraphEventKind.GEK_POST_COPY:
            case IGraphEventKind.GEK_POST_PASTE_ALL:
                reestablishPresentationReference();
                break;
        }
    }
    
    public ETComponentDrawEngine getComponentDrawEngine()
    {
        IPresentationElement thisPE = getPresentationElement();
        
        if(thisPE != null)
        {
            ETList<IReference> references = thisPE.getReferredReferences();
            
            Iterator iter = references.iterator();
            while(iter.hasNext())
            {
                IReference reference = (IReference)iter.next();
                
                if(reference != null)
                {
                    IElement tempElement = reference.getReferencingElement();
                    
                    if(tempElement instanceof IPresentationElement)
                    {
                        IPresentationElement componentPE = (IPresentationElement)tempElement;
                        
                        IDrawEngine drawEngine = TypeConversions.getDrawEngine(componentPE);
                        if(drawEngine != null)
                        {
                            if(drawEngine instanceof ETComponentDrawEngine)
                                return (ETComponentDrawEngine)drawEngine;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * @return TSSide value indicating which side this port is located on for its parant component
     */
    public int getComponentSide()
    {
        int side = TSSide.TS_SIDE_UNDEFINED;
        
        ETComponentDrawEngine engine = getComponentDrawEngine();
        if (engine != null)
        {
            TSENode portNode = getOwnerNode();
            TSENode componentNode = TypeConversions.getOwnerNode(engine);
            if ( (portNode != null) &&
                (componentNode != null) )
            {
                TSConstRect componentRect = componentNode.getBounds();
                TSConstPoint portCenter = portNode.getCenter();
                
                side = RectConversions.getClosestSide( componentRect, portCenter );
            }
        }
        
        return side;
    }
    
    public void distributeAttachedInterfaces(boolean redraw)
    {
        IEventManager eventManager = getEventManager();
        if(eventManager instanceof IADInterfaceEventManager)
        {
            IADInterfaceEventManager interfaceEventManager = (IADInterfaceEventManager)eventManager;
            interfaceEventManager.distributeAttachedInterfaces(redraw);
        }
    }
    
    public String getManagerMetaType(int nManagerKind)
    {
        String type = null;
        
        if(nManagerKind == MK_EVENTMANAGER)
        {
            type = "ADInterfaceEventManager";
        }
        
        return type;
    }
    
    public void reestablishPresentationReference()
    {
        INodePresentation nodePresentation = getNodePresentation();
        IElement portElement = getFirstModelElement();
        
        if(nodePresentation == null || portElement == null)
            return;
        
        ETList<IPresentationElement> qualifierParents = PresentationReferenceHelper.getAllReferencingElements(nodePresentation);
        
        if(qualifierParents != null && qualifierParents.size() > 0)
            return;
        
        IElement component = portElement.getOwner();
        
        if(component == null)
            return;
        
        IPresentationElement peToAttachTo = nodePresentation.findNearbyElement(true,component,"ComponentDrawEngine");
        
        if(peToAttachTo == null)
            return;
        
        PresentationReferenceHelper.createPresentationReference(peToAttachTo,nodePresentation);
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine#drawContents(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
         */
    protected void drawContents(IDrawInfo pDrawInfo)
    {
        TSEGraphics graphics = pDrawInfo.getTSEGraphics();
        TSTransform transform = graphics.getTSTransform();
        IETRect deviceBounds = pDrawInfo.getDeviceBounds();
        
        List compartments = getCompartments();
        
        Iterator iter = compartments.iterator();
        
        while(iter.hasNext())
        {
            ICompartment compartment = (ICompartment)iter.next();
            
            compartment.draw(pDrawInfo,deviceBounds);
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
         */
    public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
    {
        IETSize retVal = new ETSize( NODE_WIDTH, NODE_HEIGHT );
        
        if( !bAt100Pct &&
            (retVal != null) )
        {
            TSTransform transform = pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform();
            retVal = scaleSize( retVal, transform );
        }
        
        return retVal;
    }
    
    public void initCompartments(IPresentationElement pElement)
    {
        int numComps = getNumCompartments();
        if (numComps == 0)
        {
            try
            {
                createCompartments();
                numComps = getNumCompartments();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void movePortsToSide(int nSide, ETList < IPresentationElement > selectedPorts)
    {
        ETComponentDrawEngine compDrawingEngine = this.getComponentDrawEngine();
        if (compDrawingEngine != null )
        {
            if (selectedPorts != null)
            {
                compDrawingEngine.movePortsToSide(nSide, selectedPorts);
            }
        }
    }
}
