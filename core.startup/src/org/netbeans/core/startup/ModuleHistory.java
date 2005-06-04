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

package org.netbeans.core.startup;

// LIMITED INTERACTIONS with APIs and UI--may use ModuleInstall,
// and FileSystems API, and localized messages (but not notification),
// in addition to what is permitted for central classes (utility APIs
// and ModuleInfo-related things). Should be possible to use without
// the rest of core.

import org.openide.modules.SpecificationVersion;

/** Representation of the history of the module.
 * This includes information such as: whether the module
 * has been installed before and now just needs to be restored
 * (or in fact where it was installed); the previous version
 * of the module, in case it needs to be upgraded.
 * Used for communication
 * between the module lister and the module installer.
 * @author Jesse Glick
 */

public final class ModuleHistory {
    
    private final String jar;
    private int oldMajorVers;
    private SpecificationVersion oldSpecVers;
    private boolean upgraded;
    private byte[] installerState;
    private boolean installerStateChanged = false;
    
    /** Create a module history with essential information.
     * You also need to specify a relative or absolute JAR name.
     */
    public ModuleHistory(String jar) {
        assert jar != null;
        this.jar = jar;
        upgraded = false;
        oldMajorVers = -1;
        oldSpecVers = null;
        installerState = null;
    }
    
    /**
     * The name of the JAR relative to the installation, or
     * an absolute path.
     */
    String getJar() {
        return jar;
    }
    
    /** True if this module has been installed before. */
    boolean isPreviouslyInstalled() {
        return upgraded;
    }
    
    /** The old major version of the module,
     * before an upgrade.
     * -1 if unspecified, or it has never been installed before.
     */
    int getOldMajorVersion() {
        return oldMajorVers;
    }
    
    /** The old specification version of the module,
     * before an upgrade.
     * null if unspecified, or it has never been installed before.
     */
    SpecificationVersion getOldSpecificationVersion() {
        return oldSpecVers;
    }
    
    /** Signal that a module has been previously installed,
     * marking it as a possible candidate for upgrade.
     */
    void upgrade(int oldMajorVersion, SpecificationVersion oldSpecificationVersion) {
        upgraded = true;
        oldMajorVers = oldMajorVersion;
        oldSpecVers = oldSpecificationVersion;
    }
    
    /** Get the stored state of the ModuleInstall, if any.
     * Currently this would be a serialized bytestream.
     * null if unknown or there was no stored state.
     */
    byte[] getInstallerState() {
        return installerState;
    }
    
    /** Set the stored state of the ModuleInstall.
     * This may be null to indicate that no state
     * needs to be stored. Otherwise it would currently
     * be a serialized bytestream.
     */
    void setInstallerState(byte[] state) {
        if (installerState != null && state != null) {
            installerStateChanged = true;
        }
        installerState = state;
    }
    
    /** True if the state of the installer has changed dynamically. */
    boolean getInstallerStateChanged() {
        return installerStateChanged;
    }
    
    /** Reset history after an uninstall. */
    void resetHistory() {
        upgraded = false;
        installerState = null;
        installerStateChanged = false;
    }
    
}
