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

package org.netbeans.modules.j2ee.deployment.impl;

import java.util.Collection;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 *
 * Install.java
 *
 * Created on January 7, 2004, 7:34 AM
 * @author  nn136682
 */
public class Install extends org.openide.modules.ModuleInstall {

    /** Creates a new instance of Install */
    public Install() {
    }

    public void close() {
        if (ServerRegistry.wasInitialized ()) {
            Collection instances = ServerRegistry.getInstance().getInstances();
            for (Iterator i=instances.iterator(); i.hasNext();) {
                ((ServerInstance)i.next()).stopIfStartedByIde();
            }
        }
    }
}
