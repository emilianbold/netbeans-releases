/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.debugger.jpda.truffle.ast;

/**
 * A representation of Truffle Node class.
 */
public final class TruffleNode {

    private final String className;
    private final String description;
    private final String sourceURI;
    private final int l1;
    private final int c1;
    private final int l2;
    private final int c2;
    private final TruffleNode[] ch;
    private boolean current;

    public TruffleNode(String className, String description, String sourceURI, int l1, int c1, int l2, int c2, int numCh) {
        this.className = className;
        this.description = description;
        this.sourceURI = sourceURI;
        this.l1 = l1;
        this.c1 = c1;
        this.l2 = l2;
        this.c2 = c2;
        this.ch = new TruffleNode[numCh];
    }

    private void setChild(int i, TruffleNode node) {
        ch[i] = node;
    }

    public String getClassName() {
        return className;
    }

    public String getClassSimpleName() {
        int index = className.lastIndexOf('.');
        if (index > 0) {
            return className.substring(index + 1);
        } else {
            return className;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public int getStartLine() {
        return l1;
    }

    public int getStartColumn() {
        return c1;
    }

    public int getEndLine() {
        return l2;
    }

    public int getEndColumn() {
        return c2;
    }

    public TruffleNode[] getChildren() {
        return ch;
    }

    /** This node is currently being executed. */
    public boolean isCurrent() {
        return current;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private TruffleNode node;
        private int currentLine;

        private Builder() {}

        public Builder nodes(String nodes) {
            StringLineReader slr = new StringLineReader(nodes);
            node = parseNode(slr);
            return this;
        }

        public TruffleNode build() {
            if (currentLine > 0) {
                markCurrent(node, currentLine);
            }
            return node;
        }

        /** Mark all node paths which are currently being executed as current. */
        private boolean markCurrent(TruffleNode node, int currentLine) {
            if (node.getChildren().length == 0) {
                if (node.getStartLine() <= currentLine && currentLine <= node.getEndLine()) {
                    node.current = true;
                    return true;
                } else {
                    return false;
                }
            } else {
                boolean isSomeCurrent = false;
                for (TruffleNode ch : node.getChildren()) {
                    if (markCurrent(ch, currentLine)) {
                        isSomeCurrent = true;
                    }
                }
                node.current = isSomeCurrent;
                return isSomeCurrent;
            }
        }

        private TruffleNode parseNode(StringLineReader slr) {
            String className = slr.nextLine();
            String description = slr.nextLine();
            String sourceURI;
            int l1, c1, l2, c2;
            String ss = slr.nextLine();
            if (ss.isEmpty()) {
                sourceURI = null;
                l1 = c1 = l2 = c2 = -1;
            } else {
                sourceURI = ss;
                ss = slr.nextLine();
                int i1 = 0;
                int i2 = ss.indexOf(':');
                l1 = Integer.parseInt(ss.substring(i1, i2));
                i1 = i2 + 1;
                i2 = ss.indexOf('-');
                c1 = Integer.parseInt(ss.substring(i1, i2));
                i1 = i2 + 1;
                i2 = ss.indexOf(':', i1);
                l2 = Integer.parseInt(ss.substring(i1, i2));
                i1 = i2 + 1;
                i2 = ss.length();
                c2 = Integer.parseInt(ss.substring(i1, i2));
            }
            int numCh = Integer.parseInt(slr.nextLine());
            TruffleNode node = new TruffleNode(className, description, sourceURI, l1, c1, l2, c2, numCh);
            for (int i = 0; i < numCh; i++) {
                node.setChild(i, parseNode(slr));
            }
            return node;
        }

        public Builder currentLine(int line) {
            this.currentLine = line;
            return this;
        }

        private static class StringLineReader {

            private final String lines;
            private int i = 0;

            private StringLineReader(String lines) {
                this.lines = lines;
            }

            String nextLine() {
                int i2 = lines.indexOf('\n', i);
                if (i2 < i) {
                    return null;
                }
                String line = lines.substring(i, i2);
                i = i2 + 1;
                return line;
            }
        }
    }
}
