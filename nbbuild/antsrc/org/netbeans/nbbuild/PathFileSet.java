/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.selectors.AbstractSelectorContainer;
import org.apache.tools.ant.types.selectors.AndSelector;
import org.apache.tools.ant.types.selectors.ContainsRegexpSelector;
import org.apache.tools.ant.types.selectors.ContainsSelector;
import org.apache.tools.ant.types.selectors.DateSelector;
import org.apache.tools.ant.types.selectors.DependSelector;
import org.apache.tools.ant.types.selectors.DepthSelector;
import org.apache.tools.ant.types.selectors.DifferentSelector;
import org.apache.tools.ant.types.selectors.ExtendSelector;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.apache.tools.ant.types.selectors.MajoritySelector;
import org.apache.tools.ant.types.selectors.NoneSelector;
import org.apache.tools.ant.types.selectors.NotSelector;
import org.apache.tools.ant.types.selectors.OrSelector;
import org.apache.tools.ant.types.selectors.PresentSelector;
import org.apache.tools.ant.types.selectors.SelectSelector;
import org.apache.tools.ant.types.selectors.SelectorContainer;
import org.apache.tools.ant.types.selectors.SizeSelector;
import org.apache.tools.ant.types.selectors.TypeSelector;
import org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector;

/**
 * Does the same as ant's fileset, only for given path instead of single basedir.
 * Files in set are returned as absolute paths.
 *
 * @author Richard Michalsky
 */
public class PathFileSet extends Task implements SelectorContainer {
    private Path clusterPath;
    private String property;
//    private String excludedModules;
    private String pathsep = File.pathSeparator;
    private String include;
    private AbstractSelectorContainer selectors = new AbstractSelectorContainer() {};

    public PathFileSet() {

    }

    /**
     * Returned fileset is stored in property specified with 'property' attribute.
     * @param property
     */
    public void setProperty(String property) {
        this.property = property;
    }

//    public void setExcludedModules(String excludedModules) {
//        this.excludedModules = excludedModules;
//    }

    /**
     * Separator of entries in fileset. Defaults to path separator.
     * @param pathsep
     */
    public void setPathsep(String pathsep) {
        this.pathsep = pathsep;
    }

    /**
     * Optional include pattern for filtering files. The same as fileset's nested &gt;include&lt; tag.
     * @param include
     */
    public void setInclude(String include) {
        this.include = include;
    }

    private List<Path> paths = new ArrayList<Path>();

    /**
     * Elements of nested paths are used as basedirs for fileset.
     * @param clusterPath
     */
    public void addPath(Path clusterPath) {
        paths.add(clusterPath);
    }

    @Override
    public void execute() throws BuildException {
        try {
            log("Scanning " + paths.size() + " paths.", Project.MSG_VERBOSE);
            DirectoryScanner scanner = new DirectoryScanner();
            if (paths.size() == 0)
                throw new BuildException("No path specified");
            if (property == null)
                throw new BuildException("No output property specified.");

            StringBuilder sb = new StringBuilder();

            for (Path path : paths) {
                String[] includedClusters = path.list();
                for (String clusterName : includedClusters) {
                    scanner.setBasedir(clusterName);
                    scanner.setSelectors(selectors.getSelectors(getProject()));
                    if (include != null)
                        scanner.setIncludes(new String[] { include });
                    scanner.scan();

                    log("Scanned pathelement '" + clusterName + "', "
                        + scanner.getIncludedFilesCount() + " files found.", Project.MSG_VERBOSE);
                    for (String relFile: scanner.getIncludedFiles()) {
                        if (sb.length() > 0)
                            sb.append(pathsep);
                        sb.append(new File(clusterName, relFile).getPath());
                    }
                }
            }
            getProject().setNewProperty(property, sb.toString());
            log("Set property " + property + " = " + sb.toString(), Project.MSG_VERBOSE);
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

    public Enumeration selectorElements() {
        return selectors.selectorElements();
    }

    public int selectorCount() {
        return selectors.selectorCount();
    }

    public boolean hasSelectors() {
        return selectors.hasSelectors();
    }

    public FileSelector[] getSelectors(Project p) {
        return selectors.getSelectors(p);
    }

    public void appendSelector(FileSelector selector) {
        selectors.appendSelector(selector);
    }

    public void addType(TypeSelector selector) {
        selectors.addType(selector);
    }

    public void addSize(SizeSelector selector) {
        selectors.addSize(selector);
    }

    public void addSelector(SelectSelector selector) {
        selectors.addSelector(selector);
    }

    public void addPresent(PresentSelector selector) {
        selectors.addPresent(selector);
    }

    public void addOr(OrSelector selector) {
        selectors.addOr(selector);
    }

    public void addNot(NotSelector selector) {
        selectors.addNot(selector);
    }

    public void addNone(NoneSelector selector) {
        selectors.addNone(selector);
    }

    public void addModified(ModifiedSelector selector) {
        selectors.addModified(selector);
    }

    public void addMajority(MajoritySelector selector) {
        selectors.addMajority(selector);
    }

    public void addFilename(FilenameSelector selector) {
        selectors.addFilename(selector);
    }

    public void addDifferent(DifferentSelector selector) {
        selectors.addDifferent(selector);
    }

    public void addDepth(DepthSelector selector) {
        selectors.addDepth(selector);
    }

    public void addDepend(DependSelector selector) {
        selectors.addDepend(selector);
    }

    public void addDate(DateSelector selector) {
        selectors.addDate(selector);
    }

    public void addCustom(ExtendSelector selector) {
        selectors.addCustom(selector);
    }

    public void addContainsRegexp(ContainsRegexpSelector selector) {
        selectors.addContainsRegexp(selector);
    }

    public void addContains(ContainsSelector selector) {
        selectors.addContains(selector);
    }

    public void addAnd(AndSelector selector) {
        selectors.addAnd(selector);
    }

    public void add(FileSelector selector) {
        selectors.add(selector);
    }

}
