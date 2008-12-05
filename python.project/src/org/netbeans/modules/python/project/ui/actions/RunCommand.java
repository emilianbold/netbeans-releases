
package org.netbeans.modules.python.project.ui.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.PythonProjectUtil;
import org.netbeans.modules.python.project.ui.customizer.PythonProjectProperties;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.python.project.ui.Utils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author alley
 */
public class RunCommand extends Command {
    private static final String COMMAND_ID = ActionProvider.COMMAND_RUN;
    

    public RunCommand(PythonProject project) {
        super(project);        
    }

    @Override
    public String getCommandId() {
        return COMMAND_ID;
    }

    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
        final PythonProject pyProject = getProject();
        final PythonPlatform platform = checkProjectPythonPlatform(pyProject);
        if ( platform == null )
          return ; // invalid platform user has been warn in check so safe to return
        
        if (getProperties().getMainModule() == null ||
                getProperties().getMainModule().equals("")){
            String main = Utils.chooseMainModule(getProject().getSourceRoots().getRoots());
            getProperties().setMainModule(main);
            getProperties().save();
        }
        //System.out.println("main module " + getProperties().getMainModule());
        FileObject script = findMainFile(pyProject);
        final FileObject parent = script.getParent();
        assert script != null;

        final PythonExecution pyexec = new PythonExecution();
        pyexec.setDisplayName (ProjectUtils.getInformation(pyProject).getDisplayName());                
        //Set work dir - probably we need a property to store work dir
        String path = FileUtil.toFile(parent).getAbsolutePath();
        pyexec.setWorkingDirectory(path);        
        pyexec.setCommand(platform.getInterpreterCommand());
        //Set python script
        path = FileUtil.toFile(script).getAbsolutePath();
        pyexec.setScript(path);
        pyexec.setCommandArgs(platform.getInterpreterArgs());
        pyexec.setScriptArgs(pyProject.getEvaluator().getProperty(PythonProjectProperties.APPLICATION_ARGS));
        //build path & set 
        //build path & set
        pyexec.setPath(PythonPlatform.buildPath(super.buildPythonPath(platform,pyProject)));
        pyexec.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform,pyProject)));
        pyexec.setShowControls(true);
        pyexec.setShowInput(true);
        pyexec.setShowWindow(true);
        pyexec.run();
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
//        final PythonProject pyProject = getProject();
//        PythonPlatform platform = PythonProjectUtil.getActivePlatform(pyProject);
//        if (platform == null) {
//            return false;
//        }
//        else{
//            return true;
//        }
//        final FileObject fo = findMainFile (pyProject);
//        if (fo == null) {
//            return false;
//        }
//        return PythonMIMEResolver.PYTHON_MIME_TYPE.equals(fo.getMIMEType());
        return true;
    }
    
    protected static FileObject findMainFile (final PythonProject pyProject) {
        final FileObject[] roots = pyProject.getSourceRoots().getRoots();
        final String mainFile = pyProject.getEvaluator().getProperty(PythonProjectProperties.MAIN_FILE);
        if (mainFile == null) {
            return null;
        }
        FileObject fo = null;
        for (FileObject root : roots) {
            fo = root.getFileObject(mainFile);
            if (fo != null) {
                break;
            }
        }
        return fo;
    }

}
