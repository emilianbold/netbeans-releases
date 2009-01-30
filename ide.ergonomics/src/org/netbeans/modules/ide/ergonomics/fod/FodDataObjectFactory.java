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

package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import org.netbeans.api.autoupdate.UpdateElement;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/** Support for special dataobjects that can dynamically FoD objects.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FodDataObjectFactory implements DataObject.Factory {
    private static MultiFileLoader delegate;
    private static Method getCookie;
    static {
        try {
            getCookie = MultiDataObject.class.getDeclaredMethod("getCookieSet");
            getCookie.setAccessible(true);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private FileObject definition;
    
    private FodDataObjectFactory(FileObject fo) {
        this.definition = fo;
    }


    public static DataObject.Factory create(FileObject fo) {
        return new FodDataObjectFactory(fo);
    }

    public DataObject findDataObject(FileObject fo, Set<? super FileObject> recognized) throws IOException {
        if (delegate == null) {
            Enumeration<DataLoader> en = DataLoaderPool.getDefault().allLoaders();
            while (en.hasMoreElements()) {
                DataLoader d = en.nextElement();
                if (d instanceof MultiFileLoader) {
                    delegate = (MultiFileLoader)d;
                }
            }
            assert delegate instanceof MultiFileLoader;
        }
        return new Cookies(fo, delegate);
    }

    private final class Cookies extends MultiDataObject
    implements OpenCookie, EditCookie, Runnable {
        private FileObject fo;
        private boolean success;
        private Cookies(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
            this.fo = fo;
        }
        public void open() {
            delegate(true);
        }

        public void edit() {
            delegate(false);
        }

        private void delegate(boolean open) {
            RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY).waitFinished ();
            if (success) {
                try {
                    DataObject obj = DataObject.find(fo);
                    Class<?> what = open ? OpenCookie.class : EditCookie.class;
                    Object oc = obj.getLookup().lookup(what);
                    if (oc == this) {
                        obj.setValid(false);
                        obj = DataObject.find(fo);
                        oc = obj.getLookup().lookup(what);
                    }
                    if (oc instanceof OpenCookie) {
                        ((OpenCookie)oc).open();
                    }
                    if (oc instanceof EditCookie) {
                        ((EditCookie)oc).edit();
                    }
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        public void run() {
            FeatureInfo info = FoDFileSystem.getInstance().whichProvides(definition);
            FeatureManager.logUI("ERGO_FILE_OPEN", info.clusterName);
            FindComponentModules findModules = new FindComponentModules(info);
            Collection<UpdateElement> toInstall = findModules.getModulesForInstall ();
            Collection<UpdateElement> toEnable = findModules.getModulesForEnable ();
            if (toInstall != null && ! toInstall.isEmpty ()) {
                ModulesInstaller installer = new ModulesInstaller(toInstall, findModules);
                installer.getInstallTask ().waitFinished ();
                success = true;
            } else if (toEnable != null && ! toEnable.isEmpty ()) {
                ModulesActivator enabler = new ModulesActivator (toEnable, findModules);
                enabler.getEnableTask ().waitFinished ();
                success = true;
            } else if (toEnable.isEmpty() && toInstall.isEmpty()) {
                success = true;
            }
        }
    } // end Cookies
}
