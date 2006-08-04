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

/*
 * WSDLService.java
 *
 * Created on August 16, 2005, 1:29 PM
 *
 */
package org.netbeans.modules.mobility.end2end.classdata;

/**
 *
 * @author Michal Skvor
 */
public class WSDLService extends AbstractService {
    
    protected String name;
    protected String type;
    protected String file;
    protected String url;
    
    public void setName( final String name ) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setType( final String type ) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setFile( final String file ) {
        this.file = file;
    }
    
    public String getFile() {
        return file;
    }
    
    public void setUrl( final String url ) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
}
