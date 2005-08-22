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
package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.Preferences;
import org.netbeans.junit.*;
import org.openide.util.NbBundle;

/** Tests the behaviour of the import user dir "api".
 */
public class NonGuiHandleCheckOfLicenseTest extends NbTestCase {
    private static NonGuiHandleCheckOfLicenseTest instance;
    
    private File user;
    private File userVar;
    private boolean updaterInvoked;
    private Throwable toThrow;
    private String nbHome;
    
    public static void showLicensePanel () throws Throwable {
        if (instance != null) {
            // ok this is invoked from the test by the core-launcher
            instance.nowDoTheInstall ();
            return;
        } else {
            // initial start
            junit.textui.TestRunner.run(new junit.framework.TestSuite (NonGuiHandleCheckOfLicenseTest.class));
        }
    }

    public NonGuiHandleCheckOfLicenseTest (String name) {
        super(name);
    }

    protected void setUp () throws Exception {
        clearWorkDir ();
        CLIOptions.clearForTests ();
        
        File home = new File (getWorkDir (), "home");
        user = new File (getWorkDir (), "user");
        userVar = new File (user,"var");
        
        assertTrue ("Home dir created", home.mkdirs ());
        assertTrue ("User dir created", user.mkdirs ());
        
        System.setProperty ("netbeans.home", home.toString ());
        System.setProperty ("netbeans.user", user.toString ());
        
        System.setProperty ("netbeans.accept_license_class", NonGuiHandleCheckOfLicenseTest.class.getName ());
        
        //Clean preferences
        String licenseVersion = NbBundle.getMessage(Main.class,"licenseVersion");
        
        nbHome = System.getProperty("netbeans.home");
        File nbHomeDir = new File(nbHome);
        try {
            nbHome = nbHomeDir.getCanonicalPath();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        String md5sumKey = generateKey(nbHome);
        
        Preferences prefUserNode = Preferences.userNodeForPackage(Main.class);
        prefUserNode.remove("LICENSE" + "|" + licenseVersion + "|" + md5sumKey);
        File f = new File(userVar,"license_accepted");
        if (f.exists()) {
            f.delete();
        }
        
        instance = this;
    }
    
    protected void tearDown () throws Exception {
        instance = null;
    }
    
    private void nowDoTheInstall () throws Throwable {
        assertTrue ("Called from AWT thread", javax.swing.SwingUtilities.isEventDispatchThread ());
        if (toThrow != null) {
            Throwable t = toThrow;
            toThrow = null;
            throw t;
        }
        
        updaterInvoked = true;
    }
    
    /** Test if check is invoked when there is not file "var/license_accepted" */
    public void testIfTheUserDirIsEmptyTheLicenseCheckIsInvoked () {
        assertTrue ("Ok, returns without problems", Main.handleLicenseCheck ());
        assertTrue ("the main method invoked", updaterInvoked);
        
        toThrow = new RuntimeException ();
        
        //File "var/license_accepted" is created during first call in user dir
        //then license check is not invoked anymore
        assertTrue ("The check is not called anymore 1", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 2", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 3", Main.handleLicenseCheck ());
        assertTrue ("The check is not called anymore 4", Main.handleLicenseCheck ());
        
        //Clean preferences and user dir
        String licenseVersion = NbBundle.getMessage(Main.class,"licenseVersion");

        String md5sumKey = generateKey(nbHome);

        Preferences prefUserNode = Preferences.userNodeForPackage(Main.class);
        prefUserNode.remove("LICENSE" + "|" + licenseVersion + "|" + md5sumKey);
        File f = new File(userVar,"license_accepted");
        if (f.exists()) {
            f.delete();
        }
    }
    
    public void testIfInvokedAndThrowsExceptionTheExecutionStops () {
        toThrow = new RuntimeException();
        
        assertFalse ("Says no as exception was thrown", Main.handleLicenseCheck());
        assertNull ("Justs to be sure the exception was cleared", toThrow);
    }
    
    public void testIfThrowsUserCancelExThenLicenseCheckIsCalledAgain () {
        toThrow = new org.openide.util.UserCancelException();
        assertFalse("Says no as user did not accept the license", Main.handleLicenseCheck());
        assertNull("Justs to be sure the exception was cleared", toThrow);
        
        toThrow = new org.openide.util.UserCancelException();
        assertFalse("Says no as user did not accept the license", Main.handleLicenseCheck());
        assertNull("Justs to be sure the exception was cleared", toThrow);
    }
    
    /** Generate 32 byte long fingerprint of input string in sting form */
    private String generateKey (String input) {
        String key = null;
        //Set default value in case anything fails.
        if (input.length() > 32) {
            key = input.substring(input.length() - 32, input.length());
        } else {
            key = input;
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException exc) {
            exc.printStackTrace();
            return key;
        }

        byte [] arr = new byte[0];
        try {
            arr = nbHome.getBytes("UTF-8");
        } catch (UnsupportedEncodingException exc) {
            exc.printStackTrace();
            return key;
        }

        byte [] md5sum = md.digest(arr);
        StringBuffer keyBuff = new StringBuffer(32);
        //Convert byte array to hexadecimal string to be used as key
        for (int i = 0; i < md5sum.length; i++) {
            int val = md5sum[i];
            if (val < 0) {
                val = val + 256;
            }
            String s = Integer.toHexString(val);
            if (s.length() == 1) {
                keyBuff.append("0");
            }
            keyBuff.append(Integer.toHexString(val));
        }
        key = keyBuff.toString();
        return key;
    }
            
}
