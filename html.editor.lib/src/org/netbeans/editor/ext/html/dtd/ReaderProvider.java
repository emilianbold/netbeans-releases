/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.editor.ext.html.dtd;

import java.io.Reader;
import java.util.Collection;

/**
 * DTDReaderProvider is interface used as a source of Readers used to parse DTD
 * by DTDParser. One DTDReaderProvider shall offer all Readers for a given DTD, 
 * i.e. the provider for "-//W3C//DTD HTML 4.01//EN" shall also provide Readers
 * for proper "-//W3C//ENTITIES Latin1//EN/HTML", as this public entity is
 * referred from HTML 4.01 DTD and the file provided with 4.01 DTD differs
 * from the file provided with 4.0 DTD although they have the same
 * public identifier (They differ only in comments, though).
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public interface ReaderProvider {

    /* Asks for Reader providing content of DTD file identified by 
     * given identifier, and possibly by given fileName.
     * These parameters are typically obtained from invocation DTD directive
     * like &lt;!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES Latin1//EN//HTML" "HTMLlat1.ent">,
     * in this case, the string -//W3C//....//HTML" is identifier
     * and "HTMLlat1.ent" is name of file in which it is probably stored
     * @param identifier the public identifier of required DTD
     * @param fileName the probable name of file with DTD data, may be
     *      <CODE>null</CODE>. It is used only as helper to identifier.
     * @return Reader from which to read out the DTD content.
     */
    public Reader getReaderForIdentifier( String identifier, String fileName );
    
    /** Asks for all the identifiers available from this ReaderProvider.
     * @returns a Collection of all identifiers for which this ReaderProvider
     * is able to provide Readers for.
     */
    public Collection getIdentifiers();
}
