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

package org.netbeans.modules.project.libraries;

import org.openide.ErrorManager;
import org.xml.sax.*;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Read content of library declaration XML document.
 *
 * @author Petr Kuzel
 */
public class LibraryDeclarationHandlerImpl implements LibraryDeclarationHandler {


    private LibraryImplementation library;

    private String libraryType;
    
    private String libraryDescription;
    
    private String libraryName;

    private String localizingBundle;

    private Map contentTypes = new HashMap ();
    
    // last volume
    private List cpEntries;
    
    //last volume type
    private String contentType;
    
    //parsing volume?
    private boolean inVolume = false;
    
    public static final boolean DEBUG = false;


    /**
     */
    public LibraryDeclarationHandlerImpl() {
    }
    
    public void start_volume(final Attributes meta) throws SAXException {
        cpEntries = new ArrayList ();
        this.inVolume = true;
    }
    
    public void end_volume() throws SAXException {
        contentTypes.put (contentType, cpEntries);
        this.inVolume = false;
        this.contentType = null;
    }
    
    public void handle_type(final java.lang.String data, final Attributes meta) throws SAXException {
		if (data == null || data.length () == 0) {
			throw new SAXException ("Empty value of type element");	//NOI18N
		}
        if (this.inVolume) {
            this.contentType = data;
        }
        else {
            this.libraryType = data;
        }        
    }
        
    public void start_library(final Attributes meta) throws SAXException {
        if ("1.0".equals(meta.getValue("version")) == false) {  // NOI18N
            throw new SAXException("Invalid librray descriptor version"); // NOI18N
        }
    }
    
    public void end_library() throws SAXException {        
        boolean update;
        if (this.library != null) {
            if (this.libraryType == null || !this.libraryType.equals(this.library.getType())) {
                throw new SAXParseException("Changing library type of library: "+this.libraryName+" from: "+
                        library.getType()+" to: " + libraryType, null); //NOI18N
            }
            update = true;
        }
        else {
            LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider(this.libraryType);
            if (provider == null) {
                ErrorManager.getDefault().log (ErrorManager.WARNING, "LibraryDeclarationHandlerImpl: Cannot create library: "+this.libraryName+" of unknown type: " + this.libraryType);
                return;
            }
            this.library = provider.createLibrary();
            update = false;
        }
        if (!update || !safeEquals(this.library.getLocalizingBundle(), localizingBundle)) {
            this.library.setLocalizingBundle (this.localizingBundle);
        }
        if (!update || !safeEquals(this.library.getName(), libraryName)) {
            this.library.setName (this.libraryName);
        }
        if (!update || !safeEquals(this.library.getDescription(), libraryDescription)) {
            this.library.setDescription (this.libraryDescription);
        }
        for (Iterator it = this.contentTypes.keySet().iterator(); it.hasNext();) {
            String contentType = (String) it.next();
            List cp = (List) this.contentTypes.get (contentType);
            try {
                if (!update || !safeEquals (this.library.getContent(contentType),cp)) {
                    this.library.setContent(contentType, cp);
                }
            } catch (IllegalArgumentException e) {
                throw (SAXException) new SAXException(e.toString()).initCause(e);
            }
        }
        this.libraryName = null;
        this.libraryDescription = null;
        this.libraryType = null;
        this.localizingBundle = null;
        this.contentTypes.clear ();
    }

    public void handle_resource(java.net.URL data, final Attributes meta) throws SAXException {
        if (data != null) {
            cpEntries.add(data);
        }
    }
        
    public void handle_name(final java.lang.String data, final Attributes meta) throws SAXException {
        this.libraryName = data;
    }
    
    public void handle_description (final java.lang.String data, final Attributes meta) throws SAXException {
        libraryDescription = data;
    }

    public void handle_localizingBundle (final String data, final Attributes meta) throws SAXException {
        this.localizingBundle = data;
    }

    public void setLibrary (LibraryImplementation library) {
        this.library = library;
    }

    public LibraryImplementation getLibrary () {
        LibraryImplementation lib = this.library;
        this.library = null;
        return lib;
    }


    private static boolean safeEquals (Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals (o2);
    }

}

