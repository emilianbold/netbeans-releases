/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.rubyproject.bundler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.rubyproject.RequiredGems;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.spi.PropertiesProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Support for Bundler. Attempts to retrieve the actual tasks by reading the output
 * of 'bundle help' to show an up-to-date list of tasks supported by the installed
 * Bundler version. Falls back to a default list of tasks (taken from 0.9.5).
 *
 * @author Erno Mononen
 */
public final class BundlerSupport {

    private static final List<Task> DEFAULT_TASKS = new ArrayList<Task>();

    static {
        // default tasks to use if fetching of tasks hasn't finished (or fails for any reason)
        // NOI18N start
        DEFAULT_TASKS.add(new Task("check", "Checks if the dependencies listed in Gemfile are satisfied by currently installed gems"));
        DEFAULT_TASKS.add(new Task("init", "Generates a Gemfile into the current working directory"));
        DEFAULT_TASKS.add(new Task("install", "Install the current environment to the system"));
        DEFAULT_TASKS.add(new Task("lock", "Locks the bundle to the current set of dependencies, including all child dependencies."));
        DEFAULT_TASKS.add(new Task("pack", "Packs all the gems to vendor/cache"));
        DEFAULT_TASKS.add(new Task("show", "Shows all gems that are part of the bundle."));
        DEFAULT_TASKS.add(new Task("unlock", "Unlock the bundle. This allows gem versions to be changed."));
        // NOI18N end
    }

    /**
     * Names of tasks not to display (running exec needs params, support for that is TODO).
     */
    private static final String[] FILTERED_TASKS = new String[]{"exec"}; //NOI18N

    private static final String BUNDLE = "bundle"; //NOI18N
    private static final String BUNDLER_GEM = "bundler"; //NOI18N
    private final RubyBaseProject project;

    /**
     * Per-platform cache for fetched tasks.
     */
    private static final Map<RubyPlatform, List<Task>> TASKS_CACHE = new WeakHashMap<RubyPlatform, List<Task>>();

    private String bundle;
    private Future<Integer> result;
    private boolean installed;
    private boolean initialized;

    public BundlerSupport(RubyBaseProject project) {
        this.project = project;
    }

    /**
     * Initializes tasks, must be called before invoking {@link #getTasks() }
     * or {@link #installed() }.
     */
    public void initialize() {
        initialized = true;
        RubyPlatform platform = getPlatform();
        if (platform == null) {
            return;
        }
        bundle = platform.findExecutable(BUNDLE);
        if (bundle == null) {
            return;
        }
        GemManager gemManager = platform.getGemManager();
        if (gemManager == null) {
            return;
        }
        installed = gemManager.isGemInstalled(BUNDLER_GEM);
        if (installed) {
            fetchTasks();
        }
    }

    public boolean installed() {
        assert initialized : "not initialized";
        return installed;
    }

    private RubyPlatform getPlatform() {
        return RubyPlatform.platformFor(project);
    }

    public Action createAction() {
        return new BundlerAction(this);
    }

    List<Task> getTasks() {
        assert initialized : "not initialized";
        RubyPlatform platform = getPlatform();
        synchronized (TASKS_CACHE) {
            if (TASKS_CACHE.containsKey(platform)) {
                List<Task> tasks = TASKS_CACHE.get(platform);
                if (!tasks.isEmpty()) {
                    return filter(tasks);
                }
            }
        }
        return DEFAULT_TASKS;
    }

    Future<Integer> runBundlerTask(String name, LineConvertor convertor, boolean displayOutput, Runnable postBuild) {
        assert initialized;

        RubyExecutionDescriptor descriptor = new RubyExecutionDescriptor(getPlatform(),
                NbBundle.getMessage(BundlerSupport.class, "Bundler"),
                FileUtil.toFile(project.getProjectDirectory()));

        descriptor.cmd(new File(bundle));
        descriptor.additionalArgs(name);
        if (convertor != null) {
            descriptor.addOutConvertor(convertor);
        }
        if (postBuild != null) {
            descriptor = descriptor.postBuild(postBuild);
        }
        descriptor.setOutProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.ansiStripping(defaultProcessor);
            }
        });

        RubyProcessCreator rpc = new RubyProcessCreator(descriptor);
        ExecutionDescriptor ed = descriptor.toExecutionDescriptor();

        if (!displayOutput) {
            ed = ed.inputOutput(InputOutput.NULL);
        }

        return ExecutionService.newService(rpc,
                    ed,
                    descriptor.getDisplayName()).run();

    }

    void updateIndices() {
        final BundlerLineConvertor convertor = new BundlerLineConvertor();
        final Runnable updateTask = new Runnable() {

            @Override
            public void run() {
                if (!convertor.getGems().isEmpty()) {
                    RequiredGems requiredGems = null;
                    for (RequiredGems each : project.getLookup().lookupAll(RequiredGems.class)) {
                        if (!each.isForTests()) {
                            requiredGems = each;
                            break;
                        }
                    }
                    requiredGems.setRequiredGems(convertor.getGems());
                    SharedRubyProjectProperties properties = project.getLookup().lookup(PropertiesProvider.class).getProperties();
                    properties.setGemRequirements(requiredGems.getGemRequirements());
                    properties.save();
                }
            }
        };
        runBundlerTask("show", convertor, true, updateTask);
    }

    boolean canUpdateIndices() {
        // currently supported for Rails projects only
        return !(project instanceof RubyProject);
    }

    private static List<Task> filter(List<Task> toFilter) {
        List<Task> result = new ArrayList<Task>(toFilter.size());
        for (Task each : toFilter) {
            for (String filtered : FILTERED_TASKS) {
                if (!filtered.equals(each.name)) {
                    result.add(each);
                }
            }
        }
        return result;
    }

    private void fetchTasks() {
        // fetches bundler tasks using 'bundler help'
        final RubyPlatform platform = getPlatform();
        if (TASKS_CACHE.containsKey(platform)) {
            return;
        }

        if (result == null) {
            final TaskCollector collector = new TaskCollector();
            result = runBundlerTask("help", collector, false, new Runnable() {
                @Override
                public void run() {
                    synchronized(TASKS_CACHE) {
                        TASKS_CACHE.put(platform, collector.tasks);
                    }
                }
            });
        }
    }

    /**
     * Represents a Bundler task.
     */
    static class Task {
        final String name;
        final String descriptor;

        public Task(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }
    }

    // package private for tests
    static final class TaskCollector implements LineConvertor {

        private static final Pattern PATTERN = Pattern.compile("\\s*bundle\\s(\\S+)\\s+.*#\\s(.+)");
        private final List<Task> tasks = new ArrayList<Task>();

        @Override
        public List<ConvertedLine> convert(String line) {
            Task task = parse(line);
            if (task != null) {
                tasks.add(task);
            }
            return null;
        }

        static Task parse(String line) {
            Matcher matcher = PATTERN.matcher(line);
            if (!matcher.matches()) {
                return null;
            }
            return new Task(matcher.group(1), matcher.group(2));
        }
    }

}
