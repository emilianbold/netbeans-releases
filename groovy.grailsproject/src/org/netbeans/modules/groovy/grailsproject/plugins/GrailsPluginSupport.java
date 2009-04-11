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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform.Version;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.ProgressSupport;
import org.netbeans.modules.groovy.grailsproject.ProgressSupport.ProgressDialogDescriptor;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 *
 * @author David Calavera, Petr Hejl
 */
public class GrailsPluginSupport {

    private static Logger LOGGER = Logger.getLogger(GrailsPluginSupport.class.getName());

    private final GrailsProject project;

    public GrailsPluginSupport(GrailsProject project) {
        this.project = project;
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
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(processor));
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

    public List<GrailsPlugin> loadInstalledPlugins() {
        Version version = GrailsPlatform.getDefault().getVersion();
        if (Version.VERSION_1_1.compareTo(version) <= 0) {
            return loadInstalledPlugins11();
        }
        return loadInstalledPlugins10();
    }

    private List<GrailsPlugin> loadInstalledPlugins10() {
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

    public List<GrailsPlugin> loadInstalledPlugins11() {
        List<GrailsPlugin> plugins = new ArrayList<GrailsPlugin>();
        try {
            FileObject propertiesFile = project.getProjectDirectory().getFileObject("application.properties"); //NOI18N
            if (propertiesFile != null && propertiesFile.isData()) {
                Properties props = new Properties();
                InputStream is = new BufferedInputStream(propertiesFile.getInputStream());
                try {
                    props.load(is);

                    for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                        String name = (String) e.nextElement();
                        if (name.startsWith("plugins.")) { // NOI18N
                            String value = props.getProperty(name);
                            name = name.substring("plugins.".length()); // NOI18N
                            plugins.add(new GrailsPlugin(name, value, null));
                        }
                    }

                } finally {
                    is.close();
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        Collections.sort(plugins);
        return plugins;
    }

    public boolean uninstallPlugins(Collection<GrailsPlugin> selectedPlugins) {
        Version version = GrailsPlatform.getDefault().getVersion();
        if (Version.VERSION_1_1.compareTo(version) <= 0) {
            return uninstallPlugins11(selectedPlugins);
        }

        return uninstallPlugins10(selectedPlugins);
    }

    private boolean uninstallPlugins10(Collection<GrailsPlugin> selectedPlugins) {
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

    private boolean uninstallPlugins11(final Collection<GrailsPlugin> selectedPlugins) {
        return handlePlugins(selectedPlugins, true);
    }

    public boolean installPlugins(final Collection<GrailsPlugin> selectedPlugins) {
        return handlePlugins(selectedPlugins, false);
    }

    private boolean handlePlugins(final Collection<GrailsPlugin> selectedPlugins, boolean uninstall) {
        assert SwingUtilities.isEventDispatchThread();

        if (!(selectedPlugins != null && selectedPlugins.size() > 0)) {
            return false;
        }

        boolean installed = true;

        final GrailsPlatform platform = GrailsProjectConfig.forProject(project).getGrailsPlatform();
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            for (GrailsPlugin plugin : selectedPlugins) {
                String title = NbBundle.getMessage(GrailsPluginSupport.class,
                        uninstall ? "Uninstallation" : "Installation");
                String message = NbBundle.getMessage(GrailsPluginSupport.class,
                        uninstall ? "PluginUninstallPleaseWait" : "PluginInstallPleaseWait", plugin.getName());
                ProgressHandle handle = ProgressHandleFactory.createHandle(message);

                ProgressDialogDescriptor descriptor = ProgressSupport.createProgressDialog(title, handle, null);
                final Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);

                descriptor.addCancelListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        dlg.setVisible(false);
                        dlg.dispose();
                    }
                });

                // FIXME should it be FS atomic action ?
                Callable<Boolean> runner = getPluginHandlerCallable(platform, plugin, descriptor, dlg, uninstall);

                final Future<Boolean> result = executor.submit(runner);

                handle.start();
                handle.progress(message);

                dlg.setVisible(true);

                try {
                    installed = installed && result.get().booleanValue();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex.getCause() != null ? ex.getCause() : ex);
                } finally {
                    handle.finish();
                }
            }
        } finally {
            executor.shutdown();

            // TODO if we will support global plugins we have to refresh global plugins dir as well
            FileUtil.refreshFor(project.getBuildConfig().getProjectPluginsDir());
        }
        return installed;
    }

    private Callable<Boolean> getPluginHandlerCallable(final GrailsPlatform platform, final GrailsPlugin plugin,
            final ProgressDialogDescriptor desc, final Dialog dlg, final boolean uninstall) {
        final String command = uninstall ? "uninstall-plugin" : "install-plugin"; // NOI18N

        return new Callable<Boolean>() {
            public Boolean call() {
                ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
                String displayName = inf.getDisplayName() + " (" + command + ")"; // NOI18N

                List<String> args = new ArrayList<String>(3);
                if (GrailsPlatform.Version.VERSION_1_1.compareTo(platform.getVersion()) <= 0) {
                    args.add("--non-interactive"); // NOI18N
                }
                if (plugin.getPath() == null) {
                    args.add(plugin.getName());
                    args.add(plugin.getVersion());
                } else {
                    args.add(plugin.getPath());
                }

                Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand(
                        command, GrailsProjectConfig.forProject(project), args.toArray(new String[args.size()]));
                ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true)
                        .postExecution(new RefreshProjectRunnable(project));

                ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
                final Future<Integer> future = service.run();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        desc.addCancelListener(new ActionListener() {
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

// FIXME we would need some silent uninstall for this (not the case for 1.1)
//                    if (!uninstall && broken) {
//                        uninstallPlugins(Collections.singletonList(plugin));
//                    }

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

    public GrailsPlugin getPluginFromZipFile(String path) {
        GrailsPlugin plugin = null;
        try {
            final ZipFile file = new ZipFile(new File(path));
            try {
                final ZipEntry entry = file.getEntry("plugin.xml"); // NOI18N
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
        if (doc.getElementsByTagName("title") != null // NOI18N
                && doc.getElementsByTagName("title").getLength() > 0) { //NOI18N
            description = doc.getElementsByTagName("title") // NOI18N
                    .item(0).getTextContent(); //NOI18N
        }
        return new GrailsPlugin(name, version, description, path);
    }

    private static class PluginProcessor implements LineProcessor {

        private final List<GrailsPlugin> plugins = Collections.synchronizedList(new ArrayList<GrailsPlugin>());

        /**
         * In 1.1 plugins are listen in following format so the pattern
         * luckily works. So installed plugins are not captured.
         *
         * core repo
         * ---------------------------------------
         * name &lt;version&gt; -- description
         *
         * default repo
         * ---------------------------------------
         * name &lt;version&gt; -- description
         *
         * installed
         * ---------------------------------------
         * name version -- description
        */
        private static final Pattern PLUGIN_PATTERN = Pattern.compile("(.+)[\\s]+<([\\w\\s.-]+)>[\\s]+--(.+)"); // NOI18N

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
