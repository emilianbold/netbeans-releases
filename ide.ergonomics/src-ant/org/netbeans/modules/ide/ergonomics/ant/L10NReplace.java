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

package org.netbeans.modules.ide.ergonomics.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.apache.tools.ant.types.selectors.OrSelector;

/** Replace that handles localized files.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class L10NReplace extends MatchingTask {
    private File bundle;
    private List<Replacefilter> filters = new ArrayList<Replacefilter>();
    private File dir;

    public void setDir(File d) {
        this.dir = d;
    }

    public void setBundle(File f) {
        this.bundle = f;
    }

    public Replacefilter createReplacefilter() {
        final Replacefilter f = new Replacefilter();
        filters.add(f);
        return f;
    }

    @Override
    public void execute() throws BuildException {
        if (dir == null) {
            throw new BuildException("Root directory has to be provided");
        }
        if (bundle == null) {
            throw new BuildException("Bundle has to be provided");
        }

        DirectoryScanner ds = super.getDirectoryScanner(dir);
        String[] srcs = ds.getIncludedFiles();

        Map<String,OrSelector> l10n = new HashMap<String, OrSelector>();
        for (int i = 0; i < srcs.length; i++) {
            String noExt = srcs[i].replaceFirst("\\.[^\\.]*$", "");
            int index = noExt.indexOf("_");
            String suffix = index == -1 ? "" : noExt.substring(index + 1);
            log("suffix: " + suffix + " name: " + srcs[i], Project.MSG_VERBOSE);
            OrSelector or = l10n.get(suffix);
            if (or == null) {
                or = new OrSelector();
                l10n.put(suffix, or);
            }
            final FilenameSelector sel = new FilenameSelector();
            sel.setName(srcs[i]);
            or.addFilename(sel);
        }

        for (Map.Entry<String, OrSelector> entry : l10n.entrySet()) {
            Replace replace = new Replace();
            replace.setProject(getProject());
            replace.setDir(dir);
            File locBundle = localeVariant(bundle, entry.getKey());
            replace.setPropertyFile(locBundle);

            for (Replacefilter replacefilter : this.filters) {
                Replace.Replacefilter f = replace.createReplacefilter();
                f.setToken(replacefilter.token);
                f.setProperty(replacefilter.property);
            }

            replace.addOr(entry.getValue());

            replace.execute();
        }
    }
    static File localeVariant(File base, String locale) {
        if (locale.length() == 0) {
            return base;
        }
        String name = base.getName().replaceFirst("\\.", "_" + locale + ".");
        File f = new File(base.getParentFile(), name);
        if (f.exists()) {
            return f;
        } else {
            return base;
        }
    }

    public static final class Replacefilter {
        String token;
        String property;
        public void setToken(String t) {
            this.token = t;
        }
        public void setProperty(String p) {
            this.property = p;
        }
    } // End of Replacefilter
}
