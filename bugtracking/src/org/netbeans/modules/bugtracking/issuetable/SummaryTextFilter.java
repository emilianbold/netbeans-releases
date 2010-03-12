/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.issuetable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 * Shouldn't be used by clients
 *
 * @author Tomas Stupka
 */
final class SummaryTextFilter extends Filter {
    private boolean highlighting;
    private Pattern pattern;

    public SummaryTextFilter() {
        pattern = Pattern.compile("$^"); // NOI18N
    }

    public void setText(String text, boolean regular, boolean wholeWords, boolean matchCase) {
        if (!regular) {
            text = Pattern.quote(text);
            if (wholeWords) {
                text ="\\b"+ text +"\\b"; // NOI18N
            }
        }
        int flags = 0;
        if (!matchCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        try {
            pattern = Pattern.compile(text, flags);
        } catch (PatternSyntaxException psex) {
            String message = NbBundle.getMessage(SummaryTextFilter.class, "FindInQueryBar.invalidExpression"); // NOI18N
            StatusDisplayer.getDefault().setStatusText(message, StatusDisplayer.IMPORTANCE_FIND_OR_REPLACE);
        }
    }

    public void setHighlighting(boolean on) {
        highlighting = on;
    }

    @Override
    public String getDisplayName() {
        throw new IllegalStateException();
    }

    @Override
    public boolean accept(Issue issue) {
        return pattern.matcher(issue.getSummary()).find();
    }

    boolean isHighLightingOn() {
        return highlighting;
    }

    Pattern getPattern() {
        return pattern;
    }
}
