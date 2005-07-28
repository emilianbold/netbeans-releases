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
package org.netbeans.modules.collab.core.module;

import com.sun.collablet.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.core.bridge.NbAccountManager;
import org.netbeans.modules.collab.core.bridge.NbAccountManagerLocator;
import org.netbeans.modules.collab.core.bridge.NbCollabManagerLocator;
import org.netbeans.modules.collab.core.bridge.NbCollabletFactoryManager;
import org.netbeans.modules.collab.core.bridge.NbCollabletFactoryManagerLocator;
import org.netbeans.modules.collab.core.bridge.NbUserInterfaceLocator;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 * @version        jatotools/@version@ $Id$
 */
public class Install extends ModuleInstall {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!

    /**
     *
     *
     */
    public Install() {
        super();
    }

    /**
     *
     *
     */
    public void installed() {
        Introspector.flushCaches();
        restored();
    }

    /**
     *
     *
     */
    public void restored() {
        // Initialize the bridge between the collablet classes and NetBeans
        CollabManager.setLocator(new NbCollabManagerLocator());
        UserInterface.setLocator(new NbUserInterfaceLocator());
        AccountManager.setLocator(new NbAccountManagerLocator(new NbAccountManager()));
        CollabletFactoryManager.setLocator(new NbCollabletFactoryManagerLocator(new NbCollabletFactoryManager()));

        // Initialize the debug
        new Thread() {
                public void run() {
                    try {
                        sleep(15000);
                        Debug.initializeOut();
                    } catch (InterruptedException e) {
                        // Do nothing
                    }
                }
            }.start();
    }
}
