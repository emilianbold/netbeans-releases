/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.core.bridge;

import com.sun.collablet.*;

import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class NbAccountManager extends AccountManager implements LookupListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////

    /** NOTE: This locations must match those declared in the layer! */
    public static final String ACCOUNT_FOLDER = "Services/Collaboration/Accounts"; // NOI18N

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Lookup.Result results;

    /**
     *
     *
     */
    public NbAccountManager() {
        super();

        // Lookup all the Accounts in the system.  By default, these should 
        // be registered in the /Services/Collaboration/Accounts folder.
        // The results will be updated as they change.
        results = Lookup.getDefault().lookup(new Lookup.Template(Account.class));
        results.addLookupListener(this);
    }

    /**
     *
     *
     */
    public synchronized Account[] getAccounts() {
        Collection accounts = results.allInstances();

        if (accounts == null) {
            accounts = new ArrayList();
        }

        return (Account[]) accounts.toArray(new Account[accounts.size()]);
    }

    /**
     *
     *
     */
    public void resultChanged(LookupEvent lookupEvent) {
        // We must invoke this in the AWT thread if we are to avoid deadlocks.
        // Otherwise, this code and all listeners' code may be invoked on the 
        // Folder Recognizer thread, thereby causing deadlocks when in 
        // contention with the AWT thread.
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    getChangeSupport().firePropertyChange(PROP_ACCOUNTS, null, null);
                }
            }
        );
    }

    /**
     *
     *
     * @return        The wrapper object that was created to contain the account
     *                        instance.
     */
    public Object addAccount(Account account) throws IOException {
        DataFolder root = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot());
        DataFolder folder = DataFolder.create(root, ACCOUNT_FOLDER);

        final String NAME = "Account"; // NOI18N

        // Put the account object as an instance into the account folder
        InstanceDataObject dataObject = InstanceDataObject.create(folder, NAME, account, null, true); // NOI18N
        getChangeSupport().firePropertyChange(PROP_NEW_ACCOUNT, null, account);

        return dataObject;
    }

    /**
     *
     *
     */
    public DataObject findAccountDataObject(Account account)
    throws IOException {
        DataFolder root = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot());
        DataFolder folder = DataFolder.create(root, ACCOUNT_FOLDER);

        // Find the InstanceDataObject with the matching account object
        for (Enumeration e = folder.children(); e.hasMoreElements();) {
            DataObject dataObject = (DataObject) e.nextElement();
            InstanceCookie cookie = (InstanceCookie) dataObject.getCookie(InstanceCookie.class);

            if (cookie != null) {
                try {
                    if (cookie.instanceCreate() == account) {
                        return dataObject;
                    }
                } catch (ClassNotFoundException ex) {
                    // Cannot happen
                    assert false : "This exception should not occur: " + ex;
                }
            }
        }

        return null;
    }

    /**
     *
     *
     */
    public void removeAccount(Account account) throws IOException {
        DataObject dataObject = findAccountDataObject(account);

        if (dataObject != null) {
            dataObject.delete();
        }
    }
}
