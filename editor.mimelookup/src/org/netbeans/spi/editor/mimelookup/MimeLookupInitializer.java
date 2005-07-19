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

import org.openide.util.Lookup;

/**
 *  Provides an initialization of MimeLookup on either global or mime-type
 *  specific level.
 *  <br>
 *  The implementations of this class should be registed to default lookup by 
 *  <a href="http://openide.netbeans.org/lookup/index.html"> META-INF/services registration</a>.
 *  <br>
 *  Such registered instance serves as a global level initializer
 *  which can further be asked for children by {@link #child(String)}
 *  which will lead to forming of a tree initializers hierarchy.
 *  <br>
 *  The contents provided by {@link #lookup()} of the global-level initializer
 *  (the one registered in the layer) will automatically appear
 *  in all the results returned by <code>MimeLookup</code> for any particular mime type.
 *  <br>
 *  Once someone asks for a <code>MimeLookup</code> for a specific mime-type
 *  by using {@link org.netbeans.api.editor.mimelookup.MimeLookup#getMimeLookup(String)}
 *  the global level initializer will be asked for {@link #child(String)}
 *  and the {@link #lookup()} on the returned children
 *  will define the result data (together with the global-level initializer's lookup).
 *  <br>
 *  This process can be arbitrarily nested for embedded mime-types.
 *  
 * <p> 
 *  An example implementation of MimeLookupInitializer
 *  that works over xml layer file system can be found at mime lookup module
 *  implementation <a href="http://editor.netbeans.org/source/browse/editor/mimelookup/src/org/netbeans/modules/editor/mimelookup/Attic/LayerMimeLookupImplementation.java">LayerMimeLookupInitializer</a>
 *
 *  @author Miloslav Metelka, Martin Roskanin
 */
public interface MimeLookupInitializer {

    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    Lookup lookup();
    
    /**
     * Retrieves a Lookup.Result of MimeLookupInitializers for the given sub-mimeType.
     *
     * @param mimeType mime-type string representation e.g. "text/x-java"
     * @return non-null lookup result of MimeLookupInitializer(s).
     *  <br/>
     *  Typically there should be just one child initializer although if there
     *  will be more than one all of them will be taken into consideration.
     *  <br/>
     *  If there will be no specific initializers for the particular mime-type
     *  then an empty result should be returned.
     */
    Lookup.Result child(String mimeType);

}
