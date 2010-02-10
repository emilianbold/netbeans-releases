/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import org.netbeans.modules.web.beans.api.model.support.WebBeansModelSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryDescriptor;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryDescriptorCache;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrarySupport;
import org.netbeans.modules.web.jsf.editor.index.JsfIndex;
import org.netbeans.modules.web.jsf.editor.tld.LibraryDescriptor;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrariesCache;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.netbeans.modules.web.jsf.editor.tld.LibraryDescriptorException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * per web-module instance
 *
 * @author marekfukala
 */
public class JsfSupport {

    private static Logger LOGGER = Logger.getLogger("JsfSupport"); //NOI18N

    //TODO remove later
    static {
        LOGGER.setLevel(Level.ALL);
    }
    private static final WeakHashMap<WebModule, JsfSupport> INSTANCIES = new WeakHashMap<WebModule, JsfSupport>();

    public static JsfSupport findFor(Source source) {
        FileObject fo = source.getFileObject();
        if (fo == null) {
            return null;
        } else {
            return findFor(fo);
        }
    }

    public static JsfSupport findFor(Document doc) {
        return findFor(DataLoadersBridge.getDefault().getFileObject(doc));
    }

    public static JsfSupport findFor(FileObject fo) {
        WebModule wm = WebModule.getWebModule(fo);
        if (wm == null) {
            return null;
        }
	ClassPath classPath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
	if(classPath == null) {
	    return null;
	}
        synchronized (INSTANCIES) {
            JsfSupport instance = INSTANCIES.get(wm);
            if (instance == null) {
                instance = new JsfSupport(wm, classPath);
                INSTANCIES.put(wm, instance);
            }
            return instance;
        }

    }
    private TldLibrariesCache tldLibrariesCache;
    private FaceletsLibraryDescriptorCache faceletsDescriptorsCache;
    private FaceletsLibrarySupport faceletsLibrarySupport;
    private WebModule wm;
    private ClassPath classpath;
    private JsfIndex index;
    private MetadataModel<WebBeansModel> webBeansModel;

    private JsfSupport(WebModule wm, ClassPath classPath) {
        assert wm != null;

        this.wm = wm;

        this.classpath = classPath;
        //create classpath support
        this.tldLibrariesCache = new TldLibrariesCache(this);
        this.faceletsDescriptorsCache = new FaceletsLibraryDescriptorCache(this);
        this.faceletsLibrarySupport = new FaceletsLibrarySupport(this);

        //adds a classpath listener which invalidates the index instance after classpath change
        //and also invalidates the facelets library descriptors and tld caches
        this.classpath.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (JsfSupport.this) {
                    index = null;
                }
                tldLibrariesCache.clearCache();
                faceletsDescriptorsCache.clearCache();
            }
        });
        //register html extension
        //TODO this should be done declaratively via layer
        JsfHtmlExtension.activate();

    }

    public ClassPath getClassPath() {
        return classpath;
    }

    public WebModule getWebModule() {
        return wm;
    }

    @Override
    public String toString() {
        return wm.getDocumentBase().toString();
    }

    public TldLibrary getTldLibrary(String namespace) {
        try {
            return tldLibrariesCache.getLibrary(namespace);
        } catch (LibraryDescriptorException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    public FaceletsLibraryDescriptor getFaceletsLibraryDescriptor(String namespace) {
        try {
            return faceletsDescriptorsCache.getLibrary(namespace);
        } catch (LibraryDescriptorException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    /** Returns a library descriptor for facelets library. If there is a .taglib.xml
     *  file returns the data from it otherwise tries to find corresponding .tld file.
     */
    public LibraryDescriptor getLibraryDescriptor(String namespace) {
        FaceletsLibraryDescriptor fld = getFaceletsLibraryDescriptor(namespace);
        return fld != null ? fld : getTldLibrary(namespace);
    }

    /** Library's uri to library map */
    public Map<String, FaceletsLibrary> getFaceletsLibraries() {
        return faceletsLibrarySupport.getLibraries();
    }

    public synchronized JsfIndex getIndex() {
        if(index == null) {
	    this.index = JsfIndex.create(wm);
        }
        return this.index;
    }

    public FaceletsLibrarySupport getFaceletsLibrarySupport() {
	return faceletsLibrarySupport;
    }

    public synchronized MetadataModel<WebBeansModel> getWebBeansModel() {
	if(webBeansModel == null) {
	    ModelUnit modelUnit = WebBeansModelSupport.getModelUnit(getWebModule());
	    webBeansModel = WebBeansModelFactory.getMetaModel(modelUnit);
	}
	return webBeansModel;
    }

    
}
