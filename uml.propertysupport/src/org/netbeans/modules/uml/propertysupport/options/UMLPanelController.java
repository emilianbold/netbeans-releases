/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * MyPanelController.java
 *
 * Created on October 28, 2005, 3:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.propertysupport.options;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
/**
 *
 * @author krichard
 */
public class UMLPanelController extends OptionsPanelController {
    
        
    private boolean changed = false ;
    private static final boolean debug = false ;
            
    
    /**
     * Creates a new instance of UMLPanelController
     */
    public UMLPanelController() {
        if (debug) log("MyPanelController");
    }
    
    
    public void update() {
        changed = false ;
        if (debug) log("update");
    }
    
    public void applyChanges() {
        changed = true ;
        if (debug) log("applyChanges");        
    }    
    
    public void cancel() {
        if (debug) log("cancel");
    }
    
    public boolean isValid() {
        if (debug) log("isValid");
        return true ;
    }
    
    public boolean isChanged() {
        if (debug) log("isChanged");
        return changed ;
    }
    
    public JComponent getComponent() {
        if (debug) log("getComponent");
        return new UMLOptionsPanelForm() ;
    }
    
    public HelpCtx getHelpCtx() {
        if (debug) log("getHelpCtx");
        return HelpCtx.DEFAULT_HELP ;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (debug) log("addPropertyChangeListener::"+propertyChangeListener.toString());
                
    }
    
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        if (debug) log("removePropertyChangeListener");
                
    }
    
    private static void log(String s) {
        System.out.println("MyPanelController::"+s);
    }

    public JComponent getComponent(Lookup lookup) {
        return this.getComponent() ;
    }
    
}
