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

package org.netbeans.modules.hudson.j2seproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory.Helper;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory.ProjectHudsonJobCreator;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JobCreator extends JPanel implements ProjectHudsonJobCreator {

    @ServiceProvider(service=ProjectHudsonJobCreatorFactory.class, position=200)
    public static class Factory implements ProjectHudsonJobCreatorFactory {
        public ProjectHudsonJobCreator forProject(Project project) {
            J2SEPropertyEvaluator eval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            if (eval == null) {
                return null;
            }
            return new JobCreator(project, eval);
        }
    }

    private final Project project;
    private final J2SEPropertyEvaluator eval;

    public JobCreator(Project project, J2SEPropertyEvaluator eval) {
        this.project = project;
        this.eval = eval;
        initComponents();
    }

    public String jobName() {
        return ProjectUtils.getInformation(project).getName();
    }

    public JComponent customizer() {
        return this;
    }

    public String error() {
        // XXX check uiProperties.getProject().getAntProjectHelper().isSharableProject()
        // (if false, try something like CustomizerLibraries.librariesBrowseActionPerformed)
        return null;
    }

    public void addChangeListener(ChangeListener listener) {}
    public void removeChangeListener(ChangeListener listener) {}

    public Document configure() throws IOException {
        File basedir = FileUtil.toFile(project.getProjectDirectory());
        Document doc = XMLUtil.createDocument("project", null, null, null);
        Element projectE = doc.getDocumentElement();
        List<String> targets = new ArrayList<String>();
        if (runTests.isSelected()) {
            targets.add("test");
        }
        if (buildJar.isSelected()) {
            targets.add("jar");
        }
        if (buildJavadoc.isSelected()) {
            targets.add("javadoc");
        }
        StringBuilder targetsS = new StringBuilder();
        for (String target : targets) {
            if (targetsS.length() > 0) {
                targetsS.append(' ');
            }
            targetsS.append(target);
        }
        projectE.appendChild(doc.createElement("builders")).
                appendChild(doc.createElement("hudson.tasks.Ant")).
                appendChild(doc.createElement("targets")).
                appendChild(doc.createTextNode(targetsS.toString()));
        Element publishers = (Element) projectE.appendChild(doc.createElement("publishers"));
        // XXX use appropriate properties from project evaluator where possible
        if (buildJar.isSelected()) {
            publishers.appendChild(doc.createElement("hudson.tasks.ArtifactArchiver")).
                    appendChild(doc.createElement("artifacts")).
                    // XXX consider including lib/ subdir too
                    appendChild(doc.createTextNode("dist/*.jar"));
        }
        if (buildJavadoc.isSelected()) {
            publishers.appendChild(doc.createElement("hudson.tasks.JavadocArchiver")).
                    appendChild(doc.createElement("javadocDir")).
                    appendChild(doc.createTextNode("dist/javadoc"));
        }
        if (runTests.isSelected()) {
            publishers.appendChild(doc.createElement("hudson.tasks.junit.JUnitResultArchiver")).
                    appendChild(doc.createElement("testResults")).
                    appendChild(doc.createTextNode("build/test/results/TEST-*.xml"));
        }
        for (String dummy : new String[] {"actions", "buildWrappers"}) {
            projectE.appendChild(doc.createElement(dummy));
        }
        Helper.addSCM(basedir, doc);
        return doc;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buildJar = new javax.swing.JCheckBox();
        buildJavadoc = new javax.swing.JCheckBox();
        runTests = new javax.swing.JCheckBox();

        buildJar.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(buildJar, org.openide.util.NbBundle.getMessage(JobCreator.class, "JobCreator.buildJar.text")); // NOI18N
        buildJar.setEnabled(false);

        buildJavadoc.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(buildJavadoc, org.openide.util.NbBundle.getMessage(JobCreator.class, "JobCreator.buildJavadoc.text")); // NOI18N

        runTests.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(runTests, org.openide.util.NbBundle.getMessage(JobCreator.class, "JobCreator.runTests.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(buildJar)
                    .add(buildJavadoc)
                    .add(runTests))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(buildJar)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buildJavadoc)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(runTests)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox buildJar;
    private javax.swing.JCheckBox buildJavadoc;
    private javax.swing.JCheckBox runTests;
    // End of variables declaration//GEN-END:variables

}
