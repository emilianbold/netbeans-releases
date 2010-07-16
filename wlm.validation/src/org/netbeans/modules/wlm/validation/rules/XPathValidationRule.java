/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.validation.rules;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.wlm.model.api.ContentElement;
import org.netbeans.modules.wlm.model.api.EmailAddress;
import org.netbeans.modules.wlm.model.api.Group;
import org.netbeans.modules.wlm.model.api.Keyword;
import org.netbeans.modules.wlm.model.api.MessageBody;
import org.netbeans.modules.wlm.model.api.MessageSubject;
import org.netbeans.modules.wlm.model.api.TCopy;
import org.netbeans.modules.wlm.model.api.TDeadlineExpr;
import org.netbeans.modules.wlm.model.api.TDurationExpr;
import org.netbeans.modules.wlm.model.api.TFrom;
import org.netbeans.modules.wlm.model.api.TPriority;
import org.netbeans.modules.wlm.model.api.TTitle;
import org.netbeans.modules.wlm.model.api.TTo;
import org.netbeans.modules.wlm.model.api.User;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.xpath.WlmExternalModelResolver;
import org.netbeans.modules.wlm.model.xpath.WlmXPathModelFactory;
import org.netbeans.modules.wlm.model.xpath.WlmXpathExtFunctionResolver;
import org.netbeans.modules.wlm.validation.WLMValidationRecursiveRule;
import org.netbeans.modules.wlm.validation.WLMValidationResultBuilder;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathProblem;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class XPathValidationRule extends WLMValidationRecursiveRule 
        implements XPathValidationContext
{
    private WLMComponent xpathContainer = null;
    private XPathModel xpathModel = null;
    
    private ExtensionFunctionResolver extensionFunctionResolver;
    private ExternalModelResolver externalModelResolver;

    public XPathValidationRule(WLMValidationResultBuilder builder) {
        super(builder);
    }

    @Override
    public void visitCopy(TCopy copy) {
        TFrom from = copy.getFrom();
        TTo to = copy.getTo();

        xpathContainer = from;
        xpathModel = null;

        SchemaComponent fromComponent = (from == null) ? null
                : getXPathExpressionType(from, this, true);

        xpathContainer = to;
        xpathModel = null;

        SchemaComponent toComponent = (to == null) ? null
                : getXPathExpressionType(to, this, true);

        xpathContainer = null;
        xpathModel = null;

        if ((fromComponent == null) || (toComponent == null)) {
            return;
        }

        xpathContainer = from;
        xpathModel = null;

        Component fromType = getTypeOfElement(fromComponent);

        xpathContainer = to;
        xpathModel = null;

        Component toType = getTypeOfElement(toComponent);

        xpathContainer = null;
        xpathModel = null;

        if ((fromType == null) || (toType == null)) {
            return;
        }

        String fromName = ((Named) fromType).getName();
        String toName = ((Named) toType).getName();

        if ((fromName == null) || (toName == null)) {
            return;
        }

        if (fromName.equals("anyType") || toName.equals("anyType")) { // NOI18N
            return;
        }

        // # 135489
        if (fromName.startsWith("nonNegative") // NOI18N
                && toName.startsWith("negative")) // NOI18N
        {
            addError(copy, "FIX_NonNegative_Negative_Copy"); // NOI18N
            return;
        }

        if (fromName.startsWith("positive") // NOI18N
                && toName.startsWith("negative")) // NOI18N
        {
            addError(copy, "FIX_Positive_Negative_Copy"); // NOI18N
            return;
        }

        if (fromName.startsWith("positive") // NOI18N
                && toName.startsWith("nonPositive")) // NOI18N
        {
            addError(copy, "FIX_Positive_NonPositive_Copy"); // NOI18N
            return;
        }

        // # 135489
        if (fromName.startsWith("negative")  // NOI18N
                && toName.startsWith("nonNegative")) // NOI18N
        {
            addError(copy, "FIX_Negative_NonNegative_Copy"); // NOI18N
            return;
        }

        if (fromName.startsWith("negative") // NOI18N
                && toName.startsWith("positive")) // NOI18N
        {
            addError(copy, "FIX_Negative_Positive_Copy"); // NOI18N
            return;
        }

        if (fromName.startsWith("nonPositive") // NOI18N
                && toName.startsWith("positive")) // NOI18N
        {
            addError(copy, "FIX_NonPositive_Positive_Copy"); // NOI18N
            return;
        }

        if (fromName.startsWith("nonPositive") // NOI18N
                && toName.startsWith("nonNegative")) // NOI18N
        {
            addWarning(copy, "FIX_NonPositive_NonNegative_Copy"); // NOI18N
            return;
        }

        if (fromName.startsWith("nonNegative") // NOI18N
                && toName.startsWith("nonPositive")) // NOI18N
        {
            addWarning(copy, "FIX_NonNegative_NonPositive_Copy"); // NOI18N
            return;
        }

        // # 135489
        if (isNumeric(fromName) && isNumeric(toName)) {
            return;
        }
    }

    @Override
    public void visitDeadLine(TDeadlineExpr deadline) {
        validateXPath(deadline);
    }

    @Override
    public void visitDuration(TDurationExpr duration) {
        validateXPath(duration);
    }

    @Override
    public void visitEmailAddress(EmailAddress emailAddress) {
        validateXPath(emailAddress);
    }

    @Override
    public void visitGroup(Group group) {
        validateXPath(group);
    }

    @Override
    public void visitMessageBody(MessageBody messageBody) {
        validateXPath(messageBody);
    }

    @Override
    public void visitMessageSubject(MessageSubject messageSubject) {
        validateXPath(messageSubject);
    }

    @Override
    public void visitPriority(TPriority priority) {
        validateXPath(priority);
    }

    @Override
    public void visitTitle(TTitle title) {
        validateXPath(title);
    }

    @Override
    public void visitUser(User user) {
        validateXPath(user);
    }

    @Override
    public void visitKeyword(Keyword keyword) {
        validateXPath(keyword);
    }

    private void validateXPath(ContentElement contentElement) {
        xpathContainer = contentElement;
        xpathModel = null;
        
        getXPathExpressionType(contentElement, this, true);

        xpathContainer = null;
        xpathModel = null;

//        getXPathExpression(contentElement);
    }

//    private XPathExpression getXPathExpression(ContentElement
//            contentElement)
//    {
//        xpathContainer = contentElement;
//        xpathModel = null;
//
//        WLMModel wlmModel = contentElement.getModel();
//        String content = contentElement.getContent();
//
//        if (content == null) {
//            content = "";
//        } else {
//            content = content.trim();
//        }
//
//        XPathExpression xpath = null;
//
//        if (content.length() > 0) {
//            XPathModel model = XPathModelHelper.getInstance().newXPathModel();
//            model.setExtensionFunctionResolver(getExtensionFunctionResolver());
//            model.setExternalModelResolver(getExternalModelResolver());
//            model.setValidationContext(this);
//
//            try {
//                xpath = model.parseExpression(content);
//            } catch (XPathException ex) {
//                // do nothing information about all errors
//                // was already processed by XPathValidationContext
//            }
//        }
//
//        xpathContainer = null;
//        xpathModel = null;
//
//        return xpath;
//    }

    private ExtensionFunctionResolver getExtensionFunctionResolver() {
        if (extensionFunctionResolver == null) {
            extensionFunctionResolver = new WlmXpathExtFunctionResolver();
        }
        return extensionFunctionResolver;
    }

    private ExternalModelResolver getExternalModelResolver() {
        if (externalModelResolver == null) {
            externalModelResolver = new WlmExternalModelResolver(getModel());
        }
        return externalModelResolver;
    }


    public void addResultItem(ResultType resultType, String str, Object... values){
        addResultItemImpl(null, resultType, str, values);
    }

    /**
     * Adds validation result item in current context.
     */
    public void addResultItem(String exprText, ResultType resultType, String str, Object... values) {
        addResultItemImpl(exprText, resultType, str, values);
    }

    public void addResultItem(XPathExpression expr, ResultType resultType,
            XPathProblem problem, Object... values) {
        //
//        System.out.println("addResultItem: ");
//        System.out.println("  expr=" + expr);
//        System.out.println("  resiltType=" + resultType);
//        System.out.println("  problem=" + problem);
//        if (values != null && values.length > 0) {
//            System.out.println("  values[0]=" + values);
//        }

        String exprText = null;
        if (expr != null) {
            exprText = expr.getExpressionString();
        }
        //
        if (problem == XPathProblem.PREFIX_REQUIRED_FOR_EXT_FUNCTION) {
            String msg = NbBundle.getMessage(getClass(), problem.toString());
            addResultItemImpl(exprText, resultType, msg, values);
        } else {
            addResultItemImpl(exprText, resultType, problem.getMsgTemplate(), values);
        }
    }

    private void addResultItemImpl(String expressionText, ResultType resultType,
            String template, Object... values)
    {
        String message;
        if (values != null && values.length > 0) {
            message = MessageFormat.format(template, values);
        } else {
            message = template;
        }
        
//        ContentElement ce = getXPathContentElement();
//        String ceTypeName = etnv.getTypeName((BpelEntity)ce);
//        str = ceTypeName + ": " + str;
        
        if (expressionText == null || expressionText.length() == 0) {
            if (xpathModel != null) {
                XPathExpression rootExpression = xpathModel.getRootExpression();

                if (rootExpression != null) {
                    expressionText = rootExpression.getExpressionString();
                }
            }
        }

        if (expressionText != null) {
            message = message + " Expression: \"" + expressionText + "\"";
        }
        
        addResultItem(xpathContainer, resultType, message);
    }

    public void setXPathModel(XPathModel model) {
        xpathModel = model;
    }

    private static SchemaComponent getXPathExpressionType(
            ContentElement element, XPathValidationContext context,
            boolean showErrors)
    {
        String content = element.getContent();

        if (content == null) {
            return null;
        }
        content = content.trim();

        if (content.length() == 0) {
            return null;
        }

        String expressionLang = null;

//        if (element instanceof ExpressionLanguageSpec) {
//            expressionLang = ((ExpressionLanguageSpec) element).getExpressionLanguage();
//        }
        return checkExpression(expressionLang, content, element, context,
                showErrors);
    }

    private static XPathModel constructModel(WLMComponent entity,
            XPathValidationContext context)
    {
//        boolean useTo = entity instanceof To;
//        XPathCastResolver castResolver = createXPathCastResolver(entity, !useTo);
//        return BpelXPathModelFactory.create(entity, entity, castResolver, context);
        return WlmXPathModelFactory.create(entity, entity, null, context);
    }

    private static SchemaComponent checkExpression(String exprLang,
            String exprText, final ContentElement element,
            final XPathValidationContext context, boolean showErrors)
    {
        boolean isXPathExpr = (exprLang == null)
                || WlmXPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang);

        if (!isXPathExpr) {
            return null;
        }

        if ( !(element instanceof WLMComponent)) {
            return null;
        }
        // TODO: It probably worth to use BpelXPathModelFactory instead
        // for the same of universality.
        XPathModel model = constructModel((WLMComponent) element, context);

        if (WlmXPathModelFactory.isSplitable(exprText)) {
            if (context != null && showErrors) {
                context.addResultItem(exprText, ResultType.ERROR, 
                        NbBundle.getMessage(XPathValidationRule.class,
                        "INCOMPLETE_XPATH")); // NOI18N
            }
            String[] partsArr = WlmXPathModelFactory.split(exprText);

            for (String anExprText : partsArr) {
                checkSingleExpr(model, anExprText);
            }
            return null;
        }
        return checkSingleExpr(model, exprText);
    }

    private static SchemaComponent checkSingleExpr(XPathModel model,
            String exprText)
    {
        try {
            XPathExpression xpath = model.parseExpression(exprText);
//out();
//out("EXP: " + xpath.getClass().getName());
//out();
            model.resolveExtReferences(true);

            if (xpath instanceof XPathNumericLiteral) {
                return getIntType();
            }
            if (xpath instanceof XPathStringLiteral) {
                return getStringType();
            }
            //
            if (xpath instanceof XPathSchemaContextHolder) {
                XPathSchemaContext sContext =
                        ((XPathSchemaContextHolder)xpath).getSchemaContext();
                if (sContext != null) {
                    SchemaComponent sComp = XPathSchemaContext.Utilities.
                            getSchemaComp(sContext);
                    return sComp;
                }
            }
            // return model.getLastSchemaComponent();
        } catch (XPathException e) {
            return null;
        }
        return null;
    }


    private static GlobalSimpleType getIntType() {
        return getType("int"); // NOI18N

    }

    private static GlobalSimpleType getStringType() {
        return getType("string"); // NOI18N

    }

    private static GlobalSimpleType getType(String name) {
        Collection<GlobalSimpleType> types = getSimpleTypes();

        for (GlobalSimpleType type : types) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    protected final Component getTypeOfElement(Component component) {
        GlobalType type = null;

        if (component instanceof TypeContainer) {
            NamedComponentReference<? extends GlobalType> ref
                    = ((TypeContainer) component).getType();

            if (ref != null) {
                type = ref.get();

                if (type != null) {
                    return type;
                }
            }
        }

        if ((component instanceof DocumentComponent)
                && (component instanceof SchemaComponent))
        {
            DocumentComponent document = (DocumentComponent) component;
            String typeName = document.getPeer().getAttribute("type"); // NOI18N
            typeName = removePrefix(typeName);
            type = findType(typeName, (SchemaComponent) component);
        }

        if (type != null) {
            return type;
        }

        return component;
    }

    private static String removePrefix(String value) {
        if (value == null) {
            return null;
        }

        int index = value.indexOf(":");

        return (index < 0) ? value : value.substring(index + 1);
    }

    private static GlobalType findType(String typeName, 
            SchemaComponent component)
    {
        if (typeName == null || typeName.equals("")) { // NOI18N
            return null;
        }

        SchemaModel model = component.getModel();
        Collection<Schema> schemas = model
                .findSchemas("http://www.w3.org/2001/XMLSchema"); // NOI18N
        GlobalType type = null;

        for (Schema schema : schemas) {
            type = findType(typeName, schema);

            if (type != null) {
                return type;
            }
        }

        Schema schema = model.getSchema();
        if (schema == null) {
            return null;
        }
        
        return findType(typeName, schema);
    }

    private static Collection<GlobalSimpleType> getSimpleTypes() {
        return SchemaModelFactory.getDefault().getPrimitiveTypesModel()
                .getSchema().getSimpleTypes();
    }

    private static boolean isNumeric(String value) {
        return NUMERIC_TYPES.contains(value);
    }

    private static final Set<String> NUMERIC_TYPES = new HashSet<String>();

    static {
        NUMERIC_TYPES.add("byte"); // NOI18N
        NUMERIC_TYPES.add("decimal"); // NOI18N
        NUMERIC_TYPES.add("double"); // NOI18N
        NUMERIC_TYPES.add("float"); // NOI18N
        NUMERIC_TYPES.add("int"); // NOI18N
        NUMERIC_TYPES.add("integer"); // NOI18N
        NUMERIC_TYPES.add("long"); // NOI18N
        NUMERIC_TYPES.add("negativeInteger"); // NOI18N
        NUMERIC_TYPES.add("nonNegativeInteger"); // NOI18N
        NUMERIC_TYPES.add("nonPositiveInteger"); // NOI18N
        NUMERIC_TYPES.add("positiveInteger"); // NOI18N
        NUMERIC_TYPES.add("short"); // NOI18N
        NUMERIC_TYPES.add("unsignedByte"); // NOI18N
        NUMERIC_TYPES.add("unsignedInt"); // NOI18N
        NUMERIC_TYPES.add("unsignedLong"); // NOI18N
        NUMERIC_TYPES.add("unsignedShort"); // NOI18N
    }
}
