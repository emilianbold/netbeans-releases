/*
 * StrutsConfigLoader.java
 *
 * Created on May 7, 2005, 11:52 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.web.jsf;

import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
//import org.netbeans.modules.struts.actions.SAXParseErrorAction;

/** 
 *
 * @author Petr Pisl
 */
public class JSFConfigLoader extends UniFileLoader {
    private static final String REQUIRED_MIME_1_0 = "text/jsf-config1.0"; // NOI18N
    private static final String REQUIRED_MIME_1_1 = "text/jsf-config1.1"; // NOI18N 
   
    public JSFConfigLoader() {
        this("org.netbeans.modules.web.jsf.JSFConfigLoader");
    }
    
    // Can be useful for subclasses:
    protected JSFConfigLoader(String recognizedObjectClass) {
        super(recognizedObjectClass);
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(JSFConfigLoader.class, "LBL_loaderName");
    }
    
    protected void initialize() {
        
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME_1_0);
        getExtensions().addMimeType(REQUIRED_MIME_1_1);
    }
    
    protected SystemAction[] defaultActions() {
       return new SystemAction[] {
	    SystemAction.get(OpenAction.class),
	    SystemAction.get (FileSystemAction.class),
	    null,
	    SystemAction.get(CutAction.class),
	    SystemAction.get(CopyAction.class),
	    SystemAction.get(PasteAction.class),
	    null,
	    SystemAction.get(DeleteAction.class),
	    SystemAction.get(RenameAction.class),
            null,
            SystemAction.get (SaveAsTemplateAction.class),
	    null,
	    SystemAction.get(ToolsAction.class),
	    SystemAction.get(PropertiesAction.class),
	};
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new JSFConfigDataObject(primaryFile, this);
    }
}