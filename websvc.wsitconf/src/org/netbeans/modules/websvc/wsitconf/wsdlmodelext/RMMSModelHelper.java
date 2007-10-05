/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.rm.FlowControl;
import org.netbeans.modules.websvc.wsitmodelext.rm.MaxReceiveBufferSize;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMMSQName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author Martin Grebac
 */
public class RMMSModelHelper {
    
    /**
     * Creates a new instance of RMMSModelHelper
     */
    public RMMSModelHelper() {
    }
    
    // enables FlowControl in the config wsdl on specified binding
    public static void enableFlowControl(Binding b) {
        All a = PolicyModelHelper.createPolicy(b, true);
        PolicyModelHelper.createElement(a, RMMSQName.RMFLOWCONTROL.getQName(), FlowControl.class, false);
    }

    // disables RM in the config wsdl on specified binding
    public static void disableFlowControl(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        FlowControl fc = getFlowControl(p);
        if (fc != null) {
            PolicyModelHelper.removeElement(fc.getParent(), FlowControl.class, false);
        }
        PolicyModelHelper.cleanPolicies(b);        
    }

    // checks if Flow Control is enabled in the config wsdl  on specified binding
    public static boolean isFlowControlEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            FlowControl fc = getFlowControl(p);
            return (fc != null);
        }
        return false;
    }
    
    public static FlowControl getFlowControl(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        return getFlowControl(p);
    }

    public static FlowControl getFlowControl(Policy p) {
        return (FlowControl) PolicyModelHelper.getTopLevelElement(p, FlowControl.class);
    }

    public static void setMaxReceiveBufferSize(Binding b, String value) {
        FlowControl fc = getFlowControl(b);
        setMaxReceiveBufferSize(fc, value);
    }
    
    public static String getMaxReceiveBufferSize(Binding b) {
        FlowControl fc = getFlowControl(b);
        return getMaxReceiveBufferSize(fc);
    }
    
    public static String getMaxReceiveBufferSize(FlowControl fc) {
        String max = null;
        if (fc != null) {
            MaxReceiveBufferSize maxBuf = fc.getMaxReceiveBufferSize();
            if (maxBuf!=null) {
                max = maxBuf.getMaxReceiveBufferSize();
            }
        }
        return max;
    }

    public static void setMaxReceiveBufferSize(FlowControl fc, String value) {
        if (fc != null) {
            Model model = fc.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                MaxReceiveBufferSize maxBufSize = fc.getMaxReceiveBufferSize();
                if (maxBufSize == null) {
                    if (value != null) {    // if is null, then there's no element and we want to remove it -> do nothing
                        WSDLComponentFactory wcf = fc.getModel().getFactory();
                        MaxReceiveBufferSize maxBuf = (MaxReceiveBufferSize)wcf.create(fc, 
                                RMMSQName.MAXRECEIVEBUFFERSIZE.getQName()
                                );
                        maxBuf.setMaxReceiveBufferSize(value);
                        fc.addExtensibilityElement(maxBuf);
                    }
                } else {
                    if (value == null) {
                        fc.removeMaxReceiveBufferSize(maxBufSize);
                    } else {
                        maxBufSize.setMaxReceiveBufferSize(value);
                    }
                }                  
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
    }
    
}
