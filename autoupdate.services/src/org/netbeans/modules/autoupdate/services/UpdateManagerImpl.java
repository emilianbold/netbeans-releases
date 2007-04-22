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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class UpdateManagerImpl extends Object {
    private static final UpdateManagerImpl INSTANCE = new UpdateManagerImpl();
    
    private Map<String, UpdateUnit> updateUnits = null;
    private List<UpdateUnit> units = null;
    
    // package-private for tests only
    
    public static UpdateManagerImpl getInstance() {
        return INSTANCE;
    }
    
    /** Creates a new instance of UpdateManagerImpl */
    private UpdateManagerImpl () {}
    
    public List<UpdateUnit> getUpdateUnits() {
        
        Map<String, UpdateUnit> tmpUpdateUnits = null;
        synchronized(UpdateManagerImpl.class) {
            tmpUpdateUnits = updateUnits;
        }        
        if (tmpUpdateUnits == null) {
            tmpUpdateUnits = UpdateUnitFactory.getDefault().getUpdateUnits();
            synchronized(UpdateManagerImpl.class) {
                updateUnits = tmpUpdateUnits;
                units = Collections.list(Collections.enumeration(updateUnits.values()));
            }
        }
        return units;
    }
            
    public UpdateUnit getUpdateUnit (String moduleCodeName) {
        // trim release impl.
        if (moduleCodeName.indexOf('/') != -1) {
            int to = moduleCodeName.indexOf('/');
            moduleCodeName = moduleCodeName.substring(0, to);
        }
        if (updateUnits == null) {getUpdateUnits ();}
        assert updateUnits != null : "updateUnits must be initialized.";        
        return updateUnits.get(moduleCodeName);
    }
    
    public List<UpdateUnit> getUpdateUnits (UpdateProvider provider) {
        Map<String, UpdateUnit> units = UpdateUnitFactory.getDefault().getUpdateUnits (provider);
        return Collections.list (Collections.enumeration (units.values ()));
    }
    
    public void cleanupUpdateUnits () {
        synchronized(UpdateManagerImpl.class) {
            if (updateUnits != null) {
                updateUnits = null;
            }
        }
    }
}
