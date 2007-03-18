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

import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Endpoint;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provides;

/**
 *
 * @author jqian
 */
public interface CasaPort extends CasaComponent {
    public static final String LINK_PROPERTY = "link";                  // NOI18N
    public static final String X_PROPERTY = "x";                        // NOI18N
    public static final String Y_PROPERTY = "y";                        // NOI18N
    public static final String CONSUMES_PROPERTY = "consumes";          // NOI18N
    public static final String PROVIDES_PROPERTY = "provides";          // NOI18N
    
    public static final String BINDINGTYPE_PROPERTY = "bindingType";    // NOI18N
    public static final String PORTTYPE_PROPERTY = "portType";          // NOI18N

    String getPortType();
    void setPortType(String portType);
    
    String getBindingType();
    void setBindingType(String bindingType);
    
    int getX();
    void setX(int x);
    
    int getY();
    void setY(int y);
    
    CasaLink getLink();
    void setLink(CasaLink link);
    
    CasaConsumes getConsumes();
    void setConsumes(CasaConsumes casaConsumes);
    
    CasaProvides getProvides();
    void setProvides(CasaProvides casaProvides);
    
    // Convenience methods
    String getEndpointName();
}
