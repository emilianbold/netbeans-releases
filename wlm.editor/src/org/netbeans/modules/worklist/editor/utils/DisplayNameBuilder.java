/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.utils;

/**
 *
 * @author anjeleevich
 */
public class DisplayNameBuilder {
    
    private Tag[] stack = null;
    private int stackSize = 0;

    private StringBuilder textBuilder = new StringBuilder();
    private StringBuilder htmlBuilder = null;

    public DisplayNameBuilder() {
    }

    public DisplayNameBuilder(String text) {
        if (text != null) {
            textBuilder.append(text);
        }
    }

    public String getText() {
        if (stackSize > 0) {
            throw new IllegalStateException(
                    "There are opened tags. Text building was not finished");
        }
        return textBuilder.toString();
    }

    public String getHTML() {
        if (stackSize > 0) {
            throw new IllegalStateException(
                    "There are opened tags. Text building was not finished");
        }
        return (htmlBuilder != null)
                ? htmlBuilder.toString()
                : null;
    }

    public DisplayNameBuilder append(String value) {
        textBuilder.append(value);

        if (htmlBuilder != null) {
            if (value != null) {
                int valueLength = value.length();

                for (int i = 0; i < valueLength; i++) {
                    char c = value.charAt(i);
                    if (c == '&') {
                        htmlBuilder.append("&amp;");
                    } else if (c == '<') {
                        htmlBuilder.append("&lt;");
                    } else if (c == '>') {
                        htmlBuilder.append("&gt;");
                    } else {
                        htmlBuilder.append(c);
                    }
                }
            } else {
                htmlBuilder.append(value);
            }
        }
        return this;
    }

    public DisplayNameBuilder append(int value) {
        textBuilder.append(value);

        if (htmlBuilder != null) {
            htmlBuilder.append(value);
        }
        return this;
    }

    private StringBuilder getHTMLBuilder() {
        if (htmlBuilder == null) {
            htmlBuilder = new StringBuilder(textBuilder);
        }
        return htmlBuilder;
    }

    public DisplayNameBuilder startColor(String color) {
        push(Tag.COLOR);
        StringBuilder html = getHTMLBuilder();
        html.append("<font color=");
        html.append(color);
        html.append(">");
        return this;
    }

    public DisplayNameBuilder endColor() {
        pop(Tag.COLOR);
        getHTMLBuilder().append("</font>");
        return this;
    }
    
    public DisplayNameBuilder startItalic() {
        push(Tag.ITALIC);
        getHTMLBuilder().append("<i>");
        return this;
    }

    public DisplayNameBuilder endItalic() {
        pop(Tag.ITALIC);
        getHTMLBuilder().append("</i>");
        return this;
    }

    public DisplayNameBuilder startBold() {
        push(Tag.BOLD);
        getHTMLBuilder().append("<b>");
        return this;
    }

    public DisplayNameBuilder endBold() {
        pop(Tag.BOLD);
        getHTMLBuilder().append("</b>");
        return this;
    }

    public DisplayNameBuilder startStrike() {
        push(Tag.STRIKE);
        getHTMLBuilder().append("<s>");
        return this;
    }

    public DisplayNameBuilder endStrike() {
        pop(Tag.STRIKE);
        getHTMLBuilder().append("</s>");
        return this;
    }

    private void push(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag can not be null");
        }

        if (stack == null) {
            stack = new Tag[4];
        } else if (stackSize == stack.length) {
            Tag[] newStack = new Tag[stackSize * 4 / 3 + 1];
            System.arraycopy(stack, 0, newStack, 0, stackSize);
            stack = newStack;
        }
        stack[stackSize++] = tag;
    }

    private void pop(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag can not be null");
        }
        
        if (stack == null) {
            throw new IllegalStateException("Tags were not opened");
        }

        if (stackSize == 0) {
            throw new IllegalStateException(
                    "All tags have been closed. Stack is empty");
        }

        Tag lastTag = stack[stackSize - 1];

        if (lastTag != tag) {
            throw new IllegalStateException("Expected to close " + lastTag
                    + " tag. But get " + tag + " tag.");
        } else {
            stack[--stackSize] = null;
        }
    }

    private static enum Tag {
        COLOR,
        BOLD,
        STRIKE,
        ITALIC;
    }
}
