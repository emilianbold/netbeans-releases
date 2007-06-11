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

package org.netbeans.modules.wsdlextensions.ftp.validation.test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public enum NamespaceLocation {
    HOTEL("http://www.sun.com/javaone/05/HotelReservationService", "resources/HotelReservationService.wsdl"),
    AIRLINE("http://www.sun.com/javaone/05/AirlineReservationService", "resources/AirlineReservationService.wsdl"),
    EMPTY_TRAVEL("http://www.sun.com/javaone/05/TravelReservationService", "resources/emptyTravel.wsdl"),
    TRAVEL("http://www.sun.com/javaone/05/TravelReservationService", "resources/TravelReservationService.wsdl"),
    VEHICLE("http://www.sun.com/javaone/05/VehicleReservationService", "resources/VehicleReservationService.wsdl"),
    OTA("http://www.opentravel.org/OTA/2003/05", "resources/OTA_TravelItinerary.xsd"),
    TESTOP("test/operations", "resources/TestOperations.wsdl"),
    TESTIMPORT("http://com.stc.database/pointbase/purchaseOrder", "resources/testImports.wsdl"),
    PO_1("http://com.stc.database/pointbase/purchaseOrder", "resources/purchaseOrder_1.xsd"),
    PO("http://www.w3.org/2001/XMLSchema", "resources/PurchaseOrder.xsd"),
    SCHEMA_NS_IN_WSDL("http://new.webservice.namespace", "resources/schemaUsingNamespaceFromWsdlRoot.wsdl"),
    ECHO("http://localhost/echo/echo", "resources/echo.wsdl"),
    ECHOCONCAT("http://stc.com/echoConcat", "resources/echoConcat.wsdl"),
    PARKING("urn:ParkingLotManager/wsdl", "resources/ParkingLotManager.wsdl");
    
    private String namespace;
    private String resourcePath;
    private String location;
    
    /** Creates a new instance of NamespaceLocation */
    NamespaceLocation(String namespace, String resourcePath) {
        this.namespace = namespace;
        this.resourcePath = resourcePath;
        this.location = resourcePath.substring(resourcePath.lastIndexOf("resources/")+10);
    }
    public String getNamespace() { return namespace; }
    public String getResourcePath() { return resourcePath; }
    public URI getLocationURI() throws URISyntaxException {
        return new URI(getLocation());
    }
    public String getLocation() { return location; }
    public URI getNamespaceURI() throws URISyntaxException { return new URI(getNamespace()); }
    public static File wsdlTestDir = null;
    public static File getSchemaTestTempDir() throws Exception {
        if (wsdlTestDir == null) {
            wsdlTestDir = Util.getTempDir("wsdltest");
        }
        return wsdlTestDir;
    }
    
    public File getResourceFile() throws Exception {
        return new File(getSchemaTestTempDir(), Util.getFileName(getResourcePath()));
    }
    
    public void refreshResourceFile() throws Exception {
        if (getResourceFile().exists()) {
            ModelSource source = TestCatalogModel.getDefault().getModelSource(getLocationURI());
            DataObject dobj = (DataObject) source.getLookup().lookup(DataObject.class);
            SaveCookie save = (SaveCookie) dobj.getCookie(SaveCookie.class);
            if (save != null) save.save();
            FileObject fo = (FileObject) source.getLookup().lookup(FileObject.class);
            fo.delete();
        }
        Util.copyResource(getResourcePath(), FileUtil.toFileObject(getSchemaTestTempDir().getCanonicalFile()));
    }
    
    public URI getResourceURI() throws Exception {
        return getResourceFile().toURI();
    }
    
    public static NamespaceLocation valueFromResourcePath(String resourcePath) {
        for (NamespaceLocation nl : values()) {
            if (nl.getResourcePath().equals(resourcePath)) {
                return nl;
            }
        }
        return null;
    }
}
