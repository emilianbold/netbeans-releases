/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.model.api.*;
import org.openide.util.Parameters;

/**
 *
 * @author marekfukala
 */
public final class ElementFactoryImpl implements ElementFactory {

    private Model model;
    
    public ElementFactoryImpl(Model model) {
        this.model = model;
    }
    
    public Element createElement(Model model, Node node) { //NOI18N for whole method
        NodeType type = node.type();
        String className = Utils.getInterfaceForNodeType(type.name());
        
        //TODO generate this ugly switch!!! 
        
        if (className.equals("AtRuleId")) {
            return new AtRuleIdI(model, node);
        } else if (className.equals("AtRule")) {
            return new AtRuleI(model, node);
        } else if (className.equals("GenericAtRule")) {
            return new GenericAtRuleI(model, node);
        } else if (className.equals("MozDocument")) {
            return new MozDocumentI(model, node);
        } else if (className.equals("MozDocumentFunction")) {
            return new MozDocumentFunctionI(model, node);
        } else if (className.equals("VendorAtRule")) {
            return new VendorAtRuleI(model, node);
        } else if (className.equals("WebkitKeyframes")) {
            return new WebkitKeyframesI(model, node);
        } else if (className.equals("WebkitKeyframeSelectors")) {
            return new WebkitKeyframeSelectorsI(model, node);
        } else if (className.equals("WebkitKeyframesBlock")) {
            return new WebkitKeyframesBlockI(model, node);
        } else if (className.equals("StyleSheet")) {
            return new StyleSheetI(model, node);
        } else if (className.equals("CharSet")) {
            return new CharSetI(model, node);
        } else if (className.equals("CharSetValue")) {
            return new CharSetValueI(model, node);
        } else if (className.equals("FontFace")) {
            return new FontFaceI(model, node);
        } else if (className.equals("Imports")) {
            return new ImportsI(model, node);
        } else if (className.equals("ImportItem")) {
            return new ImportItemI(model, node);
        } else if (className.equals("ResourceIdentifier")) {
            return new ResourceIdentifierI(model, node);
        } else if (className.equals("Media")) {
            return new MediaI(model, node);
        } else if (className.equals("MediaBody")) {
            return new MediaBodyI(model, node);
        } else if (className.equals("MediaBodyItem")) {
            return new MediaBodyItemI(model, node);
        } else if (className.equals("MediaQueryList")) {
            return new MediaQueryListI(model, node);
        } else if (className.equals("MediaQuery")) {
            return new MediaQueryI(model, node);
        } else if (className.equals("MediaQueryOperator")) {
            return new MediaQueryOperatorI(model, node);
        } else if (className.equals("MediaExpression")) {
            return new MediaExpressionI(model, node);
        } else if (className.equals("MediaFeature")) {
            return new MediaFeatureI(model, node);
        } else if (className.equals("MediaFeatureValue")) {
            return new MediaFeatureValueI(model, node);
        } else if (className.equals("MediaType")) {
            return new MediaTypeI(model, node);
        } else if (className.equals("Namespaces")) {
            return new NamespacesI(model, node);
        } else if (className.equals("Namespace")) {
            return new NamespaceI(model, node);
        } else if (className.equals("NamespacePrefixName")) {
            return new NamespacePrefixNameI(model, node);
        } else if (className.equals("Body")) {
            return new BodyI(model, node);
        } else if (className.equals("BodyItem")) {
            return new BodyItemI(model, node);
        } else if (className.equals("Rule")) {
            return new RuleI(model, node);
        } else if (className.equals("SelectorsGroup")) {
            return new SelectorsGroupI(model, node);
        } else if (className.equals("Selector")) {
            return new SelectorI(model, node);
        } else if (className.equals("Declarations")) {
            return new DeclarationsI(model, node);
        } else if (className.equals("PropertyDeclaration")) {
            return new PropertyDeclarationI(model, node);
        } else if (className.equals("Declaration")) {
            return new DeclarationI(model, node);
        } else if (className.equals("Property")) {
            return new PropertyI(model, node);
        } else if (className.equals("PropertyValue")) {
            return new PropertyValueI(model, node);
        } else if (className.equals("Expression")) {
            return new ExpressionI(model, node);
        } else if (className.equals("Prio")) {
            return new PrioI(model, node);
        } else if (className.equals("PlainElement")) {
            return new PlainElementI(model, node);
        } else if (className.equals("Page")) {
            return new PageI(model, node);
        } else if(className.equals("Ws")) {
            return new WsI(model, node);
        } else if(className.equals("Token")) {
            return new PlainElementI(model, node);
        } else {
            //fallback for unknown types???
            Logger.getLogger(ElementFactoryImpl.class.getName()).log( Level.WARNING, "created element by reflection for {0}, update the ElementFactoryImpl.createElement() methods ugly switch!", className);
            return createElementByReflection(model, node);
        }
    }

    private Element createElementByReflection(Model model, Node node) {
        Parameters.notNull("model", model);
        Parameters.notNull("node", node);
        try {
            Class<?> clazz = Class.forName(Utils.getImplementingClassNameForNodeType(node.type()));
            Constructor<?> constructor = clazz.getConstructor(Model.class, Node.class);
            return (Element) constructor.newInstance(model, node);
        } catch (ClassNotFoundException cnfe ) {
            //no implementation found - use default
            return new PlainElementI(model, node);
        } catch (/* NoSuchMethodException, SecurityException,
                 InstantiationException, IllegalAccessException, IllegalArgumentException, 
                 InvocationTargetException */ Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public StyleSheet createStyleSheet() {
        return new StyleSheetI(model);
    }
 
    @Override
    public CharSet createCharSet() {
        return new CharSetI(model);
    }

    @Override
    public CharSetValue createCharSetValue() {
        return new CharSetValueI(model);
    }

    @Override
    public FontFace createFontFace() {
        return new FontFaceI(model);
    }

    @Override
    public FontFace createFontFace(Declarations declarations) {
        FontFace fontFace = createFontFace();
        fontFace.setDeclarations(declarations);
        return fontFace;
    }

    @Override
    public Imports createImports() {
        return new ImportsI(model);
    }

    @Override
    public ImportItem createImportItem() {
        return new ImportItemI(model);
    }

    @Override
    public ResourceIdentifier createResourceIdentifier() {
        return new ResourceIdentifierI(model);
    }

    @Override
    public MediaQueryList createMediaQueryList() {
        return new MediaQueryListI(model);
    }

    @Override
    public MediaQuery createMediaQuery() {
        return new MediaQueryI(model);
    }

    @Override
    public Namespaces createNamespaces() {
        return new NamespacesI(model);
    }

    @Override
    public Namespace createNamespace() {
        return new NamespaceI(model);
    }

    @Override
    public NamespacePrefixName createNamespacePrefixName() {
        return new NamespacePrefixNameI(model);
    }

    @Override
    public Body createBody() {
        return new BodyI(model);
    }

    @Override
    public BodyItem createBodyItem() {
        return new BodyItemI(model);
    }

    @Override
    public Rule createRule() {
        return new RuleI(model);
    }

    @Override
    public Rule createRule(SelectorsGroup selectorsGroup, Declarations declarations) {
        Rule rule = createRule();
        rule.setSelectorsGroup(selectorsGroup);
        rule.setDeclarations(declarations);
        return rule;
    }

    @Override
    public SelectorsGroup createSelectorsGroup() {
        return new SelectorsGroupI(model);
    }

    @Override
    public SelectorsGroup createSelectorsGroup(Selector... selectors) {
        SelectorsGroup sg = createSelectorsGroup();
        for (Selector s : selectors) {
            sg.addSelector(s);
        }
        return sg;
    }

    @Override
    public Selector createSelector() {
        return new SelectorI(model);
    }

    @Override
    public Selector createSelector(CharSequence code) {
        return new SelectorI(model, code);
    }

    @Override
    public Declarations createDeclarations() {
        return new DeclarationsI(model);
    }

    @Override
    public Declarations createDeclarations(PropertyDeclaration... declarations) {
        Declarations ds = createDeclarations();
        for (PropertyDeclaration pd : declarations) {
            Declaration declaration = createDeclaration();
            declaration.setPropertyDeclaration(pd);
            ds.addDeclaration(declaration);
        }
        return ds;
    }

     @Override
    public Declaration createDeclaration() {
        return new DeclarationI(model);
    }
    
    @Override
    public PropertyDeclaration createPropertyDeclaration() {
        return new PropertyDeclarationI(model);
    }

    @Override
    public PropertyDeclaration createPropertyDeclaration(Property property, PropertyValue propertyValue, boolean isImportant) {
        PropertyDeclaration d = createPropertyDeclaration();
        d.setProperty(property);
        d.setPropertyValue(propertyValue);

        Prio prio = createPrio();
        prio.setContent(isImportant ? "!" : "");

        d.setPrio(prio);
        return d;
    }

    @Override
    public Property createProperty() {
        return new PropertyI(model);
    }

    @Override
    public Property createProperty(CharSequence propertyName) {
        Property p = createProperty();
        p.setContent(propertyName);
        return p;
    }

    @Override
    public PropertyValue createPropertyValue() {
        return new PropertyValueI(model);
    }

    @Override
    public PropertyValue createPropertyValue(Expression expression) {
        PropertyValue pv = createPropertyValue();
        pv.setExpression(expression);
        return pv;
    }

    @Override
    public Expression createExpression() {
        return new ExpressionI(model);
    }

    @Override
    public Expression createExpression(CharSequence expression) {
        Expression e = createExpression();
        e.setContent(expression);
        return e;
    }

    @Override
    public Prio createPrio() {
        return new PrioI(model);
    }

    @Override
    public PlainElement createPlainElement() {
        return new PlainElementI(model);
    }

    @Override
    public PlainElement createPlainElement(CharSequence text) {
        return new PlainElementI(model, text);
    }

    @Override
    public MediaQueryOperator createMediaQueryOperator() {
        return new MediaQueryOperatorI(model);
    }

    @Override
    public MediaExpression createMediaExpression() {
        return new MediaExpressionI(model);
    }

    @Override
    public MediaExpression createMediaExpression(MediaFeature mediaFeature, MediaFeatureValue mediaFeatureValue) {
        MediaExpression me = createMediaExpression();
        me.setMediaFeature(mediaFeature);
        me.setMediaFeatureValue(mediaFeatureValue);
        return me;
    }

    @Override
    public MediaFeature createMediaFeature() {
        return new MediaFeatureI(model);
    }
    
    @Override
    public MediaFeatureValue createMediaFeatureValue() {
        return new MediaFeatureValueI(model);
    }

    @Override
    public MediaType createMediaType() {
        return new MediaTypeI(model);
    }

    @Override
    public Media createMedia() {
        return new MediaI(model);
    }

    @Override
    public Page createPage() {
        return new PageI(model);
    }

    @Override
    public Page createPage(CharSequence source) {
        return new PageI(model, source);
    }

    @Override
    public MediaQueryOperator createMediaQueryOperator(CharSequence text) {
        return new MediaQueryOperatorI(model, text);
    }

    @Override
    public MediaFeature createMediaFeature(CharSequence text) {
        return new MediaFeatureI(model, text);
    }

    @Override
    public MediaType createMediaType(CharSequence text) {
        return new MediaTypeI(model, text);
    }

    @Override
    public MediaQuery createMediaQuery(MediaQueryOperator mediaQueryOperator, MediaType mediaType, MediaExpression... mediaExpression) {
        MediaQuery mq = createMediaQuery();
        mq.setMediaQueryOperator(mediaQueryOperator);
        mq.setMediaType(mediaType);

        for (MediaExpression me : mediaExpression) {
            mq.addMediaExpression(me);
        }
        return mq;
    }

    @Override
    public MediaQueryList createMediaQueryList(MediaQuery... mediaQuery) {
        MediaQueryList mql = createMediaQueryList();
        for (MediaQuery mq : mediaQuery) {
            mql.addMediaQuery(mq);
        }
        return mql;
    }

    @Override
    public Media createMedia(MediaQueryList mediaQueryList, MediaBody mediaBody) {
        Media media = createMedia();
        media.setMediaQueryList(mediaQueryList);
        media.setMediaBody(mediaBody);
        return media;
    }
    
    @Override
    public MediaBody createMediaBody() {
        return new MediaBodyI(model);
    }
    
    public MediaBodyItem createMediaBodyItem() {
        return new MediaBodyItemI(model);
    }
    
    @Override
    public MediaBody createMediaBody(Rule... rules) {
        MediaBody mediaBody = createMediaBody();
        for (Rule r : rules) {
            mediaBody.addRule(r);
        }
        return mediaBody;
    }

    @Override
    public MediaBody createMediaBody(Page... pages) {
        MediaBody mediaBody = createMediaBody();
        for (Page page : pages) {
            mediaBody.addPage(page);
        }
        return mediaBody;
    }

    @Override
    public VendorAtRule createVendorAtRule() {
        return new VendorAtRuleI(model);
    }

    @Override
    public AtRuleId createAtRuleId() {
        return new AtRuleIdI(model);
    }

    @Override
    public AtRuleId createAtRuleId(CharSequence text) {
        AtRuleId atRuleId = createAtRuleId();
        atRuleId.setContent(text);
        return atRuleId;
    }

    @Override
    public MozDocument createMozDocument() {
        return new MozDocumentI(model);
    }

    @Override
    public MozDocumentFunction createMozDocumentFunction() {
        return new MozDocumentFunctionI(model);
    }

    @Override
    public GenericAtRule createGenericAtRule() {
        return new GenericAtRuleI(model);
    }

    @Override
    public WebkitKeyframes createWebkitKeyFrames() {
        return new WebkitKeyframesI(model);
    }

    @Override
    public WebkitKeyframesBlock createWebkitKeyFramesBlock() {
        return new WebkitKeyframesBlockI(model);
    }

    @Override
    public WebkitKeyframeSelectors createWebkitKeyframeSelectors() {
        return new WebkitKeyframeSelectorsI(model);
    }

    @Override
    public AtRule createAtRule() {
        return new AtRuleI(model);
    }

    @Override
    public MediaFeatureValue createMediaFeatureValue(Expression expression) {
        MediaFeatureValue mediaFeatureValue = createMediaFeatureValue();
        mediaFeatureValue.setExpression(expression);
        return mediaFeatureValue;
    }

}
