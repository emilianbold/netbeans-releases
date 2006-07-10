/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
