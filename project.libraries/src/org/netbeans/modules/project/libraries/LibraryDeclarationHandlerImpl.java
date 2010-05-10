/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.project.libraries;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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

    private Map<String,List<URL>> contentTypes = new HashMap<String,List<URL>>();
    
    // last volume
    private List<URL> cpEntries;
    
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
        cpEntries = new ArrayList<URL>();
        this.inVolume = true;
    }
    
    public void end_volume() throws SAXException {
        contentTypes.put (contentType, cpEntries);
        this.inVolume = false;
        this.contentType = null;
    }
    
    public void handle_type(final String data, final Attributes meta) throws SAXException {
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
        cleanUp();
    }
    
    /**
     * Sets preconditions
     */
    private void cleanUp () {
        this.libraryName = null;
        this.libraryDescription = null;
        this.libraryType = null;
        this.localizingBundle = null;
        this.contentTypes.clear ();
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
            if (this.libraryType == null) {
                throw new SAXParseException("Unspecified library type for: "+this.libraryName, null); //NOI18N
            }
            LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider(this.libraryType);
            if (provider == null) {
                throw new SAXParseException("LibraryDeclarationHandlerImpl: Cannot create library: "+this.libraryName+" of unknown type: " + this.libraryType,null);
            }
            this.library = provider.createLibrary();
            update = false;
            LibrariesStorage.LOG.log(Level.FINE, "LibraryDeclarationHandlerImpl library {0} type {1} found", new Object[] { this.libraryName, this.libraryType });
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
        for (Map.Entry<String,List<URL>> entry : contentTypes.entrySet()) {
            String contentType = entry.getKey();
            List<URL> cp = entry.getValue();
            try {
                if (!update || !urlsEqual(this.library.getContent(contentType),cp)) {
                    this.library.setContent(contentType, cp);
                }
            } catch (IllegalArgumentException e) {
                throw (SAXException) new SAXException(e.toString()).initCause(e);
            }
        }        
    }

    public void handle_resource(URL data, final Attributes meta) throws SAXException {
        if (data != null) {
            cpEntries.add(data);
        }
    }
        
    public void handle_name(final String data, final Attributes meta) throws SAXException {
        this.libraryName = data;
    }
    
    public void handle_description (final String data, final Attributes meta) throws SAXException {
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

    private static boolean urlsEqual (final Collection<? extends URL> first, final Collection<? extends URL> second) {
        assert first != null;
        assert second != null;
        if (first.size() != second.size()) {
            return false;
        }
        for (Iterator<? extends URL> fit = first.iterator(), sit = second.iterator(); fit.hasNext();) {
            final URL furl = fit.next();
            final URL surl = sit.next();
            if (!furl.toExternalForm().equals(surl.toExternalForm())) {
                return false;
            }
        }
        return true;
    }

}
