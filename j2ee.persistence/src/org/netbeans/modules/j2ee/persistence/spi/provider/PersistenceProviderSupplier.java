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

package org.netbeans.modules.j2ee.persistence.spi.provider;

import java.util.List;
import org.netbeans.modules.j2ee.persistence.provider.Provider;

/**
 * This interface should typically be implemented by projects where it 
 * is possible to use the Java Persistence API support. It provides means
 * for getting the supported persistence providers and for querying
 * whether a default persistence provider is supported.
 * 
 * @author Erno Mononen
 */
public interface PersistenceProviderSupplier {

    /**
     * Gets the persistence providers that are supported in 
     * the project. The preferred provider should
     * be the first item in the returned list.
     * 
     * @return a list of the supported providers, or an empty list if no
     * providers were supported; never null.
     */ 
    List<Provider> getSupportedProviders();

    /**
     * Queries whether a default persistence provider supported 
     * in the project (a default persistence provider is a provider that
     * doesn't need to be specified in persistence.xml). 
     * @return true if the project supports a default
     * persistence provider. 
     */ 
    boolean supportsDefaultProvider();

}
