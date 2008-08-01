/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.ruby.testrunner.ui;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.netbeans.modules.ruby.platform.execution.OutputProcessor;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.FileLocation;
import org.netbeans.modules.ruby.platform.execution.RegexpOutputRecognizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
final class OutputUtils {

    private static final Logger LOGGER = Logger.getLogger(OutputUtils.class.getName());
    
    private OutputUtils() {
    }

    /**
     */
    static void openCallstackFrame(Node node,
            String frameInfo) {

        List<? extends RegexpOutputRecognizer> stdRecognizers = RubyExecution.getStandardRubyRecognizers();
        Report report = getTestsuiteNode(node).getReport();
        for (RegexpOutputRecognizer each : stdRecognizers) {
            FileLocation location = each.processLine(frameInfo);
            if (location != null) {
                FileObject fo = findFile(location.file, report.getFileLocator());
                if (fo != null) {
                    OutputProcessor.open(fo, location.line);
                    return;
                } 
            }
        }

        LOGGER.info("Could not open a file for " + frameInfo) ;
    }

    private static TestsuiteNode getTestsuiteNode(Node node) {
        while (!(node instanceof TestsuiteNode)) {
            node = node.getParentNode();
        }
        return (TestsuiteNode) node;
    }

    // TODO: copied from OutputUtils, should introduce this as a utility method
    // in ruby.platform
    private static FileObject findFile(final String path, FileLocator fileLocator) {
        if (fileLocator != null) {
            FileObject fo = fileLocator.find(path);
            if (fo != null) {
                return fo;
            }
        }

        // Perhaps it's an absolute path of some sort... try to resolve those
        // Absolute path? Happens for stack traces in JRuby libraries and such
        File file = new File(path);
        if (file.isFile()) {
            return FileUtil.toFileObject(FileUtil.normalizeFile(file));
        } else {
            LOGGER.warning("Cannot resolve file for \"" + path + "\" path.");
            return null;
        }
    }

}
