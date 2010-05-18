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
 * File       : AttributeChangeFacility.java
 * Created on : Nov 21, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import java.lang.reflect.Modifier;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;


/**
 * @author Aztec
 */
public class AttributeChangeFacility extends RequestFacility
                                        implements IAttributeChangeFacility
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#addAttribute(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, boolean)
     */
    public void addAttribute(
        String sName,
        String sType,
        IClassifier pClassifier,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        addAttribute2(sName, sType, pClassifier, rtOffCreate, rtOffPostProcessing);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#addAttribute2(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, boolean)
     */
    public IAttribute addAttribute2(
        String sName,
        String sType,
        IClassifier pClassifier,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        if(pClassifier == null) return null;

        int roundTripMode = getRoundTripMode();

        // We do not want to send round trip events if rtOffCreate is true
        if (rtOffCreate && roundTripMode != RTMode.RTM_OFF)
        {
            setRoundTripMode(RTMode.RTM_OFF);
        }
        else
        {
            setRoundTripMode(RTMode.RTM_LIVE);
        }

        IAttribute pAttribute = null;
        
        try
        {
        	pAttribute = createAttribute(sName, sType, pClassifier);

			// Only handle the post processing of the attribute if round 
			// trip was off during the creation of the attribute.  If RT
			// was on during the creation of the attribute RT has already
			// done the post processing, so do not do it again.
			if(pAttribute != null && rtOffCreate)
			{         
				if(rtOffPostProcessing)
				{
					setRoundTripMode(RTMode.RTM_OFF);
				}
				else
				{
					setRoundTripMode(RTMode.RTM_LIVE);
				}

				added(pAttribute);
			}
        }
        finally
        {
			// Reset the state of RT.
			setRoundTripMode(roundTripMode);
        }

        return pAttribute;

    }


    public IAttribute addAttribute3(
        String sName,
        String sType,
        IClassifier pClassifier,
        boolean rtOffCreate,
        boolean rtOffPostProcessing,int modifierMask)
    {
        if(pClassifier == null) return null;

        int roundTripMode = getRoundTripMode();

        // We do not want to send round trip events if rtOffCreate is true
        if(rtOffCreate)
        {
            setRoundTripMode(RTMode.RTM_OFF);
        }
        else
        {
            setRoundTripMode(RTMode.RTM_LIVE);
        }

        IAttribute pAttribute = null;
        
        try
        {
            pAttribute = createAttribute(sName, sType, pClassifier);
            
            if(Modifier.isFinal(modifierMask))
                pAttribute.setIsFinal(true);
            if(Modifier.isStatic(modifierMask))
                pAttribute.setIsStatic(true);
            
            // Only handle the post processing of the attribute if round 
            // trip was off during the creation of the attribute.  If RT
            // was on during the creation of the attribute RT has already
            // done the post processing, so do not do it again.
            if(pAttribute != null && rtOffCreate)
            {         
                if(rtOffPostProcessing)
                {
                    setRoundTripMode(RTMode.RTM_OFF);
                }
                else
                {
                    setRoundTripMode(RTMode.RTM_LIVE);
                }

                added(pAttribute);
            }
        }
        finally
        {
            // Reset the state of RT.
            setRoundTripMode(roundTripMode);
        }
        
        return pAttribute;

    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#added(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void added(IAttribute pAttr)
    {
        // No valid implementation in the C++ code base.

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#changeAttributeType(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, java.lang.String)
     */
    public IAttribute changeAttributeType(
        IAttribute pAttr,
        IClassifier pClassifier,
        String sName,
        String sNewType)
    {
        if(pAttr == null || pClassifier == null) return null;
        
        String shortTypeNameStr = retrieveShortName(sNewType);
        boolean isDataType      = isDataType(shortTypeNameStr);
        
        IClassifier pAttrClassifier = getClassifier(sNewType, pClassifier);
        
        if(pAttrClassifier != null)
        {
            // First check if we are dealing with a IAttribute or a INavigableEnd.
            // Then check if the new type should be a member of the classifier or
            // an association.
            INavigableEnd pNavEnd = (pAttr instanceof INavigableEnd)
                                    ?(INavigableEnd)pAttr : null;
        
            if(pNavEnd != null && isDataType)
            {
                return changeNavigableToAttribute(sName, pNavEnd, pClassifier, pAttrClassifier);
            }
            else if(pNavEnd != null && !isDataType)
            {
                pNavEnd.setParticipant(pAttrClassifier);
                return pNavEnd;
            }
            else if(pNavEnd == null && isDataType)
            {
                pAttr.setType(pAttrClassifier);         
                return pAttr;
            }
            else
            {
                return changeAttributeToNavigable(sName, pAttr, pClassifier, pAttrClassifier);            
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#changeFinal(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, boolean)
     */
    public void changeFinal(
        IAttribute pAttribute,
        boolean isFinal,
        boolean rtOff)
    {
        if(pAttribute == null) return;
        
        RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils.getRTModeFromTurnOffFlag(rtOff));
        
        pAttribute.setIsFinal(isFinal);
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#changeInitializer(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, boolean)
     */
    public void changeInitializer(
        IAttribute pAttribute,
        String initializer,
        boolean rtOff)
    {
        if(pAttribute == null) return;
        
        RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils.getRTModeFromTurnOffFlag(rtOff));
        
        pAttribute.setDefault2(initializer);
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#changeMultiplicity(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, boolean)
     */
    public void changeMultiplicity(
        IAttribute pAttribute,
        IMultiplicity pMultiplicity,
        boolean rtOffWhileChanging,
        boolean rtOffPostProcessing)
    {
        if(pAttribute == null) return;
        
        RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils.getRTModeFromTurnOffFlag(rtOffWhileChanging));
        
        RoundTripUtils.setAttributeMultiplicity( pAttribute, pMultiplicity);
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#changeName(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, boolean, boolean)
     */
    public void changeName(
        IAttribute pAttr,
        String sNewName,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        if(pAttr == null) return;
        
        RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils
                                        .getRTModeFromTurnOffFlag( rtOffCreate ));
        
        pAttr.setName(sNewName);

        restorer.setMode(RoundTripUtils.
                            getRTModeFromTurnOffFlag(rtOffPostProcessing));
        
        nameChanged(pAttr);
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#changeStatic(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, boolean)
     */
    public void changeStatic(
        IAttribute pAttribute,
        boolean isStatic,
        boolean rtOff)
    {
        if(pAttribute == null) return;
        
        RoundTripModeRestorer restorer 
            =new RoundTripModeRestorer(RoundTripUtils.getRTModeFromTurnOffFlag(rtOff));
        
        pAttribute.setIsStatic(isStatic);
        
        restorer.restoreOriginalMode();
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#changeType(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, boolean, boolean)
     */
    public void changeType(
        IAttribute pAttr,
        String sNewType,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        if(pAttr == null) return;
        
        RoundTripModeRestorer restorer 
            = new RoundTripModeRestorer(RoundTripUtils
                                        .getRTModeFromTurnOffFlag( rtOffCreate ));
        
        pAttr.setType2(sNewType);

        restorer.setMode(RoundTripUtils.
                            getRTModeFromTurnOffFlag(rtOffPostProcessing));
        
        typeChanged(pAttr);
        
        restorer.restoreOriginalMode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#changeVisibility(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, int, boolean, boolean)
     */
    public void changeVisibility(
        IAttribute pAttr,
        int visibility,
        boolean rtOffCreate,
        boolean rtOffPostProcessing)
    {
        if(pAttr == null) return;
        
        RoundTripModeRestorer restorer =
            new RoundTripModeRestorer(RoundTripUtils
                                        .getRTModeFromTurnOffFlag(rtOffCreate));
        
        pAttr.setVisibility(visibility);
        
        restorer.restoreOriginalMode();

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#createAttribute(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public IAttribute createAttribute(
        String sName,
        String sType,
        IClassifier pClassifier)
    {
        if(pClassifier == null) return null;
        
        // Initialization of variables
        String shortTypeName = retrieveShortName(sType);                      
              
        ILanguage pLanguage = getLanguage();
                
                      
        // Now if the attribute type is a data type (as specified by the 
        // language manager then we want to create a IAttribute inside the
        // classifier.  Otherwise we want to create an association.
        if(pLanguage != null)
        {
            RoundTripModeRestorer rest = new RoundTripModeRestorer(RTMode.RTM_OFF);
            try
            {
				// First check if the type is been specified as a data type
				// either a primitive or user defined data type.
				boolean isDataType = pLanguage.isDataType(shortTypeName);
                
				IElement pElement = resolvedScopedElement(pClassifier, sType);
                    
				IClassifier pAttrType = (pElement instanceof IClassifier)
										?(IClassifier)pElement : null;
                
				if(pAttrType != null)
				{
					if(isDataType)
					{
						return createAttribute(sName, pAttrType, pClassifier);
					}
					else
					{
						return createAssociation(sName, 
													pAttrType, 
													pClassifier, 
													AssociationKindEnum.
														AK_AGGREGATION);
					}
				}
            }
            finally
            {
            	rest.restoreOriginalMode();
            }
        }
        
        return null;        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#delete(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, boolean)
     */
    public void delete(
        IAttribute pAttr,
        boolean rtOffDelete,
        boolean rtOffPostDelete)
    {
        if(pAttr == null) return;
        
        IClassifier pClassifier = pAttr.getFeaturingClassifier();
      
        if(pClassifier != null)
        {
            // save the original RT mode and set the mode to rtOffDelete
            RoundTripModeRestorer restorer = new RoundTripModeRestorer();
            
            // set the RT mode to rtOffDelete
            restorer.setMode(RoundTripUtils.getRTModeFromTurnOffFlag(rtOffDelete));
           
            // First delete the setter and getter of the attribute
            deleted(pAttr, pClassifier);

            restorer.setMode(RoundTripUtils.getRTModeFromTurnOffFlag( rtOffPostDelete));
            
            pClassifier.removeFeature(pAttr);
            
            INavigableEnd pNavigableEnd = (pAttr instanceof INavigableEnd) ? (INavigableEnd)pAttr : null;
            
            if(pNavigableEnd != null) 
            {
                IAssociation pAssociation = pNavigableEnd.getAssociation();
                
                if(pAssociation != null) 
                {
                    pAssociation.delete();
                }
            } 
            
            // delete the attribute itself
            pAttr.delete();
            
            // restore the orginal mode
            restorer.restoreOriginalMode();

        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#deleted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void deleted(IAttribute pAttr, IClassifier pClassifier)
    {
        // No valid implementation in the C++ code base.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#findAndChangeName(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void findAndChangeName(
        String sOldName,
        String sNewName,
        IClassifier pClassifier)
    {
        // No valid implementation in the C++ code base.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#findAndChangeType(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public IAttribute findAndChangeType(
        String sName,
        String sNewType,
        IClassifier pClassifier)
    {
        // No valid implementation in the C++ code base.
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#findAndDelete(java.lang.String, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void findAndDelete(String sName, IClassifier pClassifier)
    {
        // No valid implementation in the C++ code base.

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#getLanguage(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage)
     */
    public ILanguage getLanguage()
    {
        // Stubbed. To be overridden
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#nameChanged(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void nameChanged(IAttribute pAttr)
    {
        // No valid implementation in the C++ code base.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeChangeFacility#typeChanged(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void typeChanged(IAttribute pAttr)
    {
        // No valid implementation in the C++ code base.
    }
    
    /**************************************************************/
    // Protected Methods
    /**************************************************************/
    
    protected IAttribute createAttribute(String attrName, IClassifier pAttrType, IClassifier pClassifier)
    {
        if (pClassifier == null || attrName == null || pAttrType == null) return null;
        IAttribute pAttribute = pClassifier.createAttribute2(pAttrType,attrName);  
        if(pAttribute != null)
        {
            pClassifier.addAttribute(pAttribute);
        }
        return pAttribute;
    }
    
    protected IAttribute createAssociation(String attrName, 
                                        IClassifier pAttrType, 
                                        IClassifier pClassifier, 
                                        /*AssociationKind*/int kind)
    {
        if (pClassifier == null || attrName == null || pAttrType == null) return null;
        
        IAssociation assoc = new RelationFactory().
                                createAssociation2(pClassifier, 
                                                    pAttrType, 
                                                    kind, 
                                                    false, 
                                                    true, 
                                                    pClassifier.getNamespace());
        
        IAttribute pNewAttr = getNavigableEnd(assoc,pAttrType);
        if(pNewAttr != null)
            pNewAttr.setName(attrName);
            
        return pNewAttr;            
    }
    
    protected IAttribute getNavigableEnd(IAssociation assoc, IClassifier to)
    {
        if (assoc == null || assoc == null) return null;

        ETList<IAssociationEnd> pEnds = assoc.getEnds();
        
        if(pEnds != null)
        {
            IAssociationEnd pItem = null;
            long count = pEnds.size();
            for (int index = 0; index < count; index++)
            {
                pItem = pEnds.get(index);
                if (pItem != null)
                {
                    INavigableEnd pNavEnd = (pItem instanceof INavigableEnd)
                                            ?(INavigableEnd)pItem : null;
                    if(pNavEnd != null)
                    {       
                        if(pItem.isSameParticipant(to)) 
                        {  
                            return pNavEnd;                            
                        }
                    }
                }
            }
        }
        return null;
    }
    
    protected String retrieveShortName(String fullNameStr)
    {
        int pos = fullNameStr.lastIndexOf("::");
        return pos != -1 ? fullNameStr.substring(pos + 2) : fullNameStr;
    }
    
    protected String retrievePackageName(String fullNameStr)
    {
        int pos = 0;
        pos = (pos = fullNameStr.lastIndexOf("::")) >= 0  ? pos : 0;
        return fullNameStr.substring(0, pos);
    }
    
    protected void setRoundTripMode(/*RTMode*/int value)
    {
        ProductRetriever.retrieveProduct().getRoundTripController().setMode(value);
    }
    
    protected int getRoundTripMode()
    {
        return ProductRetriever.retrieveProduct().getRoundTripController().getMode();
    }
    
    protected IAttribute changeNavigableToAttribute(String attrName, 
                                                    INavigableEnd pNavEnd, 
                                                    IClassifier pOwnedElement, 
                                                    IClassifier pAttrType)
    {
        if (pNavEnd == null || pOwnedElement == null || pAttrType == null) return null;
        
        IAttribute pVal = createAttribute(attrName, pAttrType, pOwnedElement);
        
        IAssociation pAssoc = pNavEnd.getAssociation();
        if(pAssoc != null)
        {
            pAssoc.delete();
        }
        return pVal;
    }
    
    protected IAttribute changeAttributeToNavigable(String attrName, 
                                                    IAttribute pAttr, 
                                                    IClassifier pOwnedElement, 
                                                    IClassifier pAttrType)
    {
        if (pAttr == null || pOwnedElement == null || pAttrType == null) return null;
        
        IAttribute pVal = createAssociation(attrName, 
                                            pAttrType, 
                                            pOwnedElement, 
                                            AssociationKindEnum.AK_AGGREGATION); 
      
        INavigableEnd pNavEnd = null;
        if(pNavEnd != null)
        {
            pNavEnd = preChangeAttributeToNavigable(pAttr);
            
            pAttr.delete();

            if ( pNavEnd != null )
            {
                return pNavEnd;
            }
        }
        return pVal;
    }
    
    protected IAttribute preChangeNavigableToAttribute(INavigableEnd pEnd)
    {
        // C++ code does nothing.
        return null;
    }
            
    protected INavigableEnd preChangeAttributeToNavigable(IAttribute pAttr)
    {
//      C++ code does nothing.
        return null;
    }
    
    protected boolean isDataType(String name)
    {
        boolean retVal = true;
        if (name != null)
        {
            ILanguage pLanguage = getLanguage();
      
            if(pLanguage != null)
            {
                retVal = pLanguage.isDataType(name);
            }
        }
        else
        {
            retVal = false;
        }
        return retVal;
    }
    
    protected IClassifier getClassifier(
        String sNewType, IClassifier pClassifier)
    {
        if (pClassifier == null) 
            return null;
        
        IElementLocator pLocator = new ElementLocator();
              
        ETList<IElement> pElements = 
            pLocator.findScopedElements(pClassifier, sNewType);
        
        // IZ 80035: conover
        // added size check so it won't throw an ArrayIndexOOBEx
        if (pElements != null && pElements.size() > 0)
        {
            IElement pElement = pElements.get(0);                
            return (pElement instanceof IClassifier)
                ? (IClassifier)pElement 
                : null;
        }
        
        return null;
    }
    
    protected IElement resolvedScopedElement(IClassifier pOwner, String sType)
    {
        IElement retVal = null;
        
        if(pOwner == null || sType == null) return null;
        
        IElementLocator pLocator = new ElementLocator();
        
        if(sType.indexOf('<') > 0)
        {
            // We have a template instance.  We should create a derived 
            // classifier instead of a DataType.
            ETList < IElement > elements = pLocator.findScopedElements(pOwner, sType);
            if((elements != null) && (elements.size() > 0))
            {
                retVal = elements.get(0);
            }
            else
            {
                FactoryRetriever fact = FactoryRetriever.instance();
                Object obj = fact.createType("DerivationClassifier", null);
                if (obj != null && obj instanceof INamedElement)
                {
                   INamedElement nEle = (INamedElement)obj;
                   nEle.setName(sType);
                   pOwner.getProject().addOwnedElement(nEle);
                   retVal = nEle;
                }
            }
        }
        else
        {
            retVal = pLocator.resolveScopedElement(pOwner, sType);
        }
        return retVal;
    }
}
