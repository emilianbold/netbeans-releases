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

package org.netbeans.modules.masterfs;

import java.io.File;
import java.util.Collection;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.util.Lookup;

/**
 * Provides implementation of <code>SharabilityQueryImplementation</code> that
 * is tightly coupled with <code>GlobalVisibilityQueryImpl</code> which is based on regular 
 * expression provided by users via  property in IDESettings with property name 
 * IDESettings.PROP_IGNORED_FILES in Tools/Options.  
 *
 * Invisible files are considered as not shared. 
 *
 * @author Radek Matous
 */
public class GlobalSharabilityQueryImpl implements SharabilityQueryImplementation {
    private GlobalVisibilityQueryImpl visibilityQuery;
    /** Creates a new instance of GlobalSharabilityQueryImpl */
    public GlobalSharabilityQueryImpl() {
    }

    public int getSharability(final File file) {
        if (visibilityQuery == null) {
            Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(VisibilityQueryImplementation.class));
            Collection allInstance = result.allInstances();
            assert allInstance.contains(GlobalVisibilityQueryImpl.INSTANCE);
            visibilityQuery = GlobalVisibilityQueryImpl.INSTANCE;
        }
        return (visibilityQuery.isVisible(file.getName())) ? SharabilityQuery.UNKNOWN : SharabilityQuery.NOT_SHARABLE;
    }    
}
