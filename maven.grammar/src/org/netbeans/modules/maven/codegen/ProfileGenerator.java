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
package org.netbeans.modules.maven.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.maven.model.pom.Activation;
import org.netbeans.modules.maven.model.pom.ActivationFile;
import org.netbeans.modules.maven.model.pom.ActivationOS;
import org.netbeans.modules.maven.model.pom.ActivationProperty;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Milos Kleint
 */
public class ProfileGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> toRet = new ArrayList<CodeGenerator>();
            POMModel model = context.lookup(POMModel.class);
            JTextComponent component = context.lookup(JTextComponent.class);
            if (model != null) {
                toRet.add(new ProfileGenerator(model, component));
            }
            return toRet;
        }
    }

    private POMModel model;
    private JTextComponent component;
    
    /** Creates a new instance of ProfileGenerator */
    private ProfileGenerator(POMModel model, JTextComponent component) {
        this.model = model;
        this.component = component;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ProfileGenerator.class, "NAME_Profile");
    }

    public void invoke() {
        try {
            model.sync();
        } catch (IOException ex) {
            Logger.getLogger(ProfileGenerator.class.getName()).log(Level.INFO, "Error while syncing the editor document with model for pom.xml file", ex); //NOI18N
        }
        if (!model.getState().equals(State.VALID)) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ProfileGenerator.class, "MSG_Cannot_Parse"));
            return;
        }
        NewProfilePanel panel = new NewProfilePanel(model);
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ProfileGenerator.class, "TIT_Add_profile"));
        panel.attachDialogDisplayer(dd);
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (ret == DialogDescriptor.OK_OPTION) {
            String id = panel.getProfileId();
            Profile prof = model.getProject().findProfileById(id);
            boolean pomPackaging = "pom".equals(model.getProject().getPackaging()); //NOI18N
            if (prof == null) {
                try {
                    model.startTransaction();
                    prof = model.getFactory().createProfile();
                    prof.setId(id);
                    if (panel.generateDependencies()) {
                        Dependency dep = model.getFactory().createDependency();
                        dep.setGroupId("foo"); //NOI18N
                        dep.setArtifactId("bar"); //NOI18N
                        dep.setVersion("1.0"); //NOI18N
                        if (pomPackaging) {
                            DependencyManagement dm = model.getFactory().createDependencyManagement();
                            prof.setDependencyManagement(dm);
                            dm.addDependency(dep);
                        } else {
                            prof.addDependency(dep);
                        }
                    }
                    if (panel.generatePlugins()) {
                        BuildBase base = model.getFactory().createBuildBase();
                        prof.setBuildBase(base);
                            Plugin plug = model.getFactory().createPlugin();
                            plug.setGroupId("foo"); //NOI18N
                            plug.setArtifactId("bar"); //NOI18N
                            plug.setVersion("1.0"); //NOI18N
                        if (pomPackaging) {
                            PluginManagement pm = model.getFactory().createPluginManagement();
                            base.setPluginManagement(pm);
                            pm.addPlugin(plug);
                        } else {
                            base.addPlugin(plug);
                        }
                    }
                    if (panel.isActivation()) {
                        Activation act = model.getFactory().createActivation();
                        prof.setActivation(act);
                        if (panel.isActiovationByProperty()) {
                            ActivationProperty prop = model.getFactory().createActivationProperty();
                            act.setActivationProperty(prop);
                            prop.setName("foo");//NOI18N
                            prop.setValue("bar");//NOI18N
                        }
                        if (panel.isActiovationByFile()) {
                            ActivationFile file = model.getFactory().createActivationFile();
                            act.setActivationFile(file);
                            file.setExists("${basedir}/foo.bar"); //NOI18N
                        }
                        if (panel.isActiovationByOS()) {
                            ActivationOS os = model.getFactory().createActivationOS();
                            if (Utilities.isMac()) {
                                os.setFamily("MacOS");//NOI18N
                            } else if (Utilities.isUnix()) {
                                os.setFamily("Linux");//NOI18N
                            } else {
                                os.setFamily("Windows"); //NOI18N
                            }
                            act.setActivationOS(os);
                        }
                    }
                    model.getProject().addProfile(prof);
                } finally {
                    model.endTransaction();
                }
                int pos = prof.getModel().getAccess().findPosition(prof.getPeer());
                component.setCaretPosition(pos);
            }
        }
    }
}
