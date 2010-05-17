package org.netbeans.modules.iep.model;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import org.netbeans.modules.tbls.model.LibraryProvider;

public class DefaultLibraryProvider implements LibraryProvider {

    public InputStream getLibraryXml() {
        return DefaultLibraryProvider.class.getResourceAsStream("/vm/iep/library.xml");
        
    }
    
    public ImageIcon resolveIcon(String iconName) {
        URL imgURL = DefaultLibraryProvider.class.getResource("/images/icons32x32/" + iconName);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + iconName);
            return null;
        }
    }
    
    public Object newInstance(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return Class.forName(className).newInstance();
    }
}
