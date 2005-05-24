/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CommonPropertyPanel.java
 *
 * Created on November 19, 2003, 12:33 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;

/**
 *
 * @author  nityad
 */
public class CommonPropertyPanel implements WizardDescriptor.Panel, WizardConstants{
    
    private CommonPropertyVisualPanel component;
    private ResourceConfigHelper helper;
    private Wizard wiz;
    boolean firstTime = false;
    /** Creates a new instance of CommonPropertyPanel */
    public CommonPropertyPanel(ResourceConfigHelper helper, Wizard wiz) {
        this.helper = helper;
        this.wiz = wiz;
    }
    
     // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CommonPropertyVisualPanel(this);
        }
        return component;
    }
    
    public void setInitialFocus(){
        if(component != null) {
            component.refreshFields();
            component.setInitialFocus();
        }
    }
    
    public FieldGroup getFieldGroup(String groupName) {
        return FieldGroupHelper.getFieldGroup(wiz, groupName);
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public HelpCtx getHelp() {
        if (wiz.getName().equals(__JdbcResource)) {
            return new HelpCtx("AS_Wiz_DataSource_props"); //NOI18N
        }else if (wiz.getName().equals(__PersistenceManagerFactoryResource)) {
            return new HelpCtx("AS_Wiz_PMF_props"); //NOI18N
        }else {
            return HelpCtx.DEFAULT_HELP;
        }
    }
    
    public boolean isValid() {
        ResourceConfigData data = helper.getData();
        Vector vec = data.getProperties();
        for (int i = 0; i < vec.size(); i++) {
            NameValuePair pair = (NameValuePair)vec.elementAt(i);
            if (pair.getParamName() == null || pair.getParamValue() == null ||
            pair.getParamName().length() == 0 || pair.getParamValue().length() == 0)
                return false;
        }
        return true;
    }
    
   private final Set listeners = new HashSet(1); 
    
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
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        if(firstTime){
            ChangeEvent ev = new ChangeEvent(this);
            while (it.hasNext()) {
                ((ChangeListener) it.next()).stateChanged(ev);
            }
        }else{
            firstTime = true;
        }    
    }
    
     // You can use a settings object to keep track of state.
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
    }
    public void storeSettings(Object settings) {
    }
    
}
