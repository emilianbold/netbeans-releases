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
package org.netbeans.modules.j2ee.sun.dd.impl;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.xml.sax.SAXParseException;


/**
 * Interface for internal methods applicable to all bean root proxies, but are
 * not public in RootInterface.
 * 
 * Formerly derived from RootInterface, but that meant these were implemented
 * in the schema2beans generated roots as well as the proxy classes and that
 * wasn't very nice, nor did it make sense.
 * 
 * Now used as a mixin class for the Proxy implementations.
 *
 * @author Peter Williams
 */
public interface RootInterfaceImpl
{    
    
    /** Converts to RootInterface
     * 
     * @return RootInterface instance for this proxy
     */
    public RootInterface getRootInterface();
    
    /** Sets parsing status
     * 
     * @param value parser state (valid, invalid, unparsable.  See RootInterface
     *   for flags.
     */
    public void setStatus(int value);
      
    /** Retrieve current parser error.
     * 
     * @return current parser exception, if any.
     */
    public SAXParseException getError();
    
    /** Sets error status
     *
     * @param error current parser exception or null if none.
     */
    public void setError(SAXParseException error);
    
    /** Returns whether this proxy is current wrapping a tree or not.
     * 
     * @return true if this proxy has a valid xml tree.
     */
    public boolean hasOriginal();
      
    /** Adds property change listener to particular CommonDDBean object (WebApp object).
     * 
     * @param pcl property change listener
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl);
    
    /** Removes property change listener from CommonDDBean object.
     * 
     * @param pcl property change listener
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl);
    
    /** Get ASDDVersion object for the current graph, if any
     * 
     * @return ASDDVersion object for the dtd used by this graph or null if it
     * cannot be determined.
     */
    public ASDDVersion getASDDVersion();
    
}
