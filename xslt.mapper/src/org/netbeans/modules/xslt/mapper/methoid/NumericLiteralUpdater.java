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

package org.netbeans.modules.xslt.mapper.methoid;

import java.text.NumberFormat;
import java.text.ParseException;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.BasicLiteralEditorFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralEditor;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater.LiteralSubTypeInfo;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.xml.xpath.XPathNumericLiteral;
import org.netbeans.modules.xslt.mapper.model.nodes.LiteralCanvasNode;


/**
 * Updates the xpath expression for number literals.
 *
 * @author jsandusky
 */
public class NumericLiteralUpdater extends AbstractLiteralUpdater {
    
    private final String TYPE_LONG   = "Long";  // NOI18N
    private final String TYPE_DOUBLE = "Double";    // NOI18N
    
    
    public ILiteralEditor getEditor(IBasicMapper basicMapper, IFieldNode field) {
        return BasicLiteralEditorFactory.createStrictNumericEditor(basicMapper, field, this);
    }
    
    public String literalSet(IFieldNode fieldNode, String newValue) {
        LiteralSubTypeInfo typeInfo = getLiteralSubType(newValue);
        String newType = typeInfo.getType();
        // TODO reimplement
//        XPathLiteralNodeImpl literalNode = (XPathLiteralNodeImpl) fieldNode.getNodeObject();
//        if (literalNode == null) {
//            if (TYPE_LONG.equals(typeInfo.getType())) {
//                XPathNumericLiteral literal = AbstractXPathModelHelper.getInstance().newXPathNumericLiteral(new Long(newValue));
//                literalNode = new XPathLiteralNodeImpl(literal);
//            } else {
//                XPathNumericLiteral literal = AbstractXPathModelHelper.getInstance().newXPathNumericLiteral(new Double(newValue));
//                literalNode = new XPathLiteralNodeImpl(literal);
//            }
//        } else {
        LiteralCanvasNode node =
                (LiteralCanvasNode) fieldNode.getGroupNode().getNodeObject();
        
        XPathNumericLiteral literal = (XPathNumericLiteral) node.getDataObject();
        if (TYPE_LONG.equals(newType)) {
            literal.setValue(new Long(newValue));
        } else {
            literal.setValue(new Double(newValue));
        }

        mProcessor.updateNodeExpression(fieldNode);
        
        return newValue;
    }
    
    public LiteralSubTypeInfo getLiteralSubType(String freeTextValue) {
        if (freeTextValue == null || freeTextValue.length() < 1) {
            return null;
        }
        String value = freeTextValue.trim().toUpperCase();
        Number number = null;
        try {
            // This parsing returns a Long or Double.
            // The format of value may still be invalid because
            // unintelligable characters may still exist in value.
            number = NumberFormat.getInstance().parse(value);
        } catch (ParseException pe) {
            return null;
        }
        LiteralSubTypeInfo info = null;
        
        try {
            if (value.indexOf(".") > 0) {
                Double.parseDouble(value);
                return new LiteralSubTypeInfo(TYPE_DOUBLE, value);
            }
            if (number instanceof Long) {
                Long.parseLong(value); // ensure value has all valid characters
                info = new LiteralSubTypeInfo(TYPE_LONG, new Long(value).toString());
            } else {
                Double.parseDouble(value); // ensure value has all valid characters
                info = new LiteralSubTypeInfo(TYPE_DOUBLE, value);
            }
        } catch (NumberFormatException nfe) {
            // just return null to indicate no valid number
            return null;
        }
        
        return info;
    }
}
