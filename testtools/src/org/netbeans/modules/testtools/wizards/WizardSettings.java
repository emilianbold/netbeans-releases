/*
 * WizardSettings.java
 *
 * Created on April 24, 2002, 3:45 PM
 */

package org.netbeans.modules.testtools.wizards;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.src.MethodElement;
import org.openide.loaders.TemplateWizard;

/**
 *
 * @author  as103278
 */
class WizardSettings extends Object {
    
    private static final String PROPERTY_NAME = "WIZARD_SETTINGS_PROPERTY";
    
    static WizardSettings get(Object o) {
        return (WizardSettings)((TemplateWizard)o).getProperty(PROPERTY_NAME);
    }
    
    void store(TemplateWizard wiz) {
        wiz.putProperty(PROPERTY_NAME, this);
    }
    
    boolean createBag = false;
    boolean createType = false;
    boolean createSuite = false;

    DataFolder workspaceTarget = null;
    String workspaceName = null;
    DataObject workspaceTemplate = null;
    DataObject workspaceScript = null;
    int workspaceLevel = -1;
    
    DataFolder typeTarget = null;
    String typeName = null;
    DataObject typeTemplate = null;

    String bagName = null;
    
    DataFolder suiteTarget = null;
    String suiteName = null;
    DataObject suiteTemplate = null;
    String suitePackage = null;

    String defaultType = null;
    String defaultAttributes = null;
    String netbeansHome = null;
    String xtestHome = null;
    String jemmyHome = null;
    String jellyHome = null;
    
    WizardIterator.CaseElement methods[];
    MethodElement templateMethods[];
}
