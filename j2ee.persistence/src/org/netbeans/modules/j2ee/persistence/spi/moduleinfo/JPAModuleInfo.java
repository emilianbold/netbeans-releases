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

package org.netbeans.modules.j2ee.persistence.spi.moduleinfo;

/**
 * This interface provides information on the project module, such 
 * as its type and version number. It should be implemented by projects that 
 * provide EJB or Web modules. 
 * 
 * @author Erno Mononen
 */
public interface JPAModuleInfo {

    enum ModuleType {
        EJB, 
        WEB
    }
    
    /**
     * Gets the type of our module.
     * 
     * @return the type of the module.
     */ 
    ModuleType getType();
    
    /**
     * Gets the version number of our module, i.e. for an EJB module
     * it might be <tt>"2.1" or "3.0"</tt> and for a Web module <tt>"2.4" or "2.5"</tt>.
     * 
     * @return the version number of the module.
     */ 
    String getVersion();
    
}
