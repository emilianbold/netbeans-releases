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

import java.io.File;

/**
 *
 * @author Dmitry Lipin
 */
public class SystemApplication {
    private String location;
    private String friendlyName;
    private String command;
    private boolean useByDefault;
    private boolean addOpenWithList;
    
    public SystemApplication(String location) {
        this.location = location;
    }
    public SystemApplication(File file) {
        this((file!=null) ? file.getPath() : null);
    }
    protected SystemApplication(SystemApplication sapp) {
        location=sapp.location;
        friendlyName=sapp.friendlyName;
        command = sapp.command;
        useByDefault = sapp.isUseByDefault();
        addOpenWithList = sapp.isAddOpenWithList();
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String appLocation) {
        this.location = appLocation;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String appFriendlyName) {
        this.friendlyName = appFriendlyName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setByDefault(boolean useByDefault) {
        this.useByDefault = useByDefault;
    }
    
    public void setOpenWithList(boolean addOpenWithList) {
        this.addOpenWithList = addOpenWithList;
    }

    public boolean isUseByDefault() {
        return useByDefault;
    }

    public boolean isAddOpenWithList() {
        return addOpenWithList;
    }
    
}
