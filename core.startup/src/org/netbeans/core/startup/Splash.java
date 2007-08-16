/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import static javax.swing.SwingConstants.*;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.netbeans.Util;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/** A class that encapsulates all the splash screen things.
*
* @author Ian Formanek, David Peroutka, Radim Kubacki
*/
public final class Splash {

    private static Splash splash;
    
    /** is there progress bar in splash or not */
    private static boolean noBar = Boolean.getBoolean("netbeans.splash.nobar");
    
    static {
	ResourceBundle bundle = NbBundle.getBundle(Splash.class);
	noBar |= !Boolean.parseBoolean(bundle.getString("SplashShowProgressBar"));
    }
    
    public static Splash getInstance() {
	if (splash == null) {
	    splash = new Splash();
	}
	return splash;
    }
    
    public static void showAboutDialog (java.awt.Frame parent, javax.swing.JComponent info) {
        createAboutDialog (parent, info).setVisible(true);
    }
    
    private static JDialog createAboutDialog (java.awt.Frame parent, javax.swing.JComponent info) {
        SplashDialog splashDialog = new SplashDialog (parent, info);
        return splashDialog;
    }

    // Copied from MainWindow:
    private static final String ICON_SMALL = "org/netbeans/core/startup/frame.gif"; // NOI18N
    private static final String ICON_BIG = "org/netbeans/core/startup/frame32.gif"; // NOI18N
    
    private static Image createIDEImage() {
	return Utilities.loadImage(Utilities.isLargeFrameIcons() ? ICON_BIG : ICON_SMALL, true);
    }
    
    private Frame frame;
    
    private SplashComponent comp;
    
    private Splash() {
	comp = new SplashComponent(false);
    }
    
    /** Enables or disables splash component and its progress
     * animation
     */
    public void setRunning(boolean running) {
	if (CLIOptions.isNoSplash())
	    return;
	
	if (running) {
	    if (frame == null) {
		frame = new Frame(NbBundle.getMessage(Splash.class, "LBL_splash_window_title")); // e.g. for window tray display
		frame.setIconImage(createIDEImage()); // again, only for possible window tray display
		frame.setUndecorated(true);
		// add splash component
		frame.setLayout (new BorderLayout());
		frame.add(comp, BorderLayout.CENTER);
		
		int width = Integer.parseInt(NbBundle.getMessage(Splash.class, "SPLASH_WIDTH"));
		int height = Integer.parseInt(NbBundle.getMessage(Splash.class, "SPLASH_HEIGHT"));
		frame.setPreferredSize(new Dimension(width, height));
		
		SwingUtilities.invokeLater (new SplashRunner(frame, true));
	    }
	}
	else {
	    SwingUtilities.invokeLater (new SplashRunner(frame, false));
	}
    }
    
    public void dispose() {
	setRunning(false);
	splash = null;
    }
    
    public void increment(int steps) {
	if (noBar || CLIOptions.isNoSplash())
	    return;
	
//System.out.println("Splash.increment ("+steps+"), "+comp);
	if (comp != null) {
	    comp.increment(steps);
	}
    }
    
    public Component getComponent() {
	return comp;
    }
    
    /** Updates text in splash window
     */
    public void print (String s) {
	if (CLIOptions.isNoSplash() || comp == null)
	    return;
	
	comp.setText(s);
//System.out.println("Splash.print ("+s+"), "+comp);
    }

    /** Adds specified numbers of steps to a progress
     */
    public void addToMaxSteps(int steps) {
	if (noBar || CLIOptions.isNoSplash())
	    return;
	
//System.out.println("Splash.addToMaxSteps ("+steps+"), "+comp);
	if (comp != null) {
	    comp.addToMaxSteps(steps);
	}
    }
    
//****************************************************************************    
    /**
     * Standard way how to place the window to the center of the screen.
     */
    static final void center(Window c) {
        c.pack();
        c.setBounds(Utilities.findCenterBounds(c.getSize()));
    }

    /** Loads a splash image from its source 
     *  @param about if true then about image is loaded, if false splash image is loaded
     */
    public static Image loadContent(boolean about) {
        if (about) {
            Image img = Utilities.loadImage("org/netbeans/core/startup/about.png", true);
            if (img != null) {
                return img;
            }
        }
        return Utilities.loadImage("org/netbeans/core/startup/splash.gif", true);
    }

    /**
     * This class implements double-buffered splash screen component.
     */
    private static class SplashComponent extends JComponent implements Accessible {

	Rectangle view;
	Color color_text;
	Color color_bar;
	Color color_edge;
	Color color_corner;
	
	/** font size for splash texts */
	private int size = 12;
	private Rectangle dirty = new Rectangle();
	private Rectangle rect = new Rectangle();
	private Rectangle bar = new Rectangle();
	private Rectangle bar_inc = new Rectangle();
	
	private int progress = 0;
	private int maxSteps = 0;
	private int barStart = 0;
	private int barLength = 0;
	
        private Image image;
	private String text;
	private FontMetrics fm;

	/**
         * Creates a new splash screen component.
	 * param about true is this component will be used in about dialog
         */
        public SplashComponent(boolean about) {
	    
	    // 100 is allocated for module system that will adjust this when number 
	    // of existing modules is known
	    maxSteps = 140; 
	    
	    ResourceBundle bundle = NbBundle.getBundle(Splash.class);
	    StringTokenizer st = new StringTokenizer(
		    bundle.getString("SplashRunningTextBounds"), " ,"); // NOI18N
	    view = new Rectangle(Integer.parseInt(st.nextToken()),
		    Integer.parseInt(st.nextToken()),
		    Integer.parseInt(st.nextToken()),
		    Integer.parseInt(st.nextToken()));
	    st = new StringTokenizer(
		    bundle.getString("SplashProgressBarBounds"), " ,"); // NOI18N
	    try {
		bar = new Rectangle(Integer.parseInt(st.nextToken()),
			Integer.parseInt(st.nextToken()),
			Integer.parseInt(st.nextToken()),
			Integer.parseInt(st.nextToken()));
		Integer rgb = Integer.decode(bundle.getString("SplashRunningTextColor")); // NOI18N
		color_text = new Color(rgb.intValue());
		rgb = Integer.decode(bundle.getString("SplashProgressBarColor")); // NOI18N
		color_bar = new Color(rgb.intValue());
		rgb = Integer.decode(bundle.getString("SplashProgressBarEdgeColor")); // NOI18N
		color_edge = new Color(rgb.intValue());
		rgb = Integer.decode(bundle.getString("SplashProgressBarCornerColor")); // NOI18N
		color_corner = new Color(rgb.intValue());
	    } catch (NumberFormatException nfe) {
		//IZ 37515 - NbBundle.DEBUG causes startup to fail - provide some useless values
		Util.err.warning("Number format exception " + //NOI18N
			"loading splash screen parameters."); //NOI18N
		Logger.getLogger("global").log(Level.WARNING, null, nfe);
		color_text = Color.BLACK;
		color_bar = Color.ORANGE;
		color_edge = Color.BLUE;
		color_corner = Color.GREEN;
		bar = new Rectangle(0, 0, 80, 10);
	    }
	    try {
		String sizeStr = bundle.getString("SplashRunningTextFontSize");
		size = Integer.parseInt(sizeStr);
	    } catch(MissingResourceException e){
		//ignore - use default size
	    } catch (NumberFormatException nfe) {
		//ignore - use default size
	    }
	    
	    image = loadContent(about);
	
	    Font font = new Font("Dialog", Font.PLAIN, size);
	    
	    setFont(font); // NOI18N
	    fm = getFontMetrics(font);
        }

        /**
         * Defines the single line of text this component will display.
         */
        public void setText(final String text) {
	    // trying to set again the same text?
	    if (text != null && text.equals(this.text)) return;
	    
            // run in AWT, there were problems with accessing font metrics
            // from now AWT thread
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    if (text == null) {
                        repaint(dirty);
                        return;
                    }

                    if (fm == null)
                        return;

                    adjustText(text);            

                    SwingUtilities.layoutCompoundLabel(fm, text, null,
                                                       BOTTOM, LEFT, BOTTOM, LEFT,
                                                       view, new Rectangle(), rect, 0);
                    dirty = dirty.union(rect);
                    // update screen (assume repaint manager optimizes unions;)
                    repaint(dirty);
                    dirty = new Rectangle(rect);
                }
            });
        }
        
        /**
         * Creates new text with the ellipsis at the end when text width is
         * bigger than allowed space
         */
        private void adjustText(String text) {
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
                // #71064 - cut the text and put the ellipsis correctly when 
                // very loong text without spaces that exceeds available space is used
                // it can happen in multibyte environment (such as japanese) 
                if (newText == null) {
                    this.text = "";
                    newString = "";
                    newText = "";
                    for (int i = 0; i < text.length(); i++) {
                        newString += text.charAt(i);
                        if (fm.stringWidth(newString + "...") > view.width) { // NOI18N
                            this.text = newText + "..."; // NOI18N
                            break;
                        } else {
                            newText = newString;
                        }
                    }
                }
            } else
                this.text = text;
        }
    
	public void increment(int steps) {
	    progress += steps;
	    if (progress > maxSteps)
		progress = maxSteps;
	    else if (maxSteps > 0) {
		int bl = bar.width * progress / maxSteps - barStart;
		if (bl > 1 || barStart % 2 == 0) {
		    barLength = bl;
		    bar_inc = new Rectangle(bar.x + barStart, bar.y, barLength + 1, bar.height);
//System.out.println(this);
		    repaint(bar_inc);
//System.out.println("(painting " + bar_inc + ")");
		}
	    }
	}
	

	/** Adds space for given number of steps.
	 * It also alters progress to preserve ratio between completed and total 
	 * number of steps.
	 */
	private void addToMaxSteps(int steps) {
	    int max = maxSteps + steps;
	    int prog = progress / maxSteps * max;
	    maxSteps = max;
	    progress = prog;
	    // do repaint on next increment
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
        public void paint(Graphics graphics) {
            graphics.setColor(color_text);
            graphics.drawImage(image, 0, 0, null);

            if (text != null) {
		if (fm == null) {
		    // XXX(-ttran) this happened on Japanese Windows NT, don't
		    // fully understand why
		    return;
		}
		
		SwingUtilities.layoutCompoundLabel(fm, text, null,
			BOTTOM, LEFT, BOTTOM, LEFT,
			view, new Rectangle(), rect, 0);
		// turn anti-aliasing on for the splash text
		Graphics2D g2d = (Graphics2D)graphics;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.drawString(text, rect.x, rect.y + fm.getAscent());
            }

            // Draw progress bar if applicable
            if (!noBar && maxSteps > 0/* && barLength > 0*/)
            {
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
            return new Dimension(image.getWidth (null), image.getHeight (null));
        }

        public boolean isOpaque () {
            return true;
        }
        
	public String toString() {
	    return "SplashComponent - " +
		    "progress: " + progress + "/" + maxSteps +
		    " text: " + text;
	}
    }

    private static class SplashDialog extends JDialog implements ActionListener {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 5185644855500178404L;

        private final SplashComponent splashComponent = new SplashComponent(true);
        
        /** Creates a new SplashDialog */
        public SplashDialog (java.awt.Frame parent, javax.swing.JComponent infoPanel) {
            super (parent, true);
    
            JPanel splashPanel = new JPanel();
            JTabbedPane tabbedPane = new JTabbedPane();
            setTitle (NbBundle.getMessage(Splash.class, "CTL_About_Title"));
            // add splash component
            splashPanel.add (splashComponent);
            tabbedPane.addTab(NbBundle.getMessage(Splash.class, "CTL_About_Title"), splashPanel);
            tabbedPane.addTab(NbBundle.getMessage(Splash.class, "CTL_About_Detail"), infoPanel);
            getContentPane().add(tabbedPane, BorderLayout.CENTER);

            getRootPane().registerKeyboardAction(
                this,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
            );
            
            tabbedPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(Splash.class, "ACSN_AboutTabs"));
            tabbedPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Splash.class, "ACSD_AboutTabs"));
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Splash.class, "ACSD_AboutDialog"));
            
            Splash.center(this);
            
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        public void actionPerformed(ActionEvent e) {
            setVisible (false);
            dispose();
        }
    }

    private static class SplashRunner implements Runnable {

        private Window splashWindow;
        private boolean visible;

        public SplashRunner(Window splashWindow, boolean visible) {
            this.splashWindow = splashWindow;
            this.visible = visible;
        }

        public void run() {
            if (visible) {
                Splash.center(splashWindow);
                splashWindow.setVisible(true);
                splashWindow.toFront ();
            }
            else {
                splashWindow.setVisible (false);
                splashWindow.dispose ();
            }
        }
    }

}
