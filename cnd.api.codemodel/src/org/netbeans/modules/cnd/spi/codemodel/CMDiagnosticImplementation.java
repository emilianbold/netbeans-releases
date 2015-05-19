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
package org.netbeans.modules.cnd.spi.codemodel;

import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;

/**
 *
 * @author Vladimir Voskresensky
 */
public interface CMDiagnosticImplementation {

    /**
     * \brief Determine the severity of the given diagnostic.
     */
    CMDiagnostic.Severity getSeverity();

    /**
     * \brief Retrieve the source location of the given diagnostic.
     *
     * This location is where to print the caret ('^') when displaying the
     * diagnostic on the command line.
     */
    CMSourceLocationImplementation getLocation();

    /**
     * \brief Format the given diagnostic in a manner that is suitable for
     * display.
     *
     * @return A new string containing for formatted diagnostic.
     */
    CharSequence getFormattedText();

    /**
     * \brief Retrieve the text of the given diagnostic.
     */
    CharSequence getSpelling();

    /**
     * \brief Retrieve the details for this diagnostic (if any).
     */
    CMDiagnosticSetImplementation getDetails();

    /**
     * \brief Retrieve the name of the command-line option that enabled this
     * diagnostic.
     *
     * @return A string that contains the command-line option used to enable
     * this warning, such as "-Wconversion" or "-pedantic".
     */
    CharSequence getEnableOption();

    /**
     * \brief Retrieve the name of the command-line option that disables this
     * diagnostic (if any).
     *
     * @return A string that contains the command-line option used to disable
     * this diagnostic.
     */
    CharSequence getDisableOption();

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
    CMDiagnosticCategoryImplementation getCategory();

    /**
     * \brief Retrieve source ranges associated with the diagnostic.
     *
     * A diagnostic's source ranges highlight important elements in the source
     * code. On the command line, Clang displays source ranges by underlining
     * them with '~' characters.
     *
     * @return the requested source ranges.
     */
    public Iterable<CMSourceRangeImplementation> getHighlightRanges();

    /**
     * \brief Retrieve the replacement information for diagnostic.
     * @return 
     */
    public Iterable<FixHintImplementation> getFixHints();

    @Override
    boolean equals(Object other);

    @Override
    int hashCode();
    
    interface FixHintImplementation {

        public CharSequence getTextToInsert();

        public CMSourceRangeImplementation getRange();
        
        @Override
        boolean equals(Object other);

        @Override
        int hashCode();
    }
}
