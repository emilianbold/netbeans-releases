/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.screen.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DesignListener;
import org.netbeans.modules.vmd.api.model.ListenerManager;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.DeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.DisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.Injector;
import org.netbeans.modules.vmd.api.screen.display.ScreenComponentElement;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyElement;
import org.netbeans.modules.vmd.api.screen.display.DeviceInfo.DeviceViewResources;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.openide.util.Utilities;

/**
 * 
 * A presenter for Displayable MIDP class. ALl other presenters should
 * inherit from this presenter (e.g. TextBoxDisplayPresenter, 
 * FormDisplayPresenter, ...)
 * 
 * @author breh
 *
 */
public class DisplayableDisplayPresenter extends DisplayPresenter {

    
    private static final Image BATTERY = Utilities.loadImage("/org/netbeans/modules/vmd/midp/screen/display/resources/battery.png"); // NOI18M
    private static final Image SIGNAL = Utilities.loadImage("/org/netbeans/modules/vmd/midp/screen/display/resources/signal.png"); // NOI18M    

    private DeviceInfo.DeviceViewResources deviceViewResources;
    
    private JComponent deviceDisplay;    
    private DisplayableBackground background;
    private DisplayableForeground foreground;
    
    
    private TitlePropertyElement titlePropertyElement;
    
    private Dimension displaySize;
    
    public DisplayableDisplayPresenter() {                
    }

    


    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.DisplayPresenter#getScreenComponentElements()
     */
    @Override
    public Collection<ScreenComponentElement> getScreenComponentElements() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.ScreenComponentElement#getScreenPropertyElement(java.awt.Point)
     */
    @Override
    public ScreenPropertyElement getScreenPropertyElement(Point point) {
        // TODO Auto-generated method stub
        return null;
    }

    
    
    
    
    private void initialize(Dimension displaySize) {
        background = new DisplayableBackground();
        foreground = new DisplayableForeground();        
        
        this.displaySize = displaySize;
        
        deviceDisplay = new JPanel();
        deviceDisplay.setSize(displaySize);        
        deviceDisplay.setMinimumSize(displaySize);
        deviceDisplay.setPreferredSize(displaySize);
        deviceDisplay.setOpaque(false);
        deviceDisplay.setLayout(null); // using absolute layout for this one
        deviceDisplay.add(background);
        background.setBounds(0,0,displaySize.width,displaySize.height);
        
        deviceDisplay.add(foreground);
        foreground.setBounds(0,0,displaySize.width,displaySize.height);
        
        
    }
    
    private void dispose() {
        deviceDisplay.remove(background);
        deviceDisplay = null;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.DisplayPresenter#showNotify()
     */
    @Override
    public void showNotify(Dimension screenSize, DeviceViewResources deviceViewResources) {
        this.deviceViewResources = deviceViewResources;
        initialize(screenSize);
        ListenerManager listenerManager = getComponent().getDocument().getListenerManager();
        listenerManager.addDesignListener(background, background.getDesignEventFilter(getComponent()));
        
        titlePropertyElement = new TitlePropertyElement();
    }
    
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.DisplayPresenter#hideNotify()
     */
    @Override
    public void hideNotify() {
        ListenerManager listenerManager = getComponent().getDocument().getListenerManager();
        listenerManager.removeDesignListener(background);
        dispose();
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.DisplayPresenter#getHooverShape(java.awt.Point)
     */
    @Override
    public Shape getHooverShape(Point point) {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.DisplayPresenter#getSelectionShape(java.awt.Point)
     */
    @Override
    public Shape getSelectionShape(Point point) {
        // TODO Auto-generated method stub
        return null;
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.model.DynamicPresenter#designChanged(org.netbeans.modules.vmd.api.model.DesignEvent)
     */
    @Override
    protected void designChanged(DesignEvent event) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.model.DynamicPresenter#getEventFilter()
     */
    @Override
    protected DesignEventFilter getEventFilter() {
        DesignComponent dc = getComponent(); 
        return new DesignEventFilter()
                .addComponentFilter(dc, true)
                .addHierarchyFilter(dc, true);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.ScreenInfoListener#deviceScreenSizeChanged(int, int)
     */
    public void deviceScreenSizeChanged(int width, int height) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.ScreenInfoListener#profileChanged(java.lang.String)
     */
    public void profileChanged(String currentProfile) {
        // TODO Auto-generated method stub
        
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.DisplayPresenter#getView(java.awt.Dimension)
     */
    @Override
    public JComponent getView() {
        return deviceDisplay;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.DisplayPresenter#getActualDisplaySize()
     */
    @Override
    public Dimension getActualDisplaySize() {
        return displaySize;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.ScreenComponentElement#getDefaultInjector()
     */
    @Override
    public Injector getDefaultInjector() {
        // TODO Auto-generated method stub
        return null;
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.ScreenComponentElement#getScreenPropertyElements()
     */
    @Override
    public Collection<ScreenPropertyElement> getScreenPropertyElements() {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.display.ScreenComponentElement#getSelectionShape()
     */
    @Override
    public Shape getSelectionShape() {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.screen.ScreenInfoListener#deviceChanged(java.lang.String)
     */
    public void deviceChanged(String deviceID) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Gets height of the header of the display. Header
     * usually contains things like battery/singal strength.
     * It might include also title, icon and other graphics
     * not contained in the "main" view.
     * 
     *  Please note, this value might change based on the
     *  actual values contained by this component
     * 
     * @return
     */
    protected final int getDisplayHeaderHeight() {
        // TODO need to add real implementation
        return 30;
    }
    
    
    /**
     * Gets height of the footer of the display. Footer
     * usually contains commands. Please note,
     * this value might change dynamically, based
     * on the components contained by this displayble
     * 
     * @return
     */
    protected final int getDisplayFooterHeight() {
        // TODO need to add real implementation
        return 20;
    }
        
     
    
    
    // title property element
    private class TitlePropertyElement extends ScreenPropertyElement {

        /**
         * @param propertyName
         */
        public TitlePropertyElement() {
            super(DisplayableCD.PROP_TITLE);
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.vmd.api.screen.display.ScreenPropertyElement#getHooverShape()
         */
        @Override
        public Shape getHooverShape() {
            // TODO Auto-generated method stub
            return foreground.title.getBounds();
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.vmd.api.model.DynamicPresenter#designChanged(org.netbeans.modules.vmd.api.model.DesignEvent)
         */
        @Override
        protected void designChanged(DesignEvent event) {
            if (hasMyPropertyChanged(event)) {
                // TODO this is just a proof of concept
                foreground.title.setText(getPropertyInfo().getValueAsString());
            }
        }
        
    }
    
    // commands property element?

    
    
    
    
    
    // foreground
    private class DisplayableForeground extends JComponent {
        
        
        private JLabel title;
        
        public DisplayableForeground() {
            initializeUI();
        }
        
        
        private void initializeUI() {
            this.setLayout(null);
            this.setOpaque(false);
            this.setBackground(Color.yellow);
            title = new JLabel();
            title.setBackground(deviceViewResources.getColor(DeviceViewResources.COLOR_BACKGROUND));
            title.setForeground(deviceViewResources.getColor(DeviceViewResources.COLOR_FOREGROUND));
            title.setFont(deviceViewResources.getFont(DeviceViewResources.FontFace.PROPORTIONAL,DeviceViewResources.FontSize.LARGE));            
            title.setText("dummy");
            this.add(title);
            title.setBounds(10, 20, 100, 100);
            System.out.println("Title added !!!");
        }
        
        
    }
    

    
    // background
    private class DisplayableBackground extends JPanel implements DesignListener {

        private boolean fullScreen = false;                    
        private JLabel battery;
        private JLabel signal;
        
        public DisplayableBackground() {
            initializeUI();
        }
        
        
        public Color getDefinedBgColor() {
            if (deviceViewResources != null) {
                return deviceViewResources.getColor(DeviceInfo.DeviceViewResources.COLOR_BACKGROUND);
            } else {
                return Color.WHITE;
            }
        }
        
        
        
        public void initializeUI() {
            this.setLayout(new GridBagLayout());
            this.setBackground(getDefinedBgColor());
            
            JPanel statusPanel = new JPanel();
            statusPanel.setBackground(getDefinedBgColor());
            statusPanel.setLayout(new BorderLayout());
            battery = new JLabel(new ImageIcon(BATTERY));
            signal = new JLabel(new ImageIcon(SIGNAL));
            statusPanel.add(battery,BorderLayout.WEST);
            statusPanel.add(signal,BorderLayout.EAST);            
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;            
            this.add(statusPanel,constraints);
            
            
            JPanel bgFill = new JPanel();
            bgFill.setBackground(getDefinedBgColor());
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            this.add(bgFill,constraints);
            
        }

        
        
        
        public DesignEventFilter getDesignEventFilter(DesignComponent component) {
            return new DesignEventFilter().addComponentFilter(component, false);
        }
        
        
        private void setFullScreen(boolean fullScreen) {            
            if (fullScreen != this.fullScreen) {
                this.fullScreen = fullScreen;
                if (fullScreen) {
                    // disable 
                    battery.setVisible(false);
                    signal.setVisible(false);
                } else {
                    // enable
                    battery.setVisible(true);
                    signal.setVisible(true);
                }
            }
        }
        
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.vmd.api.model.DesignListener#designChanged(org.netbeans.modules.vmd.api.model.DesignEvent)
         */
        public void designChanged(DesignEvent event) {
            final DesignComponent dc = DisplayableDisplayPresenter.this.getComponent();
            // currently doing just fullscreen - this one disables background
            if (event.isComponentPropertyChanged(dc, CanvasCD.PROP_IS_FULL_SCREEN)) {
                // get the new value
                dc.getDocument().getTransactionManager().readAccess(new Runnable() {
                    public void run() {
                        PropertyValue propertyValue = dc.readProperty(CanvasCD.PROP_IS_FULL_SCREEN);
                        // now get a binary value                    
                        Object value = propertyValue.getPrimitiveValue();
                        if (value instanceof Boolean) {
                            boolean booleanValue = ((Boolean)value).booleanValue();
                            setFullScreen(booleanValue);
                        }
                    }
                });
            }
        }                
    }
}
 