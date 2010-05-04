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

package org.netbeans.modules.xml.xpath.ext.schema.resolver;

import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep.SsType;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStepImpl;

/**
 * A schema context for specific XPath entities like the following:
 *    text()
 *    comment()
 *    processing-instruction()
 *    LocationStep which references to an unknown element or attribute
 *
 * Be aware that the node() isn't a special step. It rather considered as
 * a wildcard like * or @*
 *
 * @author Nikita Krjukov
 */
public class SpecialSchemaContext implements XPathSchemaContext {

    private XPathSchemaContext mParentContext;
    private XPathSpecialStep mSStep;
    private boolean lastInChain = false;

    public SpecialSchemaContext(XPathSchemaContext parentContext, SsType ssType) {
        mParentContext = parentContext; // can be null!
        mSStep = new XPathSpecialStepImpl(ssType, this);
    }

    public XPathSpecialStep getSpecialStep() {
        return mSStep;
    }

    public XPathSchemaContext getParentContext() {
        return mParentContext;
    }


    public Set<SchemaCompPair> getSchemaCompPairs() {
        return Collections.EMPTY_SET;
    }

    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        return Collections.EMPTY_SET;
    }

    public void setUsedSchemaCompH(Set<SchemaCompHolder> compHolderSet) {
        // Ignore the set because the context doesn't imply any schema components
    }

    public String toStringWithoutParent() {
        String stepText = null;
        switch (mSStep.getType()) {
            case COMMENT:
            case TEXT:
            case PROCESSING_INSTR:
                stepText = mSStep.getType().getDisplayName();
                break;
            default:
                stepText = "???"; // NOI18N
        }
        return stepText;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //
        if (mParentContext != null) {
            sb.append(mParentContext.toString());
        }
        sb.append(LocationStep.STEP_SEPARATOR);
        //
        sb.append(toStringWithoutParent());
        //
        return sb.toString();
    }

    public String getExpressionString(NamespaceContext nsContext, SchemaModelsStack sms) {
        //
        String stepText = null;
        switch (mSStep.getType()) {
            case COMMENT:
            case TEXT:
            case PROCESSING_INSTR:
                stepText = mSStep.getType().getDisplayName();
                break;
            default:
                stepText = "???"; // NOI18N
        }
        //
        String result = null;
        if (mParentContext == null) {
            result = LocationStep.STEP_SEPARATOR + stepText;
        } else {
            result = mParentContext.getExpressionString(nsContext, sms) + 
                    LocationStep.STEP_SEPARATOR + stepText;
        }
        return  result;
    }

    public boolean isLastInChain() {
        return lastInChain;
    }

    public void setLastInChain(boolean value) {
        lastInChain = value;
    }

    @Override
    public boolean equals(Object obj)  {
        if (obj instanceof SpecialSchemaContext) {
            return SpecialSchemaContext.class.cast(obj).getSpecialStep().equals(mSStep);
        }
        //
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.mParentContext != null ? this.mParentContext.hashCode() : 0);
        hash = 11 * hash + (this.mSStep != null ? this.mSStep.hashCode() : 0);
        return hash;
    }

    public boolean equalsChain(XPathSchemaContext other) {
        return XPathSchemaContext.Utilities.equalsChain(this, other);
    }

}
