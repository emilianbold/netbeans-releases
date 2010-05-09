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

package org.netbeans.modules.xml.xpath.ext.spi;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTestType;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;

/**
 * This interface represents any special location steps like wildcards or
 * node types (node(), comment(), text(), processing-instruction()).
 * @author nikita
 */
public interface XPathSpecialStep extends XPathSchemaContextHolder {

    SsType getType();

    public enum SsType {
        NOT_SPECIAL,         // Not a special step at all
        ALL_ELEMENTS,        // *
        ALL_ATTRIBUTES,      // *@
        NODE,                // node() !!! ATTENTION: It isn't a special step but rather wildcard
        COMMENT,             // comment()
        TEXT,                // text()
        PROCESSING_INSTR;    // processing-instruction()
//        UNKNOWN_ELEMENT,
//        UNKNOWN_ATTRIBUTE;

        public String getDisplayName() {
            switch (this) {
                case ALL_ELEMENTS:
                    return "*"; // NOI18N
                case ALL_ATTRIBUTES:
                    return "@*"; // NOI18N
                case NODE:
                    return StepNodeTestType.NODETYPE_NODE.getXPathText() + "()"; // NOI18N
                case COMMENT:
                    return StepNodeTestType.NODETYPE_COMMENT.getXPathText() + "()"; // NOI18N
                case PROCESSING_INSTR:
                    // TODO: The processing-insturction can have additional
                    // argument. It hasn't supported here yet.
                    return StepNodeTestType.NODETYPE_PI.getXPathText() + "()"; // NOI18N
                case TEXT:
                    return StepNodeTestType.NODETYPE_TEXT.getXPathText() + "()"; // NOI18N
            }
            //
            return "???"; // NOI18N
        }
    }

    public class Utils {

        public static SsType calculateSsType(LocationStep lStep) {
            StepNodeTest snt = lStep.getNodeTest();
            if (snt instanceof StepNodeTypeTest) {
                switch (((StepNodeTypeTest)snt).getNodeType()) {
                    case NODETYPE_COMMENT:
                        return SsType.COMMENT;
                    case NODETYPE_NODE:
                        return SsType.NODE;
                    case NODETYPE_PI:
                        return SsType.PROCESSING_INSTR;
                    case NODETYPE_TEXT:
                        return SsType.TEXT;
                }
            } else if (snt instanceof StepNodeNameTest) {
                QName nodeName = ((StepNodeNameTest)snt).getNodeName();
                if (nodeName != null) {
                    String lName = nodeName.getLocalPart();
                    if ("*".equals(lName)) { // NOI18N
                        return SsType.ALL_ELEMENTS;
                    } else if ("@*".equals(lName)) { // NOI18N
                        return SsType.ALL_ATTRIBUTES;
                    } 
                }
            }
            //
            return SsType.NOT_SPECIAL;
        }

        public static LocationStep constructLocationStep(XPathModel model,
                XPathSpecialStep sStep) {
            //
            LocationStep result = null;
            StepNodeTest snt = null;
            XPathModelFactory factory = model.getFactory();
            switch(sStep.getType()) {
                case ALL_ATTRIBUTES:
                    snt = new StepNodeNameTest(new QName(StepNodeNameTest.ASTERISK));
                    result = factory.newLocationStep(XPathAxis.ATTRIBUTE, snt, null);
                    break;
                case ALL_ELEMENTS:
                    snt = new StepNodeNameTest(new QName(StepNodeNameTest.ASTERISK));
                    result = factory.newLocationStep(XPathAxis.CHILD, snt, null);
                    break;
                case NODE:
                    snt = new StepNodeTypeTest(StepNodeTestType.NODETYPE_NODE, null);
                    result = factory.newLocationStep(XPathAxis.CHILD, snt, null);
                    break;
                case COMMENT:
                    snt = new StepNodeTypeTest(StepNodeTestType.NODETYPE_COMMENT, null);
                    result = factory.newLocationStep(XPathAxis.CHILD, snt, null);
                    break;
                case TEXT:
                    snt = new StepNodeTypeTest(StepNodeTestType.NODETYPE_TEXT, null);
                    result = factory.newLocationStep(XPathAxis.CHILD, snt, null);
                    break;
                case PROCESSING_INSTR:
                    snt = new StepNodeTypeTest(StepNodeTestType.NODETYPE_PI, null);
                    result = factory.newLocationStep(XPathAxis.CHILD, snt, null);
                    break;
            }
            //
            return result;
        }

    }
}
