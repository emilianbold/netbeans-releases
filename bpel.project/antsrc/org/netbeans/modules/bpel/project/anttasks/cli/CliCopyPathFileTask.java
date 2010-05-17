/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.project.anttasks.cli;

import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2010.01.15
 */
public class CliCopyPathFileTask extends Task {

    public void setNbprojectDirectory(String nbprojectDirectory) {
        myNbprojectDirectory = nbprojectDirectory;
    }

    @Override
    public void execute() throws BuildException {
        if (myNbprojectDirectory == null) {
            throw new BuildException("Folder nbproject must be set."); // NOI18N
        }
        try {
            RandomAccessFile file = new RandomAccessFile(myNbprojectDirectory + "/path.properties", "rw"); // NOI18N
            file.writeBytes("netbeans.classpath=\\" + LS); // NOI18N

            for (int i = 0; i < PATH.length - 1; i++) {
                file.writeBytes("${netbeans.dir}/" + PATH[i] + ":\\" + LS); // NOI18N
            }
            file.writeBytes("${netbeans.dir}/" + PATH[PATH.length - 1] + LS); // NOI18N
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("!!! DONE !!!: " + myNbprojectDirectory);
    }

    private String myNbprojectDirectory;
    private final String LS = System.getProperty ("line.separator"); // NOI18N

    private static final String[] PATH = new String [] {
      "ide/modules/org-apache-xml-resolver.jar",
      "ide/modules/org-netbeans-modules-project-ant.jar",
      "ide/modules/org-netbeans-modules-project-libraries.jar",
      "ide/modules/org-netbeans-modules-projectapi.jar",
      "ide/modules/org-netbeans-modules-projectuiapi.jar",
      "ide/modules/org-netbeans-modules-xml-catalog.jar",
      "ide/modules/org-netbeans-modules-xml-core.jar",
      "ide/modules/org-netbeans-modules-xml-xam.jar",
      "ide/modules/org-netbeans-modules-xml-schema-model.jar",
      "ide/modules/org-netbeans-modules-xml-wsdl-model.jar",
      "ide/modules/org-netbeans-modules-xml-retriever.jar",
      "ide/modules/org-netbeans-api-xml.jar",
      "ide/modules/ext/resolver-1.2.jar",
      "platform/core/core.jar",
      "platform/core/org-openide-filesystems.jar",
      "platform/lib/boot.jar",
      "platform/lib/org-openide-modules.jar",
      "platform/lib/org-openide-util.jar",
      "platform/lib/org-openide-util-lookup.jar",
      "platform/modules/org-openide-awt.jar",
      "platform/modules/org-openide-dialogs.jar",
      "platform/modules/org-openide-io.jar",
      "platform/modules/org-openide-loaders.jar",
      "platform/modules/org-openide-nodes.jar",
      "platform/modules/org-openide-text.jar",
      "platform/modules/org-openide-windows.jar",
      "platform/modules/org-netbeans-api-progress.jar",
      "platform/modules/org-netbeans-modules-editor-mimelookup.jar",
      "platform/modules/org-netbeans-modules-masterfs.jar",
      "platform/modules/org-netbeans-modules-queries.jar",
      "soa/modules/org-netbeans-modules-bpel-core.jar",
      "soa/modules/org-netbeans-modules-bpel-debugger-api.jar",
      "soa/modules/org-netbeans-modules-bpel-model.jar",
      "soa/modules/org-netbeans-modules-bpel-project.jar",
      "soa/modules/org-netbeans-modules-bpel-validation.jar",
      "soa/modules/org-netbeans-modules-soa-ui.jar",
      "soa/modules/org-netbeans-modules-compapp-projects-base.jar",
      "soa/ant/nblib/org-netbeans-modules-bpel-project.jar",
      "xml/modules/org-netbeans-modules-xml-catalogsupport.jar",
      "xml/modules/org-netbeans-modules-xml-misc.jar",
      "xml/modules/org-netbeans-modules-xml-wsdl-extensions.jar",
      "xml/modules/org-netbeans-modules-xml-xam-ui.jar",
      "xml/modules/org-netbeans-modules-xml-xpath.jar",
      "xml/modules/org-netbeans-modules-xml-xpath-ext.jar",
      "xml/modules/ext/jxpath/jxpath1.1.jar",
    };
}
