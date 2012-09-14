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
package org.netbeans.modules.web.inspect;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/**
 * CSS-related utility methods.
 *
 * @author Jan Stola
 */
public class CSSUtils {
    /** Prefixes of CSS properties used by various vendors. */
    private static final String[] vendorPropertyPrefixes = new String[] {
        "-moz-", // NOI18N
        "-webkit-", // NOI18N
        "-ms-", // NOI18N
        "-o-" // NOI18N
    };

    private static final List<String> inheritedProperties = Arrays.asList(new String[] {
        "azimuth", // NOI18N
        "border-collapse", // NOI18N
        "border-spacing", // NOI18N
        "caption-side", // NOI18N
        "color", // NOI18N
        "cursor", // NOI18N
        "direction", // NOI18N
        "elevation", // NOI18N
        "empty-cells", // NOI18N
        "font-family", // NOI18N
        "font-size", // NOI18N
        "font-style", // NOI18N
        "font-variant", // NOI18N
        "font-weight", // NOI18N
        "font", // NOI18N
        "letter-spacing", // NOI18N
        "line-height", // NOI18N
        "list-style-image", // NOI18N
        "list-style-position", // NOI18N
        "list-style-type", // NOI18N
        "list-style", // NOI18N
        "orphans", // NOI18N
        "pitch-range", // NOI18N
        "pitch", // NOI18N
        "quotes", // NOI18N
        "richness", // NOI18N
        "speak-header", // NOI18N
        "speak-numeral", // NOI18N
        "speak-punctuation", // NOI18N
        "speak", // NOI18N
        "speech-rate", // NOI18N
        "stress", // NOI18N
        "text-align", // NOI18N
        "text-indent", // NOI18N
        "text-transform", // NOI18N
        "text-shadow", // NOI18N
        "visibility", // NOI18N
        "voice-family", // NOI18N
        "volume", // NOI18N
        "white-space", // NOI18N
        "widows", // NOI18N
        "word-spacing" // NOI18N
    });

    /**
     * Determines whether the CSS property with the specified name is inherited.
     * 
     * @param name name of the property.
     * @return {@code true} when the property is inherited,
     * returns {@code false} otherwise.
     */
    public static boolean isInheritedProperty(String name) {
        for (String propertyName : possiblePropertyNames(name)) {
            if (inheritedProperties.contains(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns possible known (base) names of the given property
     * (the returned list contains all candidates with
     * possible prefixes/suffixes removed).
     * 
     * @param name name of the property.
     * @return list of possible (base) names of the given property.
     */
    private static List<String> possiblePropertyNames(String name) {
        List<String> names = new ArrayList<String>();
        names.add(name);
        
        // -moz-color => color
        for (String prefix : vendorPropertyPrefixes) {
            if (name.startsWith(prefix)) {
                String withoutPrefix = name.substring(prefix.length());
                names.add(withoutPrefix);
            }
        }

        return names;
    }

    /**
     * Determines whether the specified CSS value means that the actual
     * value should be inherited from the parent.
     * 
     * @param value value to check.
     * @return {@code true} if the actual value should be inherited,
     * returns {@code false} otherwise.
     */
    public static boolean isInheritValue(String value) {
        return value.trim().startsWith("inherit"); // NOI18N
    }

    /**
     * Opens the specified file at the given offset. This method has been
     * copied (with minor modifications) from UiUtils class in csl.api module.
     * This method is not CSS-specific. It was placed into this file just
     * because there was no better place.
     * 
     * @param fob file that should be opened.
     * @param offset offset where the caret should be placed.
     * @return {@code true} when the file was opened successfully,
     * returns {@code false} otherwise.
     */
    public static boolean open(FileObject fob, int offset) {
        try {
            DataObject dob = DataObject.find(fob);
            Lookup dobLookup = dob.getLookup();
            EditorCookie ec = dobLookup.lookup(EditorCookie.class);
            LineCookie lc = dobLookup.lookup(LineCookie.class);
            OpenCookie oc = dobLookup.lookup(OpenCookie.class);

            if ((ec != null) && (lc != null) && (offset != -1)) {
                StyledDocument doc;
                try {
                    doc = ec.openDocument();
                } catch (UserQuestionException uqe) {
                    String title = NbBundle.getMessage(
                            CSSUtils.class,
                            "CSSUtils.openQuestion"); // NOI18N
                    Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                            uqe.getLocalizedMessage(),
                            title,
                            NotifyDescriptor.YES_NO_OPTION));
                    if (value != NotifyDescriptor.YES_OPTION) {
                        return false;
                    }
                    uqe.confirmed();
                    doc = ec.openDocument();
                }

                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;
                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);
                        if (l != null) {
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, column);
                            return true;
                        }
                    }
                }
            }

            if (oc != null) {
                oc.open();
                return true;
            }
        } catch (IOException ioe) {
            Logger.getLogger(CSSUtils.class.getName()).log(Level.INFO, null, ioe);
        }

        return false;
    }

    /**
     * Returns an unspecified "normalized" version of the selector suitable
     * for {@code String} comparison with other normalized selectors.
     *
     * @param selector selector to normalize.
     * @return "normalized" version of the selector.
     */
    public static String normalizeSelector(String selector) {
        // Hack that simplifies the following cycle: adding a dummy
        // character that ensures that the last group is ended.
        // This character is removed at the end of this method.
        selector += 'A';
        String whitespaceChars = " \t\n\r\f"; // NOI18N
        String specialChars = ".>+~#:*()[]|,"; // NOI18N
        StringBuilder main = new StringBuilder();
        StringBuilder group = null;
        for (int i=0; i<selector.length(); i++) {
            char c = selector.charAt(i);
            boolean whitespace = (whitespaceChars.indexOf(c) != -1);
            boolean special = (specialChars.indexOf(c) != -1);
            if (whitespace || special) {
                if (group == null) {
                    group = new StringBuilder();
                }
                if (special) {
                    group.append(c);
                }
            } else {
                if (group != null) {
                    if (group.length() == 0) {
                        // whitespace only group => insert single space instead
                        main.append(' ');
                    } else {
                        // group with special chars
                        main.append(group);
                    }
                    group = null;
                }
                main.append(c);
            }
        }
        // Removing the dummy character added at the beginning of the method
        return main.substring(0, main.length()-1).trim();
    }

    /**
     * Returns an unspecified "normalized" version of the media query suitable
     * for {@code String} comparison with other normalized media queries.
     *
     * @param mediaQueryList media query list to normalize.
     * @return "normalized" version of the media query.
     */
    public static String normalizeMediaQuery(String mediaQueryList) {
        mediaQueryList = mediaQueryList.trim().toLowerCase();
        StringBuilder result = new StringBuilder();
        StringTokenizer st = new StringTokenizer(mediaQueryList, ","); // NOI18N
        while (st.hasMoreTokens()) {
            String mediaQuery = st.nextToken();
            int index;
            List<String> parts = new ArrayList<String>();
            while ((index = mediaQuery.indexOf("and")) != -1) { // NOI18N
                String part = mediaQuery.substring(0,index);
                mediaQuery = mediaQuery.substring(index+3);
                parts.add(part);
            }
            parts.add(mediaQuery);
            Collections.sort(parts);
            Collections.reverse(parts); // Make sure that media type is before expressions
            for (int i=0; i<parts.size(); i++) {
                if (i != 0) {
                    result.append(" and "); // NOI18N
                }
                String part = parts.get(i);
                // 'part' is not a selector, but the same normalization
                // works well here as well.
                part = normalizeSelector(part);
                result.append(part);
            }
            if (st.hasMoreTokens()) {
                result.append(", "); // NOI18N
            }
        }
        return result.toString();
    }

}
