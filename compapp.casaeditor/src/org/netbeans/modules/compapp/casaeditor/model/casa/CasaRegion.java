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
package org.netbeans.modules.compapp.casaeditor.model.casa;

import org.netbeans.modules.compapp.casaeditor.model.*;

/**
 *
 * @author jqian
 */
public interface CasaRegion extends CasaComponent {
    public static final String NAME_PROPERTY = "name";          // NOI18N
    public static final String WIDTH_PROPERTY = "width";        // NOI18N
    
    public enum Name {
        WSDL_ENDPOINTS("WSDL Endpoints"),                       // NOI18N
        JBI_MODULES("JBI Modules"),                             // NOI18N
        EXTERNAL_MODULES("External Modules");                   // NOI18N
        
        private String name;
        
        Name(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    };
    
    String getName();
    //void setName(NAME name);
    
    int getWidth();
    void setWidth(int width);
}
