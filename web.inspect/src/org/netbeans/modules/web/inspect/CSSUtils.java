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
import org.netbeans.modules.web.inspect.actions.GoToElementSourceAction;
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
    /**
     * Common suffixes of 'compound' properties like background-position.
     * It would be nice to name all properties with these suffixes explicitly,
     * but let's use this heuristics for now.
     */
    private static final String[] propertySuffixes = new String[] {
        "-x", // NOI18N
        "-y" // NOI18N
    };
    /** All properties registered in/known to this class. */
    private static Map<String,CSSProperty> properties = new HashMap<String,CSSProperty>();
    
    static {
        // The following data comes from
        // http://www.w3.org/TR/CSS21/propidx.html
        registerProperty(new CSSProperty("azimuth", true)); // NOI18N
        registerProperty(new CSSProperty("background-attachment", false)); // NOI18N
        registerProperty(new CSSProperty("background-color", false)); // NOI18N
        registerProperty(new CSSProperty("background-image", false)); // NOI18N
        registerProperty(new CSSProperty("background-position", false)); // NOI18N
        registerProperty(new CSSProperty("background-repeat", false)); // NOI18N
        registerProperty(new CSSProperty("background", false)); // NOI18N
        registerProperty(new CSSProperty("border-collapse", true)); // NOI18N
        registerProperty(new CSSProperty("border-color", false)); // NOI18N
        registerProperty(new CSSProperty("border-spacing", true)); // NOI18N
        registerProperty(new CSSProperty("border-style", false)); // NOI18N
        registerProperty(new CSSProperty("border-top", false)); // NOI18N
        registerProperty(new CSSProperty("border-right", false)); // NOI18N
        registerProperty(new CSSProperty("border-bottom", false)); // NOI18N
        registerProperty(new CSSProperty("border-left", false)); // NOI18N
        registerProperty(new CSSProperty("border-top-color", false)); // NOI18N
        registerProperty(new CSSProperty("border-right-color", false)); // NOI18N
        registerProperty(new CSSProperty("border-bottom-color", false)); // NOI18N
        registerProperty(new CSSProperty("border-left-color", false)); // NOI18N
        registerProperty(new CSSProperty("border-top-style", false)); // NOI18N
        registerProperty(new CSSProperty("border-right-style", false)); // NOI18N
        registerProperty(new CSSProperty("border-bottom-style", false)); // NOI18N
        registerProperty(new CSSProperty("border-left-style", false)); // NOI18N
        registerProperty(new CSSProperty("border-top-width", false)); // NOI18N
        registerProperty(new CSSProperty("border-right-width", false)); // NOI18N
        registerProperty(new CSSProperty("border-bottom-width", false)); // NOI18N
        registerProperty(new CSSProperty("border-left-width", false)); // NOI18N
        registerProperty(new CSSProperty("border-width", false)); // NOI18N
        registerProperty(new CSSProperty("border", false)); // NOI18N
        registerProperty(new CSSProperty("bottom", false)); // NOI18N
        registerProperty(new CSSProperty("caption-side", true)); // NOI18N
        registerProperty(new CSSProperty("clear", false)); // NOI18N
        registerProperty(new CSSProperty("clip", false)); // NOI18N
        registerProperty(new CSSProperty("color", true)); // NOI18N
        registerProperty(new CSSProperty("content", false)); // NOI18N
        registerProperty(new CSSProperty("counter-increment", false)); // NOI18N
        registerProperty(new CSSProperty("counter-reset", false)); // NOI18N
        registerProperty(new CSSProperty("cue-after", false)); // NOI18N
        registerProperty(new CSSProperty("cue-before", false)); // NOI18N
        registerProperty(new CSSProperty("cue", false)); // NOI18N
        registerProperty(new CSSProperty("cursor", true)); // NOI18N
        registerProperty(new CSSProperty("direction", true)); // NOI18N
        registerProperty(new CSSProperty("display", false)); // NOI18N
        registerProperty(new CSSProperty("elevation", true)); // NOI18N
        registerProperty(new CSSProperty("empty-cells", true)); // NOI18N
        registerProperty(new CSSProperty("float", false)); // NOI18N
        registerProperty(new CSSProperty("font-family", true)); // NOI18N
        registerProperty(new CSSProperty("font-size", true)); // NOI18N
        registerProperty(new CSSProperty("font-style", true)); // NOI18N
        registerProperty(new CSSProperty("font-variant", true)); // NOI18N
        registerProperty(new CSSProperty("font-weight", true)); // NOI18N
        registerProperty(new CSSProperty("font", true)); // NOI18N
        registerProperty(new CSSProperty("height", false)); // NOI18N
        registerProperty(new CSSProperty("left", false)); // NOI18N
        registerProperty(new CSSProperty("letter-spacing", true)); // NOI18N
        registerProperty(new CSSProperty("line-height", true)); // NOI18N
        registerProperty(new CSSProperty("list-style-image", true)); // NOI18N
        registerProperty(new CSSProperty("list-style-position", true)); // NOI18N
        registerProperty(new CSSProperty("list-style-type", true)); // NOI18N
        registerProperty(new CSSProperty("list-style", true)); // NOI18N
        registerProperty(new CSSProperty("margin-right", false)); // NOI18N
        registerProperty(new CSSProperty("margin-left", false)); // NOI18N
        registerProperty(new CSSProperty("margin-top", false)); // NOI18N
        registerProperty(new CSSProperty("margin-bottom", false)); // NOI18N
        registerProperty(new CSSProperty("margin", false)); // NOI18N
        registerProperty(new CSSProperty("max-height", false)); // NOI18N
        registerProperty(new CSSProperty("max-width", false)); // NOI18N
        registerProperty(new CSSProperty("min-height", false)); // NOI18N
        registerProperty(new CSSProperty("min-width", false)); // NOI18N
        registerProperty(new CSSProperty("orphans", true)); // NOI18N
        registerProperty(new CSSProperty("outline-color", false)); // NOI18N
        registerProperty(new CSSProperty("outline-style", false)); // NOI18N
        registerProperty(new CSSProperty("outline-width", false)); // NOI18N
        registerProperty(new CSSProperty("outline", false)); // NOI18N
        registerProperty(new CSSProperty("overflow", false)); // NOI18N
        registerProperty(new CSSProperty("padding-top", false)); // NOI18N
        registerProperty(new CSSProperty("padding-right", false)); // NOI18N
        registerProperty(new CSSProperty("padding-bottom", false)); // NOI18N
        registerProperty(new CSSProperty("padding-left", false)); // NOI18N
        registerProperty(new CSSProperty("padding", false)); // NOI18N
        registerProperty(new CSSProperty("page-break-after", false)); // NOI18N
        registerProperty(new CSSProperty("page-break-before", false)); // NOI18N
        registerProperty(new CSSProperty("page-break-inside", false)); // NOI18N
        registerProperty(new CSSProperty("pause-after", false)); // NOI18N
        registerProperty(new CSSProperty("pause-before", false)); // NOI18N
        registerProperty(new CSSProperty("pause", false)); // NOI18N
        registerProperty(new CSSProperty("pitch-range", true)); // NOI18N
        registerProperty(new CSSProperty("pitch", true)); // NOI18N
        registerProperty(new CSSProperty("play-during", false)); // NOI18N
        registerProperty(new CSSProperty("position", false)); // NOI18N
        registerProperty(new CSSProperty("quotes", true)); // NOI18N
        registerProperty(new CSSProperty("richness", true)); // NOI18N
        registerProperty(new CSSProperty("right", false)); // NOI18N
        registerProperty(new CSSProperty("speak-header", true)); // NOI18N
        registerProperty(new CSSProperty("speak-numeral", true)); // NOI18N
        registerProperty(new CSSProperty("speak-punctuation", true)); // NOI18N
        registerProperty(new CSSProperty("speak", true)); // NOI18N
        registerProperty(new CSSProperty("speech-rate", true)); // NOI18N
        registerProperty(new CSSProperty("stress", true)); // NOI18N
        registerProperty(new CSSProperty("table-layout", false)); // NOI18N
        registerProperty(new CSSProperty("text-align", true)); // NOI18N
        registerProperty(new CSSProperty("text-decoration", false)); // NOI18N
        registerProperty(new CSSProperty("text-indent", true)); // NOI18N
        registerProperty(new CSSProperty("text-transform", true)); // NOI18N
        registerProperty(new CSSProperty("top", false)); // NOI18N
        registerProperty(new CSSProperty("unicode-bidi", false)); // NOI18N
        registerProperty(new CSSProperty("vertical-align", false)); // NOI18N
        registerProperty(new CSSProperty("visibility", true)); // NOI18N
        registerProperty(new CSSProperty("voice-family", true)); // NOI18N
        registerProperty(new CSSProperty("volume", true)); // NOI18N
        registerProperty(new CSSProperty("white-space", true)); // NOI18N
        registerProperty(new CSSProperty("widows", true)); // NOI18N
        registerProperty(new CSSProperty("width", false)); // NOI18N
        registerProperty(new CSSProperty("word-spacing", true)); // NOI18N
        registerProperty(new CSSProperty("z-index", false)); // NOI18N
        // http://www.w3.org/TR/css3-color/
        registerProperty(new CSSProperty("opacity", false)); // NOI18N
        // http://www.w3.org/TR/css3-background/
        registerProperty(new CSSProperty("background-clip", false)); // NOI18N
        registerProperty(new CSSProperty("background-origin", false)); // NOI18N
        registerProperty(new CSSProperty("background-size", false)); // NOI18N
        registerProperty(new CSSProperty("border-image", false)); // NOI18N
        registerProperty(new CSSProperty("border-image-outset", false)); // NOI18N
        registerProperty(new CSSProperty("border-image-repeat", false)); // NOI18N
        registerProperty(new CSSProperty("border-image-slice", false)); // NOI18N
        registerProperty(new CSSProperty("border-image-source", false)); // NOI18N
        registerProperty(new CSSProperty("border-image-width", false)); // NOI18N
        registerProperty(new CSSProperty("border-radius", false)); // NOI18N
        registerProperty(new CSSProperty("border-top-right-radius", false)); // NOI18N
        registerProperty(new CSSProperty("border-bottom-right-radius", false)); // NOI18N
        registerProperty(new CSSProperty("border-bottom-left-radius", false)); // NOI18N
        registerProperty(new CSSProperty("border-top-left-radius", false)); // NOI18N
        registerProperty(new CSSProperty("box-decoration-break", false)); // NOI18N
        registerProperty(new CSSProperty("box-shadow", false)); // NOI18N
        // http://www.w3.org/TR/css3-marquee/
        registerProperty(new CSSProperty("marquee-direction", true)); // NOI18N
        registerProperty(new CSSProperty("marquee-play-count", false)); // NOI18N
        registerProperty(new CSSProperty("marquee-speed", false)); // NOI18N
        registerProperty(new CSSProperty("marquee-style", false)); // NOI18N
        registerProperty(new CSSProperty("overflow-style", true)); // NOI18N
        // http://www.w3.org/TR/css3-multicol/
        registerProperty(new CSSProperty("break-after", false)); // NOI18N
        registerProperty(new CSSProperty("break-before", false)); // NOI18N
        registerProperty(new CSSProperty("break-inside", false)); // NOI18N
        registerProperty(new CSSProperty("column-count", false)); // NOI18N
        registerProperty(new CSSProperty("column-fill", false)); // NOI18N
        registerProperty(new CSSProperty("column-gap", false)); // NOI18N
        registerProperty(new CSSProperty("column-rule", false)); // NOI18N
        registerProperty(new CSSProperty("column-rule-color", false)); // NOI18N
        registerProperty(new CSSProperty("column-rule-style", false)); // NOI18N
        registerProperty(new CSSProperty("column-rule-width", false)); // NOI18N
        registerProperty(new CSSProperty("columns", false)); // NOI18N
        registerProperty(new CSSProperty("column-span", false)); // NOI18N
        registerProperty(new CSSProperty("column-width", false)); // NOI18N
        // http://www.w3.org/TR/css3-ui/
        registerProperty(new CSSProperty("box-sizing", false)); // NOI18N
        registerProperty(new CSSProperty("icon", false)); // NOI18N
        registerProperty(new CSSProperty("ime-mode", false)); // NOI18N
        registerProperty(new CSSProperty("nav-index", false)); // NOI18N
        registerProperty(new CSSProperty("nav-up", false)); // NOI18N
        registerProperty(new CSSProperty("nav-right", false)); // NOI18N
        registerProperty(new CSSProperty("nav-down", false)); // NOI18N
        registerProperty(new CSSProperty("nav-left", false)); // NOI18N
        registerProperty(new CSSProperty("outline-offset", false)); // NOI18N
        registerProperty(new CSSProperty("resize", false)); // NOI18N
        registerProperty(new CSSProperty("text-overflow", false)); // NOI18N
        // https://developer.mozilla.org/en/CSS/-moz-border-top-colors
        registerProperty(new CSSProperty("border-top-colors", false)); // NOI18N
        registerProperty(new CSSProperty("border-right-colors", false)); // NOI18N
        registerProperty(new CSSProperty("border-bottom-colors", false)); // NOI18N
        registerProperty(new CSSProperty("border-left-colors", false)); // NOI18N
    }

    /**
     * Registers a CSS property.
     * 
     * @param property property to register.
     */
    private static void registerProperty(CSSProperty property) {
        String name = property.getName();
        if (properties.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate registration of property: "+name); // NOI18N
        }
        properties.put(name, property);
    }

    /**
     * Determines whether the CSS property with the specified name is known to this class.
     * 
     * @param name name of the property.
     * @return {@code true} when the property meta-data are known by this class,
     * returns {@code false} otherwise.
     */
    public static boolean isKnownProperty(String name) {
        for (String propertyName : possiblePropertyNames(name)) {
            if (properties.containsKey(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the CSS property with the specified name is inherited.
     * 
     * @param name name of the property.
     * @return {@code true} when the property is inherited,
     * returns {@code false} otherwise.
     */
    public static boolean isInheritedProperty(String name) {
        for (String propertyName : possiblePropertyNames(name)) {
            CSSProperty property = properties.get(propertyName);
            if (property != null) {
                return property.isInherited();
            }
        }
        throw new IllegalArgumentException("Unknown property: " + name); // NOI18N
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
        String withoutPrefix = null;
        
        // -moz-color => color
        for (String prefix : vendorPropertyPrefixes) {
            if (name.startsWith(prefix)) {
                withoutPrefix = name.substring(prefix.length());
                names.add(withoutPrefix);
            }
        }

        // background-position-x => background-position
        for (String suffix : propertySuffixes) {
            if (name.endsWith(suffix)) {
                names.add(name.substring(0,name.length()-suffix.length()));
            }
            if ((withoutPrefix != null) && (withoutPrefix.endsWith(suffix))) {
                names.add(withoutPrefix.substring(0,withoutPrefix.length()-suffix.length()));
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
                            GoToElementSourceAction.class,
                            "GoToElementSourceAction.question"); // NOI18N
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
            Logger.getLogger(GoToElementSourceAction.class.getName()).log(Level.INFO, null, ioe);
        }

        return false;
    }
    
    /**
     * Descriptor of a CSS property.
     */
    private static class CSSProperty {
        /** Name of this property. */
        private String name;
        /** Determines whether this property is inherited. */
        private boolean inherited;

        /**
         * Creates a new {@code CSSProperty}.
         * 
         * @param name name of the property.
         * @param inherited specifies whether the property is inherited.
         */
        CSSProperty(String name, boolean inherited) {
            this.name = name;
            this.inherited = inherited;
        }

        /**
         * Returns the name of this property.
         * 
         * @return name of the property.
         */
        public String getName() {
            return name;
        }

        /**
         * Determines whether this property is inherited.
         * 
         * @return {@code true} when the property is inherited,
         * returns {@code false} otherwise.
         */
        public boolean isInherited() {
            return inherited;
        }
    }
    
}
