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

package org.netbeans.modules.xml.xpath.ext.metadata.impl;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.modules.xml.xpath.ext.metadata.AbstractArgument;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.GeneralFunctionMetadata.FunctionType;
import org.netbeans.modules.xml.xpath.ext.metadata.OperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.ResultTypeCalculator;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.images.IconLoader;
import org.openide.util.NbBundle;

/**
 * Contains static metadata classes for all XPath operations.
 * 
 * @author nk160297
 */
public abstract class OperationMetadataImpl implements OperationMetadata {
    
    protected ArrayList<AbstractArgument> mArguments = 
                new ArrayList<AbstractArgument>();

    protected Icon mIcon;
    
    protected OperationMetadataImpl() {
        initArguments();
    }

    public String getId() {
        return getName();
    }
    
    public Icon getIcon() {
        if (mIcon == null) {
            loadIcon();
        }
        return mIcon;
    }
    
    public String getShortDescription() {
        return "";
    }

    public String getLongDescription() {
        return "";
    }

    public FunctionType getFunctionType() {
        return FunctionType.OPERATION;
    }

    public ResultTypeCalculator getResultTypeCalculator() {
        return null;
    }

    public List<AbstractArgument> getArguments() {
        return mArguments;
    }

    protected void loadIcon() {
        mIcon = IconLoader.getIcon(getId()); // NOI18N
    }
    
    protected void initArguments() {
        mArguments.add(ArgumentDescriptor.Predefined.ANY_TYPE);
        mArguments.add(ArgumentDescriptor.Predefined.ANY_TYPE);
    }
    
    //==========================================================================
    // Boolean operations
    //==========================================================================
    
    protected static abstract class BooleanOperMetadata extends OperationMetadataImpl {
        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
    }
    
    public static final class OrOperationMetadata extends BooleanOperMetadata {

        public int getPrecedenceLevel() {
            return 10;
        }

        public String getName() {
            return "or"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_LogicalOr"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }

        @Override
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_BOOLEAN);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_BOOLEAN);
        }

    }

    public static final class AndOperationMetadata extends BooleanOperMetadata {

        public int getPrecedenceLevel() {
            return 20;
        }

        public String getName() {
            return "and"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_LogicalAnd"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }

        @Override
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_BOOLEAN);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_BOOLEAN);
        }
    }

    public static final class EqualOperationMetadata extends BooleanOperMetadata {

        public int getPrecedenceLevel() {
            return 30;
        }

        public String getName() {
            return "="; // NOI18N
        }

        @Override
        public String getId() {
            return "equal"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Equal"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }

        @Override
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.ANY_TYPE);
            mArguments.add(ArgumentDescriptor.Predefined.ANY_TYPE);
        }
    }

    public static final class NotEqualOperationMetadata extends BooleanOperMetadata {

        public int getPrecedenceLevel() {
            return 30;
        }

        public String getName() {
            return "!="; // NOI18N
        }

        @Override
        public String getId() {
            return "not_equal"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_NotEqual"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    public static final class LEOperationMetadata extends BooleanOperMetadata {

        public int getPrecedenceLevel() {
            return 40;
        }

        public String getName() {
            return "<="; // NOI18N
        }

        @Override
        public String getId() {
            return "lesser_or_equal"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_LessOrEqual"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    public static final class LTOperationMetadata extends BooleanOperMetadata {

        public int getPrecedenceLevel() {
            return 40;
        }

        public String getName() {
            return "<"; // NOI18N
        }

        @Override
        public String getId() {
            return "lesser_than"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Less"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    public static final class GEOperationMetadata extends BooleanOperMetadata {

        public int getPrecedenceLevel() {
            return 40;
        }

        public String getName() {
            return ">="; // NOI18N
        }

        @Override
        public String getId() {
            return "greater_or_equal"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_GreaterOrEqual"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    public static final class GTOperationMetadata extends BooleanOperMetadata {

        public int getPrecedenceLevel() {
            return 40;
        }

        public String getName() {
            return ">"; // NOI18N
        }

        @Override
        public String getId() {
            return "greater_than"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Greater"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    //==========================================================================
    // Number operations
    //==========================================================================
    
    protected static abstract class NumberOperMetadata extends OperationMetadataImpl {
        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        @Override
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NUMBER);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NUMBER);
        }
    }
    
    public static final class AdditionOperationMetadata extends NumberOperMetadata {

        public int getPrecedenceLevel() {
            return 50;
        }

        public String getName() {
            return "+"; // NOI18N
        }

        @Override
        public String getId() {
            return "addition"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Addition"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }

        @Override
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.REPEATED_NUMBER_2MIN);
        }
        
    }

    public static final class SubtractionOperationMetadata extends NumberOperMetadata {

        public int getPrecedenceLevel() {
            return 50;
        }

        public String getName() {
            return "-"; // NOI18N
        }

        @Override
        public String getId() {
            return "subtraction"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Subtraction"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    public static final class MultOperationMetadata extends NumberOperMetadata {

        public int getPrecedenceLevel() {
            return 60;
        }

        public String getName() {
            return "*"; // NOI18N
        }

        @Override
        public String getId() {
            return "multiplication"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Multiplication"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    public static final class DivOperationMetadata extends NumberOperMetadata {

        public int getPrecedenceLevel() {
            return 70;
        }

        public String getName() {
            return "div"; // NOI18N
        }

        @Override
        public String getId() {
            return "division"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Division"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    public static final class ModOperationMetadata extends NumberOperMetadata {

        public int getPrecedenceLevel() {
            return 80;
        }

        public String getName() {
            return "mod"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Remainder"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }
    }

    public static final class NegativeOperationMetadata extends NumberOperMetadata {

        public int getPrecedenceLevel() {
            return 100; // Unary operation have maximum precedence
        }

        public String getName() {
            return "-"; // NOI18N
        }
        
        @Override
        public String getId() {
            return "negative"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Negative"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }

        @Override
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NUMBER);
        }
        
    }

    //==========================================================================
    // Node Set operations
    //==========================================================================
    
    public static final class UnionOperationMetadata extends OperationMetadataImpl {

        public int getPrecedenceLevel() {
            return 90;
        }

        public String getName() {
            return "|"; // NOI18N
        }

        @Override
        public String getId() {
            return "union"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(OperationMetadataImpl.class, 
                            "DN_Union"); // NOI18N
        }

//        public String getShortDescription() {
//        }
//
//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NODE_SET_TYPE;
        }
        
        @Override
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.REPEATED_NODE_SET_2MIN);
        }
    }
}
