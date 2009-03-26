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
package org.netbeans.modules.cnd.makeproject.api;

import java.io.BufferedWriter;
import java.io.IOException;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;

public class DefaultMakefileWriter implements MakefileWriter {

    /**
     * Writes first section of generated makefile
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writePrelude(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writePrelude(confDescriptor, conf, bw);
    }

    /**
     * Writes main build target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeBuildTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeBuildTarget(confDescriptor, conf, bw);
    }

    /**
     * Writes all compile targets (only for managed projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeCompileTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeCompileTargets(confDescriptor, conf, bw);
    }

    /**
     * Writes link target (only for linked projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeLinkTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeLinkTarget(confDescriptor, conf, bw);
    }

    /**
     * Writes writes archive target (only for archive projects)
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeArchiveTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeArchiveTarget(confDescriptor, conf, bw);
    }

    /**
     * Writes target for unmanaged projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeMakefileTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeMakefileTarget(confDescriptor, conf, bw);
    }

    /**
     * Writes target for QT projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeQTTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeQTTarget(confDescriptor, conf, bw);
    }

    /**
     * Writes clan target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeCleanTarget(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeCleanTarget(confDescriptor, conf, bw);
    }

    /**
     * Writes targets for sub projects
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeSubProjectBuildTargets(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeSubProjectBuildTargets(confDescriptor, conf, bw);
    }

    /**
     * Writes dependency checking target
     *
     * @param confDescriptor  project configuration descriptor
     * @param conf  current project configuration
     * @param bw  output stream to generated makefile
     */
    public void writeDependencyChecking(MakeConfigurationDescriptor confDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        ConfigurationMakefileWriter.writeDependencyChecking(confDescriptor, conf, bw);
    }
}
