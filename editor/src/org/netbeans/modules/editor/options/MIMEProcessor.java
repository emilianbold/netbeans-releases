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

package org.netbeans.modules.editor.options;

import java.io.IOException;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.XMLDataObject;

/** XML Processor for MIME Options settings files
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public abstract class MIMEProcessor implements XMLDataObject.Processor, InstanceCookie {

    /** <code>XMLDataObject</code> this processor is linked to. */
    protected XMLDataObject xmlDataObject;

    /** Attaches this processor to specified xml data object. Implements <code>XMLDataObject.Processor</code> interface.
     * @param xmlDataObject xml data object to which attach this processor */
    public void attachTo(XMLDataObject xmlDataObject) {
        this.xmlDataObject = xmlDataObject;
    }
    
    /** Gets name of instance. Implements <code>InstanceCookie</code> interface method.
     * @return name of <code>xmlDataObject</code> */
    public String instanceName() {
        return xmlDataObject.getName();
    }
    
    /** Gets instance class. Implements <code>InstanceCookie</code> interface method.
     * @return MIME specific processor class */
    public Class instanceClass() throws IOException, ClassNotFoundException {
        return this.getClass();
    }
    
    /** Gets XMLDataObject to which this processor is linked to */
    public XMLDataObject getXMLDataObject(){
        return xmlDataObject;
    }
    
    /** Gets DTD's PUBLIC_ID */
    public abstract String getPublicID();
    
    /** Gets DTD's SYSTEM_ID */
    public abstract String getSystemID();
    
    /** Gets the class of MIMEOption file that handle this XML file type */
    public abstract Class getAsociatedMIMEOptionFile();
    
    /** Creates appropriate MIME Option file
     * @param o BaseOptions subClass
     * @param b object of MIMEProcessor */
    public abstract MIMEOptionFile createMIMEOptionFile(BaseOptions o, Object b);
    
    /** Creates instance. Implements <code>InstanceCookie</code> interface method. */
    public Object instanceCreate() throws IOException, ClassNotFoundException {
        return this;
    }
}
