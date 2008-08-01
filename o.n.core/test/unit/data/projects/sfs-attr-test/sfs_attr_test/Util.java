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
        URL icon1 = (URL)fo.getAttribute("icon1");
        System.out.println("Loading " + icon1 + " just to be sure...");
        // Make sure it is really loadable:
        icon1.openConnection().getInputStream().close();
        Image img = Toolkit.getDefaultToolkit().getImage(icon1);
        for (int i = 2; i <= count; i++) {
            URL iconn = (URL)fo.getAttribute("icon" + i);
            System.out.println("Loading " + iconn + " just to be sure...");
            iconn.openConnection().getInputStream().close();
            Image added = Toolkit.getDefaultToolkit().getImage(iconn);
            int x = ((Integer)fo.getAttribute("iconx" + count)).intValue();
            int y = ((Integer)fo.getAttribute("icony" + count)).intValue();
            img = Utilities.mergeImages(img, added, x, y);
        }
        return img;
    }
    
}