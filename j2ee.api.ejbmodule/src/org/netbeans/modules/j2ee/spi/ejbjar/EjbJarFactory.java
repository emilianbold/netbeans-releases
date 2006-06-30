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

package org.netbeans.modules.j2ee.spi.ejbjar;

import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.ejbjar.EjbJarAccessor;
import org.netbeans.modules.j2ee.ejbjar.EarAccessor;

/**
 * Most general way to create {@link EjbJar} and {@link Ear} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link EjbJarImplementation} or {@link EarImplementation} and use this factory.
 *
 * @author  Pavel Buzek
 */
public final class EjbJarFactory {

    private EjbJarFactory () {
    }

    /**
     * Create API ejbmodule instance for the given SPI webmodule.
     * @param spiWebmodule instance of SPI webmodule
     * @return instance of API webmodule
     */
    public static EjbJar createEjbJar(EjbJarImplementation spiWebmodule) {
        return EjbJarAccessor.DEFAULT.createEjbJar (spiWebmodule);
    }

    /**
     * Create API Ear instance for the given SPI webmodule.
     * @param spiEar instance of SPI Ear
     * @return instance of API Ear
     */
    public static Ear createEar(EarImplementation spiEar) {
        return EarAccessor.DEFAULT.createEar (spiEar);
    }
}
