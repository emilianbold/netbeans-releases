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

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrarySupport;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrarySupport;
import org.netbeans.modules.web.jsf.editor.tld.TldClassPathSupport;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.openide.filesystems.FileObject;

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
        if(fo == null) {
            return null;
        } else {
            return findFor(fo);
        }
    }

    public static JsfSupport findFor(FileObject fo) {
        WebModule wm = WebModule.getWebModule(fo);
        if(wm == null) {
            return null;
        }
        synchronized (INSTANCIES) {
            JsfSupport instance = INSTANCIES.get(wm);
            if(instance == null) {
                instance = new JsfSupport(wm);
                INSTANCIES.put(wm, instance);
            }
            return instance;
        }

    }

    private TldClassPathSupport cpSupport;
    private FaceletsLibrarySupport faceletsLibrarySupport;
    private WebModule wm;
    private ClassPath classpath;

    private JsfSupport(WebModule wm) {
        assert wm != null;

        this.wm = wm;

        this.classpath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
        //create classpath support
        this.cpSupport = new TldClassPathSupport(classpath);

        this.faceletsLibrarySupport = new FaceletsLibrarySupport(this);
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

    /** Library's uri to library map */
    public Map<String, TldLibrary> getTldLibraries() {
        return cpSupport.getLibraries();
    }

    /** Library's uri to library map */
    public Map<String, FaceletsLibrary> getFaceletsLibraries() {
        return faceletsLibrarySupport.getLibraries();
    }


}
