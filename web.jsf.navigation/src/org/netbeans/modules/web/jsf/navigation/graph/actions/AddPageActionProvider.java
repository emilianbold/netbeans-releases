/*
 * OpenPageAction.java
 *
 * Created on March 2, 2007, 5:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.modules.web.jsf.navigation.PageFlowController;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public class AddPageActionProvider extends AbstractAction implements ContextAwareAction {
    PageFlowScene scene;
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18N
    
    /** Creates a new instance of OpenPageAction */
    public AddPageActionProvider(PageFlowScene scene) {
        putValue(NAME, getDisplayName());
        this.scene = scene;
    }
    
    /**
     *
     * @return
     */
    protected String getDisplayName() {
        return NbBundle.getMessage(AddPageActionProvider.class, "LBL_AddPage");
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            PageFlowController pfc = scene.getPageFlowView().getPageFlowController();
            
            FileObject parentFolder = pfc.getProject().getProjectDirectory();
            FileObject webFileObject = parentFolder.getFileObject(DEFAULT_DOC_BASE_FOLDER);
            
            String name = FileUtil.findFreeFileName(webFileObject, "page", "jsp");
            name = JOptionPane.showInputDialog("Select Page Name", name);
            
            createIndexJSP(webFileObject, name);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        //            }
    }
    public Action createContextAwareInstance(Lookup lookup) {
        PageFlowScene scene = lookup.lookup(PageFlowScene.class);
        return new AddPageActionProvider(scene);
    }
    
    
    /**
     * Creates a JSP in the
     * @param name
     * @throws java.io.IOException
     */
    public void createIndexJSP(FileObject targetFolder, String name ) throws IOException {
        
        FileObject jspTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/JSP_Servlet/JSP.jsp" ); // NOI18N
        
        if (jspTemplate == null)
            return; // Don't know the template
        
        
        DataObject mt = DataObject.find(jspTemplate);
        DataFolder webDf = DataFolder.findFolder(targetFolder);
        mt.createFromTemplate(webDf, name); // NOI18N
    }
    
}
