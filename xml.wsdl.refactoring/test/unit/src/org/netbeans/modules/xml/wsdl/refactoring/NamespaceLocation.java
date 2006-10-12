package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author nn136682
 */
public enum NamespaceLocation {
    HOTEL("http://www.sun.com/javaone/05/HotelReservationService", "resources/HotelReservationService.wsdl"),
    AIRLINE("http://www.sun.com/javaone/05/AirlineReservationService", "resources/AirlineReservationService.wsdl"),
    EMPTY_TRAVEL("http://www.sun.com/javaone/05/TravelReservationService", "resources/emptyTravel.wsdl"),
    TRAVEL("http://www.sun.com/javaone/05/TravelReservationService", "resources/TravelReservationService.wsdl"),
    VEHICLE("http://www.sun.com/javaone/05/VehicleReservationService", "resources/VehicleReservationService.wsdl"),
    OTA("http://www.opentravel.org/OTA/2003/05", "resources/OTA_TravelItinerary.xsd");
    
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
    public static File getWsdlTestTempDir() throws Exception {
        if (wsdlTestDir == null) {
            wsdlTestDir = Util.getTempDir("wsdltest");
        }
        return wsdlTestDir;
    }
    public File getResourceFile() throws Exception {
        return new File(getWsdlTestTempDir(), Util.getFileName(getResourcePath()));
    }
    public void refreshResourceFile() throws Exception {
        if (getResourceFile().exists()) {
            ModelSource source = TestCatalogModel.getDefault().getModelSource(getLocationURI());
            DataObject dobj = (DataObject) source.getLookup().lookup(DataObject.class);
            SaveCookie save = (SaveCookie) dobj.getCookie(SaveCookie.class);
            if (save != null) save.save();
            //Thread.sleep(2000);
            FileObject fo = (FileObject) source.getLookup().lookup(FileObject.class);
            if (fo != null) fo.delete();
        }
        Util.copyResource(getResourcePath(), FileUtil.toFileObject(getWsdlTestTempDir().getCanonicalFile()));
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
