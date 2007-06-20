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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.api;

//import org.netbeans.modules.cnd.repository.impl.HashMapRepository;
//import org.netbeans.modules.cnd.repository.impl.KeyValidatorRepository;
//import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.openide.util.Lookup;

/**
 *
 * @author Sergey Grinev
 */
public class RepositoryAccessor 
{
    private RepositoryAccessor() {};
   
    private static Repository instance;
    private static final Object lock = new Object();

    /**
     * Default way for clients to get instance
     * @return instance of Repository
     */
    public static Repository getRepository() {
        if (instance == null) {
            synchronized (lock) {
                // double check is necessary because 
                // it is possible to have concurrent creators serialized on lock
                if (instance == null) {
                    instance = (Repository)Lookup.getDefault().lookup(Repository.class);
                }
            }
            assert(instance != null);
        }
        
        return instance;
    }
}
