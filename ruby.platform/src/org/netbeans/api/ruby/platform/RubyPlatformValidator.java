/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.api.ruby.platform;

import java.io.File;
import org.netbeans.modules.ruby.platform.Util;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Accompanied with every {@link RubyPlatform Ruby platform}. Able to check,
 * analyze, report, etc. possible problems of the platform.
 */
final class RubyPlatformValidator {

    private final RubyPlatform platform;

    RubyPlatformValidator(final RubyPlatform platform) {
        this.platform = platform;
    }

    boolean hasRubyGemsInstalled(boolean warn) {
        String problems = getRubyGemsProblems();
        if (problems != null && warn) {
            Util.notifyLocalized(RubyPlatform.class, "RubyPlatformValidator.DoesNotHaveRubyGems", // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE, platform.getLabel(), problems);
        }
        return problems == null;
    }

    /**
     * Return <tt>null</tt> if there are no problems running gem. Otherwise
     * return an error message which describes the problem.
     */
    String getRubyGemsProblems() {
        String gemTool = platform.getGemTool();

        if (gemTool == null) {
            return getGemMissingMessage();
        }

        String gemHome = platform.getInfo().getGemHome();
        if (gemHome == null) {
            // edge case, misconfiguration? gem tool is installed but repository is not found
            return NbBundle.getMessage(RubyPlatformValidator.class, "RubyPlatformValidator.CannotFindGemRepository");
        }

        File gemHomeF = new File(gemHome);

        if (!gemHomeF.isDirectory()) {
            // Is this possible? (Installing gems, but no gems installed yet
            return null;
        }

        return null;
    }

    boolean checkAndReportRubyGemsProblems() {
        String problems = getRubyGemsProblems();
        if (problems != null) {
            reportRubyGemsProblem();
        }
        return problems == null;
    }

    private String getGemMissingMessage() {
        if (Utilities.isMac() && "/usr/bin/ruby".equals(platform.getInterpreter())) { // NOI18N
            String version = System.getProperty("os.version"); // NOI18N
            if (version == null || version.startsWith("10.4")) { // Only a problem on Tiger // NOI18N
                return NbBundle.getMessage(RubyPlatformValidator.class, "RubyPlatformValidator.GemMissingMac");
            }
        }
        return NbBundle.getMessage(RubyPlatformValidator.class, "RubyPlatformValidator.GemMissing");
    }

    void reportRubyGemsProblem() {
        String problems = getRubyGemsProblems();
        assert problems != null : "cannot report RubyGems problems when there not any";
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                problems, NotifyDescriptor.Message.ERROR_MESSAGE));
    }
}
