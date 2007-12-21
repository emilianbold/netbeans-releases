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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.props.editors;

import java.beans.PropertyEditor;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.choosers.MessageExchangeChooserPanel;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class MessageExchPropertyCustomizer
        extends TreeChooserPropertyCustomizer<MessageExchangeChooserPanel> {
    
    public MessageExchPropertyCustomizer() {
        super();
    }
    
    protected MessageExchangeChooserPanel createChooserPanel() {
        return new MessageExchangeChooserPanel();
    }
    
    protected void applyNewValues() {
        MessageExchange messageExch = getChooserPanel().getSelectedValue();
        //
        Object[] beans = myPropertyEnv.getBeans();
        BpelNode node = (BpelNode)beans[0];
        Lookup lookup = node.getLookup();
        //
        ReferenceCollection entity = (ReferenceCollection)node.getReference();
        //
        BpelReference<MessageExchange> newRef =
                entity.createReference(messageExch, MessageExchange.class);
        //
        myPropertyEditor.setValue(newRef);
    }
    
    public void init(PropertyEnv propertyEnv, PropertyEditor propertyEditor) {
        assert propertyEnv != null && propertyEditor != null : "Wrong params"; // NOI18N
        //
        super.init(propertyEnv, propertyEditor);
        //
        Object[] beans = myPropertyEnv.getBeans();
        BpelNode node = (BpelNode)beans[0];
        Lookup lookup = node.getLookup();
        //
        MessageExchangeChooserPanel chooserPanel = getChooserPanel();
        //
        VisibilityScope visScope =
                new VisibilityScope((BpelEntity)node.getReference(), lookup);
        chooserPanel.setLookup(new ExtendedLookup(lookup, visScope));
        chooserPanel.initControls();
        //
        // Set current selection
        Object value = propertyEditor.getValue();
        try {
            if (value != null && (value instanceof BpelReference)) {
                Object referent = ((BpelReference)value).get();
                if ((referent != null) && (referent instanceof MessageExchange)) {
                    chooserPanel.setSelectedValue(
                            (MessageExchange)referent);
                } else {
                    chooserPanel.setSelectedValue(null);
                }
            } else {
                chooserPanel.setSelectedValue(null);
            }
        } catch (Exception e) {};
    }
    
}
