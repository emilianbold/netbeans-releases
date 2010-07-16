/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xpath.ext.schema.resolver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep.SsType;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStepImpl;

/**
 * The special context for wildcard location steps - '*', '*@', 'node()'.
 * Type of step correlates with lookForElements and lookForAttributes
 * parameters according to the following table:
 *
 *    lookForElements       lookFroAttributes     wildcard
 *        true                    true              node()
 *        true                    false               *
 *        false                   true                @*
 * 
 * @author nk160297
 */
public class WildcardSchemaContext implements XPathSchemaContext {

    private XPathModel mXPathModel; // Can be null!
    private XPathSchemaContext mParentContext;
    private boolean mLookForElements;
    private boolean mLookForAttributes;
    private boolean lastInChain = false;

    private XPathSpecialStep mSStep;

    // TO DO replace to weak reference
    private Set<SchemaCompPair> mSchemaCompPair = null;
    private Set<SchemaCompHolder> mUsedSchemaCompSet;

    /**
     * Special constructor without XPathModel
     * @param parentContext
     * @param ssType
     */
    public WildcardSchemaContext(XPathSchemaContext parentContext, SsType ssType) {
        this(parentContext, null, ssType);
    }

    public WildcardSchemaContext(XPathSchemaContext parentContext,
            XPathModel xPathModel, SsType ssType) {
        mXPathModel = xPathModel;
        mParentContext = parentContext; // it can be null in case of wildcard at root level
        mSStep = new XPathSpecialStepImpl(ssType, this);
        //
        switch (ssType) {
            case ALL_ATTRIBUTES:
                mLookForElements = false;
                mLookForAttributes = true;
                break;
            case ALL_ELEMENTS:
                mLookForElements = true;
                mLookForAttributes = false;
                break;
            case NODE:
                mLookForElements = true;
                mLookForAttributes = true;
                break;
            default:
                // This contex isn't intended for othe objects!
                assert false:
                //
                // TODO: It is set to false both now, but it is necessary to
                // figure out with it in future.
                mLookForElements = false;
                mLookForAttributes = false;
        }
    }

    public XPathSpecialStep getSpecialStep() {
        return mSStep;
    }

    public XPathSchemaContext getParentContext() {
        return mParentContext;
    }

    public synchronized Set<SchemaCompPair> getSchemaCompPairs() {
        if (mSchemaCompPair == null) {
            mSchemaCompPair = calculateSchemaComponents();
        }
        return mSchemaCompPair;
    }

    private Set<SchemaCompPair> calculateSchemaComponents() {
        HashSet<SchemaCompPair> result = new HashSet<SchemaCompPair>();
        //
        if (mParentContext == null) {
            //
            List<SchemaComponent> rootCompList = XPathUtils.findRootComponents(
                    mXPathModel, mLookForElements, mLookForAttributes, false);
            for (SchemaComponent foundComp : rootCompList) {
                SchemaCompPair newPair = new SchemaCompPair(
                        foundComp, (SchemaCompHolder)null);
                result.add(newPair);
            }
        } else {
            List<SchemaCompPair> scHolderList = XPathUtils.findSubcomponents(
                    mXPathModel, mParentContext, 
                    mLookForElements, mLookForAttributes, false);
            result.addAll(scHolderList);
        }
        //
        return result;
    }
    
    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        HashSet<SchemaCompPair> resultSet = new HashSet<SchemaCompPair>();
        //
        if (mUsedSchemaCompSet != null) {
            for (SchemaCompPair myCompPair : getSchemaCompPairs()) {
                SchemaCompHolder myCompHolder = myCompPair.getCompHolder();
                for (SchemaCompHolder usdCompHolder : mUsedSchemaCompSet) {
                    if (myCompHolder.equals(usdCompHolder)) {
                        resultSet.add(myCompPair);
                    }
                }
            }
        }
        //
        return resultSet;
    }

    public void setUsedSchemaCompH(Set<SchemaCompHolder> compSet) {
        mUsedSchemaCompSet = compSet;
    }

    public String toStringWithoutParent() {
        String wildcard = null;
        switch (mSStep.getType()) {
            case ALL_ATTRIBUTES:
                wildcard = "*@"; // NOI18N
                break;
            case ALL_ELEMENTS:
                wildcard = "*"; // NOI18N
                break;
            case NODE:
                wildcard = SsType.NODE.getDisplayName(); // NOI18N
                break;
            default:
                wildcard = "???"; // NOI18N
        }
        return wildcard;
    }

    public String getExpressionString(NamespaceContext nsContext, SchemaModelsStack sms) {
        //
        String wildcard = null;
        switch (mSStep.getType()) {
            case ALL_ATTRIBUTES:
                wildcard = "*@"; // NOI18N
                break;
            case ALL_ELEMENTS:
                wildcard = "*"; // NOI18N
                break;
            case NODE:
                wildcard = SsType.NODE.getDisplayName(); // NOI18N
                break;
            default:
                wildcard = "???"; // NOI18N
        }
        //
        String result = null;
        if (mParentContext == null) {
            result = LocationStep.STEP_SEPARATOR + wildcard;
        } else {
            result = mParentContext.getExpressionString(nsContext, sms) + 
                    LocationStep.STEP_SEPARATOR + wildcard;
        }
        return  result;
    }

    @Override
    public boolean equals(Object obj)  {
        if (obj instanceof WildcardSchemaContext) {
            return WildcardSchemaContext.class.cast(obj).
                    getSpecialStep().equals(mSStep);
        }
        //
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.mParentContext != null ? this.mParentContext.hashCode() : 0);
        hash = 71 * hash + (this.mSStep != null ? this.mSStep.hashCode() : 0);
        return hash;
    }

    public boolean equalsChain(XPathSchemaContext other) {
        return XPathSchemaContext.Utilities.equalsChain(this, other);
    }

    public boolean isLastInChain() {
        return lastInChain;
    }

    public void setLastInChain(boolean value) {
        lastInChain = value;
    }
    
}
