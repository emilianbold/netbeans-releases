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
package org.netbeans.modules.bpel.properties.props.editors;

import java.beans.PropertyEditor;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.choosers.MessageExchangeChooserPanel;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VisibilityScope;
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
