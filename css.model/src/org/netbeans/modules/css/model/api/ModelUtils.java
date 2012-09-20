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
package org.netbeans.modules.css.model.api;

import java.util.Collection;

/**
 * Various utilities for css source modifications.
 *
 * The utility methods needs to be called from within either 
 * {@link Model#runReadTask(org.netbeans.modules.css.model.api.Model.ModelTask) }
 * or null {@link Model#runWriteTask(org.netbeans.modules.css.model.api.Model.ModelTask) !!!
 *
 * TODO: add model tasks checking
 *
 * @author marekfukala
 */
public class ModelUtils {

    private Model model;
    private StyleSheet styleSheet;
    private ElementFactory factory;

    public ModelUtils(Model model) {
        this.model = model;
        this.factory = model.getElementFactory();
        this.styleSheet = model.getStyleSheet();
    }

    /**
     * Creates a new {@link Rule}
     *
     * @param selectors list of selectors from the selector group
     * @param declarations list of declarations in the "property:value" form
     */
    public Rule createRule(Collection<String> selectors, Collection<String> declarations) {
        SelectorsGroup selectorsGroup = factory.createSelectorsGroup();
        for (String selectorName : selectors) {
            selectorsGroup.addSelector(factory.createSelector(selectorName));
        }
        Declarations decls = factory.createDeclarations();
        for (String declaration : declarations) {
            decls.addDeclaration(createDeclaration(declaration));
        }
        return factory.createRule(selectorsGroup, decls);
    }

    /**
     * Returns an instance of {@link Body}.
     *
     * If the body doesn't exist in the stylesheet it is created
     *
     * @return non-null instance of {@link Body}
     */
    public Body getBody() {
        Body body = styleSheet.getBody();
        if (body == null) {
            //create body if empty file
            body = factory.createBody();
            styleSheet.setBody(body);
        }
        return body;
    }

    public Declaration createDeclaration(String code) {
        int separatorIndex = code.indexOf(':');
        if (separatorIndex == -1) {
            throw new IllegalArgumentException(String.format("Bad declaration value (forgotten colon): %s", code));
        }
        String propertyImg = code.substring(0, separatorIndex);
        String valueImg = code.substring(separatorIndex + 1);

        Property property = factory.createProperty(propertyImg);
        PropertyValue propertyValue = factory.createPropertyValue(factory.createExpression(valueImg));

        return factory.createDeclaration(property, propertyValue, false);
    }
}
