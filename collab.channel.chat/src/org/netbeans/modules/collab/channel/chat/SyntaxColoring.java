/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.collab.channel.chat;

import com.sun.collablet.ContentTypes;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SyntaxColoring extends Object {
    /**
     *
     *
     */
    private SyntaxColoring() {
        super();
    }

    /**
     *
     *
     */
    public static String convertToHTML(String content, String contentType) {
        if (contentType.equals(ContentTypes.UNKNOWN_TEXT) || contentType.equals(ContentTypes.TEXT)) {
            return escape(content);
        } else {
            // Create an editor pane to syntax color the content
            JEditorPane tempPane = new JEditorPane();
            tempPane.setContentType(contentType);
            tempPane.setText(content);

            // "Print" the document to HTML
            org.netbeans.editor.BaseDocument doc = (org.netbeans.editor.BaseDocument) tempPane.getDocument();
            HtmlPrintContainer printContainer = new HtmlPrintContainer();
            doc.print(printContainer, false, true, 0, doc.getLength());

            return printContainer.toString();
        }
    }

    /**
     *
     *
     */
    public static String escape(String content) {
        content = StringTokenizer2.replace(content, "&", "&amp;");
        content = StringTokenizer2.replace(content, "\"", "&quot;");
        content = StringTokenizer2.replace(content, "<", "&lt;");
        content = StringTokenizer2.replace(content, ">", "&gt;");
        content = StringTokenizer2.replace(content, "\n", "<br>");
        content = StringTokenizer2.replace(content, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

        // We don't want to unilaterally convert spaces to nbsp's because it
        // screws up wrapping the chat
        StringBuffer buffer = new StringBuffer();
        String token = null;
        String lastToken = null;
        boolean foundSpace = false;

        final int NORMAL = 0;
        final int FOUND_SPACE = 1;
        final int FOUND_MULTIPLE_SPACES = 2;
        int state = NORMAL;

        final String SPACE = " ";
        final String NBSP = "&nbsp;";

        StringTokenizer tok = new StringTokenizer(content, " ", true);

        while (tok.hasMoreTokens()) {
            token = tok.nextToken();

            switch (state) {
            case NORMAL: {
                if (token.equals(SPACE)) {
                    state = FOUND_SPACE;
                } else {
                    buffer.append(token);
                }

                break;
            }

            case FOUND_SPACE: {
                if (token.equals(SPACE)) {
                    // Found another space, need to preserve whitespace
                    state = FOUND_MULTIPLE_SPACES;

                    // Convert spaces to nbsp's
                    buffer.append(SPACE); // The last token
                    buffer.append(NBSP); // This token
                } else {
                    // Found regular token, don't preserve excess whitespace
                    state = NORMAL;
                    buffer.append(SPACE); // The last token
                    buffer.append(token);
                }

                break;
            }

            case FOUND_MULTIPLE_SPACES: {
                if (token.equals(SPACE)) {
                    buffer.append(NBSP); // This token, converted to nbsp
                } else {
                    state = NORMAL;
                    buffer.append(token);
                }

                break;
            }
            }
        }

        content = buffer.toString();

        return content;
    }
}
