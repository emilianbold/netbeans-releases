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

package org.netbeans.installer;

import com.installshield.product.i18n.ProductResourcesConst;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.LocalizedStringResolver;
import com.installshield.util.Log;
import com.installshield.util.StringUtils;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.WizardPanel;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.WizardServicesUI;
import com.installshield.wizard.service.file.FileService;

import java.io.IOException;
import java.util.Vector;

public class DirectoryChooserPanel extends WizardPanel // implements ActionListener
{
    /**
     * The caption that appears above the destination field.
     */
    private String destinationCaption = "";
    
    /**
     * The caption of the browse button.
     */
    private String browseCaption = "";
    
    /**
     * Transient field for storing current destination value. This value is updated
     * by the panel's current impl via setDestination.
     */
    private String destination = "";
    
    /**
     * The caption that appears above the list field.
     */
    private String destinationsCaption = "";
    
    /**
     * Transient field for storing list of valid destination values. This value is updated
     * by the panel's current impl via setDestinations.
     */
    protected Vector destinations = new Vector();
    
    /**
     * The caption that appears in the case the destinations list is empty.
     */
    private String destinationMessage = "";
    
    /**
     * Holds value of property selectedDestination.
     */
    private String selectedDestinationIndex = "0";    
    
    /**
     * Console I/I.
     */
    private String consolePrompt = "";
    private String consolePromptWithDefault = "";

    /**
     * Constructs the destination panel and set the panel's description.
     * The localizable description text appears on the top of panel.
     */
    public DirectoryChooserPanel() {
        setDescription("$L(com.installshield.product.i18n.ProductResources,DestinationPanel.description,$P(displayName))");
        setDestinationCaption("$L(com.installshield.wizardx.i18n.WizardXResources,DirectoryInputComponent.DirectoryName)");
        setBrowseCaption("$L(com.installshield.wizard.i18n.WizardResources,browseWithMn)");
    }
    
    public void setDestinationCaption(String caption) {
        destinationCaption = caption;
        propertyChanged("destinationCaption");
    }
    
    public String getDestinationCaption() {
        return destinationCaption;
    }
    
    public void setBrowseCaption(String caption) {
        // This mod to caption is to ensure that project create in ISMP < v5.x will have the
        // correct browse caption. While this type of mod is typically handled with project
        // transforms (XSL), project transforms are dependent on project API versions and the
        // API version should not be updated as a result of this change. Note: a developer can
        // work around this logic by setting browseCaption to:
        //
        //    "$L(com.installshield.wizard.i18n.WizardResources, browse)"
        //
        // (note the space before 'browse')
        //
        if (caption.equals("$L(com.installshield.wizard.i18n.WizardResources,browse)")) {
            caption = "$L(com.installshield.wizard.i18n.WizardResources,browseWithMn)";
        }
        
        browseCaption = caption;
        propertyChanged("browseCaption");
    }
    
    public String getBrowseCaption() {
        return browseCaption;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestinationsCaption(String caption) {
        destinationsCaption = caption;
        propertyChanged("destinationsCaption");
    }
    
    public String getDestinationsCaption() {
        return destinationsCaption;
    }
    
    /*
    public void setDestinations(Vector destinations) {
        this.destinations = destinations;
    }*/
    
    public Vector getDestinations() {
        return destinations;
    }
    
    public void setDestinationsMessage(String message) {
        destinationMessage = message;
        propertyChanged("destinationsMessage");
    }
    
    public String getDestinationsMessage() {
        return destinationMessage;
    }
    
    /**
     * Getter for property selectedDestination.
     * @return Value of property selectedDestination.
     */
    public String getSelectedDestinationIndex() {
        return this.selectedDestinationIndex;
    }
    
    /**
     * Setter for property selectedDestination.
     * @param selectedDestination New value of property selectedDestination.
     */
    public void setSelectedDestinationIndex(String selectedDestinationIndex) {
        this.selectedDestinationIndex = selectedDestinationIndex;
    }
    
    public String getDestinationValue(int i) {
        Object o = destinations.elementAt(i);
        if (o instanceof String) {
            return (String) o;
        }
        else if (o instanceof DestinationItem) {
            return ((DestinationItem) o).getValue();
        }
        else {
            return ""+o;
        }
    }
    
    public String getDestinationDescription(int i) {
        Object o = destinations.elementAt(i);
        if (o instanceof String) {
            return (String) o;
        }
        else if (o instanceof DestinationItem) {
            return ((DestinationItem) o).getDescription();
        }
        else {
            return ""+o;
        }
    }
    
    public Vector getDestinationDescriptions() {
        Vector result = new Vector(destinations.size());
        for (int i = 0; i < destinations.size(); i++) {
            result.add(getDestinationDescription(i));
        }
        return result;
    }
    
    public String getConsolePrompt() {
        return consolePrompt;
    }
   
    public void setConsolePrompt(String prompt) {
        consolePrompt = prompt;
    }
   
    public String getConsolePromptWithDefault() {
        return consolePromptWithDefault;
    }
   
    public void setConsolePromptWithDefault(String prompt) {
        consolePromptWithDefault = prompt;
    }
   
    public boolean queryEnter(WizardBeanEvent event) {
        // right now noop
        return true;
    }
    
    /**
     *  Called by the ISMP runtime framework just before destination panel exits.
     *  Updates the product tree to set the new install location for
     *  the product.
     *
     *  @param event  generated by the ISMP runtime time framework.
     *  @return true if the panel can be exited.
     *
     *  @see #queryEnter
     *  @see #entered
     *  @see #exited
     *  @see#updateProducTree
     */
    public boolean queryExit(WizardBeanEvent event) {
        return validateDestination();
    }
    
    public void exited(WizardBeanEvent event) {
        // right now noop
    }
    
    /**
     * Helper method to validate  the data entered for the product destination.
     *
     * @boolean true if the specified destination directory exists and writable. false otherwise.
     */
    private boolean validateDestination() {
        // There are three ways a directory can be invalid:
        //
        //  1 - If it is blank or is all white-space
        //  2 - If the file service rejects it as valid
        //  3 - If it cannot be created
        
        // blank/all white-space check
        if (StringUtils.isWhitespace(destination)) {
            showLocalizedErrorMsg(ProductResourcesConst.NAME,
            "DestinationPanel.destinationDirectory",
            "DestinationPanel.specifyDirectory");
            return false;
        }
        
        try {
            
            FileService fileService = (FileService)getService(FileService.NAME);
            
            // validate file name
            fileService.validateFileName(destination);
            
            // writable check
            if (!isDirectoryValid(fileService, destination)) {
                showErrorMsg(LocalizedStringResolver.resolve(ProductResourcesConst.NAME, "DestinationPanel.destinationDirectory"), "Destination "+destination+" isn't a valid directory");
                return false;
            }
            
        } catch (ServiceException e) {
            showErrorMsg(LocalizedStringResolver.resolve(ProductResourcesConst.NAME, "DestinationPanel.destinationDirectory"), e.getMessage());
            return false;
        }
        
        // all checks pass, directory is ok
        return true;
    }
    
    protected void showErrorMsg(String title, String msg) {
        try {
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
    
    protected void showLocalizedErrorMsg(String bundle, String titleKey, String msgKey) {
        try {
            String title = LocalizedStringResolver.resolve(bundle, titleKey);
            String msg = LocalizedStringResolver.resolve(bundle, msgKey);
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
    
    protected void showLocalizedErrorMsg(String bundle, String titleKey, String msgKey, String[] params) {
        try {
            String title = LocalizedStringResolver.resolve(bundle, titleKey);
            String msg = LocalizedStringResolver.resolve(bundle, msgKey, params);
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
    
    /**
     *  This method will be called by the ISMP IDE during build time to <b>build</b> this panel. This
     *  method registers the class resource and the runtime services, ProductService and File Service in this case
     *  with the builder so that they are available to the wizard during install/uninstall time.
     *
     *  @param support provides the ability to add resources to the archive.
     */
    public void build(WizardBuilderSupport support) {
        super.build(support);
        
        // register need for required services
        support.putRequiredService(ProductService.NAME);
        support.putRequiredService(FileService.NAME);
        
        try {
            support.putResourceBundles(ProductResourcesConst.NAME);
            support.putClass(org.netbeans.installer.DirectoryChooserPanel.class.getName());
            support.putClass(org.netbeans.installer.DirectoryChooserPanelSwingImpl.class.getName());
            support.putClass("org.netbeans.installer.DirectoryChooserPanelSwingImpl$1");
            support.putClass(org.netbeans.installer.DestinationItem.class.getName());
        } 
        catch (java.io.IOException e) {
            support.logEvent(this, Log.ERROR, e);
        }
    }
    
    /**
     * Helper method to check whether the directory is writeable.
     *
     * @param destination the directory hierarchy.
     * @param fileService the runtime service used for the task.
     */
    private boolean isDirectoryValid(FileService fileService, String destination) {
        try {
            
            // check if the directory already existed
            if (!fileService.fileExists(destination)) {
                throw new IOException("Destination doesn't exist");
            }
            if (!fileService.isDirectory(destination)) {
                throw new IOException("Destination isn't a valid directory");
            }
            // we were able to read and use selected destination
            return true;
            
        } catch (Exception e) {
            logEvent(this, Log.WARNING, e.getMessage());
            return false;
        }
    }
}
