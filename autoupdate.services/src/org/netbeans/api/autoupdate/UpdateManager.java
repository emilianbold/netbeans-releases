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
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;

/** The central manager of content available to users in Autoupdate UI.
 * It providers list of units which can be browsed in UI and handles operations 
 * which can be performed on units (i.e. install, update or unistall etc.).
 *
 * @author Jiri Rechtacek(jrechtacek@netbeans.org), Radek Matous 
 */
public final class UpdateManager {
    
    public static enum TYPE {
        MODULE,
        FEATURE,
        STANDALONE_MODULE,
        KIT_MODULE,
        CUSTOM_HANDLED_COMPONENT,
        LOCALIZATION
    }
        
    /**
     * Creates a new instance of UpdateManager
     */
    private UpdateManager () {}
    
    private static UpdateManager mgr = null;
    
    /** Returns singleton instance of <code>UpdateManager</code>
     * 
     * @return UpdateManager
     */
    public static final UpdateManager getDefault () {
        if (mgr == null) {
            mgr = new UpdateManager ();
        }
        return mgr;
    }
    
        
    /** Returns <code>java.util.List</code> of <code>UpdateUnit</code> build on the top of 
     * <code>UpdateUnitProvider</code>. Only enabled providers are taken in the result.
     * 
     * @return list of UpdateUnit
     */
    public List<UpdateUnit> getUpdateUnits () {
        return getImpl().getUpdateUnits ();
    }
                
    /** Returns <code>java.util.List</code> of <code>UpdateUnit</code> build on the top of 
     * <code>UpdateUnitProvider</code>. Only enabled providers are taken in the result.
     * 
     * @param types returns <code>UpdateUnit</code>s contain only given types, e.g. modules for <code>MODULE</code> type.
     * If types is <code>null</code> or null then returns default types
     * @return list of UpdateUnit
     */
    public List<UpdateUnit> getUpdateUnits (TYPE... types) {
        return getImpl().getUpdateUnits (types);
    }
                
    private static UpdateManagerImpl getImpl() {
        return UpdateManagerImpl.getInstance();
    }    
}
