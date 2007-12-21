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
