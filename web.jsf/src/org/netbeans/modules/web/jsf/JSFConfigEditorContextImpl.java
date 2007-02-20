/*
 * JSFConfigContextImpl.java
 *
 * Created on February 9, 2007, 11:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf;

import java.io.Serializable;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author petr
 */
public class JSFConfigEditorContextImpl implements JSFConfigEditorContext, Serializable{
    static final long serialVersionUID = -4802489998350639459L;

    JSFConfigDataObject jsfDataObject;
    /** Creates a new instance of JSFConfigContextImpl */
    public JSFConfigEditorContextImpl(JSFConfigDataObject data) {
        jsfDataObject = data;
    }
    
    public FileObject getFacesConfigFile() {
        return jsfDataObject.getPrimaryFile();
    }

    public UndoRedo getUndoRedo() {
        return jsfDataObject.getEditorSupport().getUndoRedoManager();
    }

    public void setMultiViewTopComponent(TopComponent topComponent) {
        jsfDataObject.getEditorSupport().setMVTC(topComponent);
    }

}
