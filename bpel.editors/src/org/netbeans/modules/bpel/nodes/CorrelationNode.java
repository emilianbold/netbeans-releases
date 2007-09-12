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

import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseCorrelation;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CorrelationNode extends BpelNode<Correlation> {
    
    public CorrelationNode(Correlation correlation, Children children, Lookup lookup) {
        super(correlation, children, lookup);
    }

    public CorrelationNode(Correlation correlation, Lookup lookup) {
        super(correlation, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CORRELATION;
    }

    protected  String getNameImpl() {
        CorrelationSet corrSet = null;
        Correlation ref = getReference();
        if (ref != null) {
            BpelReference<CorrelationSet> corrSetRef = ref.getSet();
            if (corrSetRef != null) {
                corrSet = corrSetRef.get();
            }
        }
        return corrSet == null ? "" : corrSet.getName();
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
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                BaseCorrelation.SET, CORRELATION_SET, 
                "getSet", null, null); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                BaseCorrelation.INITIATE, CORRELATION_INITIATE, 
                "getInitiate", "setInitiate", "removeInitiate"); // NOI18N
        //
        return sheet;
    }

    protected String getImplHtmlDisplayName() {
        String nodeName = null;
        Correlation ref = getReference();
        if (ref != null) {
            Initiate initiate = getReference().getInitiate();
            nodeName = initiate == null || initiate.equals(Initiate.INVALID)
                ? "" 
                : " initiate="+initiate.toString(); // NOI18N
        }
        
        return SoaUiUtil.getGrayString(getName(), nodeName == null ? "" : nodeName);
    }
    
//    protected String getImplShortDescription() {
//        return NbBundle.getMessage(CorrelationNode.class,
//            "LBL_CORRELATION_SET_NODE_TOOLTIP", // NOI18N
//            getName()
//            );
//    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
}
