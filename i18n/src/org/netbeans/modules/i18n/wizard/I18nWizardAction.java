/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.wizard;


import java.awt.Dialog;
import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.SwingUtilities;

import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.I18nUtil;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.WizardDescriptor;


/**
 * Action which runs i18n wizard.
 *
 * @author  Peter Zavadsky
 */
public class I18nWizardAction extends NodeAction {

    /** Generated serial version UID. */
    static final long serialVersionUID = 6965968608028644524L;

    
    /** Implements superclass abstract method. 
     * @return <code>true</code> */
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    /** Actually performs action. Implements superclass abstract method. */
    public void performAction(Node[] activatedNodes) {

        WizardDescriptor wizardDesc = new I18nWizardDescriptor(
            getWizardIterator(),
            getSettings(activatedNodes)
        );

        initWizard(wizardDesc);
        
        Dialog dialog = TopManager.getDefault().createDialog(wizardDesc);
        
        dialog.show();
    }

    /** Gets wizard iterator thru panels used in wizard invoked by this action, 
     * i.e I18N wizard. */
    private WizardDescriptor.Iterator getWizardIterator() {
        ArrayList panels = new ArrayList(4);
        
        panels.add(new SourceWizardPanel.Panel());
        panels.add(new ResourceWizardPanel.Panel());
        
        if(I18nUtil.getOptions().isAdvancedWizard())
            panels.add(new AdditionalWizardPanel.Panel());
        
        panels.add(new HardStringWizardPanel.Panel());
        
        return new WizardDescriptor.ArrayIterator(
            (WizardDescriptor.Panel[])panels.toArray(new WizardDescriptor.Panel[panels.size()]));
    }

    /** Initializes wizard descriptor. */
    private void initWizard(WizardDescriptor wizardDesc) {
        // Init properties.
        wizardDesc.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        wizardDesc.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        wizardDesc.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        ArrayList contents = new ArrayList(4);
        contents.add(NbBundle.getBundle(getClass()).getString("TXT_SelectSourcesHelp"));
        contents.add(NbBundle.getBundle(getClass()).getString("TXT_SelectResourceHelp"));
        
        if(I18nUtil.getOptions().isAdvancedWizard())
            contents.add(NbBundle.getBundle(getClass()).getString("TXT_AdditionalHelp"));
        
        contents.add(NbBundle.getBundle(getClass()).getString("TXT_FoundStringsHelp"));
        
        wizardDesc.putProperty("WizardPanel_contentData", (String[])contents.toArray(new String[contents.size()])); // NOI18N
        
        wizardDesc.setTitle(NbBundle.getBundle(getClass()).getString("LBL_WizardTitle"));
        wizardDesc.setTitleFormat(new MessageFormat("{0} ({1})")); // NOI18N

        wizardDesc.setModal(false);
    }

    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return NbBundle.getBundle(getClass()).getString("LBL_WizardActionName");
    }

    /** Gets the action's icon location.
     * @return the action's icon location */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/i18nAction.gif"; // NOI18N
    }
    
    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nWizardAction.class);
    }
    
    /** Utility method. Gets settings based on selected nodes. Finds all accepted data objects. 
     * @param activatedNodes selected nodes 
     * @return map with accepted data objects as keys or empty map if no such data objec were found */
    static Map getSettings(Node[] activatedNodes) {
        Map settings = new TreeMap(new SourceData.DataObjectComparator());
        
        if(activatedNodes != null && activatedNodes.length > 0) {
            for(int i = 0; i < activatedNodes.length; i++) {
                DataObject dataObject = (DataObject)activatedNodes[i].getCookie(DataObject.class);
                
                if(dataObject == null)
                    continue;
                
                if(dataObject instanceof DataFolder) {
                    Iterator it = SourceWizardPanel.getAcceptedDataObjects((DataFolder)dataObject).iterator();
                    
                    while(it.hasNext())
                        settings.put(it.next(), null);
                } else if(FactoryRegistry.hasFactory(dataObject.getClass().getName()))                    
                    settings.put(dataObject, null);
            }
        }
        
        return settings;
    }
    
}
