/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.project.ui.actions;

import javax.swing.JOptionPane;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;

import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.PythonProjectUtil;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.Lookup;

/**
 *
 * @author alley
 */
public class RunSingleCommand extends Command {
    PythonPlatformManager manager = PythonPlatformManager.getInstance();
    public RunSingleCommand(PythonProject project) {
        super(project);
    }

        
    @Override
    public String getCommandId() {
        return ActionProvider.COMMAND_RUN_SINGLE;
    }

    @Override
    public void invokeAction(Lookup context) throws IllegalArgumentException {
        Node[] activatedNodes = getSelectedNodes();
        DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
        if (gdo.getPrimaryFile().getMIMEType().equals(PythonMIMEResolver.PYTHON_MIME_TYPE) ){
            String path = FileUtil.toFile(gdo.getPrimaryFile().getParent()).getAbsolutePath();
            // String workingdir = FileUtil.toFile(getProject().getSrcFolder()).getAbsolutePath();
            //int pos = path.lastIndexOf("/");
            //path = path.substring(0, pos);
            String script = FileUtil.toFile(gdo.getPrimaryFile()).getAbsolutePath();
            //System.out.println("Folder " + path);
            //System.out.println("File " + script);
            PythonExecution pyexec = new PythonExecution();
            pyexec.setDisplayName(gdo.getName());
            pyexec.setWorkingDirectory(path);
            if(PythonOptions.getInstance().getPromptForArgs()){
               String args =  JOptionPane.showInputDialog("Enter the args for this script.", "");
               pyexec.setScriptArgs(args);
               
            }
            final PythonProject pyProject = getProject();
            final PythonPlatform platform = checkProjectPythonPlatform(pyProject);
            if ( platform == null )
              return ; // invalid platform user has been warn in check so safe to return
            pyexec.setCommand(platform.getInterpreterCommand());
            pyexec.setScript(script);
            pyexec.setCommandArgs(platform.getInterpreterArgs());
            pyexec.setPath(PythonPlatform.buildPath(super.buildPythonPath(platform,pyProject)));
            pyexec.setJavaPath(PythonPlatform.buildPath(super.buildJavaPath(platform,pyProject)));
            pyexec.setShowControls(true);
            pyexec.setShowInput(true);
            pyexec.setShowWindow(true);
            pyexec.run();
        }
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        boolean results = false; //super.enable(activatedNodes);
        Node[] activatedNodes = getSelectedNodes();
        if(activatedNodes != null && activatedNodes.length > 0){
            DataObject gdo = activatedNodes[0].getLookup().lookup(DataObject.class);
            if(gdo != null && gdo.getPrimaryFile() != null)
                results = gdo.getPrimaryFile().getMIMEType().equals(
                        PythonMIMEResolver.PYTHON_MIME_TYPE);
        }
        return results;
    }


}
