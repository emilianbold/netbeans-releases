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

import java.util.ArrayList;
import java.util.List;

/**
 * Value objects for code coverage session.
 * @author Tomas Mysik
 */
public final class CoverageVO {
    private final List<FileVO> files = new ArrayList<FileVO>();
    private long generated = -1;
    private String phpUnitVersion;
    private CoverageMetricsVO metrics;

    public long getGenerated() {
        return generated;
    }

    public void setGenerated(long generated) {
        assert this.generated == -1;
        this.generated = generated;
    }

    public String getPhpUnitVersion() {
        return phpUnitVersion;
    }

    public void setPhpUnitVersion(String phpUnitVersion) {
        assert phpUnitVersion != null;
        assert this.phpUnitVersion == null;
        this.phpUnitVersion = phpUnitVersion;
    }

    public CoverageMetricsVO getMetrics() {
        return metrics;
    }

    public void setMetrics(CoverageMetricsVO metrics) {
        assert metrics != null;
        assert this.metrics == null;
        this.metrics = metrics;
    }

    public List<FileVO> getFiles() {
        return files;
    }

    public void addFile(FileVO file) {
        assert file != null;
        files.add(file);
    }

    public static final class FileVO {
        private final String path;
        private final List<ClassVO> classes = new ArrayList<ClassVO>();
        private final List<LineVO> lines = new ArrayList<LineVO>();
        private FileMetricsVO metrics;

        public FileVO(String path) {
            assert path != null;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public FileMetricsVO getMetrics() {
            return metrics;
        }

        public void setMetrics(FileMetricsVO metrics) {
            assert metrics != null;
            assert this.metrics == null;
            this.metrics = metrics;
        }

        public List<ClassVO> getClasses() {
            return classes;
        }

        public void addClass(ClassVO clazz) {
            assert clazz != null;
            classes.add(clazz);
        }

        public List<LineVO> getLines() {
            return lines;
        }

        public void addLine(LineVO line) {
            assert line != null;
            lines.add(line);
        }
    }

    public static final class ClassVO {
        private final String name;
        private final String namespace;
        private ClassMetricsVO metrics;

        public ClassVO(String name, String namespace) {
            assert name != null;
            assert namespace != null;

            this.name = name;
            this.namespace = namespace;
        }

        public String getName() {
            return name;
        }

        public String getNamespace() {
            return namespace;
        }

        public ClassMetricsVO getMetrics() {
            return metrics;
        }

        public void setMetrics(ClassMetricsVO metrics) {
            assert metrics != null;
            assert this.metrics == null;
            this.metrics = metrics;
        }

        @Override
        public String toString() {
            return String.format("ClassVO{name: %s, namespace: %s, classMetrics: %s}",
                    name, namespace, metrics);
        }
    }

    public static final class LineVO {
        public final int num;
        public final String type; // method / stmt / ???
        public final int count;

        public LineVO(int num, String type, int count) {
            this.num = num;
            this.type = type;
            this.count = count;
        }
    }

    public static class ClassMetricsVO {
        public final int methods;
        public final int coveredMethods;
        public final int statements;
        public final int coveredStatements;
        public final int elements;
        public final int coveredElements;

        public ClassMetricsVO(int methods, int coveredMethods, int statements, int coveredStatements, int elements, int coveredElements) {
            this.methods = methods;
            this.coveredMethods = coveredMethods;
            this.statements = statements;
            this.coveredStatements = coveredStatements;
            this.elements = elements;
            this.coveredElements = coveredElements;
        }

        @Override
        public String toString() {
            return String.format("ClassMetricsVO{methods: %d, coveredMethods: %d, statements: %d, coveredStatements: %d, "
                    + "elements: %d, coveredElements: %d}",
                    methods, coveredMethods, statements, coveredStatements, elements, coveredElements);
        }
    }

    public static class FileMetricsVO extends ClassMetricsVO {
        public final int loc;
        public final int ncloc;
        public final int classes;

        public FileMetricsVO(int loc, int ncloc, int classes, int methods, int coveredMethods,
                int statements, int coveredStatements, int elements, int coveredElements) {
            super(methods, coveredMethods, statements, coveredStatements, elements, coveredElements);

            this.loc = loc;
            this.ncloc = ncloc;
            this.classes = classes;
        }

        @Override
        public String toString() {
            return String.format("FileMetricsVO{loc: %d, ncloc: %d, classes: %d, methods: %d, coveredMethods: %d, "
                    + "statements: %d, coveredStatements: %d, elements: %d, coveredElements: %d}",
                    loc, ncloc, classes, methods, coveredMethods, statements, coveredStatements, elements, coveredElements);
        }
    }

    public static class CoverageMetricsVO extends FileMetricsVO {
        public final int files;

        public CoverageMetricsVO(int files, int loc, int ncloc, int classes, int methods, int coveredMethods,
                int statements, int coveredStatements, int elements, int coveredElements) {
            super(loc, ncloc, classes, methods, coveredMethods, statements, coveredStatements, elements, coveredElements);

            this.files = files;
        }

        @Override
        public String toString() {
            return String.format("CoverageMetricsVO{files: %d, loc: %d, ncloc: %d, classes: %d, methods: %d, coveredMethods: %d, " // NOI18N
                    + "statements: %d, coveredStatements: %d, elements: %d, coveredElements: %d}", // NOI18N
                    files, loc, ncloc, classes, methods, coveredMethods, statements, coveredStatements, elements, coveredElements);
        }
    }
}
