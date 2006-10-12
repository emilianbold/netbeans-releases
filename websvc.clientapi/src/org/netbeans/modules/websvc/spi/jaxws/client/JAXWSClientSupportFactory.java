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

package org.netbeans.modules.websvc.spi.jaxws.client;

import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.jaxws.client.JAXWSClientSupportAccessor;

/**
 * Most general way to create {@link WebServicesSupport} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link WebServicesSupportImpl} and use this factory.
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public final class JAXWSClientSupportFactory {

    private JAXWSClientSupportFactory() {
    }

    public static JAXWSClientSupport createJAXWSClientSupport(JAXWSClientSupportImpl spiJAXWSClientSupport) {
        return JAXWSClientSupportAccessor.DEFAULT.createJAXWSClientSupport(spiJAXWSClientSupport);
    }

}
