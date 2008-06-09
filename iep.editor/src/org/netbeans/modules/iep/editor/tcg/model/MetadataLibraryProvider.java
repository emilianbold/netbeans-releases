package org.netbeans.modules.iep.editor.tcg.model;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import org.netbeans.modules.iep.model.spi.LibraryProvider;

public class MetadataLibraryProvider implements LibraryProvider {

    public InputStream getLibraryXml() {
        return DefaultLibraryProvider.class.getResourceAsStream("/vm/metadata/library.xml");
        
    }
    
    public ImageIcon resolveIcon(String iconName) {
        URL imgURL = DefaultLibraryProvider.class.getResource("/images/" + iconName);
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
