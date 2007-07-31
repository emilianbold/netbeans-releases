/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.wizard;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class RetrieveXMLResourceWizardPanel1 implements WizardDescriptor.Panel, ChangeListener{
    
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private RetrieveXMLResourceVisualPanel1 retrieveXMLResourceVisualPanel1;
    WizardDescriptor wizd = null;
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new RetrieveXMLResourceVisualPanel1(this);
            retrieveXMLResourceVisualPanel1 = (RetrieveXMLResourceVisualPanel1)component;
            retrieveXMLResourceVisualPanel1.addChangeListener(this);
        }
        return component;
    }
    
    public WizardDescriptor getWizardDescriptor(){
        return wizd;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        //return HelpCtx.DEFAULT_HELP;
        return new HelpCtx(RetrieveXMLResourceWizardPanel1.class);
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        if(retrieveXMLResourceVisualPanel1.getSelectedSourceType() == RetrieveXMLResourceVisualPanel1.SourceType.LOCAL_FILE){
            if(retrieveXMLResourceVisualPanel1.getSourceLocation() == null) {
                return false;
            }
            File sourceFile = null;
            try {
                sourceFile = new File(new URI(retrieveXMLResourceVisualPanel1.getSourceLocation()));
            } catch (URISyntaxException ex) {
            }
            if(!sourceFile.isDirectory()){
                return false;
            }
            
            if(retrieveXMLResourceVisualPanel1.getSaveLocation() == null){
                return false;
            }
            return true;
        }
        if(retrieveXMLResourceVisualPanel1.getSelectedSourceType() == RetrieveXMLResourceVisualPanel1.SourceType.URL_ADDR){
            String sourceLocation = retrieveXMLResourceVisualPanel1.getSourceLocation();
            if((sourceLocation != null) && (sourceLocation.length() <= 0)){
                return false;
            }
            try {
                URL url = new URL(sourceLocation);
                if(url.getPath().trim().length() <= 0){
                    return false;
                }                
                if(url.getProtocol().equalsIgnoreCase("http") && //NOI18N
                   (url.getHost().length()<= 0)) {
                    return false;
                }
            } catch (MalformedURLException ex) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    private void setErrorMessage( String key ) {
        if ( key == null ) {
            wizd.putProperty("WizardPanel_errorMessage", ""); // NOI18N
        } else {
            String message = NbBundle.getMessage(
                    RetrieveXMLResourceWizardPanel1.class, key); // NOI18N
            wizd.putProperty("WizardPanel_errorMessage", message);                    
        }
    }
        
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wizd = (WizardDescriptor) settings;
        retrieveXMLResourceVisualPanel1.refreshSaveLocation();
        //System.out.println("READDDD :"+settings);
    }
    public void storeSettings(Object settings) {
        //System.out.println("Storeeeeeee");
        wizd = (WizardDescriptor) settings;
        wizd.putProperty(IConstants.RETRIVE_CLOSURE_KEY, Boolean.valueOf(retrieveXMLResourceVisualPanel1.retrieveClosure()));
        wizd.putProperty(IConstants.SOURCE_LOCATION_KEY, retrieveXMLResourceVisualPanel1.getSourceLocation());
        wizd.putProperty(IConstants.TARGET_FILE_KEY, retrieveXMLResourceVisualPanel1.getSaveLocation());
        wizd.putProperty(IConstants.SOURCE_TYPE_KEY, retrieveXMLResourceVisualPanel1.getDocType());
        wizd.putProperty(IConstants.SOURCE_LOCATION_TYPE_KEY, retrieveXMLResourceVisualPanel1.getSelectedSourceType());
        wizd.putProperty(IConstants.USER_SELECTED_SAVE_ROOT, retrieveXMLResourceVisualPanel1.getSelectedSaveRootFolder());
        wizd.putProperty(IConstants.OVERWRITE_FILES, retrieveXMLResourceVisualPanel1.shouldOverwrite());
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}

