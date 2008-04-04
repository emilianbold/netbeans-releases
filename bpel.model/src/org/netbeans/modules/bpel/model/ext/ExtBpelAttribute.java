/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.model.ext;

import javax.xml.XMLConstants;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.openide.ErrorManager;

/**
 * This kind of attribute is intended to be used for anyAttribute 
 * 
 * @author nk160297
 */
public abstract class ExtBpelAttribute implements Attribute {

    private BpelEntity mOwner;
    
    public ExtBpelAttribute() {
    }
    
    public ExtBpelAttribute(BpelEntity owner) {
        mOwner = owner;
    }
    
    public void setOwner(BpelEntity owner) {
        mOwner = owner;
    }
    
    public BpelEntity getOwner() {
        return mOwner;
    }
    
    public abstract String getLocalName();
    public abstract String getNsUri();
    
    public String getName() {
        assert mOwner != null : "An owner has to be specified first!"; // NOI18N
        ExNamespaceContext nsContext = mOwner.getNamespaceContext();
        //
        assert nsContext != null;
        String prefix = nsContext.getPrefix(getNsUri());
        if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
            return getLocalName();
        } 
        //
        if (prefix == null) {
            // If there isn't a prefix for the specified namespace
            // then consider that attribute isn't specified at all. 
            return "";
        }
        //
        return prefix + ":" + getLocalName();
    }

    /**
     * Register a new prefix for the namespace if necessary. 
     * @return current prefix
     */
    public String registerNsPrefix() {
        assert mOwner != null : "An owner has to be specified first!"; // NOI18N
        ExNamespaceContext nsContext = mOwner.getNamespaceContext();
        //
        assert nsContext != null;
        String prefix = nsContext.getPrefix(getNsUri());
        if (prefix == null) {
            try {
                prefix = nsContext.addNamespace(getNsUri());
            } catch (InvalidNamespaceException ex) {
                // The design error if it appears
                ErrorManager.getDefault().notify(ex);
                assert false;
            }
        } 
        return prefix;
    }
    
    public static class IsAtomicAttribute extends ExtBpelAttribute {

        @Override
        public String getLocalName() {
            return Process.ATOMIC;
        }

        @Override
        public String getNsUri() {
            return Extensions.TRANSACTION_EXT_URI;
        }

        public Class getType() {
            return TBoolean.class;
        }

        public Class getMemberType() {
            return null;
        }
    }
}
