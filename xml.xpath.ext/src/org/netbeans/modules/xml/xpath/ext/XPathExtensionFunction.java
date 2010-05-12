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

package org.netbeans.modules.xml.xpath.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.metadata.AbstractArgument;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.ResultTypeCalculator;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.images.IconLoader;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;
import org.netbeans.modules.xml.xpath.ext.visitor.impl.ExpressionWriter;

/**
 * Represents a extension XPath function.
 * 
 * This class can be extended in external modules. 
 * That is why it is not an interface but a class as distinct from other XPath 
 * entities. It has its own implementation of all base interfaces. 
 * Other XPath entities has hidden implementation. Only theirs interfaces are opened. 
 * 
 * @author Enrico Lelina
 * @version 
 */
public class XPathExtensionFunction implements XPathOperationOrFuntion<QName> {
    
    public XPathModel mModel;
    public ExtFunctionMetadata mMetadata;

    /** List of child expressions. */
    public List<XPathExpression> mChildren = new ArrayList<XPathExpression>();
    
    
    public XPathExtensionFunction(XPathModel model) {
        this(model, NULL_METADATA_STUB);
    }
    
    public XPathExtensionFunction(XPathModel model, ExtFunctionMetadata metadata) {
        mModel = model;
        mMetadata = metadata;
    }
    
    public XPathModel getModel() {
        return mModel;
    }

    public ExtFunctionMetadata getMetadata() {
        return mMetadata;
    }

    public QName getName() {
        return mMetadata.getName();
    }

    public List<XPathExpression> getChildren() {
        return mChildren;
    }

    public int getChildCount() {
        return mChildren.size();
    }

    public XPathExpression getChild(int index) throws IndexOutOfBoundsException {
        return mChildren.get(index);
    }

    public void addChild(XPathExpression child) {
        mChildren.add(child);
    }

    public void insertChild(int index, XPathExpression child) {
        mChildren.add(index, child);
    }

    public void populateWithStub(int count) {
        XPathModelFactory factory = getModel().getFactory();
        for (int index = 0; index < count; index++) {
            XPathExtensionFunction newStub = factory.newXPathExtensionFunction(
                    StubExtFunction.STUB_FUNC_NAME);
            mChildren.add(newStub);
        }
    }

    public boolean removeChild(XPathExpression child) {
        return mChildren.remove(child);
    }

    public void clearChildren() {
        mChildren.clear();
    }

    public String getExpressionString() {
        XPathVisitor visitor = new ExpressionWriter(mModel);
        accept(visitor);
        return ((ExpressionWriter) visitor).getString();
    }

    public String getExpressionString(NamespaceContext nc) {
        if (mModel.getNamespaceContext() == nc) {
            // optimization
            return getExpressionString();
        }
        XPathVisitor visitor = new ExpressionWriter(nc);
        accept(visitor);
        return ((ExpressionWriter) visitor).getString();
    }

    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public String toString() {
        return getExpressionString();
    }
    
    /**
     * This metadata is a stub. It can be used as a temporary empty implementation.
     */
    public static final ExtFunctionMetadata NULL_METADATA_STUB = 
            new ExtFunctionMetadata() {

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return null;
        }

        public Icon getIcon() {
            return IconLoader.UNKNOWN_ICON;
        }
        
        public String getDisplayName() {
            return "";
        }

        public String getShortDescription() {
            return ""; // NOI18N
        }

        public String getLongDescription() {
            return ""; // NOI18N
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
            return "NULL metadata stub"; // NOI18N
        }
    };

}
