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


package org.netbeans.modules.deployment.deviceanywhere.service;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.netbeans.modules.deployment.deviceanywhere.service package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.netbeans.modules.deployment.deviceanywhere.service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ArrayOfApplicationAPIDeviceWrapper }
     * 
     */
    public ArrayOfApplicationAPIDeviceWrapper createArrayOfApplicationAPIDeviceWrapper() {
        return new ArrayOfApplicationAPIDeviceWrapper();
    }

    /**
     * Create an instance of {@link ApplicationAPIUploadApplicationReturn }
     * 
     */
    public ApplicationAPIUploadApplicationReturn createApplicationAPIUploadApplicationReturn() {
        return new ApplicationAPIUploadApplicationReturn();
    }

    /**
     * Create an instance of {@link GetLockedDevices }
     * 
     */
    public GetLockedDevices createGetLockedDevices() {
        return new GetLockedDevices();
    }

    /**
     * Create an instance of {@link ApplicationAPIStartDownloadScriptReturn }
     * 
     */
    public ApplicationAPIStartDownloadScriptReturn createApplicationAPIStartDownloadScriptReturn() {
        return new ApplicationAPIStartDownloadScriptReturn();
    }

    /**
     * Create an instance of {@link GetLockedDevicesResponse }
     * 
     */
    public GetLockedDevicesResponse createGetLockedDevicesResponse() {
        return new GetLockedDevicesResponse();
    }

    /**
     * Create an instance of {@link StartDownloadScript }
     * 
     */
    public StartDownloadScript createStartDownloadScript() {
        return new StartDownloadScript();
    }

    /**
     * Create an instance of {@link UploadApplication }
     * 
     */
    public UploadApplication createUploadApplication() {
        return new UploadApplication();
    }

    /**
     * Create an instance of {@link StartDownloadScriptResponse }
     * 
     */
    public StartDownloadScriptResponse createStartDownloadScriptResponse() {
        return new StartDownloadScriptResponse();
    }

    /**
     * Create an instance of {@link ApplicationAPIDeviceWrapper }
     * 
     */
    public ApplicationAPIDeviceWrapper createApplicationAPIDeviceWrapper() {
        return new ApplicationAPIDeviceWrapper();
    }

    /**
     * Create an instance of {@link ApplicationAPIGetLockedDevicesReturn }
     * 
     */
    public ApplicationAPIGetLockedDevicesReturn createApplicationAPIGetLockedDevicesReturn() {
        return new ApplicationAPIGetLockedDevicesReturn();
    }

    /**
     * Create an instance of {@link UploadApplicationResponse }
     * 
     */
    public UploadApplicationResponse createUploadApplicationResponse() {
        return new UploadApplicationResponse();
    }

}
