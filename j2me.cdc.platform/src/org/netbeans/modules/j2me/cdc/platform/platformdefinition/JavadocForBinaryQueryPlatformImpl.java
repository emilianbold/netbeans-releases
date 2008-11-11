/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2me.cdc.platform.platformdefinition;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.WeakListeners;

/**
 * Implementation of Javadoc query for the platform.
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation.class, position=151)
public class JavadocForBinaryQueryPlatformImpl implements JavadocForBinaryQueryImplementation {

    private static final int STATE_ERROR = -1;
    private static final int STATE_START = 0;
    private static final int STATE_DOCS = 1;
    private static final int STATE_LAN = 2;
    private static final int STATE_API = 3;
    private static final int STATE_INDEX = 4;

    private static final String NAME_DOCS = "docs"; //NOI18N
    private static final String NAME_JA = "ja";     //NOI18N
    private static final String NAME_API = "api";   //NOI18N
    private static final String NAME_IDNEX ="index-files";  //NOI18N

    /** Default constructor for lookup. */
    public JavadocForBinaryQueryPlatformImpl() {
    }
    
    public JavadocForBinaryQuery.Result findJavadoc(final URL b) {
        class R implements JavadocForBinaryQuery.Result, PropertyChangeListener {

            private JavaPlatform platform;
            private ArrayList<ChangeListener> listeners;
            private URL[] cachedRoots;

            public R (JavaPlatform plat) {
                this.platform = plat;
                this.platform.addPropertyChangeListener (WeakListeners.propertyChange(this,this.platform));
            }

            public synchronized URL[] getRoots() {
                if (this.cachedRoots == null) {
                    ArrayList<URL> l = new ArrayList<URL>();
                    for (URL url : (java.util.List<URL>)this.platform.getJavadocFolders()) {
                        l.add(getIndexFolder(url));
                    }
                    this.cachedRoots = l.toArray(new URL[l.size()]);
                }
                return this.cachedRoots;
            }

            public synchronized void addChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";      //NOI18N
                if (this.listeners == null) {
                    this.listeners = new ArrayList<ChangeListener> ();
                }
                this.listeners.add (l);
            }
            public synchronized void removeChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";  //NOI18N
                if (this.listeners == null) {
                    return;
                }
                this.listeners.remove (l);
            }
            
            public void propertyChange (PropertyChangeEvent event) {
                if (JavaPlatform.PROP_JAVADOC_FOLDER.equals(event.getPropertyName())) {
                    synchronized (this) {
                        this.cachedRoots = null;
                    }
                    this.fireChange ();
                }
            }
            
            private void fireChange () {
                Iterator it = null;
                synchronized (this) {
                    if (this.listeners == null) {
                        return;
                    }
                    it = ((ArrayList)this.listeners.clone()).iterator();
                }
                ChangeEvent event = new ChangeEvent (this);
                while (it.hasNext()) {
                    ((ChangeListener)it.next()).stateChanged(event);
                }
            }
            
        }
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform platforms[] = jpm.getInstalledPlatforms();
        for (JavaPlatform jp : platforms ) {
            Iterator it = jp.getBootstrapLibraries().entries().iterator();
            while (it.hasNext()) {
                ClassPath.Entry entry = (ClassPath.Entry)it.next();
                if (b.equals(entry.getURL())) {
                    return new R (jp);
                }
            }
        }
        return null;
    }
    
    /**
     * Search for the actual root of the Javadoc containing the index-all.html or 
     * index-files. In case when it is not able to find it, it returns the given Javadoc folder/file.
     * @param URL Javadoc folder/file
     * @return URL either the URL of folder containg the index or the given parameter if the index was not found.
     */
    protected static URL getIndexFolder (URL rootURL) {
        if (rootURL == null) {
            return null;
        }
        FileObject root = URLMapper.findFileObject(rootURL);
        if (root == null) {
            return rootURL;
        }
        FileObject result = findIndexFolder (root);
        try {
            return result == null ? rootURL : result.getURL();        
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify (e);
            return rootURL;
        }
    }
    
    private static FileObject findIndexFolder (FileObject fo) {
        int state = STATE_START;
        while (state != STATE_ERROR && state != STATE_INDEX) {
            switch (state) {
                case STATE_START:
                    {
                        FileObject tmpFo = fo.getFileObject(NAME_DOCS);    //NOI18N
                        if (tmpFo != null) {
                            fo = tmpFo;
                            state = STATE_DOCS;
                            break;
                        }
                        tmpFo = fo.getFileObject(NAME_JA);     //NOI18N
                        if (tmpFo != null) {
                            fo = tmpFo;
                            state = STATE_LAN;
                            break;

                        }
                        tmpFo = fo.getFileObject(NAME_API);
                        if (tmpFo != null) {
                            fo = tmpFo;
                            state = STATE_API;
                            break;
                        }
                        fo = null;
                        state = STATE_ERROR;
                        break;
                    }
                case STATE_DOCS:
                    {
                        FileObject tmpFo = fo.getFileObject(NAME_JA);
                        if (tmpFo != null) {
                            fo = tmpFo;
                            state = STATE_LAN;
                            break;
                        }
                        tmpFo = fo.getFileObject(NAME_API);
                        if (tmpFo != null) {
                            fo = tmpFo;
                            state = STATE_API;
                            break;
                        }
                        fo = null;
                        state = STATE_ERROR;
                        break;
                    }
                case STATE_LAN:
                    {
                        FileObject tmpFo = fo.getFileObject(NAME_API);
                        if (tmpFo != null) {
                            fo = tmpFo;
                            state = STATE_API;
                            break;
                        }
                        fo = null;
                        state = STATE_ERROR;
                        break;
                    }
                case STATE_API:
                    {
                        FileObject tmpFo = fo.getFileObject(NAME_IDNEX);
                        if (tmpFo !=null) {
                            state = STATE_INDEX;
                            break;
                        }
                        fo = null;
                        state = STATE_ERROR;
                        break;
                    }
            }
        }
        return fo;
    }
    
}
