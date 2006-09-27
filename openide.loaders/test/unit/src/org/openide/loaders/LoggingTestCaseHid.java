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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import junit.framework.AssertionFailedError;
import junit.framework.TestResult;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/** Basic skeleton for logging test case.
 *
 * @author  Jaroslav Tulach
 */
public abstract class LoggingTestCaseHid extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.LoggingTestCaseHid$Lkp");
    }

    protected LoggingTestCaseHid (String name) {
        super (name);
    }
    
    public void run(TestResult result) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.reset();
        
        super.run(result);
    }
    
    /** Allows subclasses to register content for the lookup. Can be used in 
     * setUp and test methods, after that the content is cleared.
     * @deprecated Please use {@link org.netbeans.junit.MockServices} instead.
     */
    protected final void registerIntoLookup(Object instance) {
        Lookup l = Lookup.getDefault();
        assertEquals("We can run only with our Lookup", Lkp.class, l.getClass());
        Lkp lkp = (Lkp)l;
        lkp.ic.add(instance);
    }
    
    protected void registerSwitches(String switches, int timeOut) {
        Log.controlFlow(Logger.getLogger(""), null, switches, timeOut);
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends ProxyLookup {
        InstanceContent ic;
        
        public Lkp () {
            super(new Lookup[0]);
        }
    
        public void reset() {
            this.ic = new InstanceContent();
            AbstractLookup al = new AbstractLookup(ic);
            setLookups(new Lookup[] { al, Lookups.metaInfServices(getClass().getClassLoader()) });
        }
    }
}
