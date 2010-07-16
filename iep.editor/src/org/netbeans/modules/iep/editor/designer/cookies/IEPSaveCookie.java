package org.netbeans.modules.iep.editor.designer.cookies;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.netbeans.api.project.FileOwnerQuery;

import org.netbeans.modules.iep.editor.PlanEditorSupport;

import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

public class IEPSaveCookie implements SaveCookie {
    
    private Logger mLogger = Logger.getLogger(IEPSaveCookie.class.getName());
    
    private PlanEditorSupport mEditorSupport;
    
    //by default we want to disable wsdl generation
    //if only things which has changed is x, y location or documentation,
    //then wsdl generation remains false.
    private boolean mGenerateWsdl = false;
    
    private IEPModelListener mModelListener = null;
    
    public IEPSaveCookie(PlanEditorSupport editorSupport) {
        this.mEditorSupport = editorSupport;
        
        if(this.mEditorSupport.getModel() != null) {
            this.mModelListener = new IEPModelListener();
            this.mEditorSupport.getModel().addComponentListener(this.mModelListener);
        }
    }
    
    public void cleanup() {
	if(this.mEditorSupport.getModel() != null) {
	    if(this.mModelListener != null) {
		this.mEditorSupport.getModel().removeComponentListener(this.mModelListener);
		}
	}
	
    }
    
    public void save() throws IOException {
        try {
            
            //first save iep file if everything is fine
            this.mEditorSupport.saveDocument();
            
            try {
                //first generate wsdl
                File wsdlFile = this.mEditorSupport.getModel().getWsdlFile();
                
                //generate abstract wsdl or not
                //
                boolean alwaysGenerateAbstractWsdl = false;
                
                //find project
                Project project = FileOwnerQuery.getOwner(this.mEditorSupport.getDataObject().getPrimaryFile());
                if(project != null) {
                 AntProjectHelper h = project.getLookup().lookup(AntProjectHelper.class);    
                 EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);   
                    //check always.generate.abstract.wsdl property see iep project
                    //for this property to available
                 String val  = ep.getProperty("always.generate.abstract.wsdl");
                 if(val != null && val.equals("true")) {
                     alwaysGenerateAbstractWsdl = true;
                 }
                 
                }
                //if mGenerateWsdl is true then we need to generate wsdl.
                if(!mGenerateWsdl) {
                    //if there is no wsdl file exist then we should
                    //at least generate it once.
                    if(!wsdlFile.exists()) {
                        //generate and save wsdl
                        this.mEditorSupport.getModel().saveWsdl(alwaysGenerateAbstractWsdl);
                        selectWSDLFile(wsdlFile);
                    }
                } else {
                    mGenerateWsdl = false;
                    //generate and save wsdl
                    this.mEditorSupport.getModel().saveWsdl(alwaysGenerateAbstractWsdl);
                    selectWSDLFile(wsdlFile);
                }
            } catch (Exception ex) {
                mLogger.log(Level.INFO, "Failed to save wsdl", ex);
                //show error
                String msg = ex.getMessage();
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
            }
            
            
        } catch (Exception ex) {
            mLogger.log(Level.INFO, "Failed to save ", ex);
            //show error
            String msg = NbBundle.getMessage(IEPSaveCookie.class, "IEPSaveCookie_Failed_to_save", ex.getMessage());
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
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
