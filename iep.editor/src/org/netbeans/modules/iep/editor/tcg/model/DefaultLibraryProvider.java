package org.netbeans.modules.iep.editor.tcg.model;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import org.netbeans.modules.iep.model.lib.ImageUtil;
import org.netbeans.modules.iep.model.spi.LibraryProvider;

public class DefaultLibraryProvider implements LibraryProvider {

    public InputStream getLibraryXml() {
        return DefaultLibraryProvider.class.getResourceAsStream("/vm/iep/library.xml");
        
    }
    
    public ImageIcon resolveIcon(String iconName) {
        URL imgURL = DefaultLibraryProvider.class.getResource("/images/new/icons32x32/" + iconName);
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
