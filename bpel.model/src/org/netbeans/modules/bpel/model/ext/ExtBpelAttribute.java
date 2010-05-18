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

import java.util.logging.Logger;
import javax.xml.XMLConstants;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.NMPropertyHolder;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.bpel.model.api.support.AtomicTxType;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.xam.BpelAttributes.AttrType;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * This kind of attribute is intended to be used for any extension Attribute 
 * 
 * @author nk160297
 * @author Vitaly Bychkov
 */
public abstract class ExtBpelAttribute implements Attribute {
    
    public static final String COLUMN_STR = ":"; //NOI18N
    
    public enum ExtBpelAttributeMeta {
        IGNORE_MISSING_FROM_DATA(Extensions.SUN_EXT_URI, Copy.IGNORE_MISSING_FROM_DATA, TBoolean.class),
        IS_ATOMIC(Extensions.TRANSACTION_EXT_URI, Process.ATOMIC, TBoolean.class),
        ATOMIC_TX_TYPE(Extensions.TRANSACTION_EXT_URI, Process.ATOMIC_TX_TYPE, AtomicTxType.class),
        NM_PROPERTY(Extensions.NM_PROPERTY_EXT_URI, NMPropertyHolder.NM_PROPERTY, String.class),
        PERSISTENCE_OPT_OUT(Extensions.SUN_EXT_URI, Process.PERSISTENCE_OPT_OUT, TBoolean.class),
        WAITING_REQUEST_LIFE_SPAN(Extensions.SUN_EXT_URI, Process.WAITING_REQUEST_LIFE_SPAN, Integer.class, AttrType.NON_NEGATIVE_INTEGER);
        
        private String myAttributeNsUri;
        private String myAttributeLocalName;
        private Class myAttributeType;
        private Class myAttributeTypeInContainer;
        private AttrType myType; 
        
        private ExtBpelAttributeMeta(String nsUri, String name, Class type , 
                Class subType, AttrType attrType) 
        {
            myAttributeNsUri = nsUri;
            myAttributeLocalName = name;
            myAttributeType = type;
            myType = attrType;
        }

        private ExtBpelAttributeMeta(String nsUri, String name, Class type , 
                AttrType attrType) 
        {
            this(nsUri, name, type, null, attrType);
        }
        
        private ExtBpelAttributeMeta(String nsUri, String name, Class type) {
            this(nsUri, name, type, null);
        }

        @Override
        public String toString() {
            return myAttributeLocalName;
        }
        
        public static ExtBpelAttributeMeta forName(String nsUri, String name) {
            for (ExtBpelAttributeMeta attr : values()) {
                if ( attr.getNsUri().equals(nsUri)
                        && attr.getLocalName().equals(name)) 
                {
                    return attr;
                }
            }
            return null;
        }

        public Class getMemberType() {
            return myAttributeTypeInContainer;
        }
        
        public String getLocalName() {
            return myAttributeLocalName;
        }
        
        public String getNsUri() {
            return myAttributeNsUri;
        }
        
        public AttrType getAttributeType() {
            return myType;
        }
        
        public Class getType() {
            return myAttributeType;
        }
    }
    
    private static Logger LOGGER = Logger.getLogger(ExtBpelAttribute.class.getName());
    private BpelEntity mOwner;
    
    public ExtBpelAttribute() {
    }
    
    public ExtBpelAttribute(BpelEntity owner) {
        mOwner = owner;
    }

    /**
     * 
     * @param qnameStr 
     * @param owner
     * @return
     */
    public static ExtBpelAttributeMeta forName(String qnameStr, BpelEntity owner) {
        if (qnameStr == null || owner == null) {
            return null;
        }
        ExNamespaceContext context = owner.getNamespaceContext();
        int columnPos = qnameStr.indexOf(COLUMN_STR);
        if (columnPos <= 0) {
            return null;
        }
        
        String prefix = qnameStr.substring(0, columnPos);
        String localPart = qnameStr.substring(columnPos+1);
        String nsUri = context.getNamespaceURI(prefix);
        return ExtBpelAttributeMeta.forName(nsUri, localPart);
    }
    
    public abstract ExtBpelAttributeMeta getAttrMeta();
    
    public void setOwner(BpelEntity owner) {
        mOwner = owner;
    }
    
    public BpelEntity getOwner() {
        return mOwner;
    }

    public Class getType() {
        return getAttrMeta().getType();
    }

    public Class getMemberType() {
        return getAttrMeta().getMemberType();
    }
    
    /**
     * @return type of attribute value
     */
    public AttrType getAttributeType(){
        return getAttrMeta().getAttributeType();
    }
    
    public String getLocalName() {
        return getAttrMeta().getLocalName();
    }
    
    public String getNsUri() {
        return getAttrMeta().getNsUri();
    }
    
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

    public static class IsAtomicAttribute extends ExtBpelAttribute {

        public IsAtomicAttribute() {
            super();
        }

        public IsAtomicAttribute(BpelEntity owner) {
            super(owner);
        }
        
        public ExtBpelAttributeMeta getAttrMeta() {
            return ExtBpelAttributeMeta.IS_ATOMIC;
        }
    }
    
    public static class AtomicTxTypeAttribute extends ExtBpelAttribute {

        public AtomicTxTypeAttribute() {
            super();
        }

        public AtomicTxTypeAttribute(BpelEntity owner) {
            super(owner);
        }
        
        public ExtBpelAttributeMeta getAttrMeta() {
            return ExtBpelAttributeMeta.ATOMIC_TX_TYPE;
        }
    }

    public static class NMPropertyAttibute extends ExtBpelAttribute {
        
        public NMPropertyAttibute() {
            super();
        }
        
        public NMPropertyAttibute(BpelEntity owner) {
            super(owner);
        }
        
        @Override
        public ExtBpelAttributeMeta getAttrMeta() {
            return ExtBpelAttributeMeta.NM_PROPERTY;
        }
    }

    public static class PersistenceOptOutAttribute extends ExtBpelAttribute {

        public PersistenceOptOutAttribute() {
            super();
        }
        
        public PersistenceOptOutAttribute(BpelEntity owner) {
            super(owner);
        }
        
        @Override
        public ExtBpelAttributeMeta getAttrMeta() {
            return ExtBpelAttributeMeta.PERSISTENCE_OPT_OUT;
        }
    }
    
    public static class WaitingRequestLifeSpanAttribute extends ExtBpelAttribute {

        public WaitingRequestLifeSpanAttribute() {
            super();
        }
        
        public WaitingRequestLifeSpanAttribute(BpelEntity owner) {
            super(owner);
        }
        
        
        @Override
        public ExtBpelAttributeMeta getAttrMeta() {
            return ExtBpelAttributeMeta.WAITING_REQUEST_LIFE_SPAN;
        }
    }

    /**
     * It is for process level ignoreMissingFromData attribute with the similar meaning as 
     * ignoreMissingFromData attribute at the copy level but for the whole process.
     * @see org.netbeans.modules.bpel.model.api.Process#getIgnoreMissingFromData()
     */
    public static class IgnoreMissingFromDataAttribute extends ExtBpelAttribute {

        public IgnoreMissingFromDataAttribute() {
            super();
        }
        
        public IgnoreMissingFromDataAttribute(BpelEntity owner) {
            super(owner);
        }
        
        
        @Override
        public ExtBpelAttributeMeta getAttrMeta() {
            return ExtBpelAttributeMeta.IGNORE_MISSING_FROM_DATA;
        }
    }
}
