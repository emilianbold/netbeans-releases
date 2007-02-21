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

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.ResolverUtility;

/**
 *
 * @author nk160297
 */
public class QNamePropEditor extends StringPropEditor {
    
    public QNamePropEditor() {
    }
    
    public boolean isEditable() {
        return false;
    }
    
    public String getAsText() {
        Object value = super.getValue();
        if (value == null) return "";
        assert(value instanceof QName);
        //
        String retValue = null;
        QName qValue = (QName)value;
        //
        String prefix = qValue.getPrefix();
        if (prefix == null || prefix.length() == 0) {
            //
            // Try calculate prefix here
            if (myPropertyEnv != null) {
                Object[] beans = myPropertyEnv.getBeans();
                if (beans != null && beans.length > 0) {
                    Object firstBean = beans[0];
                    if (firstBean != null && firstBean instanceof BpelNode) {
                        Object refObj = ((BpelNode)firstBean).getReference();
                        if (refObj instanceof BpelEntity) {
                            BpelEntity entity = (BpelEntity)refObj;
                            retValue = ResolverUtility.
                                    qName2DisplayText(qValue, entity);
                        }
                    }
                }
            }
        } else {
            retValue = ResolverUtility.qName2DisplayText(qValue);
        }
        return retValue;
    }
    
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        if (text == null || text.length() == 0) {
            setValue(null);
        } else {
            setValue(QName.valueOf(text));
        }
    }
    
}
