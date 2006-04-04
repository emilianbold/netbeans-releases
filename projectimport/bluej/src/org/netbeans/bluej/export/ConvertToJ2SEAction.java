package org.netbeans.bluej.export;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.bluej.BluejProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class ConvertToJ2SEAction extends AbstractAction {

    private BluejProject project;
    
    public ConvertToJ2SEAction(BluejProject project) {
        putValue(NAME, getName());
        this.project = project;
    }
    
    public void actionPerformed(ActionEvent e) {
        ExportPanel panel = new ExportPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, "Convert to J2SE Project type");
        Object ret = DialogDisplayer.getDefault().notify(dd);
        //TODO make sure the user selects en empty directory
        if (NotifyDescriptor.OK_OPTION == ret) {
            try {
                doExport(panel.getNewProjectLocation());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(ConvertToJ2SEAction.class, "CTL_ConvertToJ2SEAction");
    }
    

    /**
     * lets assume the file is directory and it's empty or not existing..
     */
    private void doExport(File file) throws IOException {
        if (!file.exists()) {
            file.mkdirs();
        }
        FileObject root = FileUtil.toFileObject(file);
        AntProjectHelper helper = ProjectGenerator.createProject(root, "org.netbeans.modules.java.j2seproject"); //constant copied from J2seProjectType.java
        FileObject fo = project.getProjectDirectory();
        //TODO - there's a lot of work to be done here.. most of it is in J2SEProjectGenerator..
        // copy or use? not in APIs, implementation dependency is bad, use reflection??
        
        
        
        Project j2seproject = ProjectManager.getDefault().findProject(root);
        ProjectManager.getDefault().saveProject(j2seproject);
        
    }

    
}
