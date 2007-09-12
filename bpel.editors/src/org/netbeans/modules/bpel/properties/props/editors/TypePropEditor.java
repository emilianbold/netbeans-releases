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

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.soa.ui.form.Reusable;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * @author nk160297
 */
public class TypePropEditor extends PropertyEditorSupport
        implements ExPropertyEditor, Reusable {
    private PropertyEnv myPropertyEnv = null;
    private StereotypeFilter myFilter;
    
    public TypePropEditor() {
        myFilter = StereotypeFilter.ALL;
    }
    
    protected TypePropEditor(StereotypeFilter filter) {
        myFilter = filter;
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        TypePropertyCustomizer customizer = PropertyUtils.propertyCustomizerPool.
                getObjectByClass(TypePropertyCustomizer.class);
        customizer.init(myPropertyEnv, this);
        return customizer;
        // return new MyCustomEditor(myPropertyEnv, this);
    }
    
    public String getAsText() {
        Object value = getValue();
        if (value != null) {
            assert value instanceof TypeContainer; // has to be!!!
            QName typeQName = ((TypeContainer)value).getTypeQName();
            Object[] beans = myPropertyEnv.getBeans();
            if (beans.length > 0) {
                Object firstBean = beans[0];
                if (firstBean != null && firstBean instanceof BpelNode) {
                    Object refObj = ((BpelNode)firstBean).getReference();
                    if (refObj instanceof BpelEntity) {
                        BpelEntity entity = (BpelEntity)refObj;
                        return ResolverUtility.
                                qName2DisplayText(typeQName, entity);
                    }
                }
            }
            return ResolverUtility.qName2DisplayText(typeQName);
        }
        //
        return "";
    }
    
    public void attachEnv(PropertyEnv propertyEnv) {
        myPropertyEnv = propertyEnv;
    }
    
    public StereotypeFilter getSTypeFilter() {
        return myFilter;
    }
    
}
