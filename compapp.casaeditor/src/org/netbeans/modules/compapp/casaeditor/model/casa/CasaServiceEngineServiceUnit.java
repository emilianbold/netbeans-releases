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

/**
 *
 * @author jqian
 */
public interface CasaServiceEngineServiceUnit extends CasaServiceUnit {
    
    public static final String ENDPOINTS_PROPERTY = "endpoints";        // NOI18N
    public static final String X_PROPERTY = "x";                        // NOI18N
    public static final String Y_PROPERTY = "y";                        // NOI18N
    public static final String INTERNAL_PROPERTY = "internal";          // NOI18N
    public static final String DEFINED_PROPERTY = "defined";            // NOI18N
    public static final String UNKNOWN_PROPERTY = "unknown";            // NOI18N
    public static final String CONSUMES_PROPERTY = "consumes";          // NOI18N
    public static final String PROVIDES_PROPERTY = "provides";          // NOI18N
       
    List<CasaConsumes> getConsumes();
    void addConsumes(int index, CasaConsumes casaConsumes);
    void removeConsumes(CasaConsumes casaConsumes);       
    
    List<CasaProvides> getProvides();
    void addProvides(int index, CasaProvides casaProvides);
    void removeProvides(CasaProvides casaProvides); 
    
    int getX();
    void setX(int x);
    
    int getY();
    void setY(int y);
    
    boolean isInternal();
    void setInternal(boolean internal);
    
    /**
     * Checks whether this service unit is defined or not.
     * A defined service unit is a service unit that has been built by the
     * compapp build script.
     */
    boolean isDefined();
    void setDefined(boolean defined);
    
    /**
     * Checks whether this service unit is unknown.
     * An unknown service unit is a service unit that has not been associated
     * with a concrete component project.
     */
    boolean isUnknown();
    void setUnknown(boolean unknown);
    
    // Convinience methods
    List<CasaEndpointRef> getEndpoints();    
}
