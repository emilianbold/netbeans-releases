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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.autoupdate;

import java.util.List;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.UpdateUnitImpl;

/** Instances provided by the <code>UpdateManager</code> which represents wrapper of
 * <code>UpdateElement</code>. The one unit contains all avaliable elements of
 * as same unit, i.e. Editor module, version 1.1 is installed in IDE, server with module
 * update can contain Editor module, version 1.2 and Editor, version 1.0 is in
 * IDE backup.
 * 
 * @author Jiri Rechtacek (jrechtacek@netbeans.org)
 */
public final class UpdateUnit {
    static {
        Trampoline.API = new TrampolineAPI();
    }
    
    final UpdateUnitImpl impl;
    
    UpdateUnit (UpdateUnitImpl i) {
        this.impl = i;
    }

    /** Return code name of unit, it's unique among rest of another units.
     * 
     * @return code name
     */
    public String getCodeName () {
        return impl.getCodeName();
    }
    
    /** Returns installed <code>UpdateElement</code> if any or null if
     * no element which unit's code name is already installed.
     * 
     * @return installed <code>UpdateElement</code>
     */
    public UpdateElement getInstalled () {
        return impl.getInstalled();
    }
    
    /** Returns list of avaiable element which are not installed in IDE
     * and has higher version then installed element (is any). These elements
     * can be installed as new one element or as update of already installed element.
     * 
     * @return list of available and not installed <code>UpdateElement</code>
     */
    public List<UpdateElement> getAvailableUpdates () {
        return impl.getAvailableUpdates();
    }
    
    /** Returns <code>UpdateElement</code> in IDE backup if any or null. The element
     * can found in backup if any another element did update them.
     * 
     * @return <code>UpdateElement</code> from backup
     */
    public UpdateElement getBackup () {
        return impl.getBackup();
    }

    /** Returns localization <code>UpdateElement</code> active with current <code>Locale</code>
     * or null.
     * 
     * @return localization <code>UpdateElement</code> installed in IDE
     */
    public UpdateElement getInstalledLocalization () {
        return impl.getInstalledLocalization ();
    }
    
    /** Returns list of avaiable localization active with current <code>Locale</code>,
     * the localization are not installed in IDE and has higher version then
     * installed localization (is any). These elements can be installed as new one element
     * or as update of already installed element.
     * 
     * @return list of available and not installed localization <code>UpdateElement</code>
     */
    public List<UpdateElement> getAvailableLocalizations () {
        return impl.getAvailableLocalizations ();
    }
    
    public UpdateManager.TYPE getType () {
        return impl.getType ();
    }
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UpdateUnit other = (UpdateUnit) obj;

        if (this.impl != other.impl &&
            (this.impl == null || !this.impl.equals(other.impl)))
            return false;
        return true;
    }

    public int hashCode() {
        int hash = 5;

        hash = 59 * hash + (this.impl != null ? this.impl.hashCode()
                                              : 0);
        return hash;
    }

    @Override
    public String toString() {
        return impl.getCodeName();
    }

}
