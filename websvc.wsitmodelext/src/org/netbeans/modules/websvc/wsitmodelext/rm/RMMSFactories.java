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

package org.netbeans.modules.websvc.wsitmodelext.rm;

import org.netbeans.modules.websvc.wsitmodelext.rm.impl.FlowControlImpl;
import org.netbeans.modules.websvc.wsitmodelext.rm.impl.MaxReceiveBufferSizeImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.spi.ElementFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Set;

public class RMMSFactories {

    public static class FlowControlFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMMSQName.RMFLOWCONTROL.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new FlowControlImpl(context.getModel(), element);
        }
    }

    public static class MaxReceiveBufferSizeFactory extends ElementFactory {
        @Override
        public Set<QName> getElementQNames() {
            return Collections.singleton(RMMSQName.MAXRECEIVEBUFFERSIZE.getQName());
        }
        @Override
        public WSDLComponent create(WSDLComponent context, Element element) {
            return new MaxReceiveBufferSizeImpl(context.getModel(), element);
        }
    }

}
