/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dialog;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor;
import org.netbeans.modules.extexecution.api.ExecutionService;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.input.InputProcessors;
import org.netbeans.modules.extexecution.api.input.LineProcessor;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 *
 * @author David Calavera, Petr Hejl
 */
public class GrailsPluginsManager {

    private final GrailsProject project;

    private GrailsPluginsManager(GrailsProject project) {
        this.project = project;
    }

    public static GrailsPluginsManager getInstance(GrailsProject project) {
        return new GrailsPluginsManager(project);
    }

    public List<GrailsPlugin> refreshAvailablePlugins() throws InterruptedException {
        final String command = "list-plugins"; // NOI18N

        final ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        final String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

        final Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                command, GrailsProjectConfig.forProject(project));

        final PluginProcessor processor = new PluginProcessor();
        ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true);
        descriptor = descriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {
            public InputProcessor newInputProcessor() {
                return InputProcessors.bridge(processor);
            }
        });

        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        Future<Integer> task = service.run();
        try {
            task.get();
        } catch (InterruptedException ex) {
            task.cancel(true);
            throw ex;
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex.getCause());
        }

        return processor.getPlugins();
    }

    public List<GrailsPlugin> refreshInstalledPlugins() {
        List<GrailsPlugin> plugins = new ArrayList<GrailsPlugin>();
        try {
            FileObject pluginsDir = project.getProjectDirectory().getFileObject("plugins"); //NOI18N
            if (pluginsDir != null && pluginsDir.isFolder()) {
                pluginsDir.refresh();
                for (FileObject child : pluginsDir.getChildren()) {
                    if (child.isFolder()) {
                        FileObject descriptor = child.getFileObject("plugin.xml"); //NOI18N
                        if (descriptor != null && descriptor.canRead()) {
                            plugins.add(getPluginFromInputStream(descriptor.getInputStream(), null));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        Collections.sort(plugins);
        return plugins;
    }

    public boolean uninstallPlugins(Collection<GrailsPlugin> selectedPlugins) {
        if (selectedPlugins != null && selectedPlugins.size() > 0) {
            final FileObject pluginsDir = project.getProjectDirectory().getFileObject("plugins"); //NO I18N
            if (pluginsDir != null && pluginsDir.isFolder() && pluginsDir.canWrite()) {
                pluginsDir.refresh();
                try {
                    for (GrailsPlugin plugin : selectedPlugins) {                        
                        FileObject pluginDir = pluginsDir.getFileObject(plugin.getDirName());
                        if (pluginDir != null && pluginDir.isValid()) {
                            pluginDir.delete();
                        }
                        FileObject pluginZipFile = pluginsDir.getFileObject(plugin.getZipName());
                        if (pluginZipFile != null && pluginZipFile.isValid()) {
                            pluginZipFile.delete();
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return true;
    }

    public boolean installPlugins(final Collection<GrailsPlugin> selectedPlugins) {
        //good practice:        
        // array == null || array.length == 0 throws a NullPointerException if array is null
        if (!(selectedPlugins != null && selectedPlugins.size() > 0)) {
            return false;
        }

        boolean installed = true;
        final JButton[] buttons = getProgressDlgButtons();
        
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        for (final GrailsPlugin plugin : selectedPlugins) {
            final Dialog dlg = getProgressDialog(plugin, buttons);

            buttons[0].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    dlg.setVisible(false);
                    dlg.dispose();
                }
            });

            Callable<Boolean> runner = getInstallPluginCallable(plugin, buttons[1], dlg);

            final Future<Boolean> result = executor.submit(runner);
            dlg.setVisible(true);

            try {
                installed = installed && result.get().booleanValue();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex.getCause() != null ? ex.getCause() : ex);
            }
        }
        executor.shutdown();
        return installed;
    }
    
    private Callable<Boolean> getInstallPluginCallable(final GrailsPlugin plugin,
            final JButton cancelButton, final Dialog dlg) {
        final String command = "install-plugin"; // NOI18N

        return new Callable<Boolean>() {
            public Boolean call() {
                ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
                String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

                String[] args = plugin.getPath() == null
                        ? new String[] {plugin.getName(), plugin.getVersion()}
                        : new String[] {plugin.getPath()};

                Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                        command, GrailsProjectConfig.forProject(project), args);
                ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true)
                        .postExecution(new RefreshProjectRunnable(project));

                ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
                final Future<Integer> future = service.run();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        cancelButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent ev) {
                                future.cancel(true);
                            }
                        });
                    }
                });

                boolean broken = false;
                boolean interrupted = false;
                try {
                    try {
                        Integer retValue = future.get();
                        if (retValue.intValue() != 0) {
                            broken = true;
                        }
                    } catch (InterruptedException ex) {
                        interrupted = true;
                        future.cancel(true);
                        broken = true;
                    } catch (ExecutionException ex) {
                        broken = true;
                        Exceptions.printStackTrace(ex.getCause() != null ? ex.getCause() : ex);
                    } catch (CancellationException ex) {
                        broken = true;
                    }

                    if (broken) {
                        // TODO is this enough ?
                        uninstallPlugins(Collections.singletonList(plugin));
                    }

                    return !broken;
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {                            
                            dlg.setVisible(false);
                            dlg.dispose();
                        }
                    });
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
    }
    
    private Dialog getProgressDialog(GrailsPlugin plugin, JButton[] buttons) {
        final String title = NbBundle.getMessage(GrailsPluginsManager.class, "Installation");
        
        final InstallingPluginPanel progress = new InstallingPluginPanel(
            NbBundle.getMessage(GrailsPluginsManager.class, "PluginPleaseWait", plugin.getName()));
        final DialogDescriptor descriptor = new DialogDescriptor(progress, title, true, buttons, buttons[0],
            DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(InstallingPluginPanel.class), null); // NOI18N
        descriptor.setModal(true);
        return DialogDisplayer.getDefault().createDialog(descriptor);
    }
    
    private JButton[] getProgressDlgButtons() {
        final JButton closeButton = new JButton(NbBundle.getMessage(GrailsPluginsManager.class, "CTL_Close"));
        final JButton cancelButton =
                new JButton(NbBundle.getMessage(GrailsPluginsManager.class, "CTL_Cancel"));
        closeButton.getAccessibleContext()
                .setAccessibleDescription(NbBundle.getMessage(GrailsPluginsManager.class, "CTL_Close"));

        closeButton.setEnabled(false);
        return new JButton[] {closeButton, cancelButton};
    }

    public GrailsPlugin getPluginFromZipFile(String path) {
        GrailsPlugin plugin = null;
        try {
            final ZipFile file = new ZipFile(new File(path));
            try {
                final ZipEntry entry = file.getEntry("plugin.xml");
                if (entry != null) {
                    InputStream stream = file.getInputStream(entry);
                    plugin = getPluginFromInputStream(stream, path);
                }
            } finally {
                file.close();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return plugin;
    }

    private GrailsPlugin getPluginFromInputStream(InputStream inputStream, String path) throws Exception {
        final Document doc = XMLUtil.parse(new InputSource(inputStream), false, false, null, null);
        final Node root = doc.getFirstChild();
        final String name = root.getAttributes().getNamedItem("name").getTextContent(); //NOI18N
        String version = null;
        String description = null;
        if (root.getAttributes().getNamedItem("version") != null) { //NOI18N
            version = root.getAttributes().getNamedItem("version").getTextContent(); //NOI18N
        }
        if (doc.getElementsByTagName("title") != null
                && doc.getElementsByTagName("title").getLength() > 0) { //NOI18N
            description = doc.getElementsByTagName("title")
                    .item(0).getTextContent(); //NOI18N
        }
        return new GrailsPlugin(name, version, description, path);
    }

    private static class PluginProcessor implements LineProcessor {

        private final List<GrailsPlugin> plugins = Collections.synchronizedList(new ArrayList<GrailsPlugin>());

        private static final Pattern PLUGIN_PATTERN = Pattern.compile("(.+)[\\s]+<([\\w\\s.-]+)>[\\s]+--(.+)");
        public void processLine(String line) {
            GrailsPlugin plugin = null;
            final Matcher matcher = PLUGIN_PATTERN.matcher(line);
            if (matcher.matches()) {
                if (!"no releases".equals(matcher.group(2))) { //NO I18N
                    plugin = new GrailsPlugin(matcher.group(1).trim(), matcher.group(2), matcher.group(3));
                }
            }
            if (plugin != null) {
                plugins.add(plugin);
            }
        }

        public void reset() {
            // noop
        }

        public void close() {
            // noop
        }

        public List<GrailsPlugin> getPlugins() {
            return plugins;
        }

    }
}
