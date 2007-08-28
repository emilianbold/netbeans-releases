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

package org.netbeans.modules.autoupdate.ui.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateSettings {

    private static String tempIdeIdentity = null;
    private static final Logger err = Logger.getLogger (AutoupdateSettings.class.getName ());
    private static final String PROP_IDE_IDENTITY = "ideIdentity"; // NOI18N
    private static final String PROP_PERIOD = "period"; // NOI18N
    private static final String PROP_LAST_CHECK = "lastCheckTime"; // NOI18N
    
    public static final int EVERY_STARTUP = 0;
    public static final int EVERY_DAY = 1;
    public static final int EVERY_WEEK = 2;
    public static final int EVERY_2WEEKS = 3;
    public static final int EVERY_MONTH = 4;
    public static final int EVERY_NEVER = 5;
    
    private AutoupdateSettings () {
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
    
    public static int getPeriod () {
        return getPreferences ().getInt (PROP_PERIOD, EVERY_WEEK);
    }

    public static void setPeriod (int period) {
        getPreferences ().putInt (PROP_PERIOD, period);
    }
    
    public static Date getLastCheck() {        
        long t = getPreferences ().getLong (PROP_LAST_CHECK, -1);
        return (t > 0) ? new Date (t) : null;

    }

    public static void setLastCheck (Date lastCheck) {
        err.log (Level.FINER, "Set the last check to " + lastCheck);
        if (lastCheck != null) {
            getPreferences().putLong (PROP_LAST_CHECK, lastCheck.getTime ());
        } else {
            getPreferences().remove (PROP_LAST_CHECK);
        }
    }
    
    private static Preferences getPreferences () {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate");
    }    
    
    // helper methods
    private static String modifyIdeIdentityIfNeeded (String oldIdeIdentity) {
        int idx = oldIdeIdentity.indexOf ('0');
        String [] ideIdentityArr = oldIdeIdentity.split ("\\d"); // NOI18N
        String id = null;
        String oldPrefix = null;
        
        // easy way -> no need to modify
        if (ideIdentityArr.length == 0 || idx == 0) {
            id = oldIdeIdentity;
            oldPrefix = "";
        // a way for UUID    
        } else if (idx != -1 && oldIdeIdentity.substring (ideIdentityArr [0].length ()).startsWith ("0")) {
            oldPrefix = oldIdeIdentity.substring (0, idx);
            id = oldIdeIdentity.substring (oldPrefix.length ());
        // old way for stored IDs Random.nextInt()
        } else {
            oldPrefix = ideIdentityArr [0];
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
