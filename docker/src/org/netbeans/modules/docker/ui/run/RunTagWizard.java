/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.run;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JComponent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.modules.docker.DockerContainer;
import org.netbeans.modules.docker.DockerImage;
import org.netbeans.modules.docker.DockerImageInfo;
import org.netbeans.modules.docker.DockerTag;
import org.netbeans.modules.docker.DockerUtils;
import org.netbeans.modules.docker.remote.DockerException;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.netbeans.modules.docker.remote.StreamResult;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class RunTagWizard {

    public static final String COMMAND_PROPERTY = "command";

    public static final String NAME_PROPERTY = "name";

    public static final String INTERACTIVE_PROPERTY = "interactive";

    public static final String TTY_PROPERTY = "tty";

    public static final String PORT_MAPPING_PROPERTY = "portMapping";

    private final DockerTag tag;

    public RunTagWizard(DockerTag tag) {
        this.tag = tag;
    }

    @NbBundle.Messages({
        "MSG_ReceivingImageInfo=Receiving Image Details",
        "LBL_Run=Run {0}"
    })
    public void show() {
        DockerImageInfo info = BaseProgressUtils.showProgressDialogAndRun(
                new DockerImageInfoRunnable(tag.getImage()), Bundle.MSG_ReceivingImageInfo(), false);

        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new RunCommandPanel());
        panels.add(new RunNetworkPanel(info));
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            c.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
        }
        final WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.LBL_Run(DockerUtils.getImage(tag)));
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            List<PortMapping> mapping = (List<PortMapping>) wiz.getProperty(PORT_MAPPING_PROPERTY);
            if (mapping == null) {
                mapping = Collections.emptyList();
            }
            run(tag, (String) wiz.getProperty(COMMAND_PROPERTY),
                    (String) wiz.getProperty(NAME_PROPERTY),
                    (Boolean) wiz.getProperty(INTERACTIVE_PROPERTY),
                    (Boolean) wiz.getProperty(TTY_PROPERTY),
                    mapping);
        }
    }

    private void run(final DockerTag tag, final String command, final String name,
            final boolean interactive, final boolean tty, final List<PortMapping> mapping) {

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    DockerRemote remote = new DockerRemote(tag.getImage().getInstance());
                    JSONObject config = new JSONObject();
                    if (interactive) {
                        config.put("OpenStdin", true);
                        config.put("StdinOnce", true);
                        config.put("AttachStdin", true);
                    }
                    if (tty) {
                        config.put("Tty", true);
                    }

                    String[] parsed = command == null ? new String[]{} : Utilities.parseParameters(command);
                    config.put("Image", DockerUtils.getImage(tag));
                    JSONArray cmdArray = new JSONArray();
                    cmdArray.addAll(Arrays.asList(parsed));
                    config.put("Cmd", cmdArray);
                    config.put("AttachStdout", true);
                    config.put("AttachStderr", true);
                    Map<String, List<PortMapping>> bindings = new HashMap<>();
                    for (PortMapping m : mapping) {
                        String str = m.getPort() + "/" + m.getType().name().toLowerCase(Locale.ENGLISH);
                        List<PortMapping> list = bindings.get(str);
                        if (list == null) {
                            list = new ArrayList<>();
                            bindings.put(str, list);
                        }
                        list.add(m);
                    }
                    if (!bindings.isEmpty()) {
                        JSONObject portBindings = new JSONObject();
                        JSONObject hostConfig = new JSONObject();
                        hostConfig.put("PortBindings", portBindings);
                        config.put("HostConfig", hostConfig);

                        for (Map.Entry<String, List<PortMapping>> e : bindings.entrySet()) {
                            JSONArray arr = new JSONArray();
                            for (PortMapping m : e.getValue()) {
                                JSONObject o = new JSONObject();
                                o.put("HostIp", m.getHostAddress());
                                o.put("HostPort", m.getHostPort() != null ? m.getHostPort().toString() : "");
                                arr.add(o);
                            }
                            portBindings.put(e.getKey(), arr);
                        }
                    }
                    Pair<DockerContainer, StreamResult> result = remote.run(name, config);

                    UiUtils.openTerminal(result.first(), result.second(), interactive, true);
                } catch (Exception ex) {
                    // FIXME display exception
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private static class DockerImageInfoRunnable implements ProgressRunnable<DockerImageInfo> {

        private final DockerImage image;

        public DockerImageInfoRunnable(DockerImage image) {
            this.image = image;
        }

        @Override
        public DockerImageInfo run(ProgressHandle handle) {
            try {
                DockerRemote remote = new DockerRemote(image.getInstance());
                return remote.getInfo(image);
            } catch (DockerException ex) {
                return null;
            }
        }

    }
}
