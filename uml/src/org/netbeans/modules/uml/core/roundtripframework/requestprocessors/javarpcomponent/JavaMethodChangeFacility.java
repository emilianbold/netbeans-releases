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
 * File       : JavaMethodChangeFacility.java
 * Created on : Jan 19, 2004
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.roundtripframework.MethodChangeFacility;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.support.umlsupport.UMLSupportResource;
/**
 * @author Aztec
 */
public class JavaMethodChangeFacility
    extends MethodChangeFacility
    implements IJavaMethodChangeFacility
{
    private JavaMethodChangeHandler m_Handler = new JavaMethodChangeHandler();
    private JavaChangeHandlerUtilities m_javaChangeHandlerUtilities = 
                                new JavaChangeHandlerUtilities();
     private IRequestValidator m_RequestValidator = new RequestValidator();
                                
    public JavaMethodChangeFacility()                                
    {
        m_Handler.setChangeHandlerUtilities(getHandlerUtilities());
    }

    public void added(IOperation pOp, boolean roundTripOn)
    {
         if(pOp == null) return;
         
         int roundTripMode = getRoundTripMode();         
         // We do not want to send round trip events if rtOffCreate is true
         if(roundTripOn) {
             setRoundTripMode(RTMode.RTM_LIVE);
         } else {
             setRoundTripMode(RTMode.RTM_OFF);
         }         
         try {
             IClassifier pClass = pOp.getFeaturingClassifier();
             m_Handler.added(m_RequestValidator, pOp, pClass);
         } finally {
             // Reset the state of RT.
             setRoundTripMode(roundTripMode);
         }
    }
    
    public void deleted(IOperation pOp, IClassifier pClass, boolean roundTripOn )
    {
        if((pOp == null) || (pClass == null)) return;
        
        int roundTripMode = getRoundTripMode();        
        // We do not want to send round trip events if rtOffCreate is true
        if(roundTripOn) {
            setRoundTripMode(RTMode.RTM_LIVE);
        } else {
            setRoundTripMode(RTMode.RTM_OFF);
        }
        
        try {
            m_Handler.deleted(pOp);
        } finally {
            // Reset the state of RT.
            setRoundTripMode(roundTripMode);
        }
    }
    
    public void nameChanged(IOperation pOp)
    {
        if(pOp == null) return;
        m_Handler.nameChange(pOp);
    }
    
    public void typeChanged(IOperation pOp, boolean roundTripOn) {

        if(pOp == null) return;
        int roundTripMode = getRoundTripMode();
        // We do not want to send round trip events if rtOffCreate is true
        if(roundTripOn) {
            setRoundTripMode(RTMode.RTM_LIVE);
        } else {
            setRoundTripMode(RTMode.RTM_OFF);
        }
        
        try {
            m_RequestValidator.setValid(true);
            m_Handler.typeChange(m_RequestValidator, pOp);
        } finally {
            // Reset the state of RT.
            setRoundTripMode(roundTripMode);
        }
    }
    
    protected JavaChangeHandlerUtilities getHandlerUtilities()
    {
        return m_javaChangeHandlerUtilities;
    }
    
    public void parameterChange(IParameter param, boolean roundTripOn) {
        if(param == null) return;
        int roundTripMode = getRoundTripMode();
        // We do not want to send round trip events if rtOffCreate is true
        if(roundTripOn) {
            setRoundTripMode(RTMode.RTM_LIVE);
        } else {
            setRoundTripMode(RTMode.RTM_OFF);
        }        
        try {
        m_Handler.parameterChange(param);
        } finally {
            // Reset the state of RT.
            setRoundTripMode(roundTripMode);
        }
    }

}
