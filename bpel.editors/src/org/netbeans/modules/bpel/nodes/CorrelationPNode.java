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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseCorrelation;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CorrelationPNode extends BpelNode<PatternedCorrelation> {
    
    public CorrelationPNode(PatternedCorrelation correlation, Lookup lookup) {
        super(correlation, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CORRELATION_P;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        if (getReference() == null) {
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        PropertyUtils propUtil = PropertyUtils.getInstance();
        //
        getReference().getInitiate();
        propUtil.registerAttributeProperty(this, mainPropertySet,
                BaseCorrelation.SET, CORRELATION_SET,
                "getSet", null, null); // NOI18N
        //
        propUtil.registerAttributeProperty(this, mainPropertySet,
                BaseCorrelation.INITIATE, CORRELATION_INITIATE,
                "getInitiate", "setInitiate", "removeInitiate"); // NOI18N
        //
        propUtil.registerAttributeProperty(this, mainPropertySet,
                PatternedCorrelation.PATTERN, CORRELATION_PATTERN,
                "getPattern", "setPattern", "removePattern"); // NOI18N
        //
        propUtil.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    protected String getImplHtmlDisplayName() {
        String nodeName = null;
        PatternedCorrelation ref = getReference();
        if (ref == null) {
            return super.getImplHtmlDisplayName();
        }
        Initiate initiate = ref.getInitiate();
        nodeName = initiate == null || initiate.equals(Initiate.INVALID)
            ? "" 
            : " initiate="+initiate.toString(); // NOI18N
        
        Pattern pattern = ref.getPattern();
        nodeName += pattern == null || pattern.isInvalid()
            ? "" 
            : " pattern="+pattern.toString(); // NOI18N

        String baseName = getName();
        baseName = baseName == null || baseName.length() < 1 ? 
            ref.getSet() != null 
                ? ref.getSet().getRefString() 
                : super.getImplHtmlDisplayName()
            : baseName;
        return SoaUtil.getGrayString( 
                baseName
                , nodeName == null ? getDisplayName() : nodeName);
    }

    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof PropertyRemoveEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent.equals(this.getReference())) {
                String propName = event.getName();
                if (PatternedCorrelation.PATTERN.equals(propName)) {
                    updateName();
                } 
            }
        }
    }
}
