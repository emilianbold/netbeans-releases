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
package org.netbeans.modules.css.model.impl.semantic.box;

import java.util.Collection;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.semantic.box.Box;
import org.netbeans.modules.css.model.api.semantic.box.BoxElement;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.PModel;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "CTL_BorderWidthDisplayName=Border Width", // NOI18N
    "CTL_BorderWidthDescription=Border Width Box Model", // NOI18N
    "CTL_BorderWidthCategory=Box" //NOI18N
})
public class DeclarationsBorderWidthModel extends DeclarationsBoxModelBase implements PModel {

    private static final String PROPERTY_NAME_PREFIX = "border"; //NOI18N
    private static final String PROPERTY_NAME_POSTFIX = "width"; //NOI18N
    
    private static final String PROPERTY_NAME = PROPERTY_NAME_PREFIX + "-" + PROPERTY_NAME_POSTFIX;

    public DeclarationsBorderWidthModel(Model model,
            Declarations element,
            Collection<Declaration> involved,
            Box box) {
        super(model, element, involved, box);
    }

    @Override
    protected String getPropertyName() {
        return PROPERTY_NAME;
    }

    @Override
    protected String getPropertyName(Edge edge) {
        StringBuilder b = new StringBuilder();
        b.append(PROPERTY_NAME_PREFIX);
        b.append('-');
        b.append(edge.name().toLowerCase());
        b.append('-');
        b.append(PROPERTY_NAME_POSTFIX);

        return b.toString();
    }

    @Override
    public String getDisplayName() {
        return Bundle.CTL_BorderWidthDisplayName();
    }

    @Override
    public String getDescription() {
        return Bundle.CTL_BorderWidthDescription();
    }

    @Override
    public String getCategoryName() {
        return Bundle.CTL_BorderWidthCategory();
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public BoxElement createElement(CharSequence text) {
        return BorderWidthItem.parseValue(text);
    }
}
