/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.*;
import java.util.*;
import org.openide.filesystems.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Jaroslav Tulach
 */
public class LoaderPoolNodeTest extends org.netbeans.junit.NbTestCase {
    private OldStyleLoader oldL;
    private NewStyleLoader newL;

    public LoaderPoolNodeTest (String testName) {
        super (testName);
    }

    protected void setUp () throws java.lang.Exception {
        oldL = (OldStyleLoader)OldStyleLoader.getLoader (OldStyleLoader.class);
        newL = (NewStyleLoader)NewStyleLoader.getLoader (NewStyleLoader.class);
        LoaderPoolNode.doAdd (oldL, null);
        LoaderPoolNode.doAdd (newL, null);
    }

    protected void tearDown () throws java.lang.Exception {
        LoaderPoolNode.remove (oldL);
        LoaderPoolNode.remove (newL);
    }

    public void testOldLoaderThatChangesActionsBecomesModified () throws Exception {
        assertFalse ("Not modified at begining", LoaderPoolNode.isModified (oldL));
        Object actions = oldL.getActions ();
        assertNotNull ("Some actions there", actions);
        assertTrue ("Default actions called", oldL.defaultActionsCalled);
        assertFalse ("Still not modified", LoaderPoolNode.isModified (oldL));
        
        ArrayList list = new ArrayList ();
        list.add (OpenAction.get (OpenAction.class));
        list.add (NewAction.get (NewAction.class));
        oldL.setActions ((SystemAction[])list.toArray (new SystemAction[0]));
        
        assertTrue ("Now it is modified", LoaderPoolNode.isModified (oldL));
        List l = Arrays.asList (oldL.getActions ());
        assertEquals ("Actions are the same", list, l);        
    }
    
    public void testNewLoaderThatChangesActionsBecomesModified () throws Exception {
        assertFalse ("Not modified at begining", LoaderPoolNode.isModified (newL));
        Object actions = newL.getActions ();
        assertNotNull ("Some actions there", actions);
        assertTrue ("Default actions called", newL.defaultActionsCalled);
        assertFalse ("Still not modified", LoaderPoolNode.isModified (newL));
        
        ArrayList list = new ArrayList ();
        list.add (OpenAction.get (OpenAction.class));
        list.add (NewAction.get (NewAction.class));
        newL.setActions ((SystemAction[])list.toArray (new SystemAction[0]));
        
        assertFalse ("Even if we changed actions, it is not modified", LoaderPoolNode.isModified (newL));
        List l = Arrays.asList (newL.getActions ());
        assertEquals ("But actions are changed", list, l);        
    }
    
    public static class OldStyleLoader extends org.openide.loaders.UniFileLoader {
        boolean defaultActionsCalled;
        
        public OldStyleLoader () {
            super (org.openide.loaders.MultiDataObject.class);
        }
        
        protected org.openide.loaders.MultiDataObject createMultiObject (FileObject fo) throws IOException {
            throw new IOException ("Not implemented");
        }

        protected SystemAction[] defaultActions () {
            defaultActionsCalled = true;
            SystemAction[] retValue;
            
            retValue = super.defaultActions();
            return retValue;
        }
        
        
    }
    
    public static final class NewStyleLoader extends OldStyleLoader {
        protected String actionsContext () {
            return "Loaders/IamTheNewBeggining";
        }
    }
}
