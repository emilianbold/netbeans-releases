/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.codecoverage;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProviderHelper;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.openide.filesystems.FileObject;

/**
 * @author Tomas Mysik
 */
public final class PhpCoverageProvider implements CoverageProvider {
    private static final Set<String> MIME_TYPES = Collections.singleton(PhpSourcePath.MIME_TYPE);

    private final PhpProject project;
    private Boolean enabled = null;

    public PhpCoverageProvider(PhpProject project) {
        assert project != null;

        this.project = project;
    }

    public boolean supportsHitCounts() {
        return true;
    }

    public boolean supportsAggregation() {
        return false;
    }

    public synchronized boolean isEnabled() {
        if (enabled == null) {
            enabled = CoverageProviderHelper.isEnabled(project);
        }
        return enabled;
    }

    public void setEnabled(boolean on) {
        if (enabled != null && on == enabled) {
            return;
        }
        enabled = on;
//        timestamp = 0;
//        if (!on) {
//            hitCounts = null;
//            fullNames = null;
//        }
        CoverageProviderHelper.setEnabled(project, on);
    }

    public synchronized boolean isAggregating() {
        throw new IllegalStateException("Aggregating is not supported");
    }

    public synchronized void setAggregating(boolean on) {
        throw new IllegalStateException("Aggregating is not supported");
    }

    public Set<String> getMimeTypes() {
        return MIME_TYPES;
    }

    public void clear() {
        PhpUnit.COVERAGE_LOG.delete();
    }

    public FileCoverageDetails getDetails(FileObject fo, Document doc) {
        return new PhpFileCoverageDetails(fo);
    }

    public List<FileCoverageSummary> getResults() {
        //new FileCoverageSummary(null, null, lineCount, executedLineCount, inferredCount, partialCount)
        return null;
    }
}
