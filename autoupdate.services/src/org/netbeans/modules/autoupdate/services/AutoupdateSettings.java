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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateSettings {

    private static String tempIdeIdentity = null;
    private static final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.services.AutoupdateSettings"); // NOI18N
    private static final String PROP_IDE_IDENTITY = "ideIdentity"; // NOI18N
    private static final String PROP_PERIOD = "period"; // NOI18N
    
    public static final int EVERY_STARTUP = 0;
    public static final int EVERY_DAY = 1;
    public static final int EVERY_WEEK = 2;
    public static final int EVERY_2WEEKS = 3;
    public static final int EVERY_MONTH = 4;
    public static final int EVERY_NEVER = 5;
    
    private AutoupdateSettings () {
    }
    
    public static void register () {
        getIdeIdentity ();
        // schedule refresh providers
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                scheduleRefreshProviders ();
            }
        }, 5000);
    }
    
    private static void scheduleRefreshProviders () {
        try {
            UpdateUnitProviderImpl.refresh ();
        } catch (IOException ioe) {
            err.log (Level.INFO, ioe.getMessage(), ioe);
        }
    }
    
    public static String getIdeIdentity () {
        if (tempIdeIdentity instanceof String) {
            return tempIdeIdentity;
        }
        Object oldIdeIdentity = getPreferences ().get (PROP_IDE_IDENTITY, null);
        String newIdeIdentity = null;
        if (oldIdeIdentity == null) {
            newIdeIdentity = modifyIdeIdentityIfNeeded (generateNewId ());
        } else {
            newIdeIdentity = modifyIdeIdentityIfNeeded ((String) oldIdeIdentity);
        }
        tempIdeIdentity = newIdeIdentity;
        if (! newIdeIdentity.equals (oldIdeIdentity)) {
            err.log (Level.FINE, "Put new value of PROP_IDE_IDENTITY to " + newIdeIdentity);
            getPreferences ().put (PROP_IDE_IDENTITY, newIdeIdentity);
        }
        return tempIdeIdentity;
    }
    
    public int getPeriod () {
        return getPreferences ().getInt (PROP_PERIOD, EVERY_WEEK);
    }

    public void setPeriod (int period) {
        getPreferences ().putInt (PROP_PERIOD, period);
    }
    
    private static Preferences getPreferences () {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate");
    }    
    
    // helper methods
    private static String modifyIdeIdentityIfNeeded (String oldIdeIdentity) {
        int idx = oldIdeIdentity.indexOf ('0');
        assert idx != -1 : oldIdeIdentity + " must contain delimeter 0.";
        String id = null;
        String oldPrefix = null;
        if (idx == 0) {
            id = oldIdeIdentity;
            oldPrefix = "";
        } else {
            oldPrefix = oldIdeIdentity.substring (0, idx);
            id = oldIdeIdentity.substring (oldPrefix.length ());
        }
        err.log (Level.FINER, "Old IDE Identity Prefix: " + oldPrefix); // NOI18N
        err.log (Level.FINER, "Old IDE Identity ID: " + id); // NOI18N
        String newPrefix = "";
        try {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("/productid"); // NOI18N
            if (fo != null) {
                InputStream is = fo.getInputStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader (is));
                    newPrefix = r.readLine().trim();
                } finally {
                    is.close();
                }
            }
        } catch (IOException ignore) {
            err.log (Level.FINER, ignore.getMessage(), ignore);
        }
        if (!newPrefix.equals (oldPrefix)) {
            err.log (Level.FINER, "New IDE Identity Prefix: " + newPrefix); // NOI18N
        } else {
            err.log (Level.FINER, "No new prefix."); // NOI18N
        }
        return newPrefix + id;
    }

    private static String generateNewId () {
        return "0" + UUID.randomUUID ().toString ();
    }
}
