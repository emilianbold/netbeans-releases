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

package org.netbeans.api.autoupdate;

import org.netbeans.modules.autoupdate.services.UpdateProblemHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalog;
import org.openide.modules.Dependency;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateManagerTest extends DefaultTestCase {
    
    public UpdateManagerTest (String testName) {
        super (testName);
    }
    
    private UpdateUnit independent = null;
    private UpdateUnit depending = null;
    private static UpdateProblemHandler handler = new MyProblemHandler ();
    
    public void testGetDefault() {
        UpdateManager result = UpdateManager.getDefault ();

        assertNotNull ("UpdateManager.getDefault () found.", result);
    }

    public void testGetUpdateUnits() {
        List<UpdateUnit> result = UpdateManager.getDefault ().getUpdateUnits ();
        
        assertNotNull ("List of UpdateUnit found.", result);
        
        List<UpdateUnit> newModules = new ArrayList<UpdateUnit> ();
        for (UpdateUnit unit : result) {
            if (unit.getInstalled () == null) {
                newModules.add (unit);
            }
        }
        
        assertNotNull ("New Modules found.", newModules);
        assertFalse ("New Modules not empty.", newModules.isEmpty ());
        
        for (UpdateUnit unit: newModules) {
            System.out.println ("Unit: " + unit.getCodeName ());
            if (unit.getCodeName ().indexOf ("independent") >= 0) {
                independent = unit;
            }
            if (unit.getCodeName ().indexOf ("depending") >= 0) {
                depending = unit;
            }
        }
    }
    
    public void testGetDependingUpdateUnits () {
        List<UpdateUnit> result = UpdateManager.getDefault ().getUpdateUnits ();
        
        assertNotNull ("List of UpdateUnit found.", result);
        
        List<UpdateUnit> newModules = new ArrayList<UpdateUnit> ();
        for (UpdateUnit unit : result) {
            if (unit.getInstalled () == null) {
                newModules.add (unit);
            }
        }
        
        assertNotNull ("New Modules found.", newModules);
        assertFalse ("New Modules not empty.", newModules.isEmpty ());
        
        UpdateUnit engine = null;
        
        for (UpdateUnit unit: newModules) {
            if (unit.getCodeName ().indexOf ("org.yourorghere.engine") >= 0) {
                engine = unit;
            }
        }
        
        assertTrue ("There are more depending elements.", engine.getAvailableUpdates ().size () > 1);
    }
    
    public void testInstallIndependentUnit () {
        testGetUpdateUnits ();
        assertNotNull ("I have Independent module.", independent);
        assertNotNull ("Has some UpdateElements to install.", independent.getAvailableUpdates ());
        assertFalse ("Independent has some UpdateElements to install.", independent.getAvailableUpdates ().isEmpty ());
        UpdateElement el = independent.getAvailableUpdates ().get (0);
        assertNotNull ("I have UpdateElement to install.", el);        
    }

    public void testInstallDependingUnit () {
        testGetUpdateUnits ();
        assertNotNull ("I have Depending module.", depending);
        assertNotNull ("Has some UpdateElements to install.", depending.getAvailableUpdates ());
        assertFalse ("Depending has some UpdateElements to install.", depending.getAvailableUpdates ().isEmpty ());
        UpdateElement el = depending.getAvailableUpdates ().get (0);
        assertNotNull ("I have UpdateElement to install.", el);
    }
    
    public static class MyProvider extends AutoupdateCatalog {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", UpdateManagerTest.class.getResource ("data/updates.xml"));
        }
    }
    
    public static class MyProblemHandler extends UpdateProblemHandler {
        
        public boolean ignoreBrokenDependency (Dependency dependency) {
            if (Dependency.TYPE_REQUIRES == dependency.getType ()) {
                return true;
            } else if (Dependency.TYPE_MODULE == dependency.getType ()) {
                return ! dependency.getName ().startsWith ("org.yourorghere");
            }
            
            return false;
        }
    
        public boolean addRequiredElements (Set<UpdateElement> elements) {
            System.out.println ("addRequiredElements(" + elements + ")");
            return true;
        }
    
        public boolean allowUntrustedUpdateElement(String state, UpdateElement element) {
            System.out.println ("allowUntrustedUpdateElement(" + state + ", " + element.getDisplayName () + ")");
            return true;
        }

        public boolean approveLicenseAgreement (String license) {
            return true;
        }
        
        public boolean restartNow () {
            return false;
        }
        
    }
    
}
