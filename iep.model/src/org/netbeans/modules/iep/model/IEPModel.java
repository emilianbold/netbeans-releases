/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.iep.model;

import java.io.File;
import java.util.List;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * 
 * 
 * 
 */
public abstract class IEPModel extends AbstractDocumentModel<IEPComponent> implements Referenceable {
    
     public static String IEP_NAMESPACE = "http://jbi.com.sun/iep";
     public static String IEP_COMPONENT = "component";
    
     protected IEPModel(ModelSource source) {
        super(source);
     }
    
    /**
     * @return IEP component factory.
     */
    public abstract IEPComponentFactory getFactory();
    
    /**
     * Search from all imported WLM models those with specified target namespace.
     * @param namespaceURI the target namespace to search for model
     * @return list WSDL models or empty list if none found.
     */
   //public abstract List<WSDLModel> findWSDLModel(String namespaceURI);
    
   
//    /**
//     * Find named WLM components by name and type within current model.
//     * @param name local name of target component
//     * @param type type of target component
//     * @return The list WLM component of specified type and name; empty list if not found
//     */
//   <T extends ReferenceableWLMComponent> T  findComponetnByName(String name, Class <T> type);
//    
//    /**
//     * Find named WLM component by QName and type.
//     * @param name QName of the target component.
//     * @param type type of target component
//     * @return The list WLM component of specified type and name; empty list if not found
//     */
//   <T extends ReferenceableWLMComponent> T  findComponetnByName(QName name, Class<T> type);
   
     public abstract PlanComponent getPlanComponent();
     
     public abstract File getWsdlFile(); 
     
	 public abstract void saveWsdl() throws Exception;
    		   
}
