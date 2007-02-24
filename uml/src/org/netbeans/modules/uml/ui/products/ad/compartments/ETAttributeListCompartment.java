/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.DiagramEngineResources;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IDrawingAreaAcceleratorKind;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngineFactory;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import javax.swing.Action;
import javax.swing.KeyStroke;


public class ETAttributeListCompartment extends ETNamedElementListCompartment
    implements IADAttributeListCompartment
{
    
    /**
     *
     */
    public ETAttributeListCompartment()
    {
        super();
        this.init();
    }
    
    public ETAttributeListCompartment(IDrawEngine pDrawEngine)
    {
        super(pDrawEngine);
        this.init();
    }
    
    private void init()
    {
        this.initResources();
    }
    
    public void initResources()
    {
        this.setName(DiagramEngineResources
            .getString("IDS_ATTRIBUTESCOMPARTMENTTITLE")); // NOI18N
        
        super.initResources();
    }
    
    // TODO need to use IElement instead of ICompartment
    public boolean handleLeftMouseDrop(
        IETPoint pCurrentPos, List pCompartments, boolean bMoving)
    {
        boolean eventHandled = false;
        int insertionPoint = -1;
        
        if (this.getReadOnly())
            return true;
        
        INodeDrawEngine nodeDrawEngine = (INodeDrawEngine) this.getEngine();
        IClassifier targetClassifier = nodeDrawEngine.getParentClassifier();
        
        ICompartment targetCompartment = getCompartmentAtPoint(pCurrentPos);
        
        if (targetCompartment != null)
        {
            insertionPoint = getCompartmentIndex(targetCompartment);
        }
        
        Iterator iterator = pCompartments.iterator();
        while (iterator.hasNext())
        {
            ICompartment sourceCompartment = (ICompartment) iterator.next();
            
            // Insert only compartments of the same kind
            if (sourceCompartment instanceof ETClassAttributeCompartment)
            {
                try
                {
                    
                    IElement sourceElement = 
                        sourceCompartment.getModelElement();
                    
                    if (sourceElement instanceof IFeature)
                    {
                        
                        IFeature sourceFeature = (IFeature) sourceElement;
                        
                        // check if we're dropping on ourselves
                        IClassifier sourceClassifier = 
                            sourceFeature.getFeaturingClassifier();
                        
                        // dropping on ourselves, perform an index move instead
                        if (sourceClassifier != null && 
                            targetClassifier != null && 
                            sourceClassifier.getXMIID()
                                .equals(targetClassifier.getXMIID()))
                        {
                            ICompartment foundCompartment = 
                                findCompartmentContainingElement(sourceElement);
                            
                            moveCompartment(
                                foundCompartment,insertionPoint, false);
                        }
                        
                        else
                        {
                            if (sourceFeature != null)
                            {
                                if (bMoving)
                                {
                                    sourceFeature.moveToClassifier(
                                        targetClassifier);
                                    
                                    // refresh source node affected by the move operation
                                    sourceCompartment.getEngine().init();
                                    sourceCompartment.getEngine().invalidate();
                                }
                                
                                else
                                {
                                    sourceFeature.duplicateToClassifier(
                                        targetClassifier);
                                }
                            }
                            
                            //refresh the target node affected by the move/copy operation;
                            this.getEngine().init();
                            this.getEngine().invalidate();
                        }
                    }
                    
                    eventHandled = true;
                }
                
                catch (ETException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        return eventHandled;
    }
    
    public void addModelElement(IElement pElement, int pIndex)
    {
        try
        {
            ICompartment newCompartment = 
                ETDrawEngineFactory.createCompartment(
                    ETDrawEngineFactory.CLASS_ATTRIBUTE_COMPARTMENT);
            
            if (newCompartment != null)
            {
                newCompartment.setEngine(this.getEngine());
                newCompartment.addModelElement(pElement, -1);
                this.addCompartment(newCompartment, pIndex, false);
            }
        }
        
        catch (ETException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Called when the context menu is about to be displayed.
     * The compartment should add whatever buttons it might need.
     *
     * @param pContextMenu [in] The context menu about to be displayed
     * @param logicalX [in] The logical x location of the context menu event
     * @param logicalY [in] The logical y location of the context menu event
     */
    public void onContextMenu(IMenuManager manager)
    {
        if (getEnableContextMenu())
        {
            Point point = manager.getLocation();
            
            // (LLS) Adding the buildContext logic to support A11Y issues.  The
            // user should be able to use the CTRL-F10 keystroke to activate
            // the context menu.  In the case of the keystroke the location
            // will not be valid.  Therefore, we have to just check if the
            // compartment is selected.
            //
            // A list compartment can not be selected.  Therefore, when
            // CTRL-F10 is pressed, we must always show the list compartment
            // menu items.
            boolean buildMenu = true;
            if(point != null)
            {
                buildMenu = containsPoint(point);
            }
            
            if (buildMenu == true)
            {
                // Add any buttons this compartment needs and then pass the message
                // to the sub compartments
                int count = getNumCompartments();
                
                for (int i=0; i<count; i++)
                {
                    ICompartment pComp = getCompartment(i);
                    pComp.onContextMenu(manager);
                }
                
                //add rest of the menu items
				manager.add(createMenuAction(loadString(
                    "IDS_POPUP_INSERT_ATTRIBUTE"), // NOI18N
                    "MBK_INSERT_ATTRIBUTE",IDrawingAreaAcceleratorKind.DAVK_CREATE_ATTRIBUTE));

                manager.add(createMenuAction(loadString(
                    "IDS_POPUP_DELETE_ATTRIBUTE"),  // NOI18N
                    "MBK_DELETE_ATTRIBUTE")); // NOI18N
            }
        }
    }
    
    /**
     * Adds an attribute compartment.  The compartment is always made visible.
     *
     * @param pCompartment - The compartment to add.  If NULL then a new compartment is created and the editor invoked.
     * If non-null the compartment is simply inserted into the list.
     * @param nIndex - The position in the visible list to place this compartment.  If blank or -1 the compartment is
     * added to the bottom of the list.
     * @param bRedrawNow [in] true to redraw the engine now
     */
    public long addCompartment(
        ICompartment pCompartment,
        int nIndex,
        boolean bRedrawNow)
    {
        if (pCompartment instanceof IADClassAttributeCompartment)
        {
            //call it from ListCompartmentImpl
            super.addCompartment(pCompartment, nIndex, bRedrawNow);
        }
        
        else
        {
            IDrawEngine pEngine = m_engine;
            IElement pElement = null;
            
            if (pEngine != null)
            {
                pElement = TypeConversions.getElement(pEngine);
                IAttribute pAttr = null;
                
                if (pElement != null && pElement instanceof IClassifier)
                {
                    IClassifier pClass = (IClassifier)pElement;
                    
                    // create attribute but do not attach yet
                    pAttr = pClass.createAttribute3();
                    
                    // now add attribute to the classifier
                    pClass.addAttribute(pAttr);
                }
                
                else if (pElement != null && 
                         pElement instanceof IAssociationEnd)
                {
                    IAssociationEnd pAssocEnd = (IAssociationEnd) pElement;
                    
                    // create attribute but do not attach yet
                    pAttr = pAssocEnd.createQualifier3();
                    
                    // now add attribute to the classifier
                    pAssocEnd.addQualifier(pAttr);
                }
                
                if (pAttr != null)
                {
                    // Call base class to create a new compartment at the insert position
                    ICompartment pCreatedComp = createAndAddCompartment(
                        "ADClassAttributeCompartment", // NOI18N
                        nIndex, bRedrawNow);
                    
                    if (pCreatedComp != null)
                    {
                        // This routine will initialize the compartment, make it visible and
                        // begin the edit on it.
                        finishAddCompartment(
                            pEngine, pAttr, pCreatedComp, bRedrawNow);
                    }
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Sets the sensitivities and check states of the buttons created and owned by this implementor.  By default the
     * buttons are created with sensitivity == true which means they are enabled.
     *
     * @param pContextMenu [in] The parent context menu that was displayed
     * @param pMenuItem [in] The button that the sensitivity is being requested for
     * @param buttonKind [in] The ID of the button above.  This ID is the one used when creating the button.
     */
    public boolean setSensitivityAndCheck(
        String id, ContextMenuActionClass pMenuAction)
    {
        boolean bFlag = super.setSensitivityAndCheck(id, pMenuAction);
        if (id.equals("MBK_INSERT_ATTRIBUTE")) // NOI18N
        {
            // Always sensitive, unless the diagram is readonly
            bFlag = true;
        }
        else if (id.equals("MBK_DELETE_ATTRIBUTE")) // NOI18N
        {
            bFlag = getHasSelectedCompartments();
        }
        return isParentDiagramReadOnly() ? false : bFlag;
    }
    
    /**
     * Called when the context menu button has been selected.  The compartment should handle the selection.
     *
     * @param pContextMenu [in] The context menu that was displayed to the user
     * @param pMenuItem [in] The menu that was just selected
     */
    public boolean onHandleButton(ActionEvent e, String id)
    {
        boolean retVal = super.onHandleButton(e, id);

        if (id.equals("MBK_INSERT_ATTRIBUTE")) // NOI18N
            addCompartment(null, getRightMouseButtonIndex(), true);

        else if (id.equals("MBK_DELETE_ATTRIBUTE")) // NOI18N
            deleteSelectedCompartments(true);

        return retVal;
    }
    
    /**
     * This is the name of the drawengine used when storing and reading from the product archive.
     *
     * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
     * product archive (etlp file).
     */
    public String getCompartmentID()
    {
        return "ADAttributeListCompartment"; // NOI18N
    }
    
    /**
     * Notifier that the model element has changed.
     *
     * @param pTargets [in] Information about what has changed.
     */
    public long modelElementHasChanged(INotificationTargets pTargets)
    {
        if (pTargets != null)
        {
            IElement pSecEle = pTargets.getSecondaryChangedModelElement();
            if (pSecEle != null)
            {
                String elemType = pSecEle.getElementType();
                if (elemType != null && elemType.equals("Attribute")) // NOI18N
                {
                    modelElementHasChanged2(pTargets);
                }
            }
        }
        return 0;
    }
    
    public void deleteSelectedCompartments(boolean bPrompt)
    {
        String sTitle = DiagramEngineResources
            .getString("IDS_POPUP_DELETE_ATTRIBUTE_TITLE"); // NOI18N
        
        String sText = DiagramEngineResources
            .getString("IDS_DELETE_ATTRIBUTE"); // NOI18N
        
        deleteSelectedCompartments( sTitle, sText, bPrompt );
    }
}
