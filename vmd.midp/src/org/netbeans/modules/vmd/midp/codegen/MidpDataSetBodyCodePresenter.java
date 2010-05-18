/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.midp.codegen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.InitCodeGenerator;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.midp.components.general.ClassSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Karol Harezlak
 */
public final class MidpDataSetBodyCodePresenter {

    private static final String INSTANCE_NAME = "__instancename__"; //NOI18N
    private static final String START_GUARDED_BLOCK = "__start_guardedblock__"; //NOI18N
    private static final String STOP_GUARDED_BLOCK = "__stop_guardedblock__"; //NOI18N
    //private final String bodyTemplatePath;

    public static Presenter create(final String bodyTemplatePath) {
        return new CodeClassLevelPresenter.Adapter() {

            @Override
            protected void generateClassBodyCode(StyledDocument document) {
                if (!ClassSupport.isLazyInitialized(getComponent())) {
                    return;
                }
                MultiGuardedSection section = MultiGuardedSection.create(document, getComponent().getComponentID() + "-getter");// NOI18N
                String directAccess = CodeReferencePresenter.generateDirectAccessCode(getComponent());
                section.getWriter().write("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Getter: " + directAccess + " \">\n"); // NOI18N
                section.getWriter().write("/**\n * Returns an initiliazed instance of " + directAccess + " component.\n * @return the initialized component instance\n */\n"); // NOI18N
                section.getWriter().write("public " + CodeReferencePresenter.generateTypeCode(getComponent()) + " " + CodeReferencePresenter.generateAccessCode(getComponent()) + " {\n"); // NOI18N
                section.getWriter().write("if (" + directAccess + " == null) {\n"); // NOI18N
                section.getWriter().commit();
                section.switchToEditable(getComponent().getComponentID() + "-preInit"); // NOI18N
                section.getWriter().write(" // write pre-init user code here\n").commit(); // NOI18N
                section.switchToGuarded();
                Collection<? extends CodeClassInitHeaderFooterPresenter> headersFooters = getComponent().getPresenters(CodeClassInitHeaderFooterPresenter.class);
                for (CodeClassInitHeaderFooterPresenter header : headersFooters) {
                    header.generateClassInitializationHeader(section);
                }
                InitCodeGenerator.generateInitializationCode(section, getComponent());
                for (CodeClassInitHeaderFooterPresenter footer : headersFooters) {
                    footer.generateClassInitializationFooter(section);
                }
                section.getWriter().write(directAccess + " = new " + getInstanceName(getComponent()) + "();\n");
                section.getWriter().commit();

                section.switchToEditable(getComponent().getComponentID() + "-postInit"); // NOI18N
                section.getWriter().write(" // write post-init user code here\n").commit(); // NOI18N

                section.switchToGuarded();
                section.getWriter().write("}\n"); // NOI18N
                section.getWriter().write("return " + directAccess + ";\n"); // NOI18N
                section.getWriter().write("}\n"); // NOI18N
                createBodyCode(section);
                section.switchToEditable(getComponent().getComponentID() + "end"); //NOI18N
                section.getWriter().commit();
                section.switchToGuarded();
                section.getWriter().write("//</editor-fold>\n").commit(); // NOI18N
                section.close();
            }

            private void createBodyCode(final MultiGuardedSection section) {
                try {
                    InputStream is = MidpDataSetBodyCodePresenter.class.getClassLoader().getResourceAsStream(bodyTemplatePath);
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
                    String line = in.readLine();
                    int i = 0;
                    while (line != null) {
                        if (line.contains(START_GUARDED_BLOCK)) {
                            if (!section.isGuarded()) {
                                section.switchToGuarded();
                            }
                            while ((line = in.readLine()) != null && !line.contains(STOP_GUARDED_BLOCK)) {
                                if (line.contains(INSTANCE_NAME)) {
                                    section.getWriter().write(line.replace(INSTANCE_NAME, getInstanceName(getComponent())) + "\n"); //NOI18N
                                } else if (!line.startsWith("#")) { //NOI18N
                                    section.getWriter().write(line + "\n"); //NOI18N
                                }
                            }
                            section.getWriter().commit();
                        } else if (line != null && line.contains(STOP_GUARDED_BLOCK)) {
                            i++;
                            section.switchToEditable(getComponent().getComponentID() + "codeZone" + i); //NOI18N
                            while ((line = in.readLine()) != null && !line.contains(START_GUARDED_BLOCK)) {
                                if (!line.startsWith("#")) { //NOI18N
                                    section.getWriter().write(line + "\n"); //NOI18N
                                }
                            }
                            section.getWriter().commit();
                        } else {
                            line = in.readLine();
                        }
                    }
                    in.close();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            ;
        };
    }

    private static String getInstanceName(DesignComponent component) {
        String instanceName = CodeReferencePresenter.generateDirectAccessCode(component);
        instanceName = instanceName.substring(0, 1).toUpperCase() + instanceName.substring(1);
        if (CodeReferencePresenter.generateTypeCode(component).equals(instanceName)) {
            instanceName = instanceName + "_"; //NOI18N
        }
        return instanceName;
    }

    private MidpDataSetBodyCodePresenter() {
    }
}
