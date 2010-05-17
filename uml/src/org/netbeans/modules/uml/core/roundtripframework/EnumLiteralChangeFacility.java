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
