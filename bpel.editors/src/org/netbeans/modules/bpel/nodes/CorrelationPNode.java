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
import org.netbeans.modules.soa.ui.SoaUiUtil;
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
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        getReference().getInitiate();
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                BaseCorrelation.SET, CORRELATION_SET,
                "getSet", null, null); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                BaseCorrelation.INITIATE, CORRELATION_INITIATE,
                "getInitiate", "setInitiate", "removeInitiate"); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                PatternedCorrelation.PATTERN, CORRELATION_PATTERN,
                "getPattern", "setPattern", "removePattern"); // NOI18N
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
        return SoaUiUtil.getGrayString( 
                baseName
                , nodeName == null ? getDisplayName() : nodeName);
    }

//    protected String getImplShortDescription() {
//        return NbBundle.getMessage(CorrelationPNode.class,
//            "LBL_CORRELATION_P_NODE_TOOLTIP", // NOI18N
//            getName()
//            );
//    }

    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof PropertyRemoveEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent.equals(this.getReference())) {
                String propName = event.getName();
                if (PatternedCorrelation.PATTERN.equals(propName)) {
                    updateName();
                    // updateProperty(PropertyType.CORRELATION_PATTERN);
                } 
            }
        }
    }

}
