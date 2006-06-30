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

package org.netbeans.modules.tomcat5;

import javax.enterprise.deploy.spi.Target;

/** Dummy implementation of target for Tomcat 5 server
 *
 * @author  Radim Kubacki
 */
public class TomcatTarget implements Target {

    private String name;

    private String desc;

    private String uri;

    public TomcatTarget (String name, String desc, String uri) {
        this.name = name;
        this.desc = desc;
        this.uri = uri;
    }

    public String getName () {
        return name;
    }

    public String getDescription () {
        return desc;
    }
    
    public String getServerUri () {
        return uri;
    }
    
    public String toString () {
        return name;
    }
}
