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

package org.netbeans.spi.editor.mimelookup;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Lookup;

/**
 *  Provides an initialization of data in MimeLookup for the requested mime-paths.
 *  <br>
 *  The implementations of this class should be registed to default lookup by 
 *  <a href="http://openide.netbeans.org/lookup/index.html"> META-INF/services registration</a>.
 *  
 *  @author Miloslav Metelka
 */
public interface MimeDataProvider {

    /**
     * Retrieve lookup for the given mime-path to be used by MimeLookup.
     * <br/>
     * The returned lookup will be cached by a soft reference.
     * <br>
     * The returned lookup is allowed to hardly reference the given mime-path
     * as the resulting proxy-lookup is soft-referenced so the mime-path
     * will not be strongly referenced statically.
     *
     * @param mimePath non-null mime-path for which the lookup should be retrieved.
     *   <br>
     *   It may be {@link MimePath.EMPTY} for global settings.
     * @return lookup for the given mime-path or null if there is no lookup
     *   for the given mime-path in this mime data provider.
     */
    public Lookup getLookup(MimePath mimePath);

}
