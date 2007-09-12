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
package org.netbeans.modules.bpel.properties.props;

import java.beans.PropertyEditor;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.props.editors.NodePropEditor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * This Property is very special.
 * It has associated Custom Editor and this edior is the Custom Editor of the parent Node.
 * So it allows showing Node's Custom editor from the Property Sheet.
 * <p>
 * The value of the property has the type of the parent Node.
 * <p>
 * @author nk160297
 */
public class CustomEditorProperty extends Node.Property {
    
    private BpelNode parentNode;
    
    public CustomEditorProperty(BpelNode parentNode) {
        super(parentNode.getClass());
        //
        setHidden(parentNode.isModelReadOnly());
        //
        String name = NbBundle.getMessage(FormBundle.class, "LBL_Property_Editor"); // NOI18N
        setName(name);
        setDisplayName(name);
        setValue("canEditAsText", Boolean.FALSE); // NOI18N
        this.parentNode = parentNode;
    }
    
    public boolean canRead() {
        return true;
    }
    
    public boolean canWrite() {
        return !parentNode.isModelReadOnly();
    }
    
    public void setValue(Object object) {
    }
    
    public Object getValue() {
        return parentNode;
    }
    
    public PropertyEditor getPropertyEditor() {
        PropertyEditor editor = new NodePropEditor();
        return editor;
    }
    
}
