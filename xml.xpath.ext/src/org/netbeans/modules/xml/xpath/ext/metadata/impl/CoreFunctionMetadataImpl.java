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
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.metadata.AbstractArgument;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.CoreFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.GeneralFunctionMetadata.FunctionType;
import org.netbeans.modules.xml.xpath.ext.metadata.ResultTypeCalculator;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.images.IconLoader;
import org.openide.util.NbBundle;

/**
 * Contains static metadata classes for all XPath Core Functions.
 * 
 * @author nk160297
 */
public abstract class CoreFunctionMetadataImpl implements CoreFunctionMetadata {

    protected ArrayList<AbstractArgument> mArguments = 
            new ArrayList<AbstractArgument>();

    protected Icon mIcon;
    
    protected CoreFunctionMetadataImpl() {
        initArguments();
    }

    public Icon getIcon() {
        if (mIcon == null) {
            loadIcon();
        }
        return mIcon;
    }
    
    public String getLongDescription() {
        return "";
    }

    public FunctionType getFunctionType() {
        return FunctionType.CORE_FUNCTION;
    }
        
    public ResultTypeCalculator getResultTypeCalculator() {
        return null;
    }

    public List<AbstractArgument> getArguments() {
        return mArguments;
    }

    public boolean isContextItemRequired(XPathOperationOrFuntion func) {
        return false;
    }
    
    protected void loadIcon() {
        mIcon = IconLoader.getIcon(getName()); // NOI18N
    }
    
    protected abstract void initArguments();
    
    //==========================================================================
    // Node Set Functions
    //==========================================================================

    public static final class LastFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "last"; // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Last"); // NOI18N
        }

        public String getShortDescription() {
            return "number last()";
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return true;
        }
        
        protected void initArguments() {
            // there is not an argument
        }

    }
    
    public static final class PositionFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "position"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Position"); // NOI18N
        }

        public String getShortDescription() {
            return "number position()"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return true;
        }
        
        protected void initArguments() {
            // there is not an argument
        }

    }
    
    public static final class CountFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "count"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Count"); // NOI18N
        }

        public String getShortDescription() {
            return "number count(node-set)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NODE_SET);
        }

    }
    
    public static final class IdFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "id"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Id"); // NOI18N
        }

        public String getShortDescription() {
            return "node-set id(object)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NODE_SET_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.ANY_TYPE);
        }

    }
    
    public static final class LocalNameFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "local-name"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_LocalName"); // NOI18N
        }

        public String getShortDescription() {
            return "string local-name(node-set?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return func.getChildCount() == 0;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_NODE);
        }

    }
    
    public static final class NamespaceUriFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "namespace-uri"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_NamespaceURI"); // NOI18N
        }

        public String getShortDescription() {
            return "string namespace-uri(node-set?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return func.getChildCount() == 0;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_NODE);
        }

    }
    
    public static final class NameFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "name"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Name"); // NOI18N
        }

        public String getShortDescription() {
            return "string name(node-set?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return func.getChildCount() == 0;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_NODE);
        }

    }
    
    //==========================================================================
    // String Core Functions
    //==========================================================================
    
    public static final class StringFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "string"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_String"); // NOI18N
        }

        public String getShortDescription() {
            return "string name(node-set?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

         public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return func.getChildCount() == 0;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_ANY_TYPE);
        }

   }

    public static final class ConcatFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "concat"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Concatenation"); // NOI18N
        }

        public String getShortDescription() {
            return "string concat(string, string, string*)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.REPEATED_STRING_2MIN);
        }

    }

    public static final class StartsWithFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "starts-with"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_StartsWith"); // NOI18N
        }

        public String getShortDescription() {
            return "boolean starts-with(string, string)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }

    }

    public static final class ContainsFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "contains"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Contains"); // NOI18N
        }

        public String getShortDescription() {
            return "boolean contains(string, string)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
        
         protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }

   }

    public static final class SubstringBeforeFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "substring-before"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_SubstringBefore"); // NOI18N
        }

        public String getShortDescription() {
            return "string substring-before(string, string)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }

    }

    public static final class SubstringAfterFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "substring-after"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_SubstringAfter"); // NOI18N
        }

        public String getShortDescription() {
            return "string substring-after(string, string)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }

    }

    public static final class SubstringFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "substring"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Substring"); // NOI18N
        }

        public String getShortDescription() {
            return "string substring(string, number, number?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NUMBER);
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_NUMBER);
        }

    }

    public static final class StringLengthFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "string-length"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_StringLength"); // NOI18N
        }

        public String getShortDescription() {
            return "number string-length(string?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return func.getChildCount() == 0;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_STRING);
        }
    }

    public static final class NormalizeSpaceFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "normalize-space"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_NormalizeSpace"); // NOI18N
        }

        public String getShortDescription() {
            return "string normalize-space(string?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return func.getChildCount() == 0;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_STRING);
        }
    }

    public static final class TranslateFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "translate"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Translate"); // NOI18N
        }

        public String getShortDescription() {
            return "string translate(string, string, string)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }
    }

    //==========================================================================
    // Boolean Core Functions
    //==========================================================================
    
    public static final class BooleanFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "boolean"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Boolean"); // NOI18N
        }

        public String getShortDescription() {
            return "boolean boolean(object)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.ANY_TYPE);
        }
    }

    public static final class NotFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "not"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_LogicalNot"); // NOI18N
        }

        public String getShortDescription() {
            return "boolean not(boolean)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_BOOLEAN);
        }
    }

    public static final class TrueFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "true"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_LogicalTrue"); // NOI18N
        }

        public String getShortDescription() {
            return "boolean true()"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
        
        protected void initArguments() {
        }
    }

    public static final class FalseFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "false"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_LogicalFalse"); // NOI18N
        }

        public String getShortDescription() {
            return "boolean false()"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
        
        protected void initArguments() {
        }
    }

    public static final class LangFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "lang"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Language"); // NOI18N
        }

        public String getShortDescription() {
            return "boolean lang(string)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return true;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }
    }

    //==========================================================================
    // Number Core Functions
    //==========================================================================
    
    public static final class NumberFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "number"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Number"); // NOI18N
        }

        public String getShortDescription() {
            return "number number(object?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        @Override
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return func.getChildCount() == 0;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_ANY_TYPE);
        }
    }

    public static final class SumFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "sum"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Sum"); // NOI18N
        }

        public String getShortDescription() {
            return "number sum(node-set)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NODE_SET);
        }
    }

    public static final class FloorFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "floor"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Floor"); // NOI18N
        }

        public String getShortDescription() {
            return "number floor(number)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NUMBER);
        }
    }

    public static final class CeilingFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "ceiling"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Ceiling"); // NOI18N
        }

        public String getShortDescription() {
            return "number ceiling(number)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NUMBER);
        }
    }

    public static final class RoundFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "round"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Round"); // NOI18N
        }

        public String getShortDescription() {
            return "number round(number)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NUMBER_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NUMBER);
        }
    }
    
    //==========================================================================
    // XSLT Ext Functions. This function are defined in the JXPath as core functions.
    //==========================================================================
    
    public static final class NullFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "null"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Null"); // NOI18N
        }

        public String getShortDescription() {
            return "??? null ???"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }
        
        protected void initArguments() {
        }
    }
    
    public static final class KeyFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_Key"); // NOI18N
        }

        public String getDisplayName() {
            return "key";
        }

        public String getShortDescription() {
            return "??? key ???"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.NODE_SET_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.ANY_TYPE);
        }
    }
    
    public static final class FormatNumberFuncMetadata extends CoreFunctionMetadataImpl {

        public String getName() {
            return "format-number"; // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CoreFunctionMetadataImpl.class, 
                            "DN_FormatNumber"); // NOI18N
        }

        public String getShortDescription() {
            return "string format-number(number, string?)"; // NOI18N
        }

//        public String getLongDescription() {
//        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }
        
        protected void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NUMBER);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_STRING);
        }
    }
    
}
