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
 * IEnumLiteralChangeFacility.java
 *
 * Created on April 8, 2005, 11:02 AM
 */

package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;

/**
 *
 * @author Administrator
 */
public interface IEnumLiteralChangeFacility extends IRequestFacility
{
   // These should be part of the IEnumerationChangeFacility
   public IEnumerationLiteral createLiteral(String sName, 
                                            IEnumeration pClassifier);
   
   public IEnumerationLiteral addLiteral(String sName, 
                                         IEnumeration pClassifier, 
                                         boolean rtOffCreate, 
                                         boolean rtOffPostProcessing);
   
   public void findAndDelete(String sName, IEnumeration pClassifier);
    
    public void findAndChangeName(String sOldName, 
                                  String sNewName, 
                                  IEnumeration pClassifier);
    
    public void delete(IEnumerationLiteral pAttr, 
                        boolean rtOffDelete, 
                        boolean rtOffPostDelete);
                        
    public void changeName(IEnumerationLiteral pAttr, 
                            String sNewName, 
                            boolean rtOffCreate, 
                            boolean rtOffPostProcessing);
    
    public void added(IEnumerationLiteral pAttr);
    
    public void deleted(IEnumerationLiteral pAttr, IEnumeration pClassifier);
    
    public void nameChanged(IEnumerationLiteral pAttr);
                                    
    public ILanguage getLanguage();
}
