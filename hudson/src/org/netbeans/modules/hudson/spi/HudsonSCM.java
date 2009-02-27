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

package org.netbeans.modules.hudson.spi;

import java.io.File;
import java.util.List;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents one kind of SCM (version control) supported by the Hudson integration.
 * Registered to global lookup.
 */
public interface HudsonSCM {

    /**
     * Possibly recognizes a disk folder as being under version control.
     * @param folder a disk folder which may or may not be versioned
     * @return information about its versioning, or null if unrecognized
     */
    Configuration forFolder(File folder);

    /**
     * Information about how a folder (such as the basedir of a project) is versioned.
     */
    interface Configuration {

        /**
         * Creates configuration for Hudson.
         * Would typically append a {@code <scm>} element.
         * @param configXml Hudson's {@code config.xml}
         */
        void configure(Document configXml);

        // XXX should permit SCM to say that project is in a specific subdir

    }

    /**
     * Attempts to convert a path in a remote Hudson workspace to a local file path.
     * May use SCM information to guess at how these paths should be aligned.
     * @param job a Hudson job
     * @param workspacePath a relative path within the job's remote workspace, e.g. {@code src/p/C.java}
     * @param localRoot a local disk root to consider as a starting point
     * @return a file within {@code localRoot} corresponding to {@code workspacePath}, or null if unknown
     */
    String translateWorkspacePath(HudsonJob job, String workspacePath, File localRoot);

    /**
     * Attempts to parse a build's changelog.
     * @param changeSet the {@code <changeSet>} element from a build,
     *        corresponding to some {@code hudson.scm.ChangeLogSet}
     * @return a list of parsed changelog items, or null if the SCM is unrecognized
     */
    List<? extends HudsonJobChangeItem> parseChangeSet(Element changeSet);

    /**
     * Convenience methods for SCM implementations.
     */
    class Helper {

        private Helper() {}

        public static void addTrigger(Document configXml) {
            Element root = configXml.getDocumentElement();
            root.appendChild(configXml.createElement("triggers")). // XXX reuse existing <triggers> if found
                    appendChild(configXml.createElement("hudson.triggers.SCMTrigger")).
                    appendChild(configXml.createElement("spec")).
                    // XXX pretty arbitrary but seems like a decent first guess
                    appendChild(configXml.createTextNode("@hourly"));
        }

    }

}
