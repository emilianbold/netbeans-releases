/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
class SplashComponentPreview extends JLabel {
    private FontMetrics fm;
    private Rectangle view;
    private Color color_text;
    private Color color_bar;
    private Color color_edge;
    private Color color_corner;
    
    private boolean draw_bar;
    
    protected Image image;
    private Rectangle dirty = new Rectangle();
    private String text;
    private Rectangle rect = new Rectangle();
    private Rectangle bar = new Rectangle();
    private Rectangle bar_inc = new Rectangle();
    
    private int progress = 0;
    private int maxSteps = 0;
    private int tmpSteps = 0;
    private int barStart = 0;
    private int barLength = 0;
    
    
    /**
     * Creates a new splash screen component.
     */
    public SplashComponentPreview() {                
        setBorder(new TitledBorder(NbBundle.getMessage(getClass(),"LBL_SplashPreview")));
    }
    
    void setFontSize(final String fontSize) throws NumberFormatException {
        int size;
        String sizeStr = fontSize;
        size = Integer.parseInt(sizeStr);
        
        Font font = new Font("Dialog", Font.PLAIN, size);//NOI18N
        
        setFont(font); // NOI18N
        fm = getFontMetrics(font);
    }
    
    void setSplashImageIcon(final URL url) {
        ImageIcon imgIcon = new ImageIcon(url);
        this.image = imgIcon.getImage();
        //this.image = image.getScaledInstance(398, 299, Image.SCALE_DEFAULT);
    }
    
    
    void setFontSize(final int size) throws NumberFormatException {
        Font font = new Font("Dialog", Font.PLAIN, size); // NOI18N
        
        setFont(font); // NOI18N
        fm = getFontMetrics(font);
    }
    
    
    void setRunningTextBounds(final Rectangle bounds) throws NumberFormatException {        
        view = bounds;
    }

    
    void setProgressBarEnabled(final boolean enabled) {
        draw_bar = enabled; // NOI18N
    }
    
    void setProgressBarBounds(final Rectangle bounds) throws NumberFormatException {
        bar = bounds;
    }
    
    void setColorCorner(final Color color) throws NumberFormatException {
        color_corner = color;
    }
    
    void setColorEdge(final Color color) throws NumberFormatException {
        color_edge = color;
    }

    
    void setTextColor(final Color color) throws NumberFormatException {
        color_text = color;
    }
    
    void setColorBar(final Color color) throws NumberFormatException {
        color_bar = color;
    }
    
    /**
     * Defines the single line of text this component will display.
     */
    public void setText(final String text) {
        // run in AWT, there were problems with accessing font metrics
        // from now AWT thread
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (text == null) {
                    repaint(dirty);
                    return;
                }
                
                if (fm == null)
                    return;
                
                adjustText(text);
                
                SwingUtilities.layoutCompoundLabel(fm, text, null,
                        SwingConstants.BOTTOM, SwingConstants.LEFT, SwingConstants.BOTTOM, SwingConstants.LEFT,
                        SplashComponentPreview.this.view, new Rectangle(), rect, 0);
                dirty = dirty.union(rect);
                // update screen (assume repaint manager optimizes unions;)
                repaint(dirty);
                dirty = new Rectangle(rect);
            }
        });
    }
    
    // Defines a max value for splash progress bar.
    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }
    
    // Adds temporary steps to create a max value for splash progress bar later.
    public void addToMaxSteps(int steps) {
        tmpSteps += steps;
    }
    
    // Adds temporary steps and creates a max value for splash progress bar.
    public void addAndSetMaxSteps(int steps) {
        tmpSteps += steps;
        maxSteps = tmpSteps;
    }
    
    // Increments a current value of splash progress bar by given steps.
    public void increment(int steps) {
        if (draw_bar) {
            progress += steps;
            if (progress > maxSteps)
                progress = maxSteps;
            else if (maxSteps > 0) {
                int bl = bar.width * progress / maxSteps - barStart;
                if (bl > 1 || barStart % 2 == 0) {
                    barLength = bl;
                    bar_inc = new Rectangle(bar.x + barStart, bar.y, barLength + 1, bar.height);
//                    System.out.println("progress: " + progress + "/" + maxSteps);
                    repaint(bar_inc);
                    //System.err.println("(painting " + bar_inc + ")");
                } else {
                    // too small, don't waste time painting it
                }
            }
        }
    }
    
    //Creates new text with the ellipsis at the end when text width is
    // bigger than allowed space
    private void adjustText(String text){
        String newText = null;
        String newString;
        
        if (text == null)
            return ;
        
        if (fm == null)
            return;
        
        int width = fm.stringWidth(text);
        
        if (width > view.width) {
            StringTokenizer st = new StringTokenizer(text);
            while (st.hasMoreTokens()) {
                String element = st.nextToken();
                if (newText == null)
                    newString = element;
                else
                    newString = newText + " " + element; // NOI18N
                if (fm.stringWidth(newString + "...") > view.width) { // NOI18N
                    this.text = newText + "..."; // NOI18N
                    break;
                } else
                    newText = newString;
                
            }
        } else
            this.text = text;
    }
    /**
     * Override update to *not* erase the background before painting.
     */
    public void update(Graphics g) {
        paint(g);
    }
    
    /**
     * Renders this component to the given graphics.
     */
    public void paint(Graphics g) {
        super.paint(g);
        int width = BasicBrandingModel.SPLASH_WIDTH;
        int height = BasicBrandingModel.SPLASH_HEIGHT;
        int x = (getWidth()/2)-(width/2);
        int y = (getHeight()/2)-(height/2);
        
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform tx = g2d.getTransform();
        
        
        tx.translate(x, y);
        
        //tx.scale(((double)width)/((double)image.getWidth(null)),((double)height)/((double)image.getHeight(null)));
        g2d.setTransform(tx);
        
        originalPaint(g);
    }
    
    public void originalPaint(Graphics graphics) {
        graphics.setColor(color_text);
        graphics.drawImage(image, 0, 0, null);
        
        if (text == null) {
            // no text to draw
            return;
        }
        
        if (fm == null) {
            // XXX(-ttran) this happened on Japanese Windows NT, don't
            // fully understand why
            return;
        }
        
        SwingUtilities.layoutCompoundLabel(fm, text, null,
                SwingConstants.BOTTOM, SwingConstants.LEFT, SwingConstants.BOTTOM, SwingConstants.LEFT,
                this.view, new Rectangle(), rect, 0);
        // turn anti-aliasing on for the splash text
        Graphics2D g2d = (Graphics2D)graphics;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.drawString(text, rect.x, rect.y + fm.getAscent());
        // Draw progress bar if applicable
        
        if (draw_bar && Boolean.getBoolean("netbeans.splash.nobar") == false && maxSteps > 0/* && barLength > 0*/) {
            graphics.setColor(color_bar);
            graphics.fillRect(bar.x, bar.y, barStart + barLength, bar.height);
            graphics.setColor(color_corner);
            graphics.drawLine(bar.x, bar.y, bar.x, bar.y + bar.height);
            graphics.drawLine(bar.x + barStart + barLength, bar.y, bar.x + barStart + barLength, bar.y + bar.height);
            graphics.setColor(color_edge);
            graphics.drawLine(bar.x, bar.y + bar.height / 2, bar.x, bar.y + bar.height / 2);
            graphics.drawLine(bar.x + barStart + barLength, bar.y + bar.height / 2, bar.x + barStart + barLength, bar.y + bar.height / 2);
            barStart += barLength;
            barLength = 0;
        }
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(null), image.getHeight(null));
    }
    
    /*public boolean isOpaque() {
        return true;
    }*/
    
    public Rectangle getView() {
        return view;
    }
}