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

import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.*;
import org.netbeans.modules.xml.wsdl.model.*;

import java.util.List;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.addressing.Addressing10UsingAddressing;

/**
 *
 * @author Martin Grebac
 */
public class PolicyModelHelper {
    
    /**
     * Creates a new instance of PolicyModelHelper
     */
    public PolicyModelHelper() {
    }
    
    public synchronized static All createTopExactlyOne(final Policy p, final WSDLModel model, final WSDLComponentFactory wcf) {
        All all;
        ExactlyOne eo = p.getExactlyOne();
        if (eo == null) {
            Definitions d = model.getDefinitions();
            eo = (ExactlyOne)wcf.create(p, PolicyQName.EXACTLYONE.getQName());
            all = (All)wcf.create(eo, PolicyQName.ALL.getQName());
            d.addExtensibilityElement(p);
            p.addExtensibilityElement(eo);
            eo.addExtensibilityElement(all);
        } else {
            all = eo.getAll();
        }
        return all;
    }

    public synchronized static All createTopLevelServicePolicy(final Service s, final WSDLModel model, final WSDLComponentFactory wcf) {
        All all;
        Definitions d = model.getDefinitions();
        Policy policy = (Policy)wcf.create(d, PolicyQName.POLICY.getQName());
        String policyName = s.getName() + "Policy";                 //NOI18N
        policy.setID(policyName);
        PolicyReference policyRef = (PolicyReference)model.getFactory().create(s, PolicyQName.POLICYREFERENCE.getQName());
        policyRef.setPolicyURI("#" + policyName);                   //NOI18N
        s.addExtensibilityElement(policyRef);

        ExactlyOne eo = (ExactlyOne)wcf.create(policy, PolicyQName.EXACTLYONE.getQName());
        all = (All)wcf.create(eo, PolicyQName.ALL.getQName());
        d.addExtensibilityElement(policy);
        policy.addExtensibilityElement(eo);
        eo.addExtensibilityElement(all);
        
        return all;
    }
    
    public synchronized static All createTopLevelPolicy(final Binding b, final WSDLModel model, final WSDLComponentFactory wcf) {
        All all;
        Definitions d = model.getDefinitions();
        Policy policy = (Policy)wcf.create(d, PolicyQName.POLICY.getQName());
        String policyName = b.getName() + "Policy";                 //NOI18N
        policy.setID(policyName);
        PolicyReference policyRef = (PolicyReference)model.getFactory().create(b, PolicyQName.POLICYREFERENCE.getQName());
        policyRef.setPolicyURI("#" + policyName);                   //NOI18N
        b.addExtensibilityElement(policyRef);                

        ExactlyOne eo = (ExactlyOne)wcf.create(policy, PolicyQName.EXACTLYONE.getQName());
        all = (All)wcf.create(eo, PolicyQName.ALL.getQName());
        d.addExtensibilityElement(policy);
        policy.addExtensibilityElement(eo);
        eo.addExtensibilityElement(all);

        Addressing10UsingAddressing addressing = (Addressing10UsingAddressing)wcf.create(eo, Addressing10QName.USINGADDRESSING.getQName());
        all.addExtensibilityElement(addressing);
        
//        Utf816FFFECharacterEncoding encoding = 
//                (Utf816FFFECharacterEncoding)wcf.create(eo, EncodingQName.UTF816FFFECHARACTERENCODING.getQName());
//        all.addExtensibilityElement(encoding);
        return all;
    }    

    public synchronized static All createMessageLevelPolicy(final Binding b, final WSDLComponent c, final WSDLModel model, final WSDLComponentFactory wcf) {
        All all;
        Definitions d = model.getDefinitions();
        Policy policy = (Policy)wcf.create(d, PolicyQName.POLICY.getQName());
        String policyName = null;
        if (c instanceof BindingInput) {
            String msgName = ((BindingInput)c).getName();
            if (msgName == null) {
                msgName = ((BindingOperation)c.getParent()).getName() + "Input";          //NOI18N
            }
            policyName = b.getName() + "_" + msgName + "_" + "Policy";           //NOI18N
        }
        if (c instanceof BindingOutput) {
            String msgName = ((BindingOutput)c).getName();
            if (msgName == null) {
                msgName = ((BindingOperation)c.getParent()).getName() + "Output";          //NOI18N
            }
            policyName = b.getName() + "_" + msgName + "_" + "Policy";           //NOI18N
        }
        if (c instanceof BindingFault) {
            String msgName = ((BindingFault)c).getName();
            if (msgName == null) {
                msgName = ((BindingOperation)c.getParent()).getName() + "Fault";          //NOI18N
            }
            policyName = b.getName() + "_" + msgName + "_" + "Policy";           //NOI18N
        }
        policy.setID(policyName);
        PolicyReference policyRef = (PolicyReference)model.getFactory().create(c, PolicyQName.POLICYREFERENCE.getQName());
        policyRef.setPolicyURI("#" + policyName);                   //NOI18N
        c.addExtensibilityElement(policyRef);
        
        ExactlyOne eo = (ExactlyOne)wcf.create(policy, PolicyQName.EXACTLYONE.getQName());
        all = (All)wcf.create(eo, PolicyQName.ALL.getQName());
        d.addExtensibilityElement(policy);
        policy.addExtensibilityElement(eo);
        eo.addExtensibilityElement(all);
        
        return all;
    }
    
    /* Used to get elements in the top of the policy  -under ExactlyOne/All
     */
    public static ExtensibilityElement getTopLevelElement(Policy p, Class elementClass) {
        ExtensibilityElement e = null;
        if (p != null) {
            ExactlyOne eo = p.getExactlyOne();
            if (eo != null) {
                All all = eo.getAll();
                if (all != null) {
                    List<ExtensibilityElement> l = all.getExtensibilityElements(elementClass);
                    if ((l != null) && !(l.isEmpty())) {
                        e = l.get(0);
                    }
                }
            }
        }
        return e;
    }

    /* Used to get top level All element for specific policy
     */
    public static All getTopLevelAll(Policy p) {
        All a = null;
        if (p != null) {
            ExactlyOne eo = p.getExactlyOne();
            if (eo != null) {
                a = eo.getAll();
            }
        }
        return a;
    }
    
}
