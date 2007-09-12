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
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.properties.Constants;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author nk160297
 */
public class InitiateEditor extends PropertyEditorSupport implements ExPropertyEditor {
    
    private PropertyEnv myPropertyEnv;
    
    /** Creates a new instance of ModelReferenceEditor */
    public InitiateEditor() {
    }
    
    public String getAsText() {
        Initiate val = (Initiate)getValue();
        if (val == null) {
            return Constants.NOT_ASSIGNED;
        } else if (val.equals(Initiate.INVALID)) {
            return Constants.INVALID;
        } else {
            return val.toString();
        }
    }
    
    public Component getCustomEditor() {
        return null;
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.length() == 0 ||
                Constants.NOT_ASSIGNED.equals(text)) {
            setValue(null);
        } else {
            Initiate newValue = Initiate.forString(text);
            setValue(newValue);
        }
    }
    
    public String[] getTags() {
        return new String[] {
            Initiate.NO.toString(),
            Initiate.YES.toString(),
            Initiate.JOIN.toString()};
    }
    
    public Object getValue() {
        Object obj = super.getValue();
        if (obj != null) {
            assert obj instanceof Initiate;
        }
        return obj;
    }
    
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof Initiate;
        }
        super.setValue(newValue);
    }
    
    public void attachEnv(PropertyEnv propertyEnv) {
        myPropertyEnv = propertyEnv;
//        Object[] beansArr = propertyEnv.getBeans();
//        if (beansArr != null && beansArr.length != 0 && beansArr[0] != null) {
//            // a workaround to overcame issue with the ReusablePropertyEnv
//            myPropertyEnv = propertyEnv;
//        }
    }
}
