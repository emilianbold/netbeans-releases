/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.ojet;

import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class OJETUtils {

    public static final String OJ_COMPONENT = "ojComponent";  //NOI18N

    @StaticResource
    public static final String OJET_ICON_PATH = "org/netbeans/modules/html/ojet/ui/resources/ojet-icon.png"; // NOI18N
    public static final ImageIcon OJET_ICON = ImageUtilities.loadImageIcon(OJET_ICON_PATH, false); // NOI18N

    public static String getPrefix(OJETContext ojContext, Document document, int offset) {
        TokenHierarchy th = TokenHierarchy.get(document);
        String empty = "";
        switch (ojContext) {
            case DATA_BINDING:
                TokenSequence<KODataBindTokenId> ts = LexerUtils.getTokenSequence(th, offset, KODataBindTokenId.language(), false);
                if (ts != null) {
                    int diff = ts.move(offset);
                    if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                        //we are on a token of ko-data-bind token sequence
                        Token<KODataBindTokenId> etoken = ts.token();
                        if (etoken.id() == KODataBindTokenId.KEY) {
                            //ke|
                            CharSequence prefix = diff == 0 ? etoken.text() : etoken.text().subSequence(0, diff);
                            return prefix.toString();
                        }
                    }
                    break;
                } 
        }
        return empty;
    }

    public static int getPrefixOffset(OJETContext ojContext, Document document, int offset) {
        TokenHierarchy th = TokenHierarchy.get(document);
        int result = offset;
        switch (ojContext) {
            case DATA_BINDING:
                TokenSequence<KODataBindTokenId> ts = LexerUtils.getTokenSequence(th, offset, KODataBindTokenId.language(), false);
                if (ts != null) {
                    int diff = ts.move(offset);
                    if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                        //we are on a token of ko-data-bind token sequence
                        Token<KODataBindTokenId> etoken = ts.token();
                        if (etoken.id() == KODataBindTokenId.KEY) {
                            //ke
                            return ts.offset();
                        }
                    }
                    break;
                } 
        }
        return result;
    }
}
