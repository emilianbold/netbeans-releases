package org.netbeans.modules.iep.model.common;

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
    PURCHASE_ORDER("http://jbi.com.sun/wfse/wsdl/WorkflowApp2/ApprovePurchase_TM_BPEL", "data/ApprovePurchase_TM_BPEL.wsdl");
     
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
