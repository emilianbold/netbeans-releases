/*
 * JSFTestViewFactory.java
 *
 * Created on February 7, 2007, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

//import org.netbeans.modules.jsfmultiviewtest.*;
import org.netbeans.modules.web.jsf.navigation.JSFPageFlowMultiviewDescriptor;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorViewFactory;

/**
 *
 * @author Joelle Lam
 */
public class JSFPageFlowViewFactory implements  JSFConfigEditorViewFactory {
    
    /**
     * Creates teh MultiViewDescriptor
     * @param facesContext 
     * @return MultiViewDescription
     */
    public MultiViewDescription createMultiViewDescriptor(JSFConfigEditorContext facesContext){
        return new JSFPageFlowMultiviewDescriptor(facesContext);
    }
    
}
