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

package org.netbeans.modules.xml.xpath.ext.metadata;

import java.util.List;
import javax.swing.Icon;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.metadata.GeneralFunctionMetadata.FunctionType;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.images.IconLoader;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;

/**
 * This extension function is used as special stub to generate a valid XPath 
 * expression in case when required arguments of operation or function is skipped.
 * 
 * The function is declared in empty namespace! So a prefix is not requiered.
 * 
 * The class is declared as final because the functin is very specific and 
 * is not intended to be inherited.
 *
 * @author nk160297
 */
public final class StubExtFunction extends XPathExtensionFunction {

    private static final MyMetadata STUB_FUNC_METADATA = new MyMetadata();
    public static final QName STUB_FUNC_NAME = new QName("stub"); // NOI18N
    
    public StubExtFunction(XPathModel model) {
        super(model, STUB_FUNC_METADATA);
    }

    /**
     * Calls the visitor.
     * @param visitor the visitor
     */
    @Override
    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }

    private static class MyMetadata implements ExtFunctionMetadata {

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return STUB_FUNC_NAME;
        }

        public Icon getIcon() {
            return IconLoader.UNKNOWN_ICON;
        }
        
        public String getDisplayName() {
            return "Stub"; // NOI18N
        }

        public String getShortDescription() {
            return "Mandatory argument stub"; // NOI18N
        }

        public String getLongDescription() {
            return "This function is used as special stub to generate" +
                    " a valid XPath expression in case when required arguments" +
                    " of operation or function is skipped"; // NOI18N
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public List<AbstractArgument> getArguments() {
            return null;
        }

        public XPathType getResultType() {
            return XPathType.ANY_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        public String toStrign() {
            return "Stub function metadata"; // NOI18N
        }
    }
    
}
