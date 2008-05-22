package org.netbeans.modules.iep.editor.designer.cookies;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.netbeans.api.project.FileOwnerQuery;

import org.netbeans.modules.iep.editor.PlanEditorSupport;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponentContainer;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public class IEPSaveCookie implements SaveCookie {
    
    private Logger mLogger = Logger.getLogger(IEPSaveCookie.class.getName());
    
    private PlanEditorSupport mEditorSupport;
    
    //by default we want to disable wsdl generation
    //if only things which has changed is x, y location or documentation,
    //then wsdl generation remains false.
    private boolean mGenerateWsdl = false;
    
    public IEPSaveCookie(PlanEditorSupport editorSupport) {
        this.mEditorSupport = editorSupport;
        
        if(this.mEditorSupport.getModel() != null) {
            this.mEditorSupport.getModel().addComponentListener(new IEPModelListener());
        }
    }
    
    public void save() throws IOException {
        try {
            //first always save iep file
            this.mEditorSupport.saveDocument();
            
            File wsdlFile = this.mEditorSupport.getModel().getWsdlFile();
            
            //if mGenerateWsdl is true then we need to generate wsdl.
            if(!mGenerateWsdl) {
                //if there is no wsdl file exist then we should
                //at least generate it once.
                if(!wsdlFile.exists()) {
                    //generate and save wsdl
                    this.mEditorSupport.getModel().saveWsdl();
                    selectWSDLFile(wsdlFile);
                }
            } else {
                mGenerateWsdl = false;
                //generate and save wsdl
                this.mEditorSupport.getModel().saveWsdl();
                selectWSDLFile(wsdlFile);
            }
            
            
            
            
            
            
            
        } catch (Exception ex) {
            mLogger.log(Level.SEVERE, "Failed to save ", ex);
            ErrorManager.getDefault().log(ex.getMessage());
        }
        
    }

    private void selectWSDLFile(File wsdlFile) {
        if(wsdlFile.exists()) {
            FileObject wFile =  FileUtil.toFileObject(wsdlFile);
            //call getOwner so that first time when wsdl is
            //created project can refresh and show it immediately
            FileOwnerQuery.getOwner(wFile);
        }
    }
    
    class IEPModelListener implements ComponentListener {
        
        public void childrenAdded(ComponentEvent evt) {
            mGenerateWsdl = true; 
           
        }
        
        public void childrenDeleted(ComponentEvent evt) {
            mGenerateWsdl = true;
        }
        
        public void valueChanged(ComponentEvent evt) {
            Object source = evt.getSource();
            
            if(source instanceof Property) {
                Property p = (Property) source;
                String propName = p.getName();
                if(OperatorComponent.PROP_X.equals(propName)
                   ||OperatorComponent.PROP_Y.equals(propName)
                   ||OperatorComponent.PROP_Z.equals(propName)) {
                    //if mGenerateWsdl is true previously then we
                    //have to generate wsdl, no matter if later xyz location
                    //is updated
                    mGenerateWsdl = mGenerateWsdl ? true : false;
                } else {
                    mGenerateWsdl = true;
                }
                
            } else if(source instanceof Documentation) {
                //if mGenerateWsdl is true previously then we
                //have to generate wsdl., no matter if later xyz location
                //is updated
                mGenerateWsdl = mGenerateWsdl ? true : false;
            } else {
                mGenerateWsdl = true;
            }
        }
    }
}
