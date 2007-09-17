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

package org.netbeans.api.jumpto.type;

import org.netbeans.modules.jumpto.type.GoToTypeAction;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;

/**
 * Support for browsing of the types. Opens search dialog for the type name 
 * with possibility to filter the results.
 * 
 * @author Martin Adamek
 * 
 * @since 1.3
 */
public final class TypeBrowser {

    /**
     * Blocking call for opening modal search dialog
     * 
     * @param title title of the dialog
     * @param filter optional filter of the results; can be null
     * @param typeProviders type providers defining the scope of the search; 
     * if none specified, all type providers from default lookup will be used
     * @return selected type or null if dialog was canceled
     */
    public static TypeDescriptor browse(String title, Filter filter, TypeProvider... typeProviders) {
        GoToTypeAction goToTypeAction = new GoToTypeAction(title, filter, typeProviders);
        return goToTypeAction.getSelectedType();
    }

    /**
     * Filtering support
     */
    public static interface Filter {
        
        boolean accept(TypeDescriptor typeDescriptor);
        
    }
    
}
