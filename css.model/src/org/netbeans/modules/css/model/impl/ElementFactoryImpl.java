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

import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.*;

/**
 *
 * @author marekfukala
 */
public final class ElementFactoryImpl implements ElementFactory {

    private Model model;
    
    public ElementFactoryImpl(Model model) {
        this.model = model;
    }

    public Element createElement(Model model, Node node) {
        //TODO use reflection
        switch (node.type()) {
            case imports:
                return new ImportsI(model, node);
            case importItem:
                return new ImportItemI(model, node);
            case resourceIdentifier:
                return new ResourceIdentifierI(model, node);
            case media:
                return new MediaI(model, node);
            case mediaQueryList:
                return new MediaQueryListI(model, node);
            case mediaQuery:
                return new MediaQueryI(model, node);
            case mediaExpression:
                return new MediaExpressionI(model, node);
            case mediaFeature:
                return new MediaFeatureI(model, node);
            case mediaType:
                return new MediaTypeI(model, node);
            case mediaQueryOperator:
                return new MediaQueryOperatorI(model, node);
            case namespaces:
                return new NamespacesI(model, node);
            case namespace:
                return new NamespaceI(model, node);
            case namespacePrefixName:
                return new NamespacePrefixNameI(model, node);
            case body:
                return new BodyI(model, node);
            case bodyItem:
                return new BodyItemI(model, node);
            case rule:
                return new RuleI(model, node);
            case selectorsGroup:
                return new SelectorsGroupI(model, node);
            case declarations:
                return new DeclarationsI(model, node);
            case declaration:
                return new DeclarationI(model, node);
            case selector:
                return new SelectorI(model, node);
            case property:
                return new PropertyI(model, node);
            case propertyValue:
                return new PropertyValueI(model, node);
            case expr:
                return new ExpressionI(model, node);
            case prio:
                return new PrioI(model, node);
            case charSet:
                return new CharSetI(model, node);
            case charSetValue:
                return new CharSetValueI(model, node);
            case styleSheet:
                return new StyleSheetI(model, node);

            case ws:
            default:
                return new PlainElementI(model, node);
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
    public Declarations createDeclarations(Declaration... declarations) {
        Declarations ds = createDeclarations();
        for (Declaration d : declarations) {
            ds.addDeclaration(d);
        }
        return ds;
    }

    @Override
    public Declaration createDeclaration() {
        return new DeclarationI(model);
    }

    @Override
    public Declaration createDeclaration(Property property, PropertyValue propertyValue, boolean isImportant) {
        Declaration d = createDeclaration();
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
    public MediaExpression createMediaExpression(MediaFeature mediaFeature, Expression expression) {
        MediaExpression me = createMediaExpression();
        me.setMediaFeature(mediaFeature);
        me.setExpression(expression);
        return me;
    }

    @Override
    public MediaFeature createMediaFeature() {
       return new MediaFeatureI(model);
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
        
        for(MediaExpression me : mediaExpression) {
            mq.addMediaExpression(me);
        }
        return mq;
    }

    @Override
    public MediaQueryList createMediaQueryList(MediaQuery... mediaQuery) {
        MediaQueryList mql = createMediaQueryList();
        for(MediaQuery mq : mediaQuery) {
            mql.addMediaQuery(mq);
        }
        return mql;
    }

    @Override
    public Media createMedia(MediaQueryList mediaQueryList, Rule... rule) {
        Media media = createMedia();
        media.setMediaQueryList(mediaQueryList);
        for(Rule r : rule) {
            media.addRule(r);
        }
        return media;
    }

    @Override
    public Media createMedia(MediaQueryList mediaQueryList, Page... page) {
        Media media = createMedia();
        for(Page p : page) {
            media.addPage(p);
        }
        return media;
    }
    
    
}
