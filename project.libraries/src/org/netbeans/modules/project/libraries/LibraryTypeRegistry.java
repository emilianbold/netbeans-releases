/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.project.libraries;

import org.openide.loaders.FolderInstance;
import org.openide.loaders.DataFolder;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.Repository;
import org.openide.ErrorManager;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

public final class LibraryTypeRegistry extends FolderInstance {

    private static final String REGISTRY = "org-netbeans-api-project-libraries/LibraryTypeProviders";              //NOI18N
    private static Reference instance;

    private LibraryTypeRegistry () {
        super (DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().findResource (REGISTRY)));
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
        List installers = new ArrayList(cookies.length);
        for (int i = 0; i < cookies.length; i++) {
            InstanceCookie cake = cookies[i];
            Object o = null;
            try {
                if (cake instanceof InstanceCookie.Of && !(((InstanceCookie.Of)cake).instanceOf(LibraryTypeProvider.class)))
                    continue;
                o = cake.instanceCreate();
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
        if (instance == null || (regs = (LibraryTypeRegistry)instance.get()) == null) {
            regs = new LibraryTypeRegistry();
            instance = new SoftReference (regs);
        }
        return regs;
    }

}
