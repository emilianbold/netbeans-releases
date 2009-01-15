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
package org.netbeans.modules.project.libraries;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderInstance;

public final class LibraryTypeRegistry extends FolderInstance {

    private static final String REGISTRY = "org-netbeans-api-project-libraries/LibraryTypeProviders";              //NOI18N
    private static FileObject findProvidersFolder() {
        FileObject folder = FileUtil.getConfigFile(REGISTRY);
        if (folder == null) {
            // #50391 - maybe we are turning this module off?
            try {
                folder = FileUtil.createFolder(FileUtil.getConfigRoot(), REGISTRY);
            } catch (IOException e) {
                // Hmm, what to do?
                throw (IllegalStateException) new IllegalStateException("Cannot make folder " + REGISTRY + ": " + e).initCause(e);
            }
        }
        return folder;
    }
    
    private static Reference<LibraryTypeRegistry> instance;

    private LibraryTypeRegistry () {
        super(DataFolder.findFolder(findProvidersFolder()));
    }

    public LibraryTypeProvider[] getLibraryTypeProviders () {
        try {
            return (LibraryTypeProvider[]) this.instanceCreate ();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        catch (ClassNotFoundException cnf) {
            ErrorManager.getDefault().notify(cnf);
        }
        return new LibraryTypeProvider[0];
    }

    public LibraryTypeProvider getLibraryTypeProvider (String libraryType) {
        assert libraryType != null;
        try {
            LibraryTypeProvider[] providers = (LibraryTypeProvider[]) this.instanceCreate ();
            for (int i = 0; i < providers.length; i++) {
                if (libraryType.equals(providers[i].getLibraryType()))
                    return providers[i];
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify (ioe);
        }
        catch (ClassNotFoundException cnfe) {
            ErrorManager.getDefault().notify (cnfe);
        }
        return null;
    }


    protected Object createInstance(InstanceCookie[] cookies) throws IOException, ClassNotFoundException {
        List<LibraryTypeProvider> installers = new ArrayList<LibraryTypeProvider>(cookies.length);
        for (InstanceCookie cake : cookies) {
            LibraryTypeProvider o = null;
            try {
                if (cake instanceof InstanceCookie.Of && !(((InstanceCookie.Of)cake).instanceOf(LibraryTypeProvider.class)))
                    continue;
                o = (LibraryTypeProvider) cake.instanceCreate();
            } catch (IOException ex) {
            } catch (ClassNotFoundException ex) {
            }
            if (o != null)
                installers.add(o);
        }
        return installers.toArray(new LibraryTypeProvider[installers.size()]);
    }


    public static synchronized LibraryTypeRegistry getDefault () {
        LibraryTypeRegistry regs = null;
        if (instance == null || (regs = instance.get()) == null) {
            regs = new LibraryTypeRegistry();
            instance = new SoftReference<LibraryTypeRegistry>(regs);
        }
        return regs;
    }

}
