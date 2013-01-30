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
package org.netbeans.modules.css.model.impl.semantic.background;

import org.netbeans.modules.css.lib.api.properties.GroupNode;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.NodeVisitor2;
import org.netbeans.modules.css.lib.api.properties.TokenNode;
import org.netbeans.modules.css.model.api.semantic.Length;
import org.netbeans.modules.css.model.api.semantic.Percentage;
import org.netbeans.modules.css.model.api.semantic.Size;
import org.netbeans.modules.css.model.impl.semantic.Element;
import org.netbeans.modules.css.model.impl.semantic.LengthI;
import org.netbeans.modules.css.model.impl.semantic.PercentageI;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class BgSizeI {

    private SizeI size = new SizeI();

    private enum State {
        IN_SINGLE,
        IN_PAIR;
    }

    public BgSizeI(Node node) {
        node.accept(new NodeVisitor2() {
            private State state;
            private int arg;
            
            @Override
            public boolean visitGroupNode(GroupNode groupNode) {
                switch (Element.forNode(groupNode)) {
                    case single:
                        state = State.IN_SINGLE;
                        break;
                    case pair:
                        state = State.IN_PAIR;
                        break;
                        
                    case percentage:
                        arg++;
                        switch(arg) {
                            case 1:
                                size.horizontal.percentage = new PercentageI(groupNode);
                                size.vertical.auto = true;
                                break;
                            case 2:
                                size.vertical.percentage = new PercentageI(groupNode);
                                size.vertical.auto = false;
                                break;
                        }
                        break;
                    case length:
                        arg++;
                        switch(arg) {
                            case 1:
                                size.horizontal.length = new LengthI(groupNode);
                                size.vertical.auto = true;
                                break;
                            case 2:
                                size.vertical.length = new LengthI(groupNode);
                                size.vertical.auto = false;
                                break;
                        }
                        break;
                }
                return true;
            }

            @Override
            public void visitTokenNode(TokenNode tokenNode) {
                CharSequence img = tokenNode.image();
                switch(state) {
                    case IN_SINGLE:
                        if (LexerUtils.equals("cover", img, true, true)) { //NOI18N
                            size.cover = true;
                        } else if(LexerUtils.equals("contain", img, true, true)) { //NOI18N
                            size.contain = true;
                        }
                        break;
                        
                    case IN_PAIR:
                        if(LexerUtils.equals("auto", img, true, true)) { //NOI18N
                            arg++;
                            switch (arg) {
                                case 1:
                                    size.horizontal.auto = true;
                                    //intentional fallthrough
                                case 2:
                                    size.vertical.auto = true;
                                    break;
                            }
                        }
                        break;
                        
                }

            }
        });

    }

    public Size getSize() {
        return size;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("")
                .toString();

    }

    private static class SizeI implements Size {

        private boolean cover, contain;
        private ValueI horizontal, vertical;

        public SizeI() {
            horizontal = new ValueI();
            vertical = new ValueI();
        }

        @Override
        public boolean isCover() {
            return cover;
        }

        @Override
        public boolean isContain() {
            return contain;
        }

        @Override
        public Value getHorizontalSize() {
            return horizontal;
        }

        @Override
        public Value getVerticalSize() {
            return vertical;
        }
    }

    private static class ValueI implements Size.Value {

        private Length length;
        private Percentage percentage;
        private boolean auto;

        public ValueI() {
        }

        @Override
        public Length getLength() {
            return length;
        }

        @Override
        public Percentage getPercentage() {
            return percentage;
        }

        @Override
        public boolean isAuto() {
            return auto;
        }
    }
}
