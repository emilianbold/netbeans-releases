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
package org.netbeans.modules.websvc.jaxws.client;

import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;

/* This class provides access to the {@link WebServicesClientSupport}'s private constructor 
 * from outside in the way that this class is implemented by an inner class of 
 * {@link WebServicesClientSupport} and the instance is set into the {@link DEFAULT}.
 */
public abstract class JAXWSClientSupportAccessor {

    public static JAXWSClientSupportAccessor DEFAULT;
    
    // force loading of JAXWSClientSupport class. That will set DEFAULT variable.
    static {
        Class c = JAXWSClientSupport.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public abstract JAXWSClientSupport createJAXWSClientSupport(JAXWSClientSupportImpl spiJAXWSClientSupport);

    public abstract JAXWSClientSupportImpl getJAXWSClientSupportImpl(JAXWSClientSupport wscs);
}
