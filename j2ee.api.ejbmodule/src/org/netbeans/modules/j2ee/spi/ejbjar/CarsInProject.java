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

import org.netbeans.modules.j2ee.api.ejbjar.Car;

/**
 * Put an implementation of this interface into lookup of an application
 * client (car) project.
 * This is a complementary interface to CarProvider to allow clients find all
 * car modules within a project w/o specifying a concrete file.
 * @see Car#getCar
 * @author Pavel Buzek
 * @author Lukas Jungmann
 */
public interface CarsInProject {
    
    /**
     * Get Car for all application client (car) modules in a given project.
     * 
     * @return an array of Car for all modules in a project
     * @see CarFactory
     */
    Car[] getCars();
    
}
