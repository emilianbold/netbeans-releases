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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.ClassMetricsVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.ClassVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.CoverageMetricsVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.FileMetricsVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.FileVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.LineVO;

/**
 * @author Tomas Mysik
 */
public class PhpUnitCoverageLogParserTest extends NbTestCase {

    public PhpUnitCoverageLogParserTest(String name) {
        super(name);
    }

    public void testParseLog() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getCoverageLog("phpunit-coverage.xml")));
        CoverageVO coverage = new CoverageVO();

        PhpUnitCoverageLogParser.parse(reader, coverage);

        assertEquals(1233352238, coverage.getGenerated());
        assertEquals("3.3.1", coverage.getPhpUnitVersion());
        assertEquals(2, coverage.getFiles().size());

        FileVO file = coverage.getFiles().get(0);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/src/hola/Calculator2.php5", file.getPath());
        assertEquals(2, file.getClasses().size());

        ClassVO clazz = file.getClasses().get(0);
        assertEquals("Calculator2", clazz.getName());
        assertEquals("global", clazz.getNamespace());

        ClassMetricsVO classMetrics = clazz.getMetrics();
        assertNotNull(classMetrics);
        assertEquals(11, classMetrics.methods);
        assertEquals(5, classMetrics.coveredMethods);
        assertEquals(3, classMetrics.statements);
        assertEquals(2, classMetrics.coveredStatements);
        assertEquals(7, classMetrics.elements);
        assertEquals(6, classMetrics.coveredElements);

        assertEquals(4, file.getLines().size());
        LineVO line = file.getLines().get(0);
        assertEquals(11, line.num);
        assertEquals("method", line.type);
        assertEquals(1, line.count);

        FileMetricsVO fileMetrics = file.getMetrics();
        assertNotNull(fileMetrics);
        assertEquals(32, fileMetrics.loc);
        assertEquals(18, fileMetrics.ncloc);
        assertEquals(4, fileMetrics.classes);
        assertEquals(2, fileMetrics.methods);
        assertEquals(1, fileMetrics.coveredMethods);
        assertEquals(5, fileMetrics.statements);
        assertEquals(3, fileMetrics.coveredStatements);
        assertEquals(43, fileMetrics.elements);
        assertEquals(25, fileMetrics.coveredElements);

        file = coverage.getFiles().get(1);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/src/Calculator.php", file.getPath());
        assertEquals(1, file.getClasses().size());
        assertEquals(6, file.getLines().size());

        line = file.getLines().get(0);
        assertEquals(10, line.num);
        assertEquals("method", line.type);
        assertEquals(3, line.count);
        line = file.getLines().get(5);
        assertEquals(19, line.num);
        assertEquals("stmt", line.type);
        assertEquals(0, line.count);

        CoverageMetricsVO coverageMetrics = coverage.getMetrics();
        assertNotNull(coverageMetrics);
        assertEquals(2, coverageMetrics.files);
        assertEquals(50, coverageMetrics.loc);
        assertEquals(30, coverageMetrics.ncloc);
        assertEquals(33, coverageMetrics.classes);
        assertEquals(1717, coverageMetrics.methods);
        assertEquals(665, coverageMetrics.coveredMethods);
        assertEquals(532, coverageMetrics.statements);
        assertEquals(443, coverageMetrics.coveredStatements);
        assertEquals(2344, coverageMetrics.elements);
        assertEquals(1234, coverageMetrics.coveredElements);
    }

    public void testParseLogIssue180254() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getCoverageLog("phpunit-coverage-issue180254.xml")));
        CoverageVO coverage = new CoverageVO();

        PhpUnitCoverageLogParser.parse(reader, coverage);

        assertEquals(1265274750, coverage.getGenerated());
        assertEquals("3.4.6", coverage.getPhpUnitVersion());
        assertEquals(20, coverage.getFiles().size());

        FileVO file = coverage.getFiles().get(0);
        assertEquals("/usr/local/zend/apache2/htdocs/mysgc/plugins/mcJobqueuePlugin/lib/jobhandler/McJobqueueTestjobHandler.php", file.getPath());
        assertEquals(1, file.getClasses().size());

        ClassVO clazz = file.getClasses().get(0);
        assertEquals("McJobqueueTestjobHandler", clazz.getName());
        assertEquals("global", clazz.getNamespace());

        ClassMetricsVO classMetrics = clazz.getMetrics();
        assertNotNull(classMetrics);
        assertEquals(1, classMetrics.methods);
        assertEquals(1, classMetrics.coveredMethods);
        assertEquals(2, classMetrics.statements);
        assertEquals(2, classMetrics.coveredStatements);
        assertEquals(3, classMetrics.elements);
        assertEquals(3, classMetrics.coveredElements);

        assertEquals(5, file.getLines().size());
        LineVO line = file.getLines().get(0);
        assertEquals(10, line.num);
        assertEquals("stmt", line.type);
        assertEquals(1, line.count);

        FileMetricsVO fileMetrics = file.getMetrics();
        assertNotNull(fileMetrics);
        assertEquals(13, fileMetrics.loc);
        assertEquals(7, fileMetrics.ncloc);
        assertEquals(1, fileMetrics.classes);
        assertEquals(1, fileMetrics.methods);
        assertEquals(1, fileMetrics.coveredMethods);
        assertEquals(4, fileMetrics.statements);
        assertEquals(4, fileMetrics.coveredStatements);
        assertEquals(5, fileMetrics.elements);
        assertEquals(5, fileMetrics.coveredElements);
    }


    private File getCoverageLog(String filename) throws Exception {
        File coverageLog = new File(getDataDir(), filename);
        assertTrue(coverageLog.isFile());
        return coverageLog;
    }
}
