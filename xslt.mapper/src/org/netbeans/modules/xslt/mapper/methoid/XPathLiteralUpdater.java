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
        // now we validate that the string follows xpath 1.0 spec
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
