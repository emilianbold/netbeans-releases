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
import org.netbeans.modules.css.lib.api.properties.NodeVisitor;
import org.netbeans.modules.css.lib.api.properties.NodeVisitor2;
import org.netbeans.modules.css.lib.api.properties.TokenNode;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.Length;
import org.netbeans.modules.css.model.api.semantic.Percentage;
import org.netbeans.modules.css.model.api.semantic.background.BackgroundPosition;
import org.netbeans.modules.css.model.impl.semantic.Element;
import org.netbeans.modules.css.model.impl.semantic.LengthI;
import org.netbeans.modules.css.model.impl.semantic.PercentageI;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class BgPos implements BackgroundPosition {

    private PositionI horizontal, vertical;

    public BgPos(Node node) {
        node.accept(new NodeVisitor.Adapter() {
            @Override
            public boolean visit(Node node) {
                switch (Element.forNode(node)) {
                    case bg_pos_1:
                        node.accept(new NodeVisitor2() {
                            @Override
                            public boolean visitGroupNode(GroupNode node) {
                                switch (Element.forNode(node)) {
                                    case percentage:
                                        Percentage p = new PercentageI(node);
                                        horizontal = new PositionI(Edge.LEFT, false, p, null);
                                        vertical = new PositionI(Edge.TOP, true, null, null); //center
                                        return false;
                                    case length:
                                        Length l = new LengthI(node);
                                        horizontal = new PositionI(Edge.LEFT, false, null, l);
                                        vertical = new PositionI(Edge.TOP, true, null, null); //center
                                        return false;
                                }
                                return true;
                            }

                            @Override
                            public void visitTokenNode(TokenNode tokenNode) {
                                String image = tokenNode.image().toString();
                                if (LexerUtils.equals("center", image, true, true)) { //NOI18N
                                    horizontal = new PositionI(Edge.LEFT, true, null, null);
                                    vertical = new PositionI(Edge.TOP, true, null, null); //center
                                } else {
                                    Edge e = Edge.valueOf(image.toUpperCase());
                                    if(e.isVerticalEdge()) {
                                        horizontal = new PositionI(e, false, null, null);
                                        vertical = new PositionI(Edge.TOP, true, null, null); 
                                    } else {
                                        //horizontal
                                        horizontal = new PositionI(Edge.LEFT, true, null, null);
                                        vertical = new PositionI(e, false, null, null);
                                        
                                    }
                                }
                            }
                        });
                        break;

                    case bg_pos_2:
                        node.accept(new NodeVisitor2() {
                            @Override
                            public boolean visitGroupNode(GroupNode node) {
                                switch (Element.forNode(node)) {
                                    case bg_pos_2_horizontal:
                                        node.accept(new NodeVisitor2() {
                                            @Override
                                            public boolean visitGroupNode(GroupNode node) {
                                                switch (Element.forNode(node)) {
                                                    case percentage:
                                                        Percentage p = new PercentageI(node);
                                                        horizontal = new PositionI(Edge.LEFT, false, p, null);
                                                        return false;
                                                    case length:
                                                        Length l = new LengthI(node);
                                                        horizontal = new PositionI(Edge.LEFT, false, null, l);
                                                        return false;
                                                }
                                                return true;
                                            }

                                            @Override
                                            public void visitTokenNode(TokenNode tokenNode) {
                                                String image = tokenNode.image().toString();
                                                if (LexerUtils.equals("center", image, true, true)) { //NOI18N
                                                    horizontal = new PositionI(Edge.LEFT, true, null, null);
                                                } else {
                                                    Edge e = Edge.valueOf(image.toUpperCase());
                                                    horizontal = new PositionI(e, false, null, null);
                                                }
                                            }
                                        });
                                    case bg_pos_2_vertical:
                                        node.accept(new NodeVisitor2() {
                                            @Override
                                            public boolean visitGroupNode(GroupNode node) {
                                                switch (Element.forNode(node)) {
                                                    case percentage:
                                                        Percentage p = new PercentageI(node);
                                                        vertical = new PositionI(Edge.TOP, false, p, null);
                                                        return false;
                                                    case length:
                                                        Length l = new LengthI(node);
                                                        vertical = new PositionI(Edge.TOP, false, null, l);
                                                        return false;
                                                }
                                                return true;
                                            }

                                            @Override
                                            public void visitTokenNode(TokenNode tokenNode) {
                                                String image = tokenNode.image().toString();
                                                if (LexerUtils.equals("center", image, true, true)) { //NOI18N
                                                    vertical = new PositionI(Edge.TOP, true, null, null);
                                                } else {
                                                    Edge e = Edge.valueOf(image.toUpperCase());
                                                    vertical = new PositionI(e, false, null, null);
                                                }
                                            }
                                        });
                                }
                                return true;
                            }
                        });

                        break;

                    case bg_pos_34_1:
                        node.accept(new NodeVisitor2() {
                            @Override
                            public boolean visitGroupNode(GroupNode groupNode) {
                                switch (Element.forNode(groupNode)) {
                                    case bg_pos_34_left_right_pair:
                                        horizontal = new PositionI();
                                        groupNode.accept(new NodeVisitor2() {
                                            @Override
                                            public boolean visitGroupNode(GroupNode node) {
                                                switch (Element.forNode(node)) {
                                                    case percentage:
                                                        horizontal.setPercentage(new PercentageI(node));
                                                        return false;
                                                    case length:
                                                        horizontal.setLength(new LengthI(node));
                                                        return false;
                                                }
                                                return true;
                                            }

                                            @Override
                                            public void visitTokenNode(TokenNode tokenNode) {
                                                //left | right
                                                Edge edge = Edge.valueOf(tokenNode.image().toString().toUpperCase());
                                                horizontal.setRelativeTo(edge);
                                            }
                                        });
                                }
                                return true;
                            }

                            @Override
                            public void visitTokenNode(TokenNode tokenNode) {
                                if (LexerUtils.equals("center", tokenNode.image(), true, true)) {
                                    horizontal = new PositionI(Edge.LEFT, true, null, null);
                                }
                            }
                        });

                        break;
                    case bg_pos_34_2:
                        node.accept(new NodeVisitor2() {
                            @Override
                            public boolean visitGroupNode(GroupNode groupNode) {
                                switch (Element.forNode(groupNode)) {
                                    case bg_pos_34_top_botoom_pair:
                                        vertical = new PositionI();
                                        groupNode.accept(new NodeVisitor2() {
                                            @Override
                                            public boolean visitGroupNode(GroupNode node) {
                                                switch (Element.forNode(node)) {
                                                    case percentage:
                                                        vertical.setPercentage(new PercentageI(node));
                                                        return false;
                                                    case length:
                                                        vertical.setLength(new LengthI(node));
                                                        return false;
                                                }
                                                return true;
                                            }

                                            @Override
                                            public void visitTokenNode(TokenNode tokenNode) {
                                                //top | bottom
                                                Edge edge = Edge.valueOf(tokenNode.image().toString().toUpperCase());
                                                vertical.setRelativeTo(edge);
                                            }
                                        });
                                }
                                return true;
                            }

                            @Override
                            public void visitTokenNode(TokenNode tokenNode) {
                                if (LexerUtils.equals("center", tokenNode.image(), true, true)) {
                                    vertical = new PositionI(Edge.TOP, true, null, null);
                                }
                            }
                        });

                        break;

                    default:
                        return true;
                }
                return false; //stop visiting
            }
        });

    }

    @Override
    public Position getHorizontalPosition() {
        return horizontal;
    }

    @Override
    public Position getVerticalPosition() {
        return vertical;
    }

    /**
     * if no isCenter() returns true and both getPercentage() and getLength()
     * are null it means the position if 0% (0px) to the specified edge.
     */
    private static class PositionI implements Position {

        private Edge relativeTo;
        private Percentage percentage;
        private Length length;
        private boolean center;

        public PositionI() {
        }

        public PositionI(Edge relativeTo, boolean center, Percentage percentage, Length length) {
            this.relativeTo = relativeTo;
            this.center = center;
            this.percentage = percentage;
            this.length = length;
        }

        public boolean isCenter() {
            return center;
        }

        @Override
        public Edge getRelativeTo() {
            return relativeTo;
        }

        @Override
        public Percentage getPercentage() {
            return percentage;
        }

        @Override
        public Length getLength() {
            return length;
        }

        public void setRelativeTo(Edge relativeTo) {
            this.relativeTo = relativeTo;
        }

        public void setPercentage(Percentage percentage) {
            this.percentage = percentage;
        }

        public void setLength(Length length) {
            this.length = length;
        }

        public void setCenter(boolean center) {
            this.center = center;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("Position(relativeTo=")
                    .append(relativeTo.name())
                    .append(", center=")
                    .append(center)
                    .append(", percentage=")
                    .append(percentage)
                    .append(", length=")
                    .append(length)
                    .append(')')
                    .toString();

        }
    }
}
