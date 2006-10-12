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
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.ejbjar.EarAccessor;
import org.netbeans.modules.j2ee.ejbjar.CarAccessor;

/**
 * Most general way to create {@link Car} and {@link Ear} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link CarImplementation} or {@link EarImplementation} and use this factory.
 *
 * @author  Pavel Buzek
 * @author  Lukas Jungmann
 */
public final class CarFactory {

    private CarFactory () {
    }

    /**
     * Create API application client module (carmodule)
     * instance for the given SPI carmodule.
     * @param spiCarmodule instance of SPI carmodule
     * @return instance of API car module
     */
    public static Car createCar(CarImplementation spiCarmodule) {
        return CarAccessor.DEFAULT.createCar (spiCarmodule);
    }

    /**
     * Create API Ear instance for the given SPI carmodule.
     * @param spiEar instance of SPI Ear
     * @return instance of API Ear
     */
    public static Ear createEar(EarImplementation spiEar) {
        return EarAccessor.DEFAULT.createEar (spiEar);
    }
}
