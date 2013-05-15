/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.makeproject.launchers;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mtishkov
 */
public final class LauncherExecutor {

    private final Launcher launcher;
    private final ProjectActionEvent.PredefinedType actionType;
    private static final Logger LOG = Logger.getLogger("LauncherExecutor");//NOI18N

    /**
     * Creates launcher for the project to be executed as project action of
     * actionType
     *
     * @param launcher
     * @param actionType
     * @param project
     * @return
     */
    public static LauncherExecutor createExecutor(Launcher launcher, ProjectActionEvent.PredefinedType actionType) {
        return new LauncherExecutor(launcher, actionType);
    }

    private LauncherExecutor(Launcher launcher, ProjectActionEvent.PredefinedType actionType) {
        this.launcher = launcher;
        this.actionType = actionType;
    }

    public void execute(final Project project) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project).clone();
                if (conf != null) {
                    RunProfile profile = conf.getProfile();

                    String runCommand = launcher.getCommand();
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
                    String[] symbolFiles;   // SymbolFiles (now the single symbol file is only supported!!)
                    if (launcher.getSymbolFiles() != null) {
                        symbolFiles = launcher.getSymbolFiles().split(","); //NOI18N
                    } else {
                        symbolFiles = new String[]{conf.getOutputValue()};
                    }
                    //executable: we will try to get linker output from laucnher
                    //if not defined will set to default value of the configuration
                    String executable = symbolFiles[0];
                    //expand macros if presented
                    executable = preprocessValueField(executable, conf);
                    // Copied from MakeConfiguration.getOutputValue()
                    if (conf.isLinkerConfiguration()) {
                        conf.getLinkerConfiguration().getOutput().setValue(executable);
                    } else if (conf.isArchiverConfiguration()) {
                        conf.getArchiverConfiguration().getOutput().setValue(executable);
                    } else if (conf.isMakefileConfiguration()) {
                        conf.getMakefileConfiguration().getOutput().setValue(executable);
                    } else if (conf.isQmakeConfiguration()) {
                    } else {
                        assert false;
                    }
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionType,
                            executable, conf,
                            profile,
                            false);
                    ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[]{projectActionEvent});
                }
            }

            // Preprocessing commands inside `` and macroses
            private String preprocessValueField(String value, MakeConfiguration conf) {
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
                    String out = value;
                    
                    do {
                        value = out;
                        out = conf.expandMacros(value);
                    } while (!out.equals(value));
                    
                    value = CndPathUtilitities.expandAllMacroses(out, "${PROJECT_DIR}", conf.getProfile().getBaseDir()); //NOI18N
                }
                return value;
            }
        });
    }
}
