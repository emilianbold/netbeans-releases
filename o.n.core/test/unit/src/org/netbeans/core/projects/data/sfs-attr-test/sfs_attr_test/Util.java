package sfs_attr_test;

import java.awt.Image;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.awt.Toolkit;
import java.net.URL;
import org.openide.util.Utilities;

public abstract class Util {
    private Util() {}

    private static Image mergeIcons(FileObject fo) throws IOException {
        int count = ((Integer)fo.getAttribute("iconCount")).intValue();
        if (count < 2) throw new IOException();
        Image img = Toolkit.getDefaultToolkit().getImage((URL)fo.getAttribute("icon1"));
        for (int i = 2; i <= count; i++) {
            Image added = Toolkit.getDefaultToolkit().getImage((URL)fo.getAttribute("icon" + i));
            int x = ((Integer)fo.getAttribute("iconx" + count)).intValue();
            int y = ((Integer)fo.getAttribute("icony" + count)).intValue();
            img = Utilities.mergeImages(img, added, x, y);
        }
        return img;
    }
    
}