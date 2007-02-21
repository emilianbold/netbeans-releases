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
public interface CasaServiceUnit extends CasaComponent {
    
    public static final String NAME_PROPERTY = "name";
    public static final String UNIT_NAME_PROPERTY = "unit-name";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String COMPONENT_NAME_PROPERTY = "component-name";
    public static final String ARTIFACTS_ZIP_PROPERTY = "artifacts-zip";
           
    String getName();
    void setName(String name);
    
    String getUnitName();
    void setUnitName(String unitName);
    
    String getDescription();
    void setDescription(String description);
    
    String getComponentName();
    void setComponentName(String componentName);
    
    String getArtifactsZip();
    void setArtifactsZip(String artifactsZip);
}
