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

import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.model.api.*;

/**
 *
 * @author marekfukala
 */
public final class ElementFactoryImpl implements ElementFactory {

    private static final ElementFactoryImpl INSTANCE = new ElementFactoryImpl();

    private ElementFactoryImpl() {
    }

    public static ElementFactoryImpl getDefault() {
        return INSTANCE;
    }

    public Element createElement(ModelElementContext ctx) {
        //TODO use reflection
        switch (ctx.getNode().type()) {
            case imports:
                return new ImportsI(ctx);
            case importItem:
                return new ImportItemI(ctx);
            case resourceIdentifier:
                return new ResourceIdentifierI(ctx);
            case media:
                return new MediaI(ctx);
            case mediaQueryList:
                return new MediaQueryListI(ctx);
            case mediaQuery:
                return new MediaQueryI(ctx);
            case mediaExpression:
                return new MediaExpressionI(ctx);
            case mediaFeature:
                return new MediaFeatureI(ctx);
            case mediaType:
                return new MediaTypeI(ctx);
            case mediaQueryOperator:
                return new MediaQueryOperatorI(ctx);
            case namespaces:
                return new NamespacesI(ctx);
            case namespace:
                return new NamespaceI(ctx);
            case namespacePrefixName:
                return new NamespacePrefixNameI(ctx);
            case body:
                return new BodyI(ctx);
            case bodyItem:
                return new BodyItemI(ctx);
            case rule:
                return new RuleI(ctx);
            case selectorsGroup:
                return new SelectorsGroupI(ctx);
            case declarations:
                return new DeclarationsI(ctx);
            case declaration:
                return new DeclarationI(ctx);
            case selector:
                return new SelectorI(ctx);
            case property:
                return new PropertyI(ctx);
            case expr:
                return new ExpressionI(ctx);
            case prio:
                return new PrioI(ctx);
            case charSet:
                return new CharSetI(ctx);
            case charSetValue:
                return new CharSetValueI(ctx);
            case styleSheet:
                return new StyleSheetI(ctx);

            case ws:
            default:
                return new PlainElementI(ctx);
        }
    }
    
    @Override
    public StyleSheet createStyleSheet() {
        return new StyleSheetI();
    }

    @Override
    public CharSet createCharSet() {
        return new CharSetI();
    }

    @Override
    public CharSetValue createCharSetValue() {
        return new CharSetValueI();
    }

    @Override
    public Imports createImports() {
        return new ImportsI();
    }

    @Override
    public ImportItem createImportItem() {
        return new ImportItemI();
    }

    @Override
    public ResourceIdentifier createResourceIdentifier() {
        return new ResourceIdentifierI();
    }

    @Override
    public MediaQueryList createMediaQueryList() {
        return new MediaQueryListI();
    }

    @Override
    public MediaQuery createMediaQuery() {
        return new MediaQueryI();
    }

    @Override
    public Namespaces createNamespaces() {
        return new NamespacesI();
    }

    @Override
    public Namespace createNamespace() {
        return new NamespaceI();
    }

    @Override
    public NamespacePrefixName createNamespacePrefixName() {
        return new NamespacePrefixNameI();
    }

    @Override
    public Body createBody() {
        return new BodyI();
    }

    @Override
    public BodyItem createBodyItem() {
        return new BodyItemI();
    }

    @Override
    public Rule createRule() {
        return new RuleI();
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
        return new SelectorsGroupI();
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
        return new SelectorI();
    }

    @Override
    public Selector createSelector(CharSequence code) {
        SelectorI si = new SelectorI();
        si.setContent(code);
        return si;
    }

    @Override
    public Declarations createDeclarations() {
        return new DeclarationsI();
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
        return new DeclarationI();
    }

    @Override
    public Declaration createDeclaration(Property property, Expression expression, boolean isImportant) {
        Declaration d = createDeclaration();
        d.setProperty(property);
        d.setExpression(expression);

        Prio prio = createPrio();
        prio.setContent(isImportant ? "!" : "");

        d.setPrio(prio);
        return d;
    }

    @Override
    public Property createProperty() {
        return new PropertyI();
    }

    @Override
    public Property createProperty(CharSequence propertyName) {
        Property p = createProperty();
        p.setContent(propertyName);
        return p;
    }

    @Override
    public Expression createExpression() {
        return new ExpressionI();
    }

    @Override
    public Expression createExpression(CharSequence expression) {
        Expression e = createExpression();
        e.setContent(expression);
        return e;
    }

    @Override
    public Prio createPrio() {
        return new PrioI();
    }

    @Override
    public PlainElement createPlainElement() {
        return new PlainElementI();
    }

    @Override
    public PlainElement createPlainElement(CharSequence text) {
        return new PlainElementI(text);
    }

    @Override
    public MediaQueryOperator createMediaQueryOperator() {
        return new MediaQueryOperatorI();
    }

    @Override
    public MediaExpression createMediaExpression() {
        return new MediaExpressionI();
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
       return new MediaFeatureI();
    }

    @Override
    public MediaType createMediaType() {
        return new MediaTypeI();
    }

    @Override
    public Media createMedia() {
        return new MediaI();
    }

    @Override
    public Page createPage() {
        return new PageI();
    }

    @Override
    public MediaQueryOperator createMediaQueryOperator(CharSequence text) {
        return new MediaQueryOperatorI(text);
    }

    @Override
    public MediaFeature createMediaFeature(CharSequence text) {
        return new MediaFeatureI(text);
    }

    @Override
    public MediaType createMediaType(CharSequence text) {
        return new MediaTypeI(text);
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
