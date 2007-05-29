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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class UpdateManagerImpl extends Object {
    private static final UpdateManagerImpl INSTANCE = new UpdateManagerImpl();
    private static final UpdateManager.TYPE[] DEFAULT_TYPES = new UpdateManager.TYPE [] {  UpdateManager.TYPE.FEATURE };
    
    private Reference<Map<String, UpdateUnit>> updateUnitsRef = null;
    private List<UpdateUnit> holderUnits; // XXX: temporary only
    private  Map<String, UpdateUnit> holderMap; // XXX: temporary only
    private Logger logger = null;
    
    // package-private for tests only
    
    public static UpdateManagerImpl getInstance() {
        return INSTANCE;
    }
    
    /** Creates a new instance of UpdateManagerImpl */
    private UpdateManagerImpl () {}
    
    private List<UpdateUnit> getUpdateUnits () {
        Reference<Map<String, UpdateUnit>> tmpUpdateUnitsRef = null;
        synchronized(UpdateManagerImpl.class) {
            tmpUpdateUnitsRef = updateUnitsRef;
        }        
        if (tmpUpdateUnitsRef == null || tmpUpdateUnitsRef.get() == null) {
            tmpUpdateUnitsRef = new WeakReference<Map<String, UpdateUnit>> (UpdateUnitFactory.getDefault ().getUpdateUnits ());
            synchronized(UpdateManagerImpl.class) {
                updateUnitsRef = tmpUpdateUnitsRef;
            }
        }
        return unitsFromReference (tmpUpdateUnitsRef);
    }
    
    public List<UpdateUnit> getUpdateUnits (UpdateManager.TYPE... types) {
        if (types == null || types.length == 0) {
            types = DEFAULT_TYPES;
        }
        
        if (updateUnitsRef == null) {
            getUpdateUnits ();
        }
        assert updateUnitsRef != null : "updateUnits must be initialized.";
        
        List<UpdateUnit> askedUnits = new ArrayList<UpdateUnit> ();
        List<UpdateManager.TYPE> typesL = Arrays.asList (types);
        
        for (UpdateUnit unit : unitsFromReference (updateUnitsRef)) {
            UpdateUnitImpl impl = Trampoline.API.impl (unit);
            if (typesL.contains (impl.getType ())) {
                askedUnits.add (unit);
            }
        }

        holderUnits = unitsFromReference (updateUnitsRef);
        holderMap = mapFromReference (updateUnitsRef);
        
        return askedUnits;
    }
    
    private static List<UpdateUnit> unitsFromReference(Reference<Map<String, UpdateUnit>> reference) {
        final Map<String, UpdateUnit> m = reference.get();
        List<UpdateUnit>  retval = null;
        if (m != null) {
            retval = new ArrayList<UpdateUnit> (m.values()) {
                Map<String, UpdateUnit> keepIt = m;
            };
        } else {    
            retval = Collections.emptyList();
        }        
        return retval;        
    }
    
    private static Map<String, UpdateUnit> mapFromReference(Reference<Map<String, UpdateUnit>> reference) {
        Map<String, UpdateUnit> retval = null;
        if (reference != null) {
            retval = reference.get();
        } 
        if (retval == null) {
            retval = Collections.emptyMap(); 
        }
        return retval;        
    }
    
    // XXX: wrong usage; replace with something better
    public UpdateUnit getUpdateUnit (String moduleCodeName) {
        // trim release impl.
        if (moduleCodeName.indexOf('/') != -1) {
            int to = moduleCodeName.indexOf('/');
            moduleCodeName = moduleCodeName.substring(0, to);
        }
        if (updateUnitsRef == null) {
            getUpdateUnits ();
            holderUnits = unitsFromReference (updateUnitsRef);
            holderMap = mapFromReference (updateUnitsRef);
        }
        assert updateUnitsRef != null : "updateUnits must be initialized.";
        
        return mapFromReference(updateUnitsRef).get(moduleCodeName);
    }
    
    public List<UpdateUnit> getUpdateUnits (UpdateProvider provider, UpdateManager.TYPE... types) {
        Map<String, UpdateUnit> units = UpdateUnitFactory.getDefault().getUpdateUnits (provider);
        List<UpdateUnit> askedUnits = new ArrayList<UpdateUnit> ();
        
        if (types == null || types.length == 0) {
            types = DEFAULT_TYPES;
        }
        List<UpdateManager.TYPE> typesL = Arrays.asList (types);
        
        for (UpdateUnit unit : units.values ()) {
            UpdateUnitImpl impl = Trampoline.API.impl (unit);
            if (typesL.contains (impl.getType ())) {
                askedUnits.add (unit);
            }
        }
        return askedUnits;
    }
    
    public void cleanupUpdateUnits () {
        synchronized(UpdateManagerImpl.class) {
            if (updateUnitsRef != null) {
                updateUnitsRef = null;
            }
        }
    }
    
    private Logger getLogger () {
        if (logger == null) {
            logger = Logger.getLogger (UpdateManagerImpl.class.getName ());
        }
        return logger;
    }
}
