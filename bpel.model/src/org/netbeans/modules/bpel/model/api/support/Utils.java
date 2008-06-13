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
package org.netbeans.modules.bpel.model.api.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.impl.BpelContainerImpl;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.ActivityBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.EmptyBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.ThrowBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.SequenceBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.CompensateBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.InvokeBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.ReceiveBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.ReplyBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.AssignBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.WaitBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.PickBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.IfBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.ExitBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.FlowBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.WhileBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.ScopeBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.ForEachBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.RethrowBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.CompensateScopeBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.RepeatUntilBuilder;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl.ValidateBuilder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.BpelReferenceable;
import org.netbeans.modules.bpel.model.api.references.SchemaReferenceBuilder;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.impl.references.BpelAttributesType;
import org.netbeans.modules.bpel.model.impl.references.BpelReferenceBuilder;
import org.netbeans.modules.bpel.model.impl.references.WSDLReferenceBuilder;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypes;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.bpel.model.api.ExpressionLanguageSpec;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComps;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;

public final class Utils {

    public static final char SEMICOLON = ';';                                       // NOI18N

    public static final char AMP = '&';                                             // NOI18N

    public static final String QUOT = AMP + "quot" + SEMICOLON;                     // NOI18N

    public static final String APOS = AMP + "apos" + SEMICOLON;                      // NOI18N

    public static final String GT = AMP + "gt" + SEMICOLON;                         // NOI18N

    public static final String BAD_ATTRIBUTE_VALUE = "BAD_ATTRIBUTE_VALUE";         // NOI18N

    public static final String BAD_ATTRIBUTE_URI_VALUE = "BAD_ATTRIBUTE_URI_VALUE"; // NOI18N

    public static final String BAD_VARIABLE_NAME = "BAD_VARIABLE_NAME";               // NOI18N

    public static final String BAD_CORRELATION_SET_NAME = "BAD_CORRELATION_SET_NAME"; // NOI18N

    public static final String BAD_PARTNER_LINK_NAME = "BAD_PARTNER_LINK_NAME";     // NIO18N

    public static final String BAD_MESSAGE_EXCHANGE_NAME = "BAD_MESSAGE_EXCHANGE_NAME"; // NOI18N

    public static final String BAD_LINK_NAME = "BAD_LINK_NAME";                     // NOI18N

    public static final String BAD_URI_VALUE = "BAD_URI_VALUE";                      // NOI18N

    public static final String BAD_NCNAME_VALUE = "BAD_NCNAME_VALUE";               // NOI18N

    public static final String BAD_VARIABLE_FOR_FOR_EACH = "BAD_VARIABLE_FOR_FOR_EACH"; // NOI18N

    public static final String BAD_VARIABLE_FOR_ON_EVENT = "BAD_VARIABLE_FOR_ON_EVENT"; // NOI18N

    public static final String BAD_VARIABLE_FOR_SCOPE_IN_ON_EVENT = "BAD_VARIABLE_FOR_SCOPE_IN_ON_EVENT"; // NOI18N

    public static final String SOURCES_TYPE_BPELPRO = "BIZPRO"; // NOI18N

    private static final String BUNDLE = "org/netbeans/modules/bpel/model/impl/Bundle"; // NOI18N

    private static final String XML_COMMENT_START = "<!--";                             // NOI18N

    private static final String XML_COMMENT_END = "-->";                                // NOI18N

    public static final DefaultParentAccess DEFAULT_PARENT_ACCESS = new DefaultParentAccess();

    private Utils() {
    }

    public static SchemaComponent checkXPathExpression(ContentElement element) {
        return checkXPathExpression(element, null);
    }

    public static SchemaComponent checkXPathExpression(
            ContentElement element, XPathValidationContext context) {
        String content = element.getContent();

        if (content == null) {
            return null;
        }
        content = content.trim();

        if (content.length() == 0) {
            return null;
        }
        String expressionLang = null;

        if (element instanceof ExpressionLanguageSpec) {
            expressionLang = ((ExpressionLanguageSpec) element).getExpressionLanguage();
        }
        return checkExpression(expressionLang, content, element, context);
    }

    private static SchemaComponent checkExpression(
            String exprLang, String exprText, final ContentElement element, 
            final XPathValidationContext context) {
        boolean isXPathExpr = exprLang == null || 
                BpelXPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang);

        if (!isXPathExpr) {
            return null;
        }
        XPathModelHelper helper = XPathModelHelper.getInstance();
        XPathModel model = helper.newXPathModel();

        if (context != null) {
            context.setXPathModel(model);
            model.setValidationContext(context);
        }
        ExNamespaceContext nsContext = ((BpelEntity) element).getNamespaceContext();
        model.setNamespaceContext(new BpelXPathNamespaceContext(nsContext));

        model.setVariableResolver(new BpelVariableResolver(context, (BpelEntity) element));
        model.setExtensionFunctionResolver(new BpelXpathExtFunctionResolver());

        model.setExternalModelResolver(new ExternalModelResolver() {

            public Collection<SchemaModel> getModels(String modelNsUri) {
                BpelModel bpelModel = ((BpelEntity) element).getBpelModel();
                return SchemaReferenceBuilder.getSchemaModels(bpelModel, modelNsUri, true);
            }

            public Collection<SchemaModel> getVisibleModels() {
                if (context != null) {
                    context.addResultItem(Validator.ResultType.ERROR, NbBundle.getMessage(Utils.class, "ABSOLUTE_PATH_DISALLOWED")); // NOI18N

                }
                return null;
            }

            public boolean isSchemaVisible(String soughtNamspace) {
                assert soughtNamspace != null;
                //
                BpelModel model = ((BpelEntity) element).getBpelModel();
                if (model.getState() == State.VALID) {
                    for (Import anImport : model.getProcess().getImports()) {
                        if (Import.SCHEMA_IMPORT_TYPE.equals(anImport.getImportType())) {
                            if (soughtNamspace.equals(anImport.getNamespace())) {
                                return true;
                            }
                        }
                    }
                }
                //
                return false;
            }
        });
        model.setXPathCastResolver(createXPathCastResolver(element));

        if (BpelXPathModelFactory.isSplitable(exprText)) {
            if (context != null) {
                context.addResultItem(exprText, Validator.ResultType.ERROR, 
                        NbBundle.getMessage(Utils.class, "INCOMPLETE_XPATH")); // NOI18N

            }
            String[] partsArr = BpelXPathModelFactory.split(exprText);

            for (String anExprText : partsArr) {
                checkSingleExpr(model, anExprText);
            }
            return null;
        }
        return checkSingleExpr(model, exprText);
    }

    private static SchemaComponent checkSingleExpr(XPathModel model, String exprText) {
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
            return model.getLastSchemaComponent();
        } catch (XPathException e) {
            return null;
        }
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

    private static Collection<GlobalSimpleType> getSimpleTypes() {
        return SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema().getSimpleTypes();
    }

    private static void out() {
        System.out.println();
    }

    private static void out(Object object) {
        System.out.println("*** " + object); // NOI18N

    }

    private static XPathCastResolver createXPathCastResolver(ContentElement element) {
//out();
//out("CREATE CAST RESOLVER");
        if (!(element instanceof BpelEntity)) {
//out("     1");
            return null;
        }
        BpelEntity entity = (BpelEntity) element;
        BpelEntity parent = entity.getParent();
//out("     2");

        if (!(parent instanceof Copy)) {
            return null;
        }
//out("     3");
        List<Editor> editors = parent.getChildren(Editor.class);

        if (editors == null) {
            return null;
        }
//out("     4");
        List<Cast> allCasts = new ArrayList<Cast>();
        List<PseudoComp> allPseudoComps = new ArrayList<PseudoComp>();

        boolean isFrom = element instanceof From;
        boolean isTo = element instanceof To;

//out("     5");
        for (Editor editor : editors) {
            Casts editorCasts = editor.getCasts();
            //
            if (editorCasts != null) {
                Cast[] castArr = editorCasts.getCasts();
                if (castArr != null) {
                    for (Cast cast : castArr) {
                        if (cast != null) {
                            Source source = cast.getSource();
                            if (isFrom && source == Source.FROM) {
                                allCasts.add(cast);
                            } else if (isTo && source == Source.TO) {
                                allCasts.add(cast);
                            }
                        }
                    }
                }
            }
            //
            PseudoComps pseudoComps = editor.getPseudoComps();
            if (pseudoComps != null) {
                PseudoComp[] pseudoCompArr = pseudoComps.getPseudoComps();
                if (pseudoCompArr != null) {
                    for (PseudoComp pseudoComp : pseudoCompArr) {
                        if (pseudoComp != null) {
                            Source source = pseudoComp.getSource();
                            if (isFrom && source == Source.FROM) {
                                allPseudoComps.add(pseudoComp);
                            } else if (isTo && source == Source.TO) {
                                allPseudoComps.add(pseudoComp);
                            }
                        }
                    }
                }
            }
        }
//out("     6");
        if (allCasts.isEmpty() && allPseudoComps.isEmpty()) {
            return null;
        }
//out("     7");
        return new XPathCastResolverImpl(allCasts, allPseudoComps);
    }

    public static QName getQName(String value, BpelEntity entity) {
        if (value == null) {
            return null;
        }
        String[] splited = new String[2];
        splitQName(value, splited);
        String uri = entity.getNamespaceContext().getNamespaceURI(splited[0]);

        if (uri == null) {
            return null;
        }
        return new QName(uri, splited[1]);
    }

    @SuppressWarnings("unchecked")
    public static Collection<Class<? extends BpelEntity>> of(BpelTypes[] types) {
        if (types.length == 0) {
            return Collections.EMPTY_LIST;
        }
        List<Class<? extends BpelEntity>> list =
                new ArrayList<Class<? extends BpelEntity>>(types.length);
        for (BpelTypes type : types) {
            Class<? extends BpelEntity> clazz = type.getComponentType();
            list.add(clazz);
        }
        return list;
    }

    public static Object parse(Class clazz, String value) {
        if (clazz.equals(Roles.class)) {
            return Roles.forString(value);
        } else if (clazz.equals(Pattern.class)) {
            return Pattern.forString(value);
        } else if (clazz.equals(TBoolean.class)) {
            return TBoolean.forString(value);
        } else if (clazz.equals(Initiate.class)) {
            return Initiate.forString(value);
        }
        assert false;
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> parseList(BpelEntity entity, Class<T> clazz, String value) {
        if (clazz.equals(QName.class)) {
            List<QName> list = new LinkedList<QName>();
            if (value == null) {
                return (List<T>) list;
            }
            StringTokenizer tokenizer = new StringTokenizer(value, " "); // NOI18N

            while (tokenizer.hasMoreTokens()) {
                String next = tokenizer.nextToken();
                QName qName = getQName(next, entity);
                if (qName != null) {
                    list.add(qName);
                }
            }
            return (List<T>) list;
        }
        assert false;
        return null;
    }

    public static BpelEntity createActivityGroup(BpelModelImpl model, Element element) {
        ActivityBuilder builder = getActivityBuilder(element.getLocalName());
        if (builder != null) {
            return builder.build(model, element);
        }
        return null;
    }

    public static String getResourceString(String key, Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE);
        String str = bundle.getString(key);
        return MessageFormat.format(str, args);
    }

    public static boolean checkNCName(String str) {
        return org.netbeans.modules.xml.xam.dom.Utils.isValidNCName(str);
    }

    public static void splitQName(String qName, String[] result) {
        assert qName != null;
        assert result != null;
        String[] parts = qName.split(":"); //NOI18N

        String prefix;
        String localName;
        if (parts.length == 2) {
            prefix = parts[0];
            localName = parts[1];
        } else {
            prefix = null;
            localName = parts[0];
        }
        if (result.length > 0) {
            result[0] = prefix;
        }
        if (result.length > 1) {
            result[1] = localName;
        }
    }

    public static boolean isEquals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }

    public static boolean validate(QName qName) {
        assert qName != null;
        String ns = qName.getNamespaceURI();
        String localName = qName.getLocalPart();

        try {
            if (ns != null) {
                new URI(ns);
            }
            if (localName == null) {
                return false;
            }
            return checkNCName(localName);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static boolean canUpdatePrefix(Attribute attr) {
        Class clazz;
        if (attr.getMemberType() != null) {
            clazz = attr.getMemberType();
        } else {
            clazz = attr.getType();
        }
        if (BpelReferenceable.class.isAssignableFrom(clazz)) {
            BpelAttributesType.AttrType type =
                    BpelReferenceBuilder.getInstance().getAttributeType(attr);
            return type == BpelAttributesType.AttrType.QNAME;
        } else if (ReferenceableSchemaComponent.class.isAssignableFrom(clazz)) {
            BpelAttributesType.AttrType type =
                    SchemaReferenceBuilder.getInstance().getAttributeType(attr);
            return type == BpelAttributesType.AttrType.QNAME;
        } else if (ReferenceableWSDLComponent.class.isAssignableFrom(clazz)) {
            BpelAttributesType.AttrType type =
                    WSDLReferenceBuilder.getInstance().getAttributeType(attr);
            return type == BpelAttributesType.AttrType.QNAME;
        }
        return false;
    }

    public static BpelEntity getUnattachedRoot(BpelEntity entity) {
        BpelEntity parent = entity;
        BpelEntity nonNull = entity;
        while (parent != null) {
            nonNull = parent;
            parent = parent.getParent();
        }
        return nonNull;
    }

    public static boolean checkPasteCompensate(final BpelContainerImpl container,
            final Component component) {
        Collection<ExtendableActivity> collection =
                new LinkedList<ExtendableActivity>();
        collectCompensates(component, collection);

        ParentAccess access = new ParentAccess() {

            public BpelContainer getParent(BpelEntity entity) {
                if (entity == component) {
                    /* 
                     * if entity is component that we tries to insert into container
                     * then its parent is contianer
                     */
                    return container;
                } else {
                    return entity.getParent();
                }
            }
        };

        for (ExtendableActivity activity : collection) {
            if (hasAscendant(activity, FaultHandlers.class, access) ||
                    hasAscendant(activity, CompensationHandler.class, access) || hasAscendant(activity, TerminationHandler.class, access)) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

    public static boolean hasAscendant(BpelEntity entity,
            Class<? extends BpelContainer> clazz) {
        return hasAscendant(entity, clazz, DEFAULT_PARENT_ACCESS);
    }

    public static boolean hasAscendant(BpelEntity entity,
            Class<? extends BpelContainer> clazz, ParentAccess parentAccess) {
        assert entity != null;
        BpelEntity cur = entity;
        while (!cur.getElementType().equals(clazz)) {
            cur = parentAccess.getParent(cur);
            if (cur == null) {
                break;
            }
        }

        if (cur != null && cur.getElementType().equals(clazz)) {
            return true;
        } else {
            return false;
        }
    }

    public static NamedComponentReference<PortType> getPortTypeRef(
            BpelReference<PartnerLink> reference, Component component) {
        if (reference == null) {
            return null;
        }
        PartnerLink partnerLink = reference.get();
        if (partnerLink == null) {
            return null;
        }
        WSDLReference<Role> roleRef = null;
        if (component instanceof Invoke) {
            roleRef = partnerLink.getPartnerRole();
        } else {
            roleRef = partnerLink.getMyRole();
        }
        if (roleRef == null) {
            return null;
        }
        Role role = roleRef.get();

        if (role == null) {
            return null;
        }
        return role.getPortType();
    }

    public static boolean equals(QName name1, QName name2) {
        if (name1 == null) {
            return name2 == null;
        }
        if (name2 == null) {
            return false;
        }
        return isEquals(name1.getLocalPart(), name2.getLocalPart()) &&
                isEquals(name1.getNamespaceURI(), name2.getNamespaceURI());
    }

    public static String removeXmlComments(String str) {
        StringBuilder builder = new StringBuilder();
        int index = str.indexOf(XML_COMMENT_START);
        int endComment = 0;
        while (index != -1) {
            builder.append(str.substring(endComment, index));
            endComment = str.indexOf(XML_COMMENT_END, index) +
                    XML_COMMENT_END.length();
            index = str.indexOf(XML_COMMENT_START, endComment);
        }
        builder.append(str.substring(endComment));
        return builder.toString();
    }

    /*
     * This method assume on input string that can contain :
     * "&gt;", "&apos;", "&quot;". Method replace those strings
     * to ">", "'" , "\"" respectively.
     * Please note that there can be also 
     * "&lt;" and "&amp;" in original string, but 
     * this method doesn't assume presence of those symbols in string.
     * This is because <code>str</code> in argument comes from XAM/XDM
     * and it already have changed those symbols to appropriate values.  
     */
    public static String hackXmlEntities(String str) {
        if (str == null) {
            return null;
        }
        int index = str.indexOf(AMP);
        if (index >= 0) {
            StringBuilder builder = new StringBuilder(str);
            for (Entry<String, Character> entry : ActivityCreatorHolder.XML_ENTITIES.entrySet()) {
                String entity = entry.getKey();
                Character value = entry.getValue();
                for (index = builder.indexOf(entity); index >= 0;
                        index = builder.indexOf(entity)) {
                    builder.replace(index, index + entity.length(),
                            Character.toString(value));
                }
            }
            return builder.toString();
        } else {
            return str;
        }
    }

    private static void collectCompensates(Component component,
            Collection<ExtendableActivity> collection) {
        if (component instanceof Compensate ||
                component instanceof CompensateScope) {
            collection.add((ExtendableActivity) component);
        }
        List list = component.getChildren();
        for (Object obj : list) {
            collectCompensates((Component) obj, collection);
        }
    }

    public static Project safeGetProject(Model model) {
        FileObject fo = SoaUtil.getFileObjectByModel(model);
        if (fo != null && fo.isValid()) {
            return FileOwnerQuery.getOwner(fo);
        } else {
            return null;
        }
    }

    public static SchemaModel getSchemaModel(FileObject fo) {
        SchemaModel sModel = null;
        //
        try {
            ModelSource modelSource = Utilities.getModelSource(fo, true);
            sModel = SchemaModelFactory.getDefault().getModel(modelSource);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        //
        return sModel;
    }

    public static WSDLModel getWsdlModel(FileObject fo) {
        WSDLModel wsdlModel = null;
        //
        try {
            ModelSource modelSource = Utilities.getModelSource(fo, true);
            wsdlModel = WSDLModelFactory.getDefault().getModel(modelSource);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        //
        return wsdlModel;
    }

    private static ActivityBuilder getActivityBuilder(String tagName) {
        return ActivityCreatorHolder.ACTIVITY_BUILDERS.get(tagName);
    }

    public static interface ParentAccess {

        BpelContainer getParent(BpelEntity comp);
    }

    public static class DefaultParentAccess implements ParentAccess {

        public BpelContainer getParent(BpelEntity comp) {
            return comp.getParent();
        }
    }

    public static class Pair<T> {

        public Pair(T one, T two) {
            first = one;
            second = two;
        }

        public T getFirst() {
            return first;
        }

        public T getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Pair)) {
                return false;
            }
            boolean isEqual = Utils.equals(first, ((Pair) obj).first) &&
                    Utils.equals(second, ((Pair) obj).second);
            return isEqual;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            if (first != null) {
                hash += first.hashCode();
            }
            if (second != null) {
                hash += 37 * second.hashCode();
            }
            return hash;
        }

        @Override
        public String toString() {
            return "[first: " + (first == null ? null : first.toString()) + // NOI18N
                    ", second:" + // NOI18N
                    (second == null ? null : second.toString()) + "]";
        }
        private T first;
        private T second;
    }
}

class ActivityCreatorHolder {

    private static final Map<String, Character> PRIVATE_ENTITIES =
            new HashMap<String, Character>();
    private static final Map<String, ActivityBuilder> PRIVATE_BUILDERS =
            new HashMap<String, ActivityBuilder>();
    static final Map<String, ActivityBuilder> ACTIVITY_BUILDERS =
            Collections.unmodifiableMap(PRIVATE_BUILDERS);
    static final Map<String, Character> XML_ENTITIES = Collections.unmodifiableMap(
            PRIVATE_ENTITIES);
    

    static {
        PRIVATE_BUILDERS.put(BpelElements.EMPTY.getName(), new EmptyBuilder());
        PRIVATE_BUILDERS.put(BpelElements.INVOKE.getName(), new InvokeBuilder());
        PRIVATE_BUILDERS.put(BpelElements.RECEIVE.getName(), new ReceiveBuilder());
        PRIVATE_BUILDERS.put(BpelElements.REPLY.getName(), new ReplyBuilder());
        PRIVATE_BUILDERS.put(BpelElements.ASSIGN.getName(), new AssignBuilder());
        PRIVATE_BUILDERS.put(BpelElements.WAIT.getName(), new WaitBuilder());
        PRIVATE_BUILDERS.put(BpelElements.THROW.getName(), new ThrowBuilder());
        PRIVATE_BUILDERS.put(BpelElements.EXIT.getName(), new ExitBuilder());
        PRIVATE_BUILDERS.put(BpelElements.FLOW.getName(), new FlowBuilder());
        PRIVATE_BUILDERS.put(BpelElements.WHILE.getName(), new WhileBuilder());
        PRIVATE_BUILDERS.put(BpelElements.SEQUENCE.getName(),
                new SequenceBuilder());
        PRIVATE_BUILDERS.put(BpelElements.SCOPE.getName(), new ScopeBuilder());
        PRIVATE_BUILDERS.put(BpelElements.PICK.getName(), new PickBuilder());
        PRIVATE_BUILDERS.put(BpelElements.COMPENSATE.getName(),
                new CompensateBuilder());
        PRIVATE_BUILDERS.put(BpelElements.FOR_EACH.getName(), new ForEachBuilder());
        PRIVATE_BUILDERS.put(BpelElements.IF.getName(), new IfBuilder());
        PRIVATE_BUILDERS.put(BpelElements.REPEAT_UNTIL.getName(),
                new RepeatUntilBuilder());
        PRIVATE_BUILDERS.put(BpelElements.RETHROW.getName(), new RethrowBuilder());
        PRIVATE_BUILDERS.put(BpelElements.VALIDATE.getName(),
                new ValidateBuilder());
        PRIVATE_BUILDERS.put(BpelElements.COMPENSATE_SCOPE.getName(),
                new CompensateScopeBuilder());

        PRIVATE_ENTITIES.put(Utils.GT, '>');
        PRIVATE_ENTITIES.put(Utils.APOS, '\'');
        PRIVATE_ENTITIES.put(Utils.QUOT, '"');
    }
}
