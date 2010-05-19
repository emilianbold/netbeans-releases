/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.sun.manager.jbi.nodes;

import com.sun.esb.management.api.administration.AdministrationService;
import com.sun.esb.management.api.configuration.ConfigurationService;
import com.sun.esb.management.api.deployment.DeploymentService;
import com.sun.esb.management.api.installation.InstallationService;
import com.sun.esb.management.api.notification.EventNotification;
import com.sun.esb.management.api.notification.EventNotificationListener;
import com.sun.esb.management.api.notification.NotificationService;
import com.sun.esb.management.common.ManagementRemoteException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.Notification;
import javax.management.openmbean.CompositeDataSupport;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.nodes.property.JBIPropertySupportFactory;
import org.netbeans.modules.sun.manager.jbi.nodes.property.PropertySheetOwner;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import static org.netbeans.modules.sun.manager.jbi.NotificationConstants.*;

/**
 * Abstract super class for all nodes in JBI manager.
 *
 * @author jqian
 */
public abstract class AppserverJBIMgmtNode extends AbstractNode
        implements PropertySheetOwner, EventNotificationListener {
   
    protected static final String GENERAL_SHEET_SET_NAME = "GENERAL"; // NOI18N
    protected static final String SERVER_TARGET = AppserverJBIMgmtController.SERVER_TARGET;
    protected static final String MODEL_BEAN_INFO_PACKAGE_NAME =
            "org.netbeans.modules.sun.manager.jbi.management.model.beaninfo"; // NOI18N
    
    private static Logger logger;
    private NodeType nodeType;
    private AppserverJBIMgmtController appsrvrJBIMgmtController;    
    
    // Used for ordering event notifications
    private long lastSequenceNumber = -1;

    /**
     *
     *
     */
    public AppserverJBIMgmtNode(final AppserverJBIMgmtController controller,
            final Children children, final NodeType nodeType) {
        super(children);

        this.nodeType = nodeType;

        appsrvrJBIMgmtController = controller;

        if (appsrvrJBIMgmtController == null) {
            getLogger().log(Level.FINE,
                    "AppserverJBIMgmtController is " + "null for [" + nodeType + "]");  // NOI18N
        }
    }

    protected NodeType getNodeType() {
        return nodeType;
    }

    /**
     * 
     */
    public AppserverJBIMgmtController getAppserverJBIMgmtController() {
        return appsrvrJBIMgmtController;
    }

    /**
     * Creates a new property sheet set and adds it into the property sheet.
     *
     * @param sheet                 property sheet
     * @param name                  name of the property sheet set
     * @param displayNameLabel      bundle name of the diaplay name of the 
     *                              property sheet set 
     * @param descriptoinLabel      bundle name of the description of the 
     *                              property sheet set
     * @parm properties             property map
     */
    protected void addSheetSet(Sheet sheet,
            String name,
            String displayNameLabel,
            String descriptionLabel,
            Map<Attribute, ? extends MBeanAttributeInfo> properties) {

        if (properties == null) {
            return;
        }

        try {
            PropertySupport[] propertySupports =
                    createPropertySupportArray(properties);

            Sheet.Set sheetSet = createSheetSet(
                    name, displayNameLabel, descriptionLabel, propertySupports);

            if (sheetSet != null) {
                sheet.put(sheetSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a property sheet set.
     *
     * @param name                  name of the property sheet set
     * @param displayNameLabel      bundle name of the diaplay name of the 
     *                              property sheet set 
     * @param descriptoinLabel      bundle name of the description of the 
     *                              property sheet set
     * @parm properties             property map
     */
    protected Sheet.Set createSheetSet(String name,
            String displayNameLabel,
            String descriptionLabel,
            Map<Attribute, ? extends MBeanAttributeInfo> properties) {

        if (properties == null) {
            return null;
        }

        PropertySupport[] propertySupports =
                createPropertySupportArray(properties);

        Sheet.Set sheetSet = createSheetSet(
                name, displayNameLabel, descriptionLabel, propertySupports);

        return sheetSet;
    }

    protected Sheet.Set createSheetSet(String name,
            String displayNameLabel,
            String descriptionLabel,
            PropertySupport[] propertySupports) {

        Sheet.Set sheetSet = new Sheet.Set();
        sheetSet.setName(name);
        sheetSet.setDisplayName(
                NbBundle.getMessage(AppserverJBIMgmtNode.class, displayNameLabel));
        sheetSet.setShortDescription(
                NbBundle.getMessage(AppserverJBIMgmtNode.class, descriptionLabel));
        if (propertySupports != null) {
            sheetSet.put(propertySupports);
        }

        return sheetSet;
    }

    /**
     * Creates a PropertySupport array from a map of component properties.
     *
     * @param properties The properties of the component.
     * @return An array of PropertySupport objects.
     */
    protected PropertySupport[] createPropertySupportArray(
            final Map<Attribute, ? extends MBeanAttributeInfo> attrMap) {
        PropertySupport[] supports = new PropertySupport[attrMap.size()];

        int i = 0;
        for (Attribute attr : attrMap.keySet()) {
            MBeanAttributeInfo info = attrMap.get(attr);
            supports[i++] =
                    JBIPropertySupportFactory.getPropertySupport(this, attr, info);
        }
        return supports;
    }

    protected RuntimeManagementServiceWrapper getRuntimeManagementServiceWrapper() {
        try {
            return getAppserverJBIMgmtController().getRuntimeManagementServiceWrapper();
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        return null;
    }

    protected InstallationService getInstallationService() {
        try {
            return getAppserverJBIMgmtController().getInstallationService();
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        return null;
    }

    protected DeploymentService getDeploymentService() {
        try {
            return getAppserverJBIMgmtController().getDeploymentService();
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        return null;
    }

    protected ConfigurationService getConfigurationService() {
        try {
            return getAppserverJBIMgmtController().getConfigurationService();
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        return null;
    }

    protected AdministrationService getAdministrationService() {
        try {
            return getAppserverJBIMgmtController().getAdministrationService();
        } catch (ManagementRemoteException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        return null;
    }

//    /**
//     * Sets the property as an attribute to the underlying AMX mbeans. It 
//     * usually will delegate to the controller object which is responsible for
//     * finding the correct AMX mbean objectname in order to execute a 
//     * JMX setAttribute.
//     *
//     * @param attrName The name of the property to be set.
//     * @param value The value retrieved from the property sheet to be set in the
//     *        backend.
//     * @returns the updated Attribute accessed from the Sheet.
//     */
//    public abstract Attribute setSheetProperty(String attrName, Object value);
    
    protected String getInstalledIconBadgeName() {
        return IconConstants.INSTALLED_ICON;
    }

    protected String getStoppedIconBadgeName() {
        return IconConstants.STOPPED_ICON;
    }

    protected String getUnknownIconBadgeName() {
        return IconConstants.UNKNOWN_ICON;
    }
    
    protected void registerNotificationListener() {
        try {
            NotificationService notificationService = 
                    getAppserverJBIMgmtController().getNotificationService();
            if (notificationService != null) {
                notificationService.addNotificationEventListener(this);
            }
        } catch (ManagementRemoteException ex) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    "Unable to get the runtime notification service. You might need to (re)start the server.",
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    public void processNotification(EventNotification eventNotification) {
        Notification notification = eventNotification.getNotification();
        
        long newSequenceNumber = notification.getSequenceNumber();
            
        if (lastSequenceNumber < newSequenceNumber) {
            lastSequenceNumber = newSequenceNumber;
            
            CompositeDataSupport userData = (CompositeDataSupport) notification.getUserData();
            String sourceName = (String) userData.get(NOTIFICATION_SOURCE_NAME);        
            String sourceType = (String) userData.get(NOTIFICATION_SOURCE_TYPE);       
            String eventType = (String) userData.get(NOTIFICATION_EVENT_TYPE); 

            if (shouldProcessNotification(sourceName, sourceType, eventType)) { 
                processNotificationData(userData);        
            }
        }
    }
    
    /**
     * Whether the current node should process the given event.
     * @param sourceName
     * @param sourceType
     * @param eventType
     * @return
     */
    protected abstract boolean shouldProcessNotification(String sourceName, 
            String sourceType, String eventType);
    
    /**
     * Processes the given notification user data.
     * @param data
     */
    protected abstract void processNotificationData(CompositeDataSupport data);
    
    private static RequestProcessor myProcessor = new RequestProcessor("jbimgr");
    
    protected static void postToRequestProcessorThread(final Runnable runnable) {
        if (myProcessor.isRequestProcessorThread()) {
            runnable.run();
        } else {
            myProcessor.post(runnable);
        }
    }

    /**
     * Returns the logger for all nodes.
     *
     * @returns The java.util.logging.Logger impl. for this node.
     */
    protected final static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("org.netbeans.modules.sun.manager.jbi.nodes");
        }

        return logger;
    }
}
