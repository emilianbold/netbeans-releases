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
package org.netbeans.modules.gsf;

import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.HtmlFormatter;


/**
 *
 * @author Tor Norbye
 */
public class GsfHtmlFormatter extends HtmlFormatter {
    protected boolean isDeprecated;
    protected boolean isParameter;
    protected boolean isType;
    protected boolean isName;

    protected StringBuilder sb = new StringBuilder();

    public GsfHtmlFormatter() {
    }

    public void reset() {
        sb.setLength(0);
    }

    public void appendHtml(String html) {
        sb.append(html);
    }

    public void appendText(String text) {
        for (int i = 0, n = text.length(); i < n; i++) {
            char c = text.charAt(i);

            switch (c) {
            case '<':
                sb.append("&lt;"); // NOI18N

                break;

            case '>':
                sb.append("&gt;"); // NOI18N

                break;

            case '&':
                sb.append("&amp;"); // NOI18N

                break;

            case '"':
                sb.append("&quot;"); // NOI18N

                break;

            case '\'':
                sb.append("&apos;"); // NOI18N

                break;

            default:
                sb.append(c);
            }
        }
    }

    public void name(ElementKind kind, boolean start) {
        assert start != isName;
        isName = start;

        if (isName) {
            sb.append("<b>");
        } else {
            sb.append("</b>");
        }
    }

    public void parameters(boolean start) {
        assert start != isParameter;
        isParameter = start;

        if (isParameter) {
            sb.append("<font color=\"#808080\">");
        } else {
            sb.append("</font>");
        }
    }

    public void type(boolean start) {
        assert start != isType;
        isType = start;

        if (isType) {
            sb.append("<font color=\"#808080\">");
        } else {
            sb.append("</font>");
        }
    }

    public void deprecated(boolean start) {
        assert start != isDeprecated;
        isDeprecated = start;

        if (isDeprecated) {
            sb.append("<s>");
        } else {
            sb.append("</s>");
        }
    }

    public String getText() {
        assert !isParameter && !isDeprecated && !isName && !isType;

        return sb.toString();
    }
}
