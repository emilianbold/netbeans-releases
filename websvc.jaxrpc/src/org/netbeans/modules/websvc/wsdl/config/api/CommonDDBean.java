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

package org.netbeans.modules.websvc.wsdl.config.api;
/**
 * Parent of all Config Bean API interfaces.  Inspired by (and heavily copied from)
 * CommonDDBean in web/ddapi
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface.</em></font></b>
 *</p>
 *
 * @author Peter Williams
 *
 */
public interface CommonDDBean {
    /**
     * Adds property change listener to particular CommonDDBean object (WebApp object).
     * @param pcl property change listener
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl);
    /**
     * Removes property change listener from CommonDDBean object.
     * @param pcl property change listener
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl);
    /**
     * Sets the id attribute for related dd element. E.g.<pre>
&lt;servlet id="xyz"&gt;
  ...
&lt;/servlet&gt;
     *</pre>
     *
     * @param value the value for id attribute
     */    
// !PW I don't think this method is needed for configuration schema
//    public void setId(java.lang.String value);
    /**
     * Returns the id attribute for related dd element.<br>In most cases the id attribute is not specified.
     * @return value of id attribute or null if not specified
     */   
//    public java.lang.String getId();
    /**
     * Returns the clonned CommonDDBean object.
     * @return the clonned (not bound to bean graph) CommonDDBean object
     */       
    public Object clone();
    /**
     * Returns the CommonDDBean object or array of CommonDDBean object for given property.<br>
     * E.g. for retrieving the servlet-class value on Servlet object he <b>getValue("ServletClass");</b> can be used.
     * @param propertyName name of the property the value is looking for
     * @return the bean/array of beans related to given property
     */      
    public Object getValue(String propertyName);
    /**
     * Writes the whole DD or its fraction (element related to CommonDDBean) to output stream.<br>
     * For DD root object there is more convenient to use the {@link org.netbeans.modules.j2ee.dd.api.common.RootInterface#write} method.<br>
     * The correct usage with file objects is :<pre>
WebApp webApp;
FileObject fo;
...
//  code that initializes and modifies the webApp object
...
FileLock lock;
try {
    lock=fo.lock();
} catch (FileAlreadyLockedException e) {
    // handling the exception
}
if (lock!=null) {
    try {
        OutputStream os=fo.getOutputStream(lock);
        try {
            webApp.write(os);
        } finally {
            os.close();
        }
    } finally {
        lock.releaseLock();
    }
}
...
     *</pre>
     * @param os output stream for writing
     */  
    public void write(java.io.OutputStream os) throws java.io.IOException;

}
