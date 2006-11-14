package org.netbeans.installer.utils.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.List;
import org.netbeans.installer.Installer;

/////////////////////////////////////////////////////////////////////////////////
// Inner Classes

public class NbiClassLoader extends URLClassLoader {
    public NbiClassLoader(final List<ExtendedURI> uris) throws MalformedURLException {
        super(new URL[]{}, Installer.getInstance().getClass().getClassLoader());
        
        for(ExtendedURI uri : uris) {
            addURL(uri.getLocalUri().toURL());
        }
    }
    
    protected PermissionCollection getPermissions(final CodeSource source) {
        return getClass().getProtectionDomain().getPermissions();
    }
}