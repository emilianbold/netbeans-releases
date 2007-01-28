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
/*
 * RequestedEjbResource.java
 *
 * Created on June 1, 2004, 4:53 PM
 */

package org.netbeans.modules.visualweb.api.j2ee.common;

/**
 * This class encapsulates the information for the referenced EJB
 * in the web application
 *
 * @author  cao
 */
public class RequestedEjbResource extends RequestedResource
{
    // ejbRefName and jndiName are in the super class.
    // ejbRefName is the resourceName in the super class

    private String ejbRefType;
    private String homeName;
    private String remoteName;

    public RequestedEjbResource( String refName, String jndiName, String refType, String home, String remote )
    {
        super( refName );
        super.setJndiName( jndiName );
        this.ejbRefType = refType;
        this.homeName = home;
        this.remoteName = remote;
    }

    public void setEjbRefName( String refName )
    {
        super.setResourceName( refName );
    }

    public void setEjbRefType( String refType )
    {
        this.ejbRefType = refType;
    }

    public void setHome( String home )
    {
        this.homeName = home;
    }

    public void setRemote( String remote )
    {
        this.remoteName = remote;
    }

    public String getEjbRefName() { return super.getResourceName(); }
    public String getEjbRefType() { return this.ejbRefType; }
    public String getHome() { return this.homeName; }
    public String getRemote() { return this.remoteName; }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "ejbRef: " + getEjbRefName() + "\n" );
        buf.append( "ejbRefType: " + getEjbRefType() + "\n" );
        buf.append( "home: " + getHome() + "\n" );
        buf.append( "remote: " + getRemote() + "\n" );
        return buf.toString();
    }
}
