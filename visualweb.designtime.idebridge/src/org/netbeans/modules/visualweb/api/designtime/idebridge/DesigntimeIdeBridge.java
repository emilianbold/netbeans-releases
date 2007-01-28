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


package org.netbeans.modules.visualweb.api.designtime.idebridge;


import com.sun.rave.designtime.DesignBean;
import org.openide.nodes.Node;



/**
 * Service providing a bridge between designtime API (IDE independent code)
 * and openide API (IDE depdendent code).
 * <p>
 * <p>
 * <b><font color="red"><em>Important note for client: Never implement
 * this interface! Use the {@link DesigntimeIdeBridgeProvider#getDefault}
 * to retrieve the valid implementation</em></font></b>
 * </p>
 * <bold>Note for maintainer & provider:</bold> Do not any dependency on other Creator modules, this
 * service is solely to provide a 'bridge' between designtime API and
 * IDE API, which means do not add methods providing a bridge to JDK API or
 * other Creator modules.
 * </p>
 *
 * @author Peter Zavadsky
 */
public interface DesigntimeIdeBridge {

    /** Gets node representation of specified <code>DesignBean</code>.
     * @param designBean specific <code>DesignBean</code> instance, not <code>null</code>
     * @return <code>Node</code> instance representing the specified bean
     *         or broken node
     * @exception NullPointerException if the designBean is <code>null</code> */
    public Node getNodeRepresentation(DesignBean designBean);
}
