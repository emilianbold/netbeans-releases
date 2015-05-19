/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel;

import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticImplementation.FixHintImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticSetImplementation;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMDiagnostic {

    /**
     * \brief Describes the severity of a particular diagnostic.
     */
    public enum Severity {

        Invalid(-1),
        /**
         * \brief A diagnostic that has been suppressed, e.g., by a command-line
         * option.
         */
        Ignored(0),
        /**
         * \brief This diagnostic is a note that should be attached to the
         * previous (non-note) diagnostic.
         */
        Note(1),
        /**
         * \brief This diagnostic indicates suspicious code that may not be
         * wrong.
         */
        Warning(2),
        /**
         * \brief This diagnostic indicates that the code is ill-formed.
         */
        Error(3),
        /**
         * \brief This diagnostic indicates that the code is ill-formed such
         * that future parser recovery is unlikely to produce useful results.
         */
        Fatal(4);

        //<editor-fold defaultstate="collapsed" desc="hidden">
        public static Severity valueOf(int val) {
            byte langVal = (byte) val;
            for (Severity sev : Severity.values()) {
                if (sev.value == langVal) {
                    return sev;
                }
            }
            assert false : "unsupported kind " + val;
            return Invalid;
        }

        private final byte value;

        private Severity(int lang) {
            this.value = (byte) lang;
        }

        public int getValue() {
            return value;
        }
        //</editor-fold>
    };

    /**
     * FixHints are described in terms of a source range whose contents should
     * be replaced by a string.
     *
     * This approach generalizes over three kinds of operations: removal of
     * source code (the range covers the code to be removed and the replacement
     * string is empty), replacement of source code (the range covers the code
     * to be replaced and the replacement string provides the new code), and
     * insertion (both the start and end of the range point at the insertion
     * location, and the replacement string provides the text to insert).
     *
     * Note that source ranges are half-open ranges [a, b), so the source code
     * should be replaced from a and up to (but not including) b.
     *
     */
    public static final class FixHint {

        public CMSourceRange getRange() {
            return CMSourceRange.fromImpl(impl.getRange());
        }

        public CharSequence getTextToInsert() {
            return impl.getTextToInsert();
        }

        //<editor-fold defaultstate="collapsed" desc="private">
        private final FixHintImplementation impl;

        private FixHint(FixHintImplementation impl) {
            assert impl != null;
            this.impl = impl;
        }

        /*package*/
        static FixHint fromImpl(FixHintImplementation impl) {
            // FIXME: it's worth to share File instances for the same impl
            return new FixHint(impl);
        }

        private static Iterable<FixHint> fromImpls(Iterable<FixHintImplementation> fixHints) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        /*package*/
        FixHintImplementation getImpl() {
            return impl;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + this.impl.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj instanceof FixHint) {
                return this.impl.equals(((FixHint) obj).impl);
            }
            return false;
        }

        @Override
        public String toString() {
            return "FixHint{" + impl + '}'; // NOI18N
        }
        //</editor-fold>
    }

    /**
     * \brief Determine the severity of the given diagnostic.
     *
     * @return severity
     */
    public Severity getSeverity() {
        return impl.getSeverity();
    }

    /**
     * \brief Retrieve the source location of the given diagnostic.
     *
     * This location is where to print the caret ('^') when displaying the
     * diagnostic on the command line.
     *
     * @return location
     */
    public CMSourceLocation getLocation() {
        return CMSourceLocation.fromImpl(impl.getLocation());
    }

    /**
     * \brief Retrieve the category for this diagnostic.
     *
     * Diagnostics can be categorized into groups along with other, related
     * diagnostics (e.g., diagnostics under the same warning flag). This routine
     * retrieves the category number for the given diagnostic.
     *
     * @return The category that contains this diagnostic. Zero-based category
     * means uncategorized).
     */
    public CMDiagnosticCategory getCategory() {
        return new CMDiagnosticCategory(impl.getCategory());
    }

    /**
     * \brief Format the given diagnostic in a manner that is suitable for
     * display.
     *
     * @return A new string containing for formatted diagnostic.
     */
    public CharSequence getFormattedText() {
        return impl.getFormattedText();
    }

    /**
     * \brief Retrieve the text of the given diagnostic.
     *
     * @return spelling
     */
    public CharSequence getSpelling() {
        return impl.getSpelling();
    }

    /**
     * \brief Retrieve the details for this diagnostic (if any).
     *
     * @return extra details
     */
    public CMDiagnosticSet getDetails() {
        CMDiagnosticSetImplementation details = impl.getDetails();
        return details == null ? null : new CMDiagnosticSet(details);
    }

    /**
     * \brief Retrieve source ranges associated with the diagnostic.
     *
     * A diagnostic's source ranges highlight important elements in the source
     * code. On the command line, Clang displays source ranges by underlining
     * them with '~' characters.
     *
     * @return the requested source ranges.
     */
    public Iterable<CMSourceRange> getHighlightRanges() {
        return CMSourceRange.fromImpls(impl.getHighlightRanges());
    }

    /**
     * \brief Retrieve the replacement information for diagnostic.
     *
     * @return hints
     */
    public Iterable<FixHint> getFixHints() {
        return FixHint.fromImpls(impl.getFixHints());
    }

    /**
     * \brief Retrieve the name of the command-line option that enabled this
     * diagnostic.
     *
     * @return A string that contains the command-line option used to enable
     * this warning, such as "-Wconversion" or "-pedantic".
     */
    public CharSequence getEnableOption() {
        return impl.getEnableOption();
    }

    /**
     * \brief Retrieve the name of the command-line option that disables this
     * diagnostic (if any).
     *
     * @return A string that contains the command-line option used to disable
     * this diagnostic.
     */
    public CharSequence getDisableOption() {
        return impl.getDisableOption();
    }

    //<editor-fold defaultstate="collapsed" desc="hidden">
    private final CMDiagnosticImplementation impl;

    private CMDiagnostic(CMDiagnosticImplementation impl) {
        this.impl = impl;
    }

    /*package*/
    CMDiagnosticImplementation getImpl() {
        return impl;
    }

    static Iterable<CMDiagnostic> fromImpls(Iterable<CMDiagnosticImplementation> impls) {
        return IterableFactory.convert(impls, CONV);
    }

    static CMDiagnostic fromImpl(CMDiagnosticImplementation impl) {
        return new CMDiagnostic(impl);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.impl.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof CMDiagnostic) {
            return this.impl.equals(((CMDiagnostic) obj).impl);
        }
        return false;
    }

    @Override
    public String toString() {
        return CMTraceUtils.toString(this);
    }

    private static final IterableFactory.Converter<CMDiagnosticImplementation, CMDiagnostic> CONV
            = new IterableFactory.Converter<CMDiagnosticImplementation, CMDiagnostic>() {

                @Override
                public CMDiagnostic convert(CMDiagnosticImplementation in) {
                    return fromImpl(in);
                }
            };
    //</editor-fold>
}
