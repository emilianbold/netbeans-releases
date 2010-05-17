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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.propertyeditors;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import javax.faces.component.UIComponent;

/**
 * An editor for string properties that represent URLs. URLs may be typed directly
 * in-line, or created in a custom editor (see {@link UrlPropertyPanel}). Note
 * that URLs should be either absolute (beginning with a protocol) or
 * context-relative (beginning with "/"). Characters not allowed in URLs are
 * converted to escape sequences when the URL property value is saved.
 *
 * A custom editor is also supplied, which presents a tree of project resources
 * to which links may be made. These links are created as context-relative URLs.
 * The custom editor may be configured by overriding a number of property editor
 * methods:
 *
 * <ul>
 * <li>To provide support for components that render URL anchors within a page.
 * If <code>isTargetComponent()</code> returns true for a component, the custom
 * editor will show the component as a target node within its containing page
 * node. If selected, it results in a URL with <pre>#</pre> plus whatever is
 * returned by <code>getTargetComponentName()</code> added at the end.
 * <li>To limit the types of files that may be selected. By default, the method
 * <code>getFileFilter()</code> returns null, which results in all files being
 * selectable as URL targets. If a file filter is returned, it will be used to
 * determine which files are shown. There is a convenience method for creating
 * file filters, <code>createFileFilter(String,String)</code>.
 * </ul>
 *
 * @author gjmurphy
 */
//TODO Utility methods encodeUrl() and decodeUrl() should convert non-ASCII chars to hex UTF8
public class UrlPropertyEditor extends PropertyEditorBase implements
        com.sun.rave.propertyeditors.UrlPropertyEditor {

    public boolean isEditableAsText() {
        return true;
    }

    protected String getPropertyHelpId() {
        return "projrave_ui_elements_propeditors_url_prop_ed";
    }

    public String getAsText() {
        Object value = super.getValue();
        if (value == null || value.equals(super.unsetValue))
            return "";
        return decodeUrl((String) value);
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) {
            super.setValue(null);
        } else if (text.trim().length() == 0) {
            super.setValue(super.unsetValue);
        } else {
            super.setValue(encodeUrl(text.trim()));
        }
    }

    public Component getCustomEditor() {
        return new UrlPropertyPanel(this);
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns true if the component specified will generate an HTML URL anchor
     * when rendered. By default, returns <code>false</code>. This method shoud
     * be overriden by implementing classes.
     */
    public boolean isTargetComponent(UIComponent component) {
        return false;
    }

    /**
     * Returns the value of the <code>name</code> attribute that the generated
     * HTML URL anchor will have. By default, returns the value of the component's
     * <code>id</code> property. This method is intended to be overriden by
     * implementing classes.
     */
    public String getTargetComponentName(UIComponent component) {
        return component.getId();
    }

    /**
     * Returns a file filter to use in determining which files are suitable for
     * selection by this URL editor. By default, returns null, to indicate that
     * all files are suitable.
     */
    public UrlFileFilter getFileFilter() {
        return null;
    }


    /**
     * Convert a file system path to a URL by converting unsafe characters into
     * numeric character entity references. The unsafe characters are listed in
     * in the IETF specification of URLs
     * (<a href="http://www.ietf.org/rfc/rfc1738.txt">RFC 1738</a>). Safe URL
     * characters are all printable ASCII characters, with the exception of the
     * space characters, '#', <', '>', '%', '[', ']', '{', '}', and '~'. This
     * method differs from {@link java.net.URLEncoder#encode(String)}, in that
     * it is intended for encoding the path portion of a URL, not the query
     * string. This method also attempts to recognize value binding expressions
     * within the string, as any sequence of characters matching the regular
     * expression {@code #{[^{]*}). Value binding expressions are <emph>not</emph>
     * escaped.
     */
    public static String encodeUrl(String url) {
        if (url == null || url.length() == 0)
            return url;
        StringBuffer buffer = new StringBuffer();
        String anchor = null;
        int index = url.lastIndexOf('#');
        if (index >= 0) {
            if (index == url.length() - 1 || !(url.charAt(index + 1) == '{' && url.lastIndexOf('}') > index)) {
                anchor = url.substring(index + 1);
                url = url.substring(0, index);
            }
        }
        char[] chars = url.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] <= '\u0020') {
                buffer.append('%');
                buffer.append(Integer.toHexString((int) chars[i]));
            } else {
                switch(chars[i]) {
                    case '\u0009': // Tab
                        buffer.append("%09");
                        break;
                    case '\u0020': // Space
                        buffer.append("%20");
                        break;
                    case '#':
                        if (i < chars.length - 1 && chars[i+1] == '{') {
                            // Pass over value binding expressions
                            int j = i + 2;
                            while (j < chars.length && chars[j] != '}')
                                j++;
                            if (j < chars.length && chars[j] == '}') {
                                buffer.append(chars, i, (j - i) + 1);
                                i = j;
                            } else {
                                buffer.append("%23");
                            }
                        } else {
                            buffer.append("%23");
                        }
                        break;
                    case '%':
                        buffer.append("%25");
                        break;
                    case '<':
                        buffer.append("%3C");
                        break;
                    case '>':
                        buffer.append("%3E");
                        break;
                    case '[':
                        buffer.append("%5B");
                        break;
                    case ']':
                        buffer.append("%5D");
                        break;
                    case '{':
                        buffer.append("%7B");
                        break;
                    case '}':
                        buffer.append("%7D");
                        break;
                    case '~':
                        buffer.append("%7E");
                        break;
                    default:
                        buffer.append(chars[i]);
                }
            }
        }
        if (anchor != null) {
            buffer.append('#');
            buffer.append(anchor);
        }
        if (buffer.length() == url.length())
            return url;
        return buffer.toString();
    }
    
    /**
     * Convert a URL to a file system path by intrepreting all two-character
     * sequences of the form <code>%xx</code> as a hexadecimal character
     * reference in UTF8.
     */
    public static String decodeUrl(String url) {
        // Optimized for case where URL is not encoded
        if (url == null || url.indexOf('%') == -1)
            return url;
        StringBuffer buffer = new StringBuffer();
        char[] chars = url.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%' && i + 2 < chars.length) {
                buffer.append((char) Integer.parseInt(String.copyValueOf(chars, i + 1, 2), 16));
                i += 2;
            } else {
                buffer.append(chars[i]);
            }
        }
        return buffer.toString();
    }
    
}
