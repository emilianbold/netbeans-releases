/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
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
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
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
