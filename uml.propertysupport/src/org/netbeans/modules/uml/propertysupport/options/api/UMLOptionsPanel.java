/*
 * UMLOptionsPanel.java
 *
 * Created on November 15, 2005, 9:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.propertysupport.options.api;

import java.util.Hashtable;
import javax.swing.JComponent;

/**
 *
 * @author krichard
 */
public interface UMLOptionsPanel {
    
    /**
     * 
     */
    public void applyChanges() ;
    /**
     * 
     * @return 
     */
    public JComponent create() ;
    /**
     * 
     * @return 
     */
    public Hashtable getCurrentValues() ;
    /**
     * 
     * @return 
     */
    public Hashtable getUpdatedValues() ;
    /**
     * 
     * @return 
     */
    public String getDisplayName() ;
    
}
