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

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.DiagramEngineResources;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * @author sumitabhk
 *
 */
public class ETEnumerationLiteralListCompartment extends ETNamedElementListCompartment
    implements IADEnumerationLiteralListCompartment
{
    
    /**
     *
     */
    public ETEnumerationLiteralListCompartment()
    {
        super();
        this.init();
    }
    
    public ETEnumerationLiteralListCompartment(IDrawEngine pDrawEngine)
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
            .getString("IDS_LITERALSCOMPARTMENTTITLE")); // NOI18N
        
        super.initResources();
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
                // Add any buttons this compartment needs and then pass the
                // message to the sub compartments
                int count = getNumCompartments();
                
                for (int i=0; i<count; i++)
                {
                    ICompartment pComp = getCompartment(i);
                    pComp.onContextMenu(manager);
                }
                
                //add other context menu items
                manager.add(createMenuAction(loadString(
                    "IDS_POPUP_INSERT_ENUMERATIONLITERAL"), // NOI18N
                    "MBK_INSERT_ENUMERATIONLITERAL")); // NOI18N
                
                manager.add(createMenuAction(loadString(
                    "IDS_POPUP_DELETE_ENUMERATIONLITERAL"), // NOI18N
                    "MBK_DELETE_ENUMERATIONLITERAL")); // NOI18N
            }
        }
    }
    
    /**
     * Adds an enumeration literal compartment.  The compartment is always made visible.
     *
     * @param pCompartment - The compartment to add.  If NULL then a new compartment is created and the editor invoked.
     * If non-null the compartment is simply inserted into the list.
     * @param nIndex - The position in the visible list to place this compartment.  If blank or -1 the compartment is
     * added to the bottom of the list.
     */
    public long addCompartment(
        ICompartment pCompartment, int nIndex, boolean bRedrawNow)
    {
        if (pCompartment instanceof IADEnumerationLiteralCompartment)
        {
            //call super from ListCompartmentImpl
            super.addCompartment(pCompartment, nIndex, bRedrawNow);
        }
        
        else
        {
            IDrawEngine pEngine = m_engine;
            IEnumeration pEnumeration = null;
            
            if (pEngine != null)
            {
                IElement pElem = TypeConversions.getElement(pEngine);
                
                if (pElem instanceof IEnumeration)
                {
                    pEnumeration = (IEnumeration)pElem;
                }
            }
            
            if (pEnumeration != null)
            {
                IElement pCreatedEle = DrawingFactory
                    .retrieveModelElement("EnumerationLiteral"); // NOI18N
                
                if (pCreatedEle != null && 
                    pCreatedEle instanceof IEnumerationLiteral)
                {
                    IEnumerationLiteral pEnumLiteral = 
                        (IEnumerationLiteral)pCreatedEle;
                    
                    // now add enumeration literal to the enumeration
                    pEnumeration.addLiteral(pEnumLiteral);
                    
                    // Call base class to create a new compartment at the insert position
                    ICompartment pCreatedComp = createAndAddCompartment(
                        "ADEnumerationLiteralCompartment", // NOI18N
                        nIndex, bRedrawNow);
                    
                    if (pCreatedComp != null)
                    {
                        // This routine will initialize the compartment, make it visible and
                        // begin the edit on it.
                        finishAddCompartment(
                            pEngine, pEnumLiteral, pCreatedComp, bRedrawNow);
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
        
        if (id.equals("MBK_INSERT_ENUMERATIONLITERAL")) // NOI18N
        {
            // Always sensitive, unless the diagram is readonly
            bFlag = true;
        }
        
        else if (id.equals("MBK_DELETE_ENUMERATIONLITERAL")) // NOI18N
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
        boolean retval = super.onHandleButton(e, id);
        
        if (id.equals("MBK_INSERT_ENUMERATIONLITERAL")) // NOI18N
            addCompartment(null, getRightMouseButtonIndex(), true);
        
        else if (id.equals("MBK_DELETE_ENUMERATIONLITERAL")) // NOI18N
            deleteSelectedCompartments(true);
        
        return retval;
    }
    
    /**
     * This is the name of the drawengine used when storing and reading from the product archive.
     *
     * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
     * product archive (etlp file).
     */
    public String getCompartmentID()
    {
        return "ADEnumerationLiteralListCompartment"; // NOI18N
    }
    
    public void addModelElement(IElement pElement, int pIndex)
    {
        // Call the base class first
        super.addModelElement(pElement, pIndex);
        
        // don't allow re-entrant adds
        if (m_engine != null)
        {
            IADEnumerationLiteralCompartment pNewCompartment = 
                new ETEnumerationLiteralCompartment();
            
            if (pNewCompartment != null)
            {
                pNewCompartment.setEngine(m_engine);
                pNewCompartment.addModelElement( pElement,-1 );
                super.addCompartment( pNewCompartment, pIndex, false);
            }
        }
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
            modelElementHasChanged2(pTargets);
        }
        
        return 0;
    }
}


