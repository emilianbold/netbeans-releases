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

/*
 * DeploymentPluginProvider.java
 *
 */
package org.netbeans.spi.mobility.deployment;

import java.awt.Component;
import java.util.Map;

/**
 * DeploymentPlugin is an SPI providing all the functionality and UI necessary to handle one deployment type (deployment method) to the NetBeans Mobility.<p>
 * The plugin provides:
 * <ul><li>deployment method name and display name</li>
 * <li>set of global properties, their default values, and customizer (for usage in Mobility Deployment Manager)</li>
 * <li>set of project properties, their default values, and customizer (for usage in each project customizer)</li></ul>
 * <p>
 * Default deployment plugins bundled with NetBeans Mobility are for example: <samp>Copy, Ftp, Scp, WebDAV, SonyEricsson</samp>, etc....
 * @author Adam Sotona
 */
public interface DeploymentPlugin {
    
    /**
     * This method defines the deployment method unique name used for identification (non-localized).<p>
     * It is highly recomended to use Java identifier as the name (no spaces nor special characters).<p>
     * For example: <samp>Copy, Ftp, WebDAV,</samp> etc...
     * @return deployment method identification name
     */
    public String getDeploymentMethodName();
    
    /**
     * This method defines human-readable name of the method.<p>
     * The name can be longer and localized.<p>
     * For example: <samp>File Transfer Protocol (FTP)</samp>, or <samp>Secure Copy (SCP, SFTP)</samp>.
     * @return Display name of the deployment method.
     */
    public String getDeploymentMethodDisplayName();
    
    /**
     * Defines location of the Ant script performing the deployment.<p>
     * The script has to define a default target which performs the deployment.<p>
     * The script may depend on all well known project properties as it is called from the project build script.<p>
     * In addition there is a special <samp>&lt;nb-enter-password</samp> task. This task can be used to invoke NetBeans styled dialog to enter specific connection parameters during the execution (usually missing password).<p>
     * For inspiration please take a look at the the default set of deployment type Ant scripts provided by NetBeans Mobility (under <samp>modules/scr/</samp>).
     * @return relative path of the Ant Script from install root, e.g. <samp>modules/scr/deploy-xxx-impl.xml</samp> (always using <samp>/</samp> as a separator, regardless of platform).
     */
    public String getAntScriptLocation();
    
    /**
     * This method defines set of Ant properties that will be stored in each project (physically in <samp>nbproject/project.properties</samp>).<p>
     * There can be unlimited number of project configurations and each can define its own set of values for these properties.<p>
     * The correct property management and project configurations management is guaranted by Project Customizer and it is transparent for this plugin.<p>
     * To avoid collisions it is highly recommended to prefix all properties following way: <samp>deployment.&lt;deployment method name&gt;.</samp>.<p>
     * The default values of the properties can be of following types:
     * <ul><li>String - for customization using text component, combo box, or radio button group</li>
     * <li>Boolean - for customization using check box</li>
     * <li>Integer - for customization using slider, or spinner</li>
     * <li>File - for customization using text component and storage as a file reference</li></ul>
     * @return Map of properties and their default values.<p>
     * Do not return null.<p>
     * Empty map returned means no project-based management and customization is requested by this plugin.
     */
    public Map<String,Object> getProjectPropertyDefaultValues();
    
    /**
     * This method defines set of Ant properties that will be stored globally in the IDE (physically in <samp>&lt;userdir&gt;/Build.properties</samp>).<p>
     * There can be unlimited number of instances of each deployment plugin stored globally. Each instance defines its own set of values for these properties.<p>
     * The property management and deployment instances management is guaranted by Mobility Deployment Manager and it is transparent for this plugin.<p>
     * To avoid collisions it is highly recommended to prefix all properties following way: <samp>deployment.&lt;deployment method name&gt;.</samp>.<p>
     * The default values of the properties can be of following types:
     * <ul><li>String - for customization using text component, combo box, or radio button group</li>
     * <li>Boolean - for customization using check box</li>
     * <li>Integer - for customization using slider, or spinner</li></ul>
     * @return Map of properties and their default values.<p>
     * Do not return null.<p>
     * Empty map returned means no global management and customization is requested by this plugin.
     */
    public Map<String,Object> getGlobalPropertyDefaultValues();
    
    /**
     * This method returns UI panel for project-specific customization of the deployment plugin.<p>
     * Project customizer automatically scans the panel structure recursivelly, attach the listeners, read and write the property values.<p>
     * All the properties customized by this panel must be defined by getProjectPropertyDefaultValues method.<p>
     * The connection between properties and UI components is defined through the name matching. It means that each component that should be assigned to a property must return the property name by method <samp>getName()</samp>. You can assign a name to any of the component by calling <code>setName("&lt;property name&gt;")</code><p>
     * <ul><li>JTextComponent (JTextField, etc...) can have assigned property with String or File default value.</li>
     * <li>JCheckBox can have assigned property with Boolean default value. The boolean value of the property is matched with the selection status.</li>
     * <li>JComboBox can have assigned property with String default value. </li>
     * <li>JSlider can have assigned property with Integer value.</li>
     * <li>JSpinner can have assigned property with Integer value.</li>
     * <li>JRadioButton can have assigned property with String default value. The radio button will be selected when its <samp>action command</samp> equals to the property value. The property value will be set to the radio button <samp>action command</samp> when selected. The <samp>action command</samp> is taken from method <samp>getActionCommand()</samp> and you can set it by calling <samp>setActionCommand("&lt;property value&gt;")</samp> on the component.</li></ul>
     * <p>
     * There are no limitation of the other functionality provided by the customizer panel.
     * @return UI component that defines the customizer panel (usually JPanel) or null if the customizer is not provided.
     */
    public Component createProjectCustomizerPanel();

    /**
     * This method returns UI panel for IDE global customization of the deployment plugin inside Mobility Deployment Manager.<p>
     * Mobility Deployment Manager automatically scans the panel structure recursivelly, attach the listeners, read and write the property values.<p>
     * All the properties customized by this panel must be defined by getGlobalPropertyDefaultValues method.<p>
     * The connection between properties and UI components is defined through the name matching. It means that each component that should be assigned to a property must return the property name by method <samp>getName()</samp>. You can assign a name to any of the component by calling <code>setName("&lt;property name&gt;")</code><p>
     * <ul><li>JTextComponent (JTextField, etc...) can have assigned property with String default value.</li>
     * <li>JCheckBox can have assigned property with Boolean default value. The boolean value of the property is matched with the selection status.</li>
     * <li>JComboBox can have assigned property with String default value.</li>
     * <li>JSlider can have assigned property with Integer value.</li>
     * <li>JSpinner can have assigned property with Integer value.</li>
     * <li>JRadioButton can have assigned property with String default value. The radio button will be selected when its <samp>action command</samp> equals to the property value. The property value will be set to the radio button <samp>action command</samp> when selected. The <samp>action command</samp> is taken from method <samp>getActionCommand()</samp> and you can set it by calling <samp>setActionCommand("&lt;property value&gt;")</samp> on the component.</li></ul>
     * <p>
     * There are no limitation of the other functionality provided by the customizer panel.
     * @return UI component that defines the customizer panel (usually JPanel) or null if the customizer is not provided.
     */
    public Component createGlobalCustomizerPanel();
}
