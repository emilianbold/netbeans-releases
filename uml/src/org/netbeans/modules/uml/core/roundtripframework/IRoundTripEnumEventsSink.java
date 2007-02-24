/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * IRoundTripEnumEventsSink.java
 *
 * Created on May 17, 2005, 7:18 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * IRoundTripEnumEventsSink provides the event listener interface that is used
 * to listen to round trip enumeration events.
 *
 * @author Trey Spiva
 */
public interface IRoundTripEnumEventsSink
{
    /**
     * Fired after the RequestProcessor has filtered its changes, but before actual processing of the requests has been done.
     */
    public void onPreEnumChangeRequest( IChangeRequest newVal, IResultCell cell );
    
    /**
     * Fired after the RequestProcessor has filtered its changes, allowing all listeners to process the requests.
     */
    public void onEnumChangeRequest( IChangeRequest newVal, IResultCell cell );
}
