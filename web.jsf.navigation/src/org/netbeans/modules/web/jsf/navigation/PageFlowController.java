/*
 * PageFlowController.java
 *
 * Created on March 1, 2007, 1:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;

/**
 *
 * @author joelle
 */
public class PageFlowController { 
    private static JSFConfigModel configModel;
    
    /** Creates a new instance of PageFlowController 
     * @param scene 
     */
    private PageFlowController(JSFConfigModel configModel ) {
        this.configModel = configModel;        
    }
    /** Add a new page
     *  Simply creates a rule with the speciifed from ID
     * @param pageName
     * @return boolean 
     **/
    private boolean addPage(String pageName) {
        FacesConfig facesConfig = configModel.getRootComponent();
        NavigationRule navRule = facesConfig.getModel().getFactory().createNavigationRule();
        navRule.setFromViewId(pageName);
        facesConfig.addNavigationRule(navRule);        
        return true;
    }   
    


}
