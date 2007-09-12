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

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

public class MessagePartNode extends BpelNode<Part> {
    
    public MessagePartNode(Part reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public MessagePartNode(Part reference, final Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.MESSAGE_PART;
    }
    
    public String getDisplayName() {
        Part ref = getReference();
        return ref == null ? null : ref.getName();
    }
    
    protected String getImplHtmlDisplayName() {
        String result;
        String typeName = getTypeName();
        result = SoaUiUtil.getGrayString(
                getName(),
                typeName == null ? "" : " " + typeName); // NOI18N
        //
        return result;
    }
    
    private String getTypeName() {
        String result = null;
        Part part = getReference();
        if (part != null) {
            NamedComponentReference<GlobalElement> elementRef = part.getElement();
            if (elementRef != null) {
                GlobalElement element = elementRef.get();
                if (element != null) {
                    result = element.getName();
                } else {
                    result = elementRef.getRefString();
                }
            }
            //
            if (result == null || result.length() == 0) {
                NamedComponentReference<GlobalType> typeRef = part.getType();
                if (typeRef != null) {
                    GlobalType type = typeRef.get();
                    if (type != null) {
                        result = type.getName();
                    } else {
                        result = typeRef.getRefString();
                    }
                }
            } 
        }
        //
        return result;
    }
    
}
