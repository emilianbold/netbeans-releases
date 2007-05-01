/*
 * OptionsPanel.java
 *
 * Created on November 16, 2005, 12:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.propertysupport.options;

import org.openide.util.NbBundle;

/**
 *
 * @author krichard
 */
public class MiscOptionsPanel {
    
  
    private final boolean debug = true ;
    private UMLMiscOptionsPanelForm form = null ;
    
    /** Creates a new instance of OptionsPanel */
    public MiscOptionsPanel() {
        log("MiscOptionsPanel");
    }
    
    
    public UMLMiscOptionsPanelForm create() {
        if (form != null) 
            return form;
        else
            form = new UMLMiscOptionsPanelForm() ;
        
        return form ;
    }
    
    public UMLMiscOptionsPanelForm getPanel() {
        return create();
    }
    
    
    public String getDisplayName() {
        return loc("MISC_OPTIONS") ;
    }
    
    private String loc(String key) {
        return NbBundle.getMessage(DefaultOptionsPanel.class, key) ;
    }
    
    private void log (String s) {
        if (debug) System.out.println (this.getClass().toString()+"::"+s);
    }
}
