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

import org.netbeans.modules.css.model.impl.semantic.box.TokenNodeModel;
import org.netbeans.modules.css.model.impl.semantic.box.Text;
import org.netbeans.modules.css.model.impl.semantic.box.Length;
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
public class BoxEdgeSize extends NodeModel implements BoxElement {

    public TokenNodeModel auto;
    public Length length;
    public TokenNodeModel percentage;

    public BoxEdgeSize(Node node) {
        super(node);
    }

    private BoxEdgeSize(TokenNodeModel auto, Length length, TokenNodeModel percentage) {
        this.auto = auto;
        this.length = length;
        this.percentage = percentage;
    }

    private static TokenNodeModel createText(CharSequence text) {
        return new TokenNodeModel(text);
    }

    private static BoxEdgeSize createAuto() {
        return new BoxEdgeSize(createText("auto"), null, null);
    }

    private static BoxEdgeSize createLength(CharSequence length) {
        return new BoxEdgeSize(null, new Length(createText(length)), null);
    }

    private static BoxEdgeSize createPercentage(CharSequence value) {
        return new BoxEdgeSize(null, null, createText(value));
    }

    public static BoxEdgeSize parseValue(CharSequence tokenImage) {
        TokenAcceptor lengthTokenAcceptor = TokenAcceptor.getAcceptor("length"); //NOI18N
        TokenAcceptor percentageTokenAcceptor = TokenAcceptor.getAcceptor("percentage"); //NOI18N

        Tokenizer tokenizer = new Tokenizer(tokenImage);
        Token token = tokenizer.token();

        if (lengthTokenAcceptor.accepts(token)) {
            return createLength(tokenImage);
        } else if (percentageTokenAcceptor.accepts(token)) {
            return createPercentage(tokenImage);
        } else if (LexerUtils.equals("auto", tokenImage, true, true)) {
            return createAuto();
        }

        return null;
    }

    public Text getAuto() {
        return auto;
    }

    public Length getLength() {
        return length;
    }

    public Text getPercentage() {
        return percentage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BoxEdgeSize other = (BoxEdgeSize) obj;
        if (this.auto != other.auto && (this.auto == null || !this.auto.equals(other.auto))) {
            return false;
        }
        if (this.length != other.length && (this.length == null || !this.length.equals(other.length))) {
            return false;
        }
        if (this.percentage != other.percentage && (this.percentage == null || !this.percentage.equals(other.percentage))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.auto != null ? this.auto.hashCode() : 0);
        hash = 17 * hash + (this.length != null ? this.length.hashCode() : 0);
        hash = 17 * hash + (this.percentage != null ? this.percentage.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName());
        b.append("(");
        if (getAuto() != null) {
            b.append(getAuto());
        }
        if (getLength() != null) {
            b.append(getLength());
        }
        if (getPercentage() != null) {
            b.append(getPercentage());
        }
        b.append(")");
        return b.toString();
    }

    @Override
    public String asText() {
        if (auto != null) {
            return auto.getValue().toString();
        } else if (getLength() != null) {
            return getLength().getLength().getValue().toString();
        } else {
            return getPercentage().getValue().toString();
        }
    }

}
