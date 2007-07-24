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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.navigation;

import org.netbeans.modules.web.jsf.navigation.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author joelle
 */
public class JSFConfigMimeResovler extends MIMEResolver{

    public JSFConfigMimeResovler() {
    }

    public String findMIMEType(FileObject fo) {
        System.out.println("Trying to find FileObject MIME Type." + fo);
        
        if( fo.getExt().equals("xml")){
            return "ext/x-jsf+xml";
        }
        return null;
    }
    
    
    /*
     *     <file>
        <ext name="xml"/>
        <resolver mime="text/x-jsf+xml">
            <xml-rule>
            	<doctype public-id="-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN"/>
            </xml-rule>
        </resolver>
    </file>
    <file>
        <ext name="xml"/>
            <resolver mime="text/x-jsf+xml">
            <xml-rule>
                <doctype public-id="-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN"/>
            </xml-rule>
        </resolver>       
    </file>    
    <file>
        <ext name="xml"/>
            <resolver mime="text/x-jsf+xml">
            <xml-rule>
            	<element name="faces-config" ns="http://java.sun.com/xml/ns/javaee">
                    <attr name="version" text="1.2"/>
                </element>
            </xml-rule>
        </resolver>       
    </file> 
     * */

}
