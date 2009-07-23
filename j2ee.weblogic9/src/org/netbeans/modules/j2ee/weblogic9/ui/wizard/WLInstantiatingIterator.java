/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * The main class of the custom wizard for registering a new server instance.
 * It performs all the orchestration of the panels and actually creates the
 * instance.
 *
 * @author Kirill Sorokin
 */
public class WLInstantiatingIterator  implements WizardDescriptor.InstantiatingIterator {

    /**
     * Since the WizardDescriptor does not expose the property name for the
     * error message label, we have to keep it here also
     */
    private static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    /**
     * The default debugger port for the instance, it will be assigned to it
     * at creation time and can be changed via the properties sheet
     */
    private static final String DEFAULT_DEBUGGER_PORT = "8787"; // NOI18N

    /**
     * The parent wizard descriptor
     */
    private WizardDescriptor wizardDescriptor;

    /**
     * A misterious method whose purpose is obviously in freeing the resources
     * obtained by the wizard during instance registration. We do not need such
     * functionality, thus we do not implement it.
     */
    public void uninitialize(WizardDescriptor wizardDescriptor) {
        // do nothing as we do not need to release any resources
    }

    /**
     * This method initializes the wizard. AS for us the only thing we should
     * do is save the wizard descriptor handle.
     *
     * @param wizardDescriptor the parent wizard descriptor
     */
    public void initialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;

        for (int i = 0; i < this.getPanels().length; i++)
        {
            Object c = panels[i].getComponent();

            if (c instanceof JComponent)
            {
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(
                    WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N

                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    /**
     * Returns the name for the wizard. I failed to find a place where it
     * could be used, so we do not return anything sensible
     *
     * @return the wizard name
     */
    public String name() {
        return ""; // NOI18N
    }

    /**
     * This methos actually creates the instance. It fetches all the required
     * parameters, builds the URL and calls
     * InstanceProperties.createInstamceProperties(), which registers the
     * instance.
     *
     * @return a set of created instance properties
     */
    public Set instantiate() throws IOException {
        // initialize the resulting set
        Set result = new HashSet();

        String displayName = (String) wizardDescriptor.getProperty(PROP_DISPLAY_NAME);

        // if all the data is normally validated - create the instance and
        // attach the additional properties
        InstanceProperties ip = InstanceProperties.createInstanceProperties(url, username, password, displayName);
        ip.setProperty(WLPluginProperties.SERVER_ROOT_ATTR, serverRoot);
        ip.setProperty(WLPluginProperties.DOMAIN_ROOT_ATTR, domainRoot);
        ip.setProperty(WLPluginProperties.DEBUGGER_PORT_ATTR, DEFAULT_DEBUGGER_PORT);

        // add the created instance properties to the result set
        result.add(ip);

        // return the result
        return result;
    }

    /**
     * Helper method for decorating error message as HTML. Workaround for line wrap.
     */
    /*package*/ static String decorateMessage(String message) {
        return message == null
            ? null
            : "<html>" + message.replaceAll("<",  "&lt;").replaceAll(">",  "&gt;") + "</html>"; // NIO18N
    }
    // the main and additional instance properties
    private String serverRoot;
    private String domainRoot;
    private String username;
    private String password;
    private String url;

    /**
     * Setter for the instance url.
     *
     * @param url the new instance url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Setter for the server installation directory.
     *
     * @param serverRoot the new server installation directory path
     */
    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;

        // reinit the instances list
        serverPropertiesPanel.getVisual().updateInstancesList();
    }

    /**
     * Getter for the server installation directory
     *
     * @return the server installation directory path
     */
    public String getServerRoot() {
        return this.serverRoot;
    }

    /**
     * Setter for the profile root directory
     *
     * @param domainRoot the new profile root directory path
     */
    public void setDomainRoot(String domainRoot) {
        this.domainRoot = domainRoot;
    }

    /**
     * Getter for the profile root directory
     *
     * @return the profile root directory path
     */
    public String getDomainRoot() {
        return domainRoot;
    }

    /**
     * Setter for the username
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Setter for the password
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Panels section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The steps names for the wizard: Server Location & Instance properties
     */
    private String[] steps = new String[]
    {
        NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_LOCATION_STEP"),  // NOI18N
        NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_PROPERTIES_STEP") // NOI18N
    };

    /**
     * The wizard's panels
     */
    private WizardDescriptor.Panel[] panels;
    private ServerLocationPanel serverLocationPanel;
    private ServerPropertiesPanel serverPropertiesPanel;

    /**
     * Index of the currently shown panel
     */
    private int index = 0;

    /**
     * Tells whether the wizard has previous panels. Basically controls the
     * Back button
     */
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     * Reverts the wizard to the previous panel if available.
     * If the previous panel is not available a NoSuchElementException will be
     * thrown.
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /**
     * Tells whether the wizard has next panels. Basically controls the
     * Next button
     */
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    /**
     * Proceeds the wizard to the next panel if available.
     * If the next panel is not available a NoSuchElementException will be
     * thrown.
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    /**
     * Returns the current panel of the wizard
     *
     * @return current panel of the wizard
     */
    public WizardDescriptor.Panel current() {
        getPanels();
        return panels[index];
    }

    protected final WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
        }
        return panels;
    }

    protected WizardDescriptor.Panel[] createPanels() {

        serverLocationPanel = new ServerLocationPanel(this);
        serverPropertiesPanel = new ServerPropertiesPanel( this);

        return new WizardDescriptor.Panel[] { serverLocationPanel, serverPropertiesPanel };
    }


    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The registered listeners
     */
    private Vector listeners = new Vector();

    /**
     * Removes an already registered listener in a synchronized manner
     *
     * @param listener a listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * Registers a new listener in a synchronized manner
     *
     * @param listener a listener to be registered
     */
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Notifies all the listeners of the supplied event
     *
     * @param event the event to be passed to the listeners
     */
    private void fireChangeEvent(ChangeEvent event) {
        // copy the registered listeners, to avoid conflicts if the listeners'
        // list changes
        Vector targetListeners;
        synchronized (listeners) {
            targetListeners = (Vector) listeners.clone();
        }

        // notify each listener of the event
        for (int i = 0; i < targetListeners.size(); i++) {
            ChangeListener listener = (ChangeListener) targetListeners.elementAt(i);
            listener.stateChanged(event);
        }
    }

    /**
     * A simple listener that only notifies the parent iterator of all the
     * events that come to it
     *
     * @author Kirill Sorokin
     */
    private class IteratorListener implements ChangeListener {
        /**
         * Notifies the parent iterator of the supplied event
         *
         * @param event the event to be passed to the parent iterator
         */
        public void stateChanged(ChangeEvent event) {
            fireChangeEvent(event);
        }
    }

}
