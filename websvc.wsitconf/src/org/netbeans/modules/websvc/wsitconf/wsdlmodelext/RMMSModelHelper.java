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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitconf.Utilities;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.All;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.ExactlyOne;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.rm.*;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;

import java.io.IOException;
import org.openide.ErrorManager;

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
    public static void enableFlowControl(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        FlowControl fc = getFlowControl(p);
        if (fc == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();

                All all = null;
                if (p == null) {
                    all = PolicyModelHelper.createTopLevelPolicy(b, model, wcf);
                } else {
                    all = PolicyModelHelper.createTopExactlyOne(p, model, wcf);
                }

                FlowControl flowControl = (FlowControl)wcf.create(all, RMMSQName.RMFLOWCONTROL.getQName());
                all.addExtensibilityElement(flowControl);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

    // disables RM in the config wsdl on specified binding
    public static void disableFlowControl(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        FlowControl fc = getFlowControl(p);
        if (fc != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                fc.getParent().removeExtensibilityElement(fc);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

    // checks if Flow Control is enabled in the config wsdl  on specified binding
    public static boolean isFlowControlEnabled(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p != null) {
            FlowControl fc = getFlowControl(p);
            return (fc != null);
        }
        return false;
    }
    
    public static FlowControl getFlowControl(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        return getFlowControl(p);
    }

    public static FlowControl getFlowControl(Policy p) {
        FlowControl flowControl = null;
        if (p != null) {
            ExactlyOne eo = p.getExactlyOne();
            if (eo != null) {
                All all = eo.getAll();
                if (all != null) {
                    flowControl = all.getFlowControl();
                }
            }
        }
        return flowControl;
    }    

    public static void setMaxReceiveBufferSize(Binding b, WSDLModel model, String value) {
        FlowControl fc = getFlowControl(b, model);
        setMaxReceiveBufferSize(fc, value);
    }
    
    public static String getMaxReceiveBufferSize(Binding b, WSDLModel model) {
        FlowControl fc = getFlowControl(b, model);
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
