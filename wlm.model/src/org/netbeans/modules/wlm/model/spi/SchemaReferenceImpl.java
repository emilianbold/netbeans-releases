/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.spi;

import java.util.List;
import org.netbeans.modules.wlm.model.api.SchemaReference;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.impl.WLMComponentBase;
import org.netbeans.modules.wlm.model.utl.Util;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;

public class SchemaReferenceImpl <T extends ReferenceableSchemaComponent>
        extends AbstractNamedComponentReference<T> implements SchemaReference<T> {

	public SchemaReferenceImpl(Class<T> referencedType,
            AbstractDocumentComponent parent, String ref) {
		super(referencedType, parent, ref);
	}

	public SchemaReferenceImpl(T referenced, Class<T> referencedType,
            AbstractDocumentComponent parent) {
		super(referenced, referencedType, parent);
	}
	
    /**
     * @return string to use in persiting the reference as attribute value of 
     * the containing component
     */
    @Override
    public synchronized String getRefString() {
        if (refString == null) {
            assert super.getReferenced() != null;
            prefix = getParent().lookupPrefix(getEffectiveNamespace());
    		if (prefix == null) {
                TTask task = WLMModel.class.cast(getParent().getModel()).getTask();
    			prefix = Util.getNewPrefix (task);
    			WLMComponentBase.class.cast(task).addPrefix(prefix, getEffectiveNamespace());
    		}            
            localName = super.getReferenced().getName();
            if (prefix == null || prefix.length() == 0) {
                refString = localName;
            } else {
                refString = prefix + ":" + localName; //NOI18N
            }
        }
        return refString;
    }

    public String getEffectiveNamespace() {
        if (refString == null) {
            assert getReferenced() != null;
            T referenced = getReferenced();
            if (referenced != null) {
                SchemaModel sModel = referenced.getModel();
                if (sModel != null) {
                    Schema schema = sModel.getSchema();
                    if (schema != null) {
                        return schema.getTargetNamespace();
                    }
                }
            }
        } else {
        	String prfx = getPrefix();
            return getParent().lookupNamespaceURI(prfx);
        }
        //
        return null;
    }
    
	public boolean isResolved() {
		// TODO Auto-generated method stub
		get();
		if (getReferenced() == null)
			return false;
		return true;
	}

    public T get() {
        T referenced = getReferenced();
        if (referenced != null) {
            return referenced;
        }
        //
        
        AbstractDocumentComponent parent = getParent();
        List<T> children = parent.getChildren(getType());
        if (children != null && !children.isEmpty()) {
            for (T child : children) {
                String childName = child.getName();
                if (childName != null && childName.equals(this.getLocalName())) {
                    return child;
                }
            }
        }
        //
        return null;
    }
      
    
}
