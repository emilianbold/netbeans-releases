/*
 * PageContentItem.java
 *
 * Created on March 27, 2007, 5:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.pagecontentmodel;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Utilities;

/**
 *
 * @author joelle lam
 */
public class PageContentItem {
    
    private Image icon;
    private String fromAction;
    private String fromOutcome;
    private String name;
    private List<Action> actions;
    
    /**
     *
     * @return
     */
    public Action[] getActions() {
        return new Action[]{};
    }
    
    /**
     *
     * @param name
     * @param fromAction
     * @param icon
     */
    public PageContentItem( String name, String fromAction, String fromOutcome, Image icon ) {
        this.name = name;
        this.fromAction = fromAction;
        this.fromOutcome = fromOutcome;
        this.icon = icon;
    }
    
    /**
     *
     * @param name
     * @param fromString
     * @param icon
     * @param isOutcome
     */
    public PageContentItem( String name, String fromOutcome, Image icon ) {
        this.name = name;
        this.fromOutcome = fromOutcome;
        this.icon = icon;
    }
    
    
    /**
     *
     * @return
     */
    public Image getIcon() {
        return icon;
    }
    
    //    /**
    //     *
    //     * @param icon
    //     */
    //    public void setIcon(Image icon) {
    //        this.icon = icon;
    //    }
    //
    /**
     *
     * @return
     */
    public String getFromAction() {
        return fromAction;
    }
    
    /**
     *
     * @param fromAction
     */
    public void setFromAction(String fromAction) {
        this.fromAction = fromAction;
    }
    
    /**
     *
     * @return
     */
    public String getFromOutcome() {
        return fromOutcome;
    }
    
    /**
     *
     * @param fromOutcome
     */
    public void setFromOutcome(String fromOutcome) {
        this.fromOutcome = fromOutcome;
    }
    
    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
    public String toString() {
        return "PageBean[" + getName() + ", fromOutcome=" + getFromOutcome() + ", fromAction=" + getFromAction() + "," + getIcon() + "]";
    }
    
    
    private Image bufferedIcon = null;
    public Image getBufferedIcon(){
        if (bufferedIcon == null){
            bufferedIcon = toBufferedImage(getIcon());
            //bufferedIcon =  new javax.swing.ImageIcon(icon).getImage();
        }
        return bufferedIcon;
    }
    
    /** The method creates a BufferedImage which represents the same Image as the
     * original but buffered to avoid repeated loading of the icon while repainting.
     */
    private Image toBufferedImage(Image img) {
        // load the image
        if( img == null ){
            System.out.println("Image is null for Bean: " + toString());
            return null;
        }
        new javax.swing.ImageIcon(img);
        BufferedImage rep = createBufferedImage(img.getWidth(null), img.getHeight(null));
        Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();
        return rep;
    }
    
    /** Creates BufferedImage with Transparency.TRANSLUCENT */
    private BufferedImage createBufferedImage(int width, int height) {
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }
        ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().getColorModel(Transparency.TRANSLUCENT);
        BufferedImage buffImage = new BufferedImage(model,
                model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null);
        return buffImage;
    }
    
     public  <T extends Cookie> T getCookie(Class<T> type){
        return null;
     }
    
    
}
