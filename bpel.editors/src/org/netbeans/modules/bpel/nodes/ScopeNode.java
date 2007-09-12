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

import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class ScopeNode extends BaseScopeNode<Scope> {
    
    public ScopeNode(Scope reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public ScopeNode(Scope reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.SCOPE;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
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
                NamedElement.NAME, NAME, 
                "getName", "setName", null); // NOI18N
        //
//        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
//                Scope.ISOLATED, ISOLATED_SCOPE, 
//                "getIsolated", "setIsolated", "removeIsolated"); // NOI18N
//        //
//        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
//                BaseScope.EXIT_ON_STANDART_FAULT, 
//                EXIT_ON_STANDART_FAULT, 
//                "getExitOnStandardFault", "setExitOnStandardFault",  // NOI18N
//                "removeExitOnStandardFault"); // NOI18N
        //
        return sheet;
    }

    protected ActionType[] getActionsArray() {
        if (isModelReadOnly()) {
            return new ActionType[] {
                ActionType.GO_TO_SOURCE,
                ActionType.GO_TO_DIAGRAMM,
                ActionType.SEPARATOR,
                ActionType.TOGGLE_BREAKPOINT,
                ActionType.SEPARATOR,
                ActionType.REMOVE,
                ActionType.SEPARATOR,
                ActionType.PROPERTIES
            };
        }

        return new ActionType[] {
            ActionType.ADD_NEWTYPES,
            ActionType.SEPARATOR,
            ActionType.ADD_FROM_PALETTE,
            ActionType.WRAP,
            ActionType.SEPARATOR,
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.MOVE_UP,
            ActionType.MOVE_DOWN,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }

    public ActionType[] getAddActionArray() {
        return new ActionType[] {
            ActionType.ADD_VARIABLE,
            // ActionType.ADD_CORRELATION_SET, // issue #79779
            // ActionType.ADD_MESSAGE_EXCHANGE, // Issue 85553
            ActionType.ADD_EVENT_HANDLERS,
            // ActionType.ADD_COMPENSATION_HANDLER, // issue #79777, #107002
            // ActionType.ADD_TERMINATION_HANDLER, // issue #79781
            ActionType.ADD_FAULT_HANDLERS
        };
    }
}
