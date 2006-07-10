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
