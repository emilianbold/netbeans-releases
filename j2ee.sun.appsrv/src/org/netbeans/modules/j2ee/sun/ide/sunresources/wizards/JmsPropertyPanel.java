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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * JmsPropertyPanel.java
 *
 * Created on December 12, 2002
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.util.HashMap;
import java.util.Vector;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;


/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  Jennifer Chou
 */
public class JmsPropertyPanel extends ResourceWizardPanel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private JmsPropertyVisualPanel component;
    private ResourceConfigHelper helper;
    private Wizard wiz;
        
    /** Create the wizard panel descriptor. */
    public JmsPropertyPanel(ResourceConfigHelper helper, Wizard wiz) {
        this.helper = helper;
        this.wiz = wiz;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new JmsPropertyVisualPanel(this);
        }
        return component;
    }
    
    public void refreshFields(){
        if(component != null){
            component.refreshFields();
            component.setInitialFocus();
        }    
    }
    
    public FieldGroup getFieldGroup(String groupName) {
        return FieldGroupHelper.getFieldGroup(wiz, groupName); 
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("AS_Wiz_JMS_props"); //NOI18N
    }
    
    public boolean isValid() {
        setErrorMsg(bundle.getString("Empty_String"));
        ResourceConfigData data = helper.getData();
        Vector vec = data.getProperties();
        String resType = data.getString(__ResType);
        if (resType.equals("javax.jms.Queue")||resType.equals("javax.jms.Topic")) {  //NO18N
            HashMap map = getHashMap(vec);
            if(! map.containsKey(WizardConstants.__AdminObjPropertyName)){
                setErrorMsg(bundle.getString("Err_AOName"));
                return false;
            }
        }
        for (int i = 0; i < vec.size(); i++) {
            NameValuePair pair = (NameValuePair)vec.elementAt(i);
            if (pair.getParamName() == null || pair.getParamValue() == null ||
                    pair.getParamName().length() == 0 || pair.getParamValue().length() == 0){
                setErrorMsg(bundle.getString("Err_InvalidNameValue"));
                return false;
            }
        }
        return true;
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
       
    private HashMap getHashMap(Vector vec){
        HashMap map = new HashMap();
        for (int i = 0; i < vec.size(); i++) {
            NameValuePair pair = (NameValuePair)vec.elementAt(i);
            String paramName = pair.getParamName();
            if (paramName != null && paramName.length() != 0)
                map.put(paramName, pair.getParamValue());
        }
        return map;
    }
}
