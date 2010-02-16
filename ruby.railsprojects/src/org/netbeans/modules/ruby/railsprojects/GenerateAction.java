/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ruby.railsprojects;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.editor.BaseAction;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessors;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.platform.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.platform.execution.ExecutionUtils.FileLocation;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.OutputProcessor;
import org.netbeans.modules.ruby.platform.execution.RubyLineConvertorFactory;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.railsprojects.Generator.Script;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;


public final class GenerateAction extends NodeAction {
    public static final String EDITOR_ACTION_NAME = "rails-generator";

    private static final Logger LOGGER = Logger.getLogger(GenerateAction.class.getName());

    private boolean forcing;
    private boolean preview;
    
    /** Editor action which lets you open the dialog as an editor action */
    public static class EditorAction extends BaseAction {
        public EditorAction() {
            super(EDITOR_ACTION_NAME, 0);
        }

        @Override
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            SystemAction.get(GenerateAction.class).actionPerformed(evt, target);
        }
        
        @Override
        public Class getShortDescriptionBundleClass() {
            return GenerateAction.class;
        }
    }
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        Lookup lookup = activatedNodes[0].getLookup();
        RailsProject project = lookup.lookup(RailsProject.class);

        if (project == null) {
            DataObject dataObject = lookup.lookup(DataObject.class);

            if (dataObject == null) {
                return;
            }

            Project p = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            if (p instanceof RailsProject) {
                project = (RailsProject)p;
            }
        }

        if (project == null) {
            return;
        }

//        if (!RubyInstallation.getInstance().isValidRuby(true)) {
//            return;
//        }

        // #141908 -- check whether rails is installed in vendor/
        FileObject railsInstall = project.getProjectDirectory().getFileObject("vendor/rails/railties"); // NOI18N
        RubyPlatform platform = RubyPlatform.platformFor(project);
        if (railsInstall == null && !platform.hasValidRails(true)) {
            LOGGER.warning("No valid Rails installation found, platform is:" + RubyPlatform.platformFor(project));
            return;
        }

        Generator generator = activatedNodes[0].getLookup().lookup(Generator.class);

        if (generator == null) {
            generator = Generator.CONTROLLER;
        }

        generate(project, generator, null, null, false, false);
    }
    
    public void generate(Project project, String generatorName, String name, String params) {
        assert generatorName.equals("controller") : "Only the controller generator is supported"; // NOI18N
        Generator generator = Generator.CONTROLLER;
        
        if (project != null) {
            generate((RailsProject)project, generator, name, params, true, true);
        } else {
            assert false;
        }
    }

    public void generate(final RailsProject project, Generator generator, String initialName, 
            String initialParams, boolean initialEnabled, boolean noOverwrite) {
        boolean cancelled;
        final JButton okButton = new JButton(NbBundle.getMessage(GenerateAction.class, "Ok"));
        okButton.getAccessibleContext()
                .setAccessibleDescription(NbBundle.getMessage(GenerateAction.class, "AD_Ok"));

        final GeneratorPanel panel = new GeneratorPanel(project, generator);
        if (noOverwrite) {
            panel.setForcing(false);
        } else {
            panel.setForcing(forcing);
        }
        panel.setPretend(preview);
        
        if (initialName != null) {
            panel.setInitialState(initialName, initialParams);
        }

        Object[] options = new Object[] { okButton, DialogDescriptor.CANCEL_OPTION };

        panel.setChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    okButton.setEnabled(panel.isDataValid());
                }
            });

        okButton.setEnabled(initialEnabled);

        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        DialogDescriptor desc =
            new DialogDescriptor(panel,
                NbBundle.getMessage(GenerateAction.class, "GeneratorTitle", projectName), true, options,
                options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        desc.setMessageType(DialogDescriptor.PLAIN_MESSAGE);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GenerateAction.class, "AD_GeneratorDialog"));
        dlg.setVisible(true);

        if (desc.getValue() != options[0]) {
            cancelled = true;
        } else {
            cancelled = false;
        }

        dlg.dispose();
        if (!noOverwrite) {
            forcing = panel.isForce(); // Persist state for next invocation (this session only)
        }
        preview = panel.isPretend();

        if (!cancelled) {
            final String type = panel.getType();

            if (type.length() > 0) { // TODO: Toggle OK state based on valid entry

                final FileObject dir = project.getProjectDirectory();
                final File pwd = FileUtil.toFile(project.getProjectDirectory());
                final Script generatorScript = panel.getScript();
                final String scriptToRun = "script" + File.separator + generatorScript.script; // NOI18N
                List<String> argvList = new ArrayList<String>();
                argvList.addAll(generatorScript.args);
                argvList.add(type);


                String[] names = Utilities.parseParameters(panel.getGeneratedName());

                if (names != null) {
                    for (String name : names) {
                        argvList.add(name);
                    }
                }

                String[] firstParameterList = panel.getFirstParameterList();

                if (firstParameterList != null && firstParameterList.length > 0 && firstParameterList[0].length() > 0) {
                    for (String parameter : firstParameterList) {
                        argvList.add(parameter);
                    }

                    String[] remainingParameters = panel.getSecondParameterList();

                    if (remainingParameters != null && remainingParameters.length >0 && remainingParameters[0].length() > 0) {
                        for (String parameter : remainingParameters) {
                            argvList.add(parameter);
                        }
                    }
                }

                if (panel.isForce()) {
                    argvList.add("--force"); // NOI18N
                } else {
                    argvList.add("--skip"); // NOI18N
                }
                if (panel.isPretend()) {
                    argvList.add("--pretend"); // NOI18N
                }

                final String[] argv = argvList.toArray(new String[argvList.size()]);

                try {
                    final String charsetName = project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING);
                    project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                        public void run() throws IOException {
                            FileLocator locator = new DirectoryFileLocator(dir);
                            StatefulConvertor convertor = new StatefulConvertor(locator,
                                    RailsProjectGenerator.RAILS_GENERATOR_PATTERN, RubyLineConvertorFactory.EXT_RE, 2, -1);
                            String displayName = NbBundle.getMessage(GenerateAction.class, "RailsGenerator");
			    Map<String, String> env = new HashMap<String, String>();
			    String railsEnv = project.evaluator().getProperty(RailsProjectProperties.RAILS_ENV);
                            if (railsEnv != null) {
                                env.put("RAILS_ENV", railsEnv);
                            }
                            RubyExecutionDescriptor descriptor =
                                    new RubyExecutionDescriptor(RubyPlatform.platformFor(project), displayName, pwd, scriptToRun)
                                    .additionalArgs(argv)
                                    .fileLocator(locator)
                                    .addStandardRecognizers()
                                    .addOutConvertor(convertor)
                                    .addErrConvertor(convertor);
                            descriptor.addAdditionalEnv(env);
                            descriptor.setOutProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {
                                @Override
                                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                                    return InputProcessors.ansiStripping(defaultProcessor);
                                }
                            });

                            RubyProcessCreator rpc = new RubyProcessCreator(descriptor, charsetName);
                            Future<Integer> execution =
                                    ExecutionService.newService(rpc, descriptor.toExecutionDescriptor(), displayName).run();
                            try {
                                execution.get();
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            project.getProjectDirectory().getFileSystem().refresh(true);

                            List<FileLocation> locations = convertor.getLocations();
                            List<FileObject> rubyFiles = new ArrayList<FileObject>();
                            List<FileObject> rhtmlFiles = new ArrayList<FileObject>();

                            // Process in reverse order such that first files in the list are added last
                            // (so they will be on top)
                            for (int i = locations.size() - 1; i >= 0; i--) {
                                FileLocation loc = locations.get(i);
                                String file = loc.file;

                                if (file != null) {
                                    FileObject fo = locator.find(file);

                                    if (fo != null) {
                                        String mimeType = fo.getMIMEType();
                                        if (mimeType.equals(RubyInstallation.RUBY_MIME_TYPE)) {
                                            rubyFiles.add(fo);
                                        } else if (mimeType.equals(RhtmlTokenId.MIME_TYPE)) {
                                            rhtmlFiles.add(fo);
                                        }
                                    }
                                }
                            }

                            if (rhtmlFiles.size() <= 4) {
                                for (FileObject fo : rhtmlFiles) {
                                    OutputProcessor.open(fo, 1);
                                }
                            }
                            if (rubyFiles.size() <= 4) {
                                for (FileObject fo : rubyFiles) {
                                    OutputProcessor.open(fo, 1);
                                }
                            }
                        }
                    });
                    project.getProjectDirectory().getFileSystem().refresh(true);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GenerateAction.class,  EDITOR_ACTION_NAME);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return false;
        }

        Lookup lookup = activatedNodes[0].getLookup();
        RailsProject project = lookup.lookup(RailsProject.class);

        if (project != null) {
            return true;
        }

        DataObject dataObject = lookup.lookup(DataObject.class);

        return dataObject != null;
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    private static class StatefulConvertor implements LineConvertor {

        private final FileLocator locator;
        private final Pattern linePattern;
        private final Pattern filePattern;
        private final int fileGroup;
        private final int lineGroup;
        private final List<FileLocation> locations = new ArrayList<FileLocation>();


        public StatefulConvertor(FileLocator locator, Pattern linePattern,
                Pattern filePattern, int fileGroup, int lineGroup) {

            this.locator = locator;
            this.linePattern = linePattern;
            this.fileGroup = fileGroup;
            this.lineGroup = lineGroup;
            this.filePattern = filePattern;
        }

        public synchronized List<ConvertedLine> convert(final String line) {
            // Don't try to match lines that are too long - the java.util.regex library
            // throws stack exceptions (101234)
            if (line.length() > 400) {
                return null;
            }

            Matcher match = linePattern.matcher(line);

            if (match.matches()) {
                String file = null;
                int lineno = -1;

                if (fileGroup >= 0) {
                    file = match.group(fileGroup);
                    // Make some adjustments - easier to do here than in the regular expression
                    // (See 109721 and 109724 for example)
                    if (file.startsWith("\"")) { // NOI18N
                        file = file.substring(1);
                    }
                    if (file.startsWith("./")) { // NOI18N
                        file = file.substring(2);
                    }
                    if (filePattern != null && !filePattern.matcher(file).matches()) {
                        return null;
                    }
                }

                if (lineGroup >= 0) {
                    String linenoStr = match.group(lineGroup);

                    try {
                        lineno = Integer.parseInt(linenoStr);
                    } catch (NumberFormatException nfe) {
                        LOGGER.log(Level.INFO, null, nfe);
                        lineno = 0;
                    }
                }

                if (!line.trim().startsWith("skip")) { // NOI18N
                    locations.add(new FileLocation(file, lineno));
                }

                return Collections.<ConvertedLine>singletonList(
                        ConvertedLine.forText(line, new FindFileListener(file, lineno, locator)));
            }

            return null;
        }

        synchronized List<FileLocation> getLocations() {
            return locations;
        }

    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        DataObject dobj = (DataObject)target.getDocument().getProperty(Document.StreamDescriptionProperty);
        if (dobj != null) {
            Node n = dobj.getNodeDelegate();
            if (n != null) {
                Node[] nodes = new Node[] { n };
                if (enable(nodes)) {
                    performAction(nodes);
                }
            }
        }
    }

    public boolean appliesTo(String mimeType) {
        return RubyInstallation.RHTML_MIME_TYPE.equals(mimeType) ||
                RubyInstallation.RUBY_MIME_TYPE.equals(mimeType);
    }
}
