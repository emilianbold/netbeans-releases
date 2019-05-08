/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.Record;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetOwner;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionValue;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.nodes.Sheet;

/**
 *
 */
public class CustomizableHost extends Host implements Record, OptionSetOwner {
    private OptionSet options = new HostOptionSet();

    private static final String default_location = System.getProperty("spro.home");
    /**
     * Create a Default Host with the following properties.
     * host: localhost
     * platform: <empty>
     * studio location: "spro.home" property
     * user: "user.name" property
     * rememberPassword:
     */
    public CustomizableHost() {
	setHostName(localhost);
	setRemoteStudioLocation(default_location);
    }

    /**
     * The key used in the hosts DB.
     */

    // interface Record
    @Override
    public String getKey() {
	return getHostName();
    }

    // interface Record
    @Override
    public void setKey(String newKey) {

	// Do allow setting of an archetypes key because otherwise 
	// it's duplicate would be an archetype too.

	setHostName(newKey);
    }

    // interface Record
    @Override
    public boolean matches(String key) {
	return IpeUtils.sameString(getKey(), key);
    }

    // interface Record
    @Override
    public boolean isArchetype() {
	return localhost.equals(getHostName());	// NOI18N
    }

    /**
     * Convert this to an ExecutionEnvironment.
     */
    @Override
    public ExecutionEnvironment executionEnvironment() {
        ExecutionEnvironment ee;
	if (isRemote())
	    ee = ExecutionEnvironmentFactory.createNew(getHostLogin(), getHostName(), getPortNum());
	else
	    ee = ExecutionEnvironmentFactory.getLocal();
	return ee;
    }

    private static class LocalHostNameHolder {
        private static final String name = getLocalHost();

        private static String getLocalHost() {
            try {
                java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                return addr.getHostName();
            } catch (java.net.UnknownHostException x) {
                return "<unknown>"; // NOI18N
            }
        }
    }

    /**
     * Return the current actual primary hostname (not nicknames).
     */
    private static String getLocalHost() {
        return LocalHostNameHolder.name;
    }
    
    @Override
    public boolean isRemote() {
        return isRemote(getHostName());
    }

    public CustomizableHost(OptionSet options) {
	this.options = options;
    }

    public void setPlatform(Platform platform) {
	setPlatformName(platform.name());
    }

    /*
     * Stored in option property and display to users as the form of 
     * "Solaris_Sparc", "Solaris_x86" , "Linux_x86"
     */
    @Override
    public String getPlatformName() {
	String platForm = getHostOption("platform"); // NOI18N
	return platForm;
    }

    public void setPlatformName(String platform) {
	setHostOption("platform", platform); // NOI18N
    }

    @Override
    public String getHostName() {
	String hostName = getHostOption("host_name"); // NOI18N
	return hostName;
    }

    @Override
    public int getPortNum() {
        return Integer.parseInt(getHostOption("ssh_port")); // NOI18N
    }

    public boolean isRememberPassword() {
	return "on".equals(getHostOption("remember_password"));// NOI18N
    }

    public void setRememberPassword(boolean rememberPassword) {
	if (rememberPassword)
	    setHostOption("remember_password", "on");		// NOI18N
	else
	    setHostOption("remember_password", "off");		// NOI18N
    }

    public void setHostName(String hostName) {
	setHostOption("host_name", hostName); // NOI18N
    }


    /**
     * Under IDE returns user@host|localhost; under dbxtool returns host.
     * This helps use interoperate with CND's convention of using user@host 
     * for hostname.
     */
    public String getHostKey() {
        /*
	if (DebuggerManager.isStandalone()) {
	    return getHostName();
	} else {
         *
         */
	    if (isRemote()) {
                return getHostLogin() + "@" + getHostName() + // NOI18N
                        ":"  + getPortNum();	// NOI18N
            } else {
		return getHostName();
	    }
	//}
    }

    @Override
    public String getHostLogin() {
	String loginName = getHostOption("login_name"); // NOI18N
	return loginName;
    } 

    public void setHostLogin(String loginName) {
	setHostOption("login_name", loginName); // NOI18N
    }

    @Override
    public String getRemoteStudioLocation() {
	String studioLocation = getHostOption("studio_location"); // NOI18N
	if (Log.Remote.host) {
	    System.out.printf("Host.getRemoteStudioLocation() -> %s\n", studioLocation); // NOI18N
	}

	return studioLocation;
    } 

    public void setRemoteStudioLocation(String studioLocation) {
	setHostOption("studio_location", studioLocation); // NOI18N
    }

    public void setSecuritySettings(SecuritySettings ss) {
	setHostOption("ssh_port", Integer.toString(ss.sshPort())); // NOI18N
    }

    @Override
    public SecuritySettings getSecuritySettings() {
	return new SecuritySettings(Integer.parseInt(getHostOption("ssh_port")), null); // NOI18N
    }
    

    // interface Record
    @Override
    public CustomizableHost cloneRecord() {
	return new CustomizableHost(options.makeCopy());
    }
    
    // interface Record
    @Override
    public String displayName() {        
	if (getHostName().equals("localhost")) // NOI18N
	    return getHostName() + " (" + getLocalHost() + ", " + getPlatformName() + " )"; // NOI18N
	else
	    return getHostName() + " (" + getPlatformName() + " )"; // NOI18N

    }

    public void setHostOption(String name, String value) {
	if (Log.Remote.host && "studio_location".equals(name)) { // NOI18N
	    System.out.printf("Host.setHostOption(%s, %s)\n", name, value); // NOI18N
	}
        OptionValue o = options.byName(name);
	// OLD needValidate = true;
	invalidateResources();
        if (o != null)
            o.set(value);
    }

    public String getHostOption(String name) {
        OptionValue o = options.byName(name);
        if (o != null)
            return o.get();
        return null;
    }

    public void assign(CustomizableHost that) {
	// OLD needValidate = true;
	invalidateResources();
	this.options.assign(that.options);
    }

    // interface OptionSetOwner
    @Override
    public OptionSet getOptions() {
        return options;
    }

    /* LATER

    Attempt at not using Option mechanisms for property sheet.
    Not quite there. Editors aren't quite the same and we impose special
    semantics (See HostOption).

    private final Node.Property createHostNameNodeProp(Object o) throws NoSuchMethodException {
	Node.Property p = new PropertySupport.Reflection<String>(o,
		                                      String.class,
						      "getHostName",
						      "setHostName");
	p.setName("HostName");
	p.setDisplayName("host-name");
	p.setShortDescription("Shost-name");
	return p;
    }

    private final Node.Property createPlatformNodeProp(Object o) throws NoSuchMethodException {
	Node.Property p = new PropertySupport.Reflection<Platform>(o,
		                                      Platform.class,
						      "getPlatform",
						      "setPlatform");
	p.setName("Platform");
	p.setDisplayName("platform");
	p.setShortDescription("Splatform");
	return p;
    }

    private final Node.Property createLocationNodeProp(Object o) throws NoSuchMethodException {
	Node.Property p = new PropertySupport.Reflection<String>(o,
		                                      String.class,
						      "getRemoteStudioLocation",
						      "setRemoteStudioLocation");
	p.setName("RemoteStudioLocation");
	p.setDisplayName("studio-location");
	p.setShortDescription("Sstudio-location");
	return p;
    }

    private final Node.Property createUsernameNodeProp(Object o) throws NoSuchMethodException {
	Node.Property p = new PropertySupport.Reflection<String>(o,
		                                      String.class,
						      "getHostLogin",
						      "setHostLogin");
	p.setName("HostLogin");
	p.setDisplayName("host-login");
	p.setShortDescription("Shost-login");
	return p;
    }
     */

    public Sheet getSheet() {

	Sheet sheet = new Sheet();
	Sheet.Set set;

	set = new Sheet.Set();
	set.setName("General"); // NOI18N
	set.setDisplayName("General"); // FIXUP I18N // NOI18N
	set.setShortDescription("General"); // FIXUP I18N // NOI18N
	set.put(HostOption.HOST_PROP_HOSTNAME.createNodeProp(this));
	set.put(HostOption.HOST_PROP_PLATFORM.createNodeProp(this));
	set.put(HostOption.HOST_PROP_LOCATION.createNodeProp(this));
	set.put(HostOption.HOST_PROP_LOGINNAME.createNodeProp(this));
	set.put(HostOption.HOST_PROP_SSH_PORT.createNodeProp(this));
	set.put(HostOption.HOST_PROP_REMEMBER_PASSWORD.createNodeProp(this));

	/* LATER
	try {
	    set.put(createHostNameNodeProp(this));
	    set.put(createPlatformNodeProp(this));
	    set.put(createLocationNodeProp(this));
	    set.put(creFateUsernameNodeProp(this));
	} catch (NoSuchMethodException ex) {
	    Exceptions.printStackTrace(ex);
	}
	 */
	sheet.put(set);

	return sheet;
    }

    @Override
    public String toString() {
        return "CustomizableHost: " + getHostName(); //NOI18N
    }
}
