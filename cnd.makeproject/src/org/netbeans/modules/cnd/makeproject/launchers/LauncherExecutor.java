/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.makeproject.launchers;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ui.UIGesturesSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mtishkov
 */
public final class LauncherExecutor {

    private final Launcher launcher;
    private final ProjectActionEvent.PredefinedType actionType;
    private static final Logger LOG = Logger.getLogger("LauncherExecutor");//NOI18N
    private final ExecutionListener listener;
    private enum State{RUNNING, STOPPED};
    private State state = State.STOPPED;
    
    private static final String USG_CND_LAUNCHER_EXECUTOR = "USG_CND_LAUNCHER_EXECUTOR";    //NOI18N

    /**
     * Creates launcher for the project to be executed as project action of
     * actionType
     *
     * @param launcher
     * @param actionType
     * @param project
     * @return
     */
    public static LauncherExecutor createExecutor(Launcher launcher, ProjectActionEvent.PredefinedType actionType, ExecutionListener listener) {
        return new LauncherExecutor(launcher, actionType, listener);
    }

    private LauncherExecutor(Launcher launcher, ProjectActionEvent.PredefinedType actionType, ExecutionListener listener) {
        this.launcher = launcher;
        this.actionType = actionType;
        this.listener = listener;
    }
    
    // Preprocessing commands inside `` and macroses
    private static String preprocessValueField(String value, MakeConfiguration conf) {
        value = value.trim();
        if (value.startsWith("`") && value.endsWith("`")) { //NOI18N
            final String command = value.substring(1, value.length() - 1);
            final String[] execAndArgs = command.split(" ");    //NOI18N
            final String exec = execAndArgs[0];
            final String[] args = Arrays.copyOfRange(execAndArgs, 1, execAndArgs.length);

            final NativeProcessBuilder builder = NativeProcessBuilder.newProcessBuilder(
                    conf.getFileSystemHost()).setExecutable(exec).setArguments(args);
            final ProcessUtils.ExitStatus status = ProcessUtils.execute(builder);
            if (status.isOK()) {
                value = status.output;
            } else {
                LOG.info(status.error);
            }
        } else {
            value = conf.expandMacros(value);
        }
        return value;
    }
    
    private static MakeConfigurationDescriptor getProjectDescriptor(Project project) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        return pdp.getConfigurationDescriptor();
    }
    
    private static String getMakeCommand(MakeConfigurationDescriptor pd, MakeConfiguration conf) {
        String cmd;
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        if (cs != null) {
            cmd = cs.getTool(PredefinedToolKind.MakeTool).getPath();
        } else {
            CndUtils.assertFalse(true, "Null compiler collection"); //NOI18N
            cmd = "make"; // NOI18N
        }
        //cmd = cmd + " " + MakeOptions.getInstance().getMakeOptions(); // NOI18N
        return cmd;
    }
    
    private static String removeQuotes(String command) {
        if (command.startsWith("\"") && command.endsWith("\"")) { // NOI18N
            return command.substring(1, command.length() - 1);
        } else if (command.startsWith("'") && command.endsWith("'")) { // NOI18N
            return command.substring(1, command.length() - 1);
        }
        return command;
    }

    private static int getArgsIndex(String command) {
        boolean inQuote = false;
        int quote = 0;
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            switch (c) {
                case ' ':
                    if (!inQuote) {
                        return i;
                    }
                    break;
                case '\'':
                case '"':
                    if (inQuote) {
                        if (quote == c) {
                            quote = 0;
                            inQuote = false;
                        }
                    } else {
                        quote = c;
                        inQuote = true;
                    }
                    break;
            }
        }
        return -1;
    }
    
    private void onBuild(final Project project) {
        MakeConfigurationDescriptor pd = getProjectDescriptor(project);
        MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project).clone();

        MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
        String buildCommand = launcher.getBuildCommand();
        if (buildCommand == null) {
            String makeCommand = getMakeCommand(pd, conf);
            buildCommand = makeArtifact.getBuildCommand(makeCommand, ""); // NOI18N
        } else {
            //expand macros if presented
            buildCommand = preprocessValueField(buildCommand, conf);
        }
        String args = "";
        int index = getArgsIndex(buildCommand);
        if (index > 0) {
            args = buildCommand.substring(index + 1);
            buildCommand = removeQuotes(buildCommand.substring(0, index));
        }
        Map<String, String> env = launcher.getEnv();    //Environment
        Env e = new Env();
        if (env != null) {
            for (String key : env.keySet()) {
                String value = env.get(key);
                value = preprocessValueField(value, conf);
                e.putenv(key, value);
            }
        }
        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform(), conf);
        profile.setArgs(args);
        profile.setEnvironment(e);
        Lookup context = Lookups.fixed(new ExecutionListenerImpl());
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                project, 
                actionType, 
                buildCommand, conf, 
                profile, 
                true, context);
        ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[]{projectActionEvent});
    }
    
    private void onDefault(Project project) {
        MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project).clone();
        if (conf != null) {
            RunProfile profile = conf.getProfile();

            String runCommand = launcher.getCommand();
            runCommand = preprocessValueField(runCommand, conf);
            profile.getRunCommand().setValue(runCommand);     //RunCommand
            String runDir;    //RunDir
            //use run dir from the launcher if exists, use default from RunProfile otherwise
            if (launcher.getRunDir() != null) {
                runDir = launcher.getRunDir();
            } else {
                runDir = profile.getBaseDir();
            }
            runDir = preprocessValueField(runDir, conf);
            profile.setRunDir(runDir);
            Map<String, String> env = launcher.getEnv();    //Environment
            Env e = new Env();
            if (env != null) {
                for (String key : env.keySet()) {
                    String value = env.get(key);
                    value = preprocessValueField(value, conf);
                    e.putenv(key, value);
                }
            }
            profile.setEnvironment(e);
            String executable = ""; //NOI18N
            if (launcher.getSymbolFiles() != null) {
                // SymbolFiles (now the single symbol file is only supported!!)
                executable = launcher.getSymbolFiles().split(",")[0]; //NOI18N
                
                //expand macros if presented
                executable = preprocessValueField(executable, conf);
            }
            
            Lookup context = Lookups.fixed(new ExecutionListenerImpl(), executable);
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                    project,
                    actionType,
                    executable, conf,
                    profile,
                    false, context);
            ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[]{projectActionEvent});
        }
    }

    public void execute(final Project project) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                switch (actionType) {
                    case BUILD:
                        onBuild(project);
                        break;
                    default:
                        onDefault(project);
                        break;
                }
            }
        });
        UIGesturesSupport.submit(USG_CND_LAUNCHER_EXECUTOR, actionType);
    }
    
    public boolean isRunning() {
        return state.equals(State.RUNNING);
    }

    private final class ExecutionListenerImpl implements ExecutionListener {

        @Override
        public void executionStarted(int pid) {
            state = State.RUNNING;
            listener.executionStarted(pid);
        }

        @Override
        public void executionFinished(int rc) {
            state = State.STOPPED;
            listener.executionFinished(rc);
        }
    }
}
