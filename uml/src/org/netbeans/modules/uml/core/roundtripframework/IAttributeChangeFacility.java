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
