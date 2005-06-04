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

package org.netbeans.core.startup;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.security.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

import org.netbeans.CLIHandler;

import org.openide.util.NbBundle;

/**
 * Handler for core options.
 * @author Jaroslav Tulach
 */
public class CLIOptions extends CLIHandler {
    
    /** directory for modules */
    static final String DIR_MODULES = "modules"; // NOI18N
    
    /** The flag whether to create the log - can be set via -nologging
    * command line option */
    protected static boolean noLogging = false;

    /** The flag whether to show the Splash screen on the startup */
    protected static boolean noSplash = false;
    /** The Class that logs the IDE events to a log file */
    protected static TopLogging logger;
    /* The class of the UIManager to be used for netbeans - can be set by command-line argument -ui <class name> */
    protected static Class uiClass;
    /* The size of the fonts in the UI - 0 pt, the default value is set in NbTheme (for Metal L&F), for other L&Fs is set
       in the class Main. The value can be changed in Themes.xml in system directory or by command-line argument -fontsize <size> */
    protected static int uiFontSize = 0;

    /** The netbeans home dir - acquired from property netbeans.home */
    private static String homeDir;
    /** The netbeans user dir - acquired from property netbeans.user */
    private static String userDir;
    /** The netbeans system dir - ${netbeans.user}/system */
    private static String systemDir;

    
    
    private static boolean specifiedBranding = false;

    /**
     * Create a default handler.
     */
    public CLIOptions() {
        super(WHEN_BOOT);
    }
    
    protected int cli(Args arguments) {
        return cli(arguments.getArguments());
    }
    
    
    /** Checks whether we are supposed to use GUI features or not.
     */
    public static boolean isGui () {
        return "true".equals (System.getProperty ("org.openide.TopManager.GUI")); // NOI18N
    }
    
    private static boolean isOption (String value, String optionName) {
        if (value == null) return false;
        
        if (value.startsWith ("--")) {
            return value.substring (2).equals (optionName);
        } else if (value.startsWith ("-")) {
            return value.substring (1).equals (optionName);
        }
        return false;
    }
    
    public final int cli(String[] args) {
        // let's go through the command line
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }
            boolean used = true;
            if (isOption (args[i], "nogui")) { // NOI18N
                System.getProperties().put("org.openide.TopManager", "org.netbeans.core.NonGui"); // NOI18N
                System.setProperty ("org.openide.TopManager.GUI", "false"); // NOI18N
            } else if (isOption (args[i], "nosplash")) { // NOI18N
                noSplash = true;
            } else if (isOption (args[i], "noinfo")) { // NOI18N
                // obsolete switch, ignore
            } else if (isOption (args[i], "nologging")) { // NOI18N
                noLogging = true;
            } else if (isOption (args[i], "userdir")) { // NOI18N
                args[i] = null;
                try {
                    System.setProperty ("netbeans.user", args[++i]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.err.println(getString("ERR_UserDirExpected"));
                    return 2;
                }
            } else if (isOption (args[i], "ui") || isOption (args[i], "laf")) { // NOI18N
                args[i] = null;
                try {
                    String ui = args[++i];
                    //Do not allow GTK L&F on 1.4.2 - it will throw nasty 
                    //exceptions in strange places and not work right
                    if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(ui) 
                        && System.getProperty("java.version").indexOf ("1.4.2") != -1) {
                        System.err.println(getString("MSG_GTKNotSupported"));
                    } else {
                        uiClass = Class.forName(ui);
                    }
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.err.println(getString("ERR_UIExpected"));
                    return 2;
                } catch (ClassNotFoundException e2) {
                    System.err.println(getString("ERR_UINotFound"));
                }
            } else if (isOption (args[i], "fontsize")) { // NOI18N
                args[i] = null;
                try {
                    uiFontSize = Integer.parseInt(args[++i]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.err.println(getString("ERR_FontSizeExpected"));
                    return 2;
                } catch (NumberFormatException e2) {
                    System.err.println(getString("ERR_BadFontSize"));
                    return 1;
                }
            } else if (isOption (args[i], "locale")) { // NOI18N
                args[i] = null;
                String localeParam = args[++i];
                String language;
                String country = ""; // NOI18N
                String variant = ""; // NOI18N
                int index1 = localeParam.indexOf(":"); // NOI18N
                if (index1 == -1)
                    language = localeParam;
                else {
                    language = localeParam.substring(0, index1);
                    int index2 = localeParam.indexOf(":", index1+1); // NOI18N
                    if (index2 != -1) {
                        country = localeParam.substring(index1+1, index2);
                        variant = localeParam.substring(index2+1);
                    }
                    else
                        country = localeParam.substring(index1+1);
                }
                Locale.setDefault(new Locale(language, country, variant));
            } else if (isOption (args[i], "branding")) { // NOI18N
                args[i] = null;
                if (++i == args.length) {
                    System.err.println(getString("ERR_BrandingNeedsArgument"));
                    return 2;
                }
                String branding = args[i];
                if (branding.equals("-")) branding = null; // NOI18N
                try {
                    NbBundle.setBranding(branding);
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    return 1;
                }
                specifiedBranding = true;
            } else {
                used = false;
            }
            if (used) {
                args[i] = null;
            }
        }
        
        return 0;
    }
    
    /** Initializes logging etc.
     */
    public static void initialize() {
        if (!noLogging) {
            try {
                logger = new TopLogging(getLogDir());
            } catch (IOException e) {
                System.err.println("Cannot create log file. Logging disabled."); // NOI18N
                e.printStackTrace();
            }
        }
        StartLog.logProgress("TopLogging initialized"); // NOI18N
    }
    
    protected void usage(PrintWriter w) {
        w.println("Core options:");
        w.println("  --laf <LaF classname> use given LookAndFeel class instead of the default");
        w.println("  --fontsize <size>     set the base font size of the user interface, in points");
        w.println("  --locale <language[:country[:variant]]> use specified locale");
        w.println("  --userdir <path>      use specified directory to store user settings");
        w.println("");
//   \  --branding <token>    use specified branding (- for default)
//   
//   \  --nologging           do not create the log file\n\
//   \  --nosplash            do not show the splash screen\n\
//   \  --nogui               just start up internals, do not show GUI
    }
    
    private static String getString (String key) {
        return NbBundle.getMessage (CLIOptions.class, key);
    }
    
    //
    // Directory functions
    //
    

    /** Directory to place logs into logging.
    */
    public static String getLogDir () {
        return new File (new File (getUserDir (), "var"), "log").toString ();
    }

    /** Tests need to clear some static variables.
     */
    static final void clearForTests () {
        homeDir = null;
        userDir = null;
    }

    /** Getter for home directory. */
    public static String getHomeDir () {
        if (homeDir == null) {
            homeDir = System.getProperty ("netbeans.home");
        }
        return homeDir;
    }

    /** Getter for user home directory. */
    public static String getUserDir () {
        if (userDir == null) {
            userDir = System.getProperty ("netbeans.user");
            
            if ("memory".equals (userDir)) { // NOI18N
                return "memory"; // NOI18N
            }
            
            if (userDir == null) {
                System.err.println(NbBundle.getMessage(CLIOptions.class, "ERR_no_user_directory"));
                Thread.dumpStack(); // likely to happen from misbehaving unit tests, etc.
                org.netbeans.TopSecurityManager.exit(1);
            }
            if (userDir.equals(getHomeDir())) { 
                System.err.println(NbBundle.getMessage(CLIOptions.class, "ERR_user_directory_is_home"));
                org.netbeans.TopSecurityManager.exit(1);
            }
            
            /** #11735. Relative userDir is converted to absolute*/
            // #21085: userDir might contain ../ sequences which should be removed
            File f = new File (userDir);
            try {
                f = f.getCanonicalFile ();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            userDir = f.getPath();
            System.setProperty("netbeans.user", userDir); // NOI18N
            
            File systemDirFile = new File (userDir, NbRepository.SYSTEM_FOLDER);
            makedir (systemDirFile);
            systemDir = systemDirFile.getAbsolutePath ();
            makedir(new File(userDir, DIR_MODULES)); // NOI18N
        }
        return userDir;
    }

    private static void makedir (File f) {
        if (f.isFile ()) {
            Object[] arg = new Object[] {f};
            System.err.println (NbBundle.getMessage (CLIOptions.class, "CTL_CannotCreate_text", arg));
            org.netbeans.TopSecurityManager.exit (6);
        }
        if (! f.exists ()) {
            if (! f.mkdirs ()) {
                Object[] arg = new Object[] {f};
                System.err.println (NbBundle.getMessage (CLIOptions.class, "CTL_CannotCreateSysDir_text", arg));
                org.netbeans.TopSecurityManager.exit (7);
            }
        }
    }
    
    /** System directory getter.
    */
    protected static String getSystemDir () {
        getUserDir ();
        return systemDir;
    }

    //
    // other getters
    //
    
    public static int getFontSize () {
        return uiFontSize;
    }
}
