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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.soa.xpath.mapper.lsm;

import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * Special auxiliary container which holds main properties of a pseudo component.
 * For example, it is used to trasmit data from PseudoComp creation dialog. 
 * @author nk160297
 */
public final class DetachedPseudoComp implements XPathPseudoComp {

    private GlobalType mType;
    private String mName;
    private String mNamespace;
    private boolean mIsAttribute;

    public DetachedPseudoComp(GlobalType type, String name, 
            String namespace, boolean isAttribute) {
        mType = type;
        mName = name;
        mNamespace = namespace;
        mIsAttribute = isAttribute;
    }
    
    public GlobalType getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public String getNamespace() {
        return mNamespace;
    }
    
    public boolean isAttribute() {
        return mIsAttribute;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof XPathPseudoComp) {
            XPathPseudoComp otherPC = (XPathPseudoComp)other;
            if (otherPC.isAttribute() != this.isAttribute()) {
                return false;
            }
            if (!(otherPC.getName().equals(this.getName()))) {
                // different name
                return false;
            } 
            if (!(otherPC.getNamespace().equals(this.getNamespace()))) {
                // different namespace
                return false;
            } 
            if (!(otherPC.getType().equals(this.getType()))) {
                // different type
                return false;
            }
            //
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        if (isAttribute()) {
            return "/@" + mName; 
        } else {
            return "/" + mName; 
        }
    }

    //----------------------------------------------------------------
    
    public XPathSchemaContext getSchemaContext() {
        throw new UnsupportedOperationException();
    }

    public void setSchemaContext(XPathSchemaContext newContext) {
        throw new UnsupportedOperationException();
    }

}
