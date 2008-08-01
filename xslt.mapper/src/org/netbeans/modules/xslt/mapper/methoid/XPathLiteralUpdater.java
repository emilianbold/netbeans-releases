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

import java.awt.Component;
import java.awt.Window;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralEditor;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xml.xpath.XPathStringLiteral;
import org.netbeans.modules.xslt.mapper.xpatheditor.XPathLiteralEditor;
import org.netbeans.modules.xslt.mapper.model.nodes.LiteralCanvasNode;


/**
 * Updates the xpath expression for string literals.
 *
 * @author jsandusky
 */
public class XPathLiteralUpdater extends AbstractLiteralUpdater {
    
    public ILiteralEditor getEditor(IBasicMapper basicMapper, IFieldNode field) {
        return new XPathLiteralEditor(getWindowOwner(basicMapper),
                basicMapper, field, this);
    }
    
    private static Window getWindowOwner(IBasicMapper basicMapper) {
        Window window = null;
        Component parent =
                basicMapper.getMapperViewManager().getCanvasView().getCanvas().getUIComponent();
        while (
                parent != null &&
                !(parent instanceof Window)) {
            parent = parent.getParent();
        }
        if (parent == null) {
            parent = WindowManager.getDefault().getMainWindow();
        }
        return (Window) parent;
    }
    
    public String getLiteralDisplayText(String literalText) {
        return getQuotedString(literalText).string;
    }
    
    public String literalSet(IFieldNode fieldNode, String newValue) {
        if (!isValidStringLiteral(newValue)) {
            String title = NbBundle.getMessage(StringLiteralUpdater.class,
                    "STR_INVALID_STRING_LITERAL_TITLE");    // NOI18N
            String msg = NbBundle.getMessage(StringLiteralUpdater.class,
                    "STR_INVALID_STRING_LITERAL_MSG", newValue);    // NOI18N
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, title,
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
// TODO reimplement
//        XPathLiteralNodeImpl literalNode = (XPathLiteralNodeImpl) fieldNode.getNodeObject();
//        if (literalNode == null) {
//            XPathStringLiteral stringLiteral = AbstractXPathModelHelper.getInstance().newXPathStringLiteral(newValue);
//            literalNode = new XPathLiteralNodeImpl(stringLiteral);
//        } else {
//            XPathStringLiteral stringLiteral = (XPathStringLiteral) literalNode.getLiteral();
//            stringLiteral.setValue(newValue);
//        }
//        super.applyLiteral(fieldNode, newValue, literalNode);
        
        LiteralCanvasNode literalNode =
                (LiteralCanvasNode) fieldNode.getGroupNode().getNodeObject();
        if (literalNode != null) {
            XPathExpression newXPathExpr = null;
            XPathModel xpModel = AbstractXPathModelHelper.
                    getInstance().newXPathModel();
            try {
                newXPathExpr = xpModel.parseExpression(newValue);
            } catch (XPathException xpe) {
                newXPathExpr = AbstractXPathModelHelper.
                        getInstance().newXPathStringLiteral(newValue);
            }
            //
            if (newXPathExpr != null) {
                literalNode.setDataObject(newXPathExpr);
            }
        }
        mProcessor.updateNodeExpression(fieldNode);
        
        return newValue;
    }
    
    private boolean isValidStringLiteral(String literal) {
        // by this point, string is quoted
        // now we check that the string follows xpath 1.0 spec
        // - josh
        QuotedString quotedString = getQuotedString(literal);
        return quotedString.quote.isQuotingValid(quotedString.string);
    }
    
    private QuotedString getQuotedString(String literal) {
        QuoteType quoteType = QuoteType.getQuoteType(literal);
        if (quoteType == QuoteType.UNQUOTED) {
            if (literal.indexOf(QuoteType.SINGLE.quote) >= 0) {
                // string contains a single-quote,
                // it must be quoted with double-quotes
                literal = QuoteType.DOUBLE.quote + literal + QuoteType.DOUBLE.quote;
                quoteType = QuoteType.DOUBLE;
            } else {
                // by default, single-quote the string
                literal = QuoteType.SINGLE.quote + literal + QuoteType.SINGLE.quote;
                quoteType = QuoteType.SINGLE;
            }
        }
        return new QuotedString(literal, quoteType);
    }
    
    
    private static final class QuotedString {
        public final String string;
        public final QuoteType quote;
        public QuotedString(String quotedString, QuoteType quoteType) {
            string = quotedString;
            quote = quoteType;
        }
    }
    
    private static final class QuoteType {
        private static final QuoteType SINGLE   = new QuoteType("'");   // NOI18N
        private static final QuoteType DOUBLE   = new QuoteType("\"");  // NOI18N
        private static final QuoteType UNQUOTED = new QuoteType();
        public final String quote;
        private QuoteType(String quoteString) {
            quote = quoteString;
        }
        private QuoteType() {
            quote = null;
        }
        public boolean isStringQuoted(String literal) {
            if (quote == null) {
                return false;
            }
            if (literal.length() >= 2) {
                if (literal.startsWith(quote) && literal.endsWith(quote)) {
                    return true;
                }
            }
            return false;
        }
        public boolean isQuotingValid(String literal) {
            if (quote == null) {
                return false;
            }
            // ensure that we have only our beginning quote and end quote
            // any more than that and our number of sections will be greater than 3
            // any less than that and our number of sections will be less than 3
            String[] quoteSections = literal.split(quote, 4);
            return quoteSections.length == 3;
        }
        public static QuoteType getQuoteType(String literal) {
            QuoteType quoteType = UNQUOTED;
            // if string is already quoted, mark as such
            if (SINGLE.isStringQuoted(literal)) {
                quoteType = SINGLE;
            } else if (DOUBLE.isStringQuoted(literal)) {
                quoteType = DOUBLE;
            }
            return quoteType;
        }
    }
}
