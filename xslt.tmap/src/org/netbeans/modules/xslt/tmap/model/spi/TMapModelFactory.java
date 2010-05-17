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
package org.netbeans.modules.xslt.tmap.model.spi;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface TMapModelFactory {

    /**
     * Returns TMapModel instance that correspond to <code>source</code>.
     * Model will be loaded and instantiated if it doesn't exist yet.
     * @param source ModelSource class.
     * @return Instantiated model.
     */
    TMapModel getModel(ModelSource source);
    
    /**
     * Default implementation of accessor to TMapModelFactory.
     *
     */
    class TMapModelFactoryAccess {
        
        public static  TMapModelFactory getFactory() {
            return Lookup.getDefault().lookup(TMapModelFactory.class );
        }
    }

}
