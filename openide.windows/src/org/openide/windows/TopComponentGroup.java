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
package org.openide.windows;

import java.util.Set;


/**
 * Represents window group. The notion of window group is defined by
 * <a href="http://ui.netbeans.org/docs/ui/ws/ws_spec.html#3.7">Window system UI specification document</a>.
 * The concept of window group explains <a href="http://core.netbeans.org/windowsystem/changes.html#2.3">API changes document</a>.
 *
 * <p><p>
 * <b><font color="red"><em>Importatnt note: Do not provide implementation of this interface unless you are window system provider!</em></font></b>
 *
 * @author  Peter Zavadsky
 * @since 4.13
 */
public interface TopComponentGroup {
    /** Opens all TopComponent's belonging to this group which have opening flag
     * switched on. */
    public void open();

    /** Closes all TopComponent's belonging to this group which have closing flag
     * switched on. */
    public void close();

    //    /** Set of TopComponentS belonging to this group. */
    //    public Set getTopComponents(); // TEMP
}
