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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class TokenAcceptor {

    private static final Map<String, TokenAcceptor> ACCEPTORS =
            new LinkedHashMap<String, TokenAcceptor> ();

    static {
        ACCEPTORS.put("resolution", new Resolution());
        ACCEPTORS.put("angle", new Angle());
        ACCEPTORS.put("percentage", new Percentage());
        ACCEPTORS.put("length", new Length());
        ACCEPTORS.put("hash_color_code", new HashColor());
        ACCEPTORS.put("string", new StringAcceptor());
        ACCEPTORS.put("non-negative-integer", new NonNegativeInteger());
        ACCEPTORS.put("integer", new Integer());
        ACCEPTORS.put("number", new Number());
        ACCEPTORS.put("identifier", new Identifier());
        ACCEPTORS.put("time", new Time());
        ACCEPTORS.put("date", new Date());
        ACCEPTORS.put("frequency", new Frequency());
        ACCEPTORS.put("semitones", new Semitones());
        ACCEPTORS.put("decibel", new Decibel());
        ACCEPTORS.put("relative-length", new RelativeLength());
        ACCEPTORS.put("uri", new Uri());
        ACCEPTORS.put("anything", new Anything()); 
    } //NOI18N

    public static TokenAcceptor getAcceptor(String name) {
        return ACCEPTORS.get(name.toLowerCase());
    }

    public abstract boolean accepts(Token token);
    
    private static class Resolution extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Arrays.asList(new String[]{"dpi", "dppx", "dpcm"}); //NOI18N

        @Override
        public List<String> postfixes() {
            return POSTFIXES;
        }
    }

    private static class Angle extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Arrays.asList(new String[]{"deg", "rad", "grad", "turn"}); //NOI18N

        @Override
        public List<String> postfixes() {
            return POSTFIXES;
        }
    }

    private static class Anything extends TokenAcceptor {

        @Override
        public boolean accepts(Token token) {
            return true;
        }
    }

    private static class Date extends TokenImageAcceptor {

        @Override
        public boolean accepts(String token) {
            try {
                DateFormat.getDateInstance().parse(token);
                return true;
            } catch (ParseException ex) {
                return false;
            }
        }
    }

    private static class Decibel extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Collections.singletonList("dB"); //NOI18N

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    private static class Frequency extends TokenImageAcceptor {

        @Override
        public boolean accepts(String token) {
            token = token.toLowerCase();
            String numberPart = token.endsWith("hz") ? token.substring(0, token.length() - 3) : token.endsWith("khz") ? token.substring(0, token.length() - 4) : null;
            if (numberPart != null) {
                try {
                    java.lang.Integer.parseInt(numberPart);
                    return true;
                } catch (NumberFormatException nfe) {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private static class HashColor extends TokenAcceptor {

        @Override
        public boolean accepts(Token token) {
            int len = token.image().length();
            return token.tokenId() == CssTokenId.HASH && (len == 4 || len == 7); //three of six hex digits after hash sign are allowed
        }
    }

    private static class Identifier extends TokenAcceptor {

        @Override
        public boolean accepts(Token token) {
            return token.tokenId() == CssTokenId.IDENT 
                    && !LexerUtils.equals("inherit",token.image(), true, true); //hack! XXX fix!!!
        }
    }

    private static class Integer extends TokenImageAcceptor {

        @Override
        public boolean accepts(String token) {
            try {
                java.lang.Integer.parseInt(token);
                return true;
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
    }

    private static class Length extends NumberPostfixAcceptor {

        /*
         *
         * relative units: em: the 'font-size' of the relevant font ex: the
         * 'x-height' of the relevant font px: pixels, relative to the viewing
         * device gd the grid defined by 'layout-grid' described in the CSS3
         * Text module [CSS3TEXT] rem the font size of the root element vw the
         * viewport's width vh the viewport's height vm the viewport's height or
         * width, whichever is smaller of the two ch The width of the "0" (ZERO,
         * U+0030) glyph found in the font for the font size used to render. If
         * the "0" glyph is not found in the font, the average character width
         * may be used. How is the "average character width" found?
         *
         * absolute units:
         *
         * in: inches -- 1 inch is equal to 2.54 centimeters. cm: centimeters
         * mm: millimeters pt: points -- the points used by CSS2 are equal to
         * 1/72th of an inch. pc: picas -- 1 pica is equal to 12 points.
         */
        private static final List<String> POSTFIXES = Arrays.asList(new String[]{"px", "ex", "em", "in", "gd", "rem", "vw", "vh", "vm", "ch", "cm", "mm", "pt", "pc"}); //NOI18N

        @Override
        public List<String> postfixes() {
            return POSTFIXES;
        }

        @Override
        public boolean accepts(String text) {
            boolean sa = super.accepts(text);
            if (!sa) {
                return "0".equals(text); //NOI18N
            } else {
                return sa;
            }
        }
    }

    private static class NonNegativeInteger extends TokenImageAcceptor {

        @Override
        public boolean accepts(String token) {
            try {
                int i = java.lang.Integer.parseInt(token);
                return i >= 0;
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
    }

    private static class Number extends TokenImageAcceptor {

        @Override
        public boolean accepts(String token) {
            try {
                Float.parseFloat(token);
                return true;
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
    }

    public abstract static class NumberPostfixAcceptor extends TokenImageAcceptor {

        protected abstract List<String> postfixes();

        @Override
        public boolean accepts(String image) {
            for (String postfix : postfixes()) {
                if (image.toLowerCase().endsWith(postfix.toLowerCase())) {
                    String numberImage = image.substring(0, image.length() - postfix.length());
                    try {
                        Float.parseFloat(numberImage);
                        return true;
                    } catch (NumberFormatException nfe) {
                        return false;
                    }
                }
            }
            return false;
        }
    }

    private static class Percentage extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Arrays.asList(new String[]{"%"}); //NOI18N

        @Override
        public List<String> postfixes() {
            return POSTFIXES;
        }
    }

    private static class RelativeLength extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Collections.singletonList("*"); //NOI18N

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    private static class Semitones extends NumberPostfixAcceptor {

        private static final List<String> POSTFIXES = Collections.singletonList("st"); //NOI18N

        @Override
        protected List<String> postfixes() {
            return POSTFIXES;
        }
    }

    private static class StringAcceptor extends TokenImageAcceptor {

        @Override
        public boolean accepts(String token) {
            if (token.length() < 2) {
                return false;
            }
            char first = token.charAt(0);
            char last = token.charAt(token.length() - 1);

            return (first == '\'' && last == '\'') || (first == '"' && last == '"');

        }
    }

    private static class Time extends TokenImageAcceptor {

        @Override
        public boolean accepts(String token) {
            token = token.toLowerCase();
            String numberPart = token.endsWith("ms") ? token.substring(0, token.length() - 2) : token.endsWith("s") ? token.substring(0, token.length() - 1) : null;
            if (numberPart != null) {
                try {
                    java.lang.Float.parseFloat(numberPart);
                    return true;
                } catch (NumberFormatException nfe) {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private static abstract class TokenImageAcceptor extends TokenAcceptor {

        public abstract boolean accepts(String valueImage);

        @Override
        public boolean accepts(Token token) {
            String tokenImage = token.image().toString();
            return accepts(tokenImage);
        }
    }

    private static class Uri extends TokenAcceptor {

        @Override
        public boolean accepts(Token token) {
            return token.tokenId() == CssTokenId.URI;
        }
    }
}
