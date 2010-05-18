/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package test.dwarfclassview;

import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import test.dwarfclassview.consts.KIND;
import test.dwarfclassview.consts.NodeATTR;

public class ClassViewElement extends ElementImpl {

    public ClassViewElement(ClassViewDocument document) {
        super(document, "node"); // NOI18N
    }
    
    public void setAttribute(NodeATTR attr, String value) {
        setAttribute(attr.value(), value);
    }

    public void setKind(KIND kind) {
        if (kind != null) {
            setAttribute(NodeATTR.TYPE.value(), kind.value());
        }
    }

    void setName(String name) {
        setAttribute(NodeATTR.NAME.value(), name);
    }


    String getAttribute(NodeATTR attr) {
        return getAttribute(attr.value());
    }

    public boolean equals(final ClassViewElement elem) {
        boolean result = attributeEluals(elem, NodeATTR.TYPE);
        result &= attributeEluals(elem, NodeATTR.QNAME);
        result &= attributeEluals(elem, NodeATTR.PARAMS);
        return result;
    }    
    
    private boolean attributeEluals(ClassViewElement elem, NodeATTR attr) {
        String val1 = getAttribute(attr);
        String val2 = elem.getAttribute(attr);
        return (val1.equals("") || val2.equals("") || val1.equals(val2)); // NOI18N
    }
  
}
