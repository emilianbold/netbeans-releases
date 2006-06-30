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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.Collections;
import java.util.List;
import javax.enterprise.deploy.model.DDBean;


/** Interface to be implemented by
 *  (a) a J2EE module provider to return specific method structures requested
 *      by a J2EE server plugin
 *  (b) j2eeserver module, to implement a pass-through that handles determining
 *      the correct J2EE module to pass to.
 *
 *  This interface is intended to be used by the configuration DConfigBeans
 *  and their respective UI to aid in usability, e.g. providing a list of messages
 *  that could be secured in the security configuaration, list of ejb interface
 *  methods that can have optimizations configured for them, etc.
 *
 * @author Peter Williams
 */
public interface ConfigQuery {
    
    /** Invoke this method on a particular ejb to get list of the methods in
     *  each of the possible interfaces, if said interface is provided.
     *
     *  @param ejbDD DDBean for the ejb whose methods are needed.
     *  @return Structure containing a list for each of the 4 EJB interfaces.
     */
    public InterfaceData getEJBMethods(DDBean ejbDD);
    
    /** Invoke this method on a particular serviceRef (portInfo within this ref?)
     *  to retrieve a list of the operations on that port
     *
     *  @param serviceRefDD DDBean for the service ref whose operations are needed.
     *  @return List of operations, along with their parameters for this service.
     */
    public List/*MethodData*/ getServiceMessages(DDBean serviceRefDD); 
    
    
    public static class InterfaceData {
       private List/*MethodData*/ homeInterface;
       private List/*MethodData*/ remoteInterface;
       private List/*MethodData*/ localHomeInterface;
       private List/*MethodData*/ localInterface;
       
       public InterfaceData(List hi, List ri, List lhi, List li) {
           homeInterface = Collections.unmodifiableList(hi);
           remoteInterface = Collections.unmodifiableList(ri);
           localHomeInterface = Collections.unmodifiableList(lhi);
           localInterface = Collections.unmodifiableList(li);
       }
       
       public List getHomeInterface() {
           return homeInterface;
       }
       
       public List getRemoteInterface() {
           return remoteInterface;
       }
       
       public List getLocalHomeInterface() {
           return localHomeInterface;
       }
       
       public List getLocalInterface() {
           return localInterface;
       }
    }

    public static class MethodData {
       private String name;
       private List/*String*/ parameters;
       
       public MethodData(String n, List p) {
           name = n;
           parameters = Collections.unmodifiableList(p);
       }
       
       public String getOperationName() {
           return name;
       }
       
       public List getParameters() {
           return parameters;
       }
    }     
}
