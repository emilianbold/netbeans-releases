/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform.libraries;


import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;



/**
 * Implementation of Javadoc query for the library.
 */
public class JavadocForBinaryQueryLibraryImpl implements JavadocForBinaryQueryImplementation {

    /** Default constructor for lookup. */
    public JavadocForBinaryQueryLibraryImpl() {
    }

    public JavadocForBinaryQuery.Result findJavadoc(final URL b) {
        class R implements JavadocForBinaryQuery.Result {
            public URL[] getRoots() {
                LibraryManager lm = LibraryManager.getDefault();
                Library[] libs = lm.getLibraries();
                for (int i=0; i<libs.length; i++) {
                    String type = libs[i].getType();
                    if (!J2SELibraryTypeProvider.LIBRARY_TYPE.equalsIgnoreCase(type)) {
                        continue;
                    }
                    List jars = libs[i].getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);    //NOI18N
                    Iterator it = jars.iterator();
                    while (it.hasNext()) {
                        URL entry = (URL)it.next();
                        FileObject file = URLMapper.findFileObject (entry);
                        if (file != null) {
                            try {
                                if (b.equals(file.getURL())) {
                                    List l = libs[i].getContent(J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC);
                                    return (URL[])l.toArray(new URL[l.size()]);
                                }
                            } catch (FileStateInvalidException e) {
                                ErrorManager.getDefault().notify (e);
                            }
                        }
                    }
                }
                return new URL[0];
            }
            public void addChangeListener(ChangeListener l) {
                // XXX not implemented
            }
            public void removeChangeListener(ChangeListener l) {
                // XXX not implemented
            }
        }
        return new R();
    }

}
