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
 * $Id$
 */

package org.netbeans.installer.utils.system.windows;

import org.netbeans.installer.utils.StringUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class FileExtension {
    private String name;
    private String description;
    private PerceivedType perceivedType;
    private String mimeType;
    private String icon;
    public FileExtension(String extName) {
        setName(extName);
    }
    
    protected FileExtension(FileExtension fe) {
        name = fe.name;
        description = fe.description;
        perceivedType = fe.perceivedType;
        mimeType = fe.mimeType;
        icon = fe.icon;
    }
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        //remove starting dots
        if(name!=null) {
            while(name.substring(0,1).equals(StringUtils.DOT)) {
                name = name.substring(1);
            }
        }
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public PerceivedType getPerceivedType() {
        return perceivedType;
    }
    
    public void setPerceivedType(PerceivedType perceivedType) {
        this.perceivedType = perceivedType;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getDotName() {
        return StringUtils.DOT + getName();
    }
}
