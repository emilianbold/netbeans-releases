/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl;

import java.util.ResourceBundle;

import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class CaptureSchemaAction extends CallableSystemAction {

    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

    /** Create. new ObjectViewAction. */
    public CaptureSchemaAction() {
    }

    /** Name of the action. */
    public String getName () {
        return bundle.getString("ActionName"); //NOI18N
    }

    /** No help yet. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }

    protected String iconResource () {
        return "org/netbeans/modules/dbschema/jdbcimpl/DBschemaDataIcon.gif"; //NOI18N
    }

    public  void performAction() {
        try {
            TemplateWizard wizard = new TemplateWizard();
            
            DataObject templateDirs[] = wizard.getTemplatesFolder().getChildren();
            for (int i = 0; i < templateDirs.length; i++)
                if (templateDirs[i].getName().equals("Databases")) { //NOI18N
                    DataObject templates[] = ((DataFolder) templateDirs[i]).getChildren();
                    for (int j = 0; j < templates.length; j++)
                        if (templates[j].getName().equals("Database Schema")) { //NOI18N
                            Node n[] = WindowManager.getDefault().getRegistry().getActivatedNodes();
                            int nId = -1;
                            for (int k = 0; k < n.length; k++)
                                if (n[k].getCookie(DataFolder.class) instanceof DataFolder) {
                                    nId = k;
                                    break;
                                }
                            
                            wizard.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); //NOI18N
                            wizard.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
                            wizard.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N
                            String[] prop = (String[]) wizard.getProperty("WizardPanel_contentData"); // NOI18N
                            String[] stepsNames = new String[] {
                                wizard.targetChooser().getClass().toString().trim().equalsIgnoreCase("class org.openide.loaders.TemplateWizard2") ? bundle.getString("TargetLocation") :
                                    prop[0],
                                    bundle.getString("TargetLocation"),
                                    bundle.getString("ConnectionChooser"),
                                    bundle.getString("TablesChooser")
                            };
                            wizard.putProperty("WizardPanel_contentData", stepsNames); //NOI18N
                            wizard.setTitle(bundle.getString("WizardTitleName"));
                            
                            if(nId >= 0) {
                                wizard.setTargetFolder((DataFolder) n[nId].getCookie(DataFolder.class));
                                wizard.instantiate(templates[j]);
                            } else
                                wizard.instantiate(templates[j]);
                            
                            break;
                        }
                    break;
                }
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }
}
