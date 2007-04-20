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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalog;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitWithOsFactoryTest extends NbTestCase {
    
    public UpdateUnitWithOsFactoryTest (String testName) {
        super (testName);
    }
    
    private UpdateProvider p = null;
    private String alientName = "org.netbeans.modules.applemenu";
    private String alientVersion = "1.111";
    
    protected void setUp () throws IOException, URISyntaxException {
        clearWorkDir ();
        String os = org.openide.util.Utilities.isUnix () ? "Windows" : "Unix";
        System.setProperty ("netbeans.user", getWorkDirPath ());
        Lookup.getDefault ().lookup (ModuleInfo.class);
        String catalog =    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
                            "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 3.0//EN\" \"file:/space/source/nb_all/autoupdate/libsrc/org/netbeans/updater/resources/autoupdate-catalog-3_0.dtd\">" +
                            "<module_updates timestamp=\"00/00/19/08/03/2006\">" +
                            "<module codenamebase=\"com.sun.collablet\" homepage=\"http://collab.netbeans.org/\" distribution=\"http://www.netbeans.org/download/nbms/alpha/dev/1.18/com-sun-collablet.nbm\" license=\"standard-nbm-license.txt\" downloadsize=\"30732\" needsrestart=\"false\" moduleauthor=\"\" releasedate=\"2006/02/23\">" +
                            "<manifest OpenIDE-Module=\"com.sun.collablet/1\" OpenIDE-Module-Display-Category=\"Collaboration\" OpenIDE-Module-Implementation-Version=\"200602231900\" OpenIDE-Module-Long-Description=\"Core multi-user collaboration API &amp; SPI\" OpenIDE-Module-Module-Dependencies=\"org.openide.filesystems &gt; 6.2, org.openide.util &gt; 6.2, org.openide.modules &gt; 6.2, org.openide.nodes &gt; 6.2, org.openide.loaders, org.openide.io\" OpenIDE-Module-Name=\"Collablet Core &amp; API\" OpenIDE-Module-Requires=\"org.openide.windows.IOProvider, org.openide.modules.ModuleFormat1\" OpenIDE-Module-Specification-Version=\"1.3\"/>" +
                            "</module>" +
                            "<module codenamebase=\"" + alientName + "\" homepage=\"http://ide.netbeans.org/\" distribution=\"http://www.netbeans.org/download/nbms/alpha/dev/1.18/org-netbeans-modules-applemenu.nbm\" license=\"standard-nbm-license.txt\" downloadsize=\"16986\" needsrestart=\"true\" moduleauthor=\"\" releasedate=\"2006/02/23\">" +
                            "<manifest OpenIDE-Module=\"org.netbeans.modules.applemenu/1\" OpenIDE-Module-Display-Category=\"Infrastructure\" OpenIDE-Module-Implementation-Version=\"200602231900\" OpenIDE-Module-Long-Description=\"Enables Apple menu items to work properly, and moves some standard menu items there - Tools | Options becomes Preferences, Help | About becomes about, File | Exit becomes Quit.\" OpenIDE-Module-Module-Dependencies=\"org.netbeans.core.windows/2, org.netbeans.modules.editor/3, org.netbeans.modules.java.editor/1 &gt; 1.3, org.openide.filesystems &gt; 6.2, org.openide.loaders, org.openide.modules &gt; 6.2, org.openide.nodes &gt; 6.2, org.openide.util &gt; 6.2\" OpenIDE-Module-Name=\"Apple Application Menu\" OpenIDE-Module-Requires=\"org.openide.modules.os." + os + ", org.openide.modules.ModuleFormat1\" OpenIDE-Module-Short-Description=\"Enables proper support for the Apple Application menu\" OpenIDE-Module-Specification-Version=\"" + alientVersion + "\"/>" +
                            "</module>" + 
                            "</module_updates>";
        try {
            p = new MyProvider (catalog);
        } catch (Exception x) {
            x.printStackTrace ();
        }
        p.refresh (true);
    }
    
    public void testUpdateItemsDoesntContainsAlien () throws IOException {
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());
        assertTrue (alientName + " found in parsed items.", updates.keySet ().contains (alientName + "_" + alientVersion));
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (unitImpls, p);
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        assertFalse (alientName + " doesn't found in generated UpdateUnits.", newImpls.keySet ().contains (alientName));
    }
    
    private URL generateFile (String s) throws IOException {
        File res = new File (getWorkDir (), "test-updates-with-os-provider.xml");
        OutputStream os = new FileOutputStream (res);
        os.write (s.getBytes ());
        os.close ();
        return res.toURL ();
    }
    
    public class MyProvider extends AutoupdateCatalog {
        public MyProvider (String s) throws IOException {
            super ("test-updates-with-os-provider", "test-updates-with-os-provider", generateFile (s));
        }
    }
    
}
