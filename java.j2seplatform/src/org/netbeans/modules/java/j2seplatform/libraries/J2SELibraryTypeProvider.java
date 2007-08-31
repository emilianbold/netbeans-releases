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
package org.netbeans.modules.java.j2seplatform.libraries;

import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.api.project.ProjectManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;

import java.beans.Customizer;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.net.URL;
import java.net.URI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class J2SELibraryTypeProvider implements LibraryTypeProvider {

    private J2SELibraryTypeProvider () {
    }

    private static final String LIB_PREFIX = "libs.";
    public static final String LIBRARY_TYPE = "j2se";       //NOI18N
    public static final String VOLUME_TYPE_CLASSPATH = "classpath";       //NOI18N
    public static final String VOLUME_TYPE_SRC = "src";       //NOI18N
    public static final String VOLUME_TYPE_JAVADOC = "javadoc";       //NOI18N
    public static final String VOLUME_TYPE_MAVEN_POM = "maven-pom"; //NOI18N
    public static final String[] VOLUME_TYPES = new String[] {
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC,
        VOLUME_TYPE_MAVEN_POM
    };

    public String getLibraryType() {
        return LIBRARY_TYPE;
    }
    
    public String getDisplayName () {
        return NbBundle.getMessage (J2SELibraryTypeProvider.class,"TXT_J2SELibraryType");
    }

    public String[] getSupportedVolumeTypes () {
        return VOLUME_TYPES;
    }

    public LibraryImplementation createLibrary() {
        return LibrariesSupport.createLibraryImplementation(LIBRARY_TYPE,VOLUME_TYPES);
    }


    public void libraryCreated(final LibraryImplementation libraryImpl) {
        assert libraryImpl != null;
        ProjectManager.mutex().postWriteRequest(
                new Runnable () {
                    public void run () {
                        try {
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            boolean save = addLibraryIntoBuild(libraryImpl,props);
                            if (save) {
                                PropertyUtils.putGlobalProperties (props);
                            }
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify (ioe);
                        }
                    }
                }
        );
    }

    public void libraryDeleted(final LibraryImplementation libraryImpl) {
        assert libraryImpl != null;
        ProjectManager.mutex().postWriteRequest(new Runnable () {
                public void run() {
                    try {
                        EditableProperties props = PropertyUtils.getGlobalProperties();
                        for (int i=0; i < VOLUME_TYPES.length; i++) {
                            String property = LIB_PREFIX + libraryImpl.getName() + '.' + VOLUME_TYPES[i];  //NOI18N
                            props.remove(property);
                        }
                        PropertyUtils.putGlobalProperties(props);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify (ioe);
                    }
                }
            });
    }

    public Customizer getCustomizer(String volumeType) {
        if (VOLUME_TYPES[0].equals(volumeType)||
            VOLUME_TYPES[1].equals(volumeType)||
            VOLUME_TYPES[2].equals(volumeType)) {
            return new J2SEVolumeCustomizer (volumeType);
        }
        else {
            return null;
        }
    }
    

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public static LibraryTypeProvider create () {
        return new J2SELibraryTypeProvider();
    }

    private static boolean addLibraryIntoBuild(LibraryImplementation impl, EditableProperties props) {
        boolean modified = false;
        for (int i=0; i<VOLUME_TYPES.length; i++) {
            String propName = LIB_PREFIX + impl.getName() + '.' + VOLUME_TYPES[i];     //NOI18N
            List roots = impl.getContent (VOLUME_TYPES[i]);
            if (roots == null) {
                //Non valid library, but try to recover
                continue;
            }
            StringBuffer propValue = new StringBuffer();
            boolean first = true;
            for (Iterator rootsIt=roots.iterator(); rootsIt.hasNext();) {
                URL url = (URL) rootsIt.next();
                if ("jar".equals(url.getProtocol())) {
                    url = FileUtil.getArchiveFile (url);
                    // XXX check whether this is really the root
                }
                File f = null;
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    f = FileUtil.toFile(fo);
                }
                else if ("file".equals(url.getProtocol())) {    //NOI18N
                    //If the file does not exist (eg library from cleaned project)
                    // and it is a file protocol URL, add it.
                    URI uri = URI.create (url.toExternalForm());
                    if (uri != null) {
                        f = new File (uri);
                    }
                }
                if (f != null) {
                    if (!first) {
                        propValue.append(File.pathSeparatorChar);
                    }
                    first = false;
                    f = FileUtil.normalizeFile(f);
                    propValue.append (f.getAbsolutePath());
                }
                else {
                    ErrorManager.getDefault().log ("J2SELibraryTypeProvider: Can not resolve URL: "+url);
                }
            }
            String oldValue = props.getProperty (propName);
            String newValue = propValue.toString();
            if (!newValue.equals(oldValue)) {
                    props.setProperty (propName, newValue);
                    modified = true;
            }
        }
        return modified;
    }
    
}
