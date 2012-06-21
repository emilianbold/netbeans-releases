/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl.semantic.box;

import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.Token;
import org.netbeans.modules.css.lib.api.properties.TokenAcceptor;
import org.netbeans.modules.css.lib.api.properties.Tokenizer;
import org.netbeans.modules.css.model.impl.semantic.NodeModel;
import org.netbeans.modules.css.model.api.semantic.box.BoxElement;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class BorderWidthItem extends NodeModel implements BoxElement {

    public Length length;
    private TokenNodeModel fixedValue;

    public static enum FixedValue {

        medium, thin, thick;

        public String getValue() {
            return name();
        }
    }

    public BorderWidthItem(Node node) {
        super(node);
    }

    private BorderWidthItem(FixedValue fixedValue) {
        super();
        this.fixedValue = createText(fixedValue.getValue());
    }

    private BorderWidthItem(Length length) {
        super();
        this.length = length;
    }

    private static TokenNodeModel createText(CharSequence text) {
        return new TokenNodeModel(text);
    }

    public static BorderWidthItem parseValue(CharSequence tokenImage) {
        TokenAcceptor lengthTokenAcceptor = TokenAcceptor.getAcceptor("length"); //NOI18N

        Tokenizer tokenizer = new Tokenizer(tokenImage);
        Token token = tokenizer.token();

        if (lengthTokenAcceptor.accepts(token)) {
            return new BorderWidthItem(new Length(createText(tokenImage)));
        }

        FixedValue fixedValue = getFixedValue(tokenImage);
        if (fixedValue != null) {
            return new BorderWidthItem(fixedValue);
        }

        return null;
    }

    private static FixedValue getFixedValue(CharSequence sequence) {
        for (FixedValue fv : FixedValue.values()) {
            if (LexerUtils.equals(fv.getValue(), sequence, true, true)) {
                return fv;
            }
        }
        return null;
    }

    public Length getLength() {
        return length;
    }

    @Override
    protected TokenNodeModel getTokenNode(CharSequence image) {
        if (fixedValue != null) {
            if (LexerUtils.equals(fixedValue.getValue(), image, true, false)) {
                return fixedValue;
            }
        }

        //use the value from submodels
        return super.getTokenNode(image);
    }

    public TokenNodeModel getFixedValueModel(FixedValue fixedValue) {
        return getTokenNode(fixedValue.getValue());
    }

    @Override
    public String asText() {
        if (getLength() != null) {
            return getLength().getLength().getValue().toString();
        } else {
            for (FixedValue fv : FixedValue.values()) {
                TokenNodeModel tnm = getFixedValueModel(fv);
                if (tnm != null) {
                    return tnm.getValue().toString();
                }
            }
        }
        return INVALID_VALUE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BorderWidthItem other = (BorderWidthItem) obj;

        return asText().equals(other.asText());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.length != null ? this.length.hashCode() : 0);
        hash = 47 * hash + (this.fixedValue != null ? this.fixedValue.hashCode() : 0);
        return hash;
    }
}
