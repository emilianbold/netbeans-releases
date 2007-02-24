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
 * File       : IAttributeChangeFacility.java
 * Created on : Nov 21, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;

/**
 * @author Aztec
 */
public interface IAttributeChangeFacility extends IRequestFacility
{
    public IAttribute changeAttributeType(IAttribute pAttr, 
                                            IClassifier pClassifier, 
                                            String sName, 
                                            String sNewType);
    
    public IAttribute createAttribute(String sName, 
                                        String sType, 
                                        IClassifier pClassifier);
    
    public void addAttribute(String sName, 
                                    String sType, 
                                    IClassifier pClassifier, 
                                    boolean rtOffCreate, 
                                    boolean rtOffPostProcessing);
                                    
    public void findAndDelete(String sName, IClassifier pClassifier);
    
    public void findAndChangeName(String sOldName, 
                                            String sNewName, 
                                            IClassifier pClassifier);
                                            
    public IAttribute findAndChangeType(String sName, 
                                        String sNewType, 
                                        IClassifier pClassifier);
                                        
    public void delete(IAttribute pAttr, 
                        boolean rtOffDelete, 
                        boolean rtOffPostDelete);
                        
    public void changeName(IAttribute pAttr, 
                            String sNewName, 
                            boolean rtOffCreate, 
                            boolean rtOffPostProcessing);
                            
    public void changeType(IAttribute pAttr, 
                            String sNewType, 
                            boolean rtOffCreate, 
                            boolean rtOffPostProcessing);
                            
    public void changeVisibility(IAttribute pAttr, 
                                    /*VisibilityKind*/int visibility, 
                                    boolean rtOffCreate, 
                                    boolean rtOffPostProcessing);
                                    
    public void added(IAttribute pAttr);
    
    public void deleted(IAttribute pAttr, IClassifier pClassifier);
    
    public void nameChanged(IAttribute pAttr);
    
    public void typeChanged(IAttribute pAttr);
    
    public void changeInitializer(IAttribute pAttribute, 
                                    String initializer, 
                                    boolean rtOff);
                                    
    public void changeFinal(IAttribute pAttribute, 
                            boolean isFinal, 
                            boolean rtOff);
                            
    public void changeStatic(IAttribute pAttribute, 
                                boolean isStatic, 
                                boolean rtOff);
                                
    public void changeMultiplicity(IAttribute pAttribute, 
                                    IMultiplicity pMultiplicity, 
                                    boolean rtOffWhileChanging, 
                                    boolean rtOffPostProcessing);
                                    
    public IAttribute addAttribute2(String sName, 
                                    String sType, 
                                    IClassifier pClassifier, 
                                    boolean rtOffCreate, 
                                    boolean rtOffPostProcessing);
    
    public IAttribute addAttribute3(String sName,
                                    String sType,
                                    IClassifier pClassifier,
                                    boolean rtOffCreate,
                                    boolean rtOffPostProcessing,
                                    int modifierMask);
                                    
    public ILanguage getLanguage();
}
