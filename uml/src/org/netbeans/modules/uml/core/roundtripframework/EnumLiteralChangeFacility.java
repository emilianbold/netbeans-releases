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

/*
 * EnumLiteralChangeFacility.java
 *
 * Created on April 8, 2005, 11:08 AM
 */

package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 * @author Administrator
 */
public abstract class EnumLiteralChangeFacility extends RequestFacility
                                                implements IEnumLiteralChangeFacility
{
   
   /** Creates a new instance of EnumLiteralChangeFacility */
   public EnumLiteralChangeFacility()
   {
   }

    public IEnumerationLiteral addLiteral(String sName, IEnumeration pClassifier, 
                                         boolean rtOffCreate, 
                                         boolean rtOffPostProcessing)
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

        IEnumerationLiteral literal = null;
        
        try
        {
           
        	literal = createLiteral(sName, pClassifier);

			// Only handle the post processing of the attribute if round 
			// trip was off during the creation of the attribute.  If RT
			// was on during the creation of the attribute RT has already
			// done the post processing, so do not do it again.
			if(literal != null && rtOffCreate)
			{         
				if(rtOffPostProcessing)
				{
					setRoundTripMode(RTMode.RTM_OFF);
				}
				else
				{
					setRoundTripMode(RTMode.RTM_LIVE);
				}

				added(literal);
			}
        }
        finally
        {
			// Reset the state of RT.
			setRoundTripMode(roundTripMode);
        }
        
        return literal;
    }

    public void changeName(IEnumerationLiteral literal, String sNewName, 
                           boolean rtOffCreate, boolean rtOffPostProcessing)
    {
       if(literal == null) return;
       
       int createFlag = RoundTripUtils.getRTModeFromTurnOffFlag( rtOffCreate );
       RoundTripModeRestorer restorer = new RoundTripModeRestorer(createFlag);
       
       literal.setName(sNewName);
       
       int postFlag = RoundTripUtils.getRTModeFromTurnOffFlag(rtOffPostProcessing);
       restorer.setMode(postFlag);
       
       nameChanged(literal);
    }

    public IEnumerationLiteral createLiteral(String sName, IEnumeration pClassifier)
    {
       if(pClassifier == null) return null;       
       
       ILanguage pLanguage = getLanguage();
       
       
       // Now if the attribute type is a data type (as specified by the
       // language manager then we want to create a IAttribute inside the
       // classifier.  Otherwise we want to create an association.
       if(pLanguage != null)
       {
          RoundTripModeRestorer rest = new RoundTripModeRestorer(RTMode.RTM_OFF);
          try
          {
             return pClassifier.createLiteral(sName);
          }
          finally
          {
             rest.restoreOriginalMode();
          }
       }
       
       return null;
    }

    public void delete(IEnumerationLiteral literal, 
                       boolean rtOffDelete, 
                       boolean rtOffPostDelete)
    {
       if(literal == null) return;
       
       IEnumeration pClassifier = literal.getEnumeration();
       
       if(pClassifier != null)
       {
          int deleteFlag = RoundTripUtils.getRTModeFromTurnOffFlag(rtOffDelete);
          RoundTripModeRestorer restorer = new RoundTripModeRestorer(deleteFlag);
          
          pClassifier.removeLiteral(literal);          
          literal.delete();
          
          int postFlag = RoundTripUtils.getRTModeFromTurnOffFlag(rtOffPostDelete);
          restorer.setMode(postFlag);
          
          deleted(literal, pClassifier);
       }
    }

    public void findAndChangeName(String sOldName, 
                                  String sNewName, 
                                  IEnumeration pClassifier)
    {
       ETList <IEnumerationLiteral>  literals = pClassifier.getLiterals();
       for(IEnumerationLiteral literal : literals)
       {
          String name = literal.getName();
          if(sOldName.equals(name) == true)
          {
             literal.setName(sNewName);
             nameChanged(literal);
             break;
          }
       }
    }

    public void findAndDelete(String sName, IEnumeration pClassifier)
    {
       ETList <IEnumerationLiteral>  literals = pClassifier.getLiterals();
       for(IEnumerationLiteral literal : literals)
       {
          String name = literal.getName();
          if(sName.equals(name) == true)
          {             
             literal.delete();
             deleted(literal, pClassifier);
             break;
          }
       }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Helper Methods
    
    protected int getRoundTripMode()
    {
        return ProductRetriever.retrieveProduct().getRoundTripController().getMode();
    }
    
    protected void setRoundTripMode(/*RTMode*/int value)
    {
        // IZ 84855 conover - RT is always off and shouldn't be 
        // turned on for any reason
        if (value != RTMode.RTM_LIVE)
            ProductRetriever.retrieveProduct().getRoundTripController().setMode(value);
    }    
    
    protected String retrieveShortName(String fullNameStr)
    {
        int pos = fullNameStr.lastIndexOf("::");
        return pos != -1 ? fullNameStr.substring(pos + 2) : fullNameStr;
    }
}
