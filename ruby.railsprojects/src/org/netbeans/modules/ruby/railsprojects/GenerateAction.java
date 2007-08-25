/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.railsprojects;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.rubyproject.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
import org.netbeans.modules.ruby.rubyproject.execution.OutputProcessor;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer.FileLocation;
import org.netbeans.modules.ruby.rubyproject.execution.RegexpOutputRecognizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;


public final class GenerateAction extends NodeAction {
    private boolean forcing;
    private boolean preview;
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        if (!RubyInstallation.getInstance().isValidRuby(true)) {
            return;
        }

        if (!RubyInstallation.getInstance().isValidRails(true)) {
            return;
        }

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

    public void generate(RailsProject project, Generator generator, String initialName, 
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
                    okButton.setEnabled(panel.isValid());
                }
            });

        okButton.setEnabled(initialEnabled);

        DialogDescriptor desc =
            new DialogDescriptor(panel,
                NbBundle.getMessage(GenerateAction.class, "GeneratorTitle"), true, options,
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
                final String script = "script" + File.separator + "generate"; // NOI18N
                List<String> argvList = new ArrayList<String>();
                argvList.add(type);

                if (panel.isForce()) {
                    argvList.add("--force"); // NOI18N
                } else {
                    argvList.add("--skip"); // NOI18N
                }

                if (panel.isPretend()) {
                    argvList.add("--pretend"); // NOI18N
                }

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

                final String[] argv = argvList.toArray(new String[argvList.size()]);

                try {
                    final String charsetName = project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING);
                    project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                StatefulRecognizer recognizer =
                                    new StatefulRecognizer(RailsProjectGenerator.RAILS_GENERATOR);
                                FileLocator locator = new DirectoryFileLocator(dir);
                                String displayName = NbBundle.getMessage(GenerateAction.class, "RailsGenerator");
                                Task task =
                                    new RubyExecution(new ExecutionDescriptor(displayName, pwd, script).
                                            additionalArgs(argv).fileLocator(locator).
                                            addOutputRecognizer(recognizer), charsetName).run();

                                task.waitFinished();

                                List<FileLocation> locations = recognizer.getLocations();

                                List<FileObject> rubyFiles = new ArrayList<FileObject>();
                                List<FileObject> rhtmlFiles = new ArrayList<FileObject>();

                                // Process in reverse order such that first files in the list are added last
                                // (so they will be on top)
                                for (int i = locations.size()-1; i >= 0; i--) {
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
        return NbBundle.getMessage(GenerateAction.class, "CTL_GenerateAction");
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

    private static class StatefulRecognizer extends OutputRecognizer {
        private RegexpOutputRecognizer recognizer;
        private List<FileLocation> locations = new ArrayList<FileLocation>();

        private StatefulRecognizer(RegexpOutputRecognizer recognizer) {
            this.recognizer = recognizer;
        }

        @Override
        public FileLocation processLine(String line) {
            FileLocation loc = recognizer.processLine(line);

            if (loc != null && !line.trim().startsWith("skip")) { // NOI18N
                locations.add(loc);
            }

            return loc;
        }

        public List<FileLocation> getLocations() {
            return locations;
        }
    }
}
