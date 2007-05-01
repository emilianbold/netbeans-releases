/*
 * OptionsPanel.java
 *
 * Created on November 16, 2005, 12:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.propertysupport.options;

import org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel;
import java.util.Hashtable;
import javax.swing.JComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author krichard
 */
public class MiscOptionsPanel implements UMLOptionsPanel {
    
  
    private final boolean debug = true ;
    private JComponent form = null ;
    
    /** Creates a new instance of OptionsPanel */
    public MiscOptionsPanel() {
        log("MiscOptionsPanel");
    }
    
    public void applyChanges() {
        Hashtable updates = this.getUpdatedValues() ;
        if (getCurrentValues().equals ( updates )  ) return ;
        
//        settings.applyChanges ( updates ) ;
    }
    
    public JComponent create() {
        form = new UMLMiscOptionsPanelForm() ;
        
        return form ;
    }
    
    public Hashtable getCurrentValues() {
        Hashtable p = new Hashtable() ;
        
//        p = settings.getProperties() ;
        
        return p ;
    }
    
    public Hashtable getUpdatedValues() {
        Hashtable p = new Hashtable() ;
        
        //p = form.getProperties() ;
        
        return p ;
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
