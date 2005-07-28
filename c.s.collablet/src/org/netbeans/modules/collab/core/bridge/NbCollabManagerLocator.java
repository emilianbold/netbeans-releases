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
package org.netbeans.modules.collab.core.bridge;

import com.sun.collablet.*;

import org.openide.util.*;


/**
 * A simple class that proxies the use of <code>org.openide.util.Lookup</code>
 * for looking up the current <code>CollabManager</code> instance.
 *
 * @author Todd Fast, todd.fast@sun.com
 */
public class NbCollabManagerLocator extends Object implements CollabManager.Locator {
    /**
     *
     *
     */
    public NbCollabManagerLocator() {
        super();
    }

    /**
     *
     *
     */
    public CollabManager getInstance() {
        CollabManager result = (CollabManager) Lookup.getDefault().lookup(CollabManager.class);

        return result;
    }
}
