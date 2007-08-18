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
    
    
    public Action createContextAwareInstance(Lookup lookup) {
        Action addPageAction = null;
        final PageFlowScene scene = lookup.lookup(PageFlowScene.class);
        if( scene != null) {
            setEnabled(true);
            addPageAction = new AddPageAction(scene);
        } else {
            setEnabled(false);
        }
        return addPageAction;
    }
  
//    @Override
//    public boolean isEnabled() {
//        return super.isEnabled();
//    }

    
    
    public void actionPerformed(ActionEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private static final String LBL_AddPage =  NbBundle.getMessage(AddPageActionProvider.class, "LBL_AddPage");
    private class AddPageAction extends AbstractAction {
        
        private final PageFlowScene scene;
        
        /** Creates a new instance of OpenPageAction
         * @param scene
         */
        public AddPageAction(PageFlowScene scene) {
            super();
            putValue(NAME, LBL_AddPage);
            this.scene = scene;
        }
        
        /**
         *
         * @return The Display Name of this option.
         */
        protected String getDisplayName() {
            return LBL_AddPage;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
                final PageFlowController pfc = scene.getPageFlowView().getPageFlowController();
                
                final FileObject webFileObject = pfc.getWebFolder();
                
                String name = FileUtil.findFreeFileName(webFileObject, "page", "jsp");
                name = JOptionPane.showInputDialog("Select Page Name", name);
                
                createIndexJSP(webFileObject, name);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            //            }
        }
        
        private void createIndexJSP(FileObject targetFolder, String name ) throws IOException {
            
            final FileObject jspTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/JSP_Servlet/JSP.jsp" ); // NOI18N
            
            if (jspTemplate == null) {
                return; // Don't know the template
            }
            
            final DataObject mt = DataObject.find(jspTemplate);
            final DataFolder webDf = DataFolder.findFolder(targetFolder);
            mt.createFromTemplate(webDf, name); // NOI18N
        }
    }
    
    
}
