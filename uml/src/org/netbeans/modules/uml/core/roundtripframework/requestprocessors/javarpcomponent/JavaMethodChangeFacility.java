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
