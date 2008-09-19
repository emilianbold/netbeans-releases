/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.BrandingSupport;
import org.netbeans.modules.apisupport.project.suite.BrandingSupport.BrandedFile;
import org.netbeans.modules.apisupport.project.suite.BrandingSupport.BundleKey;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectType;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * @author Radek Matous
 */
public class BasicBrandingModel {
    
    private BrandingSupport branding;
    private final SuiteProperties suiteProps;
    
    /** generated properties*/
    public static final String NAME_PROPERTY = "app.name";//NOI18N
    public static final String TITLE_PROPERTY = "app.title";//NOI18N
    public static final String ICON_LOCATION_PROPERTY = "app.icon";//NOI18N
    
    public static final String BRANDING_TOKEN_PROPERTY = "branding.token";//NOI18N
    
    static final int ICON_WIDTH = 48;
    static final int ICON_HEIGHT = 48;    
    
    /** for generating property branding.token*/
    private boolean brandingEnabled;
    private boolean brandingChanged = false;
    
    /** for properties (app.name, app.title, app.icon)*/
    private String name;
    private String title;
    private BrandingSupport.BrandedFile icon = null;
    private BrandingSupport.BrandedFile icon16 = null;    
    
    /** representation of bundle keys depending on app.title */
    private BrandingSupport.BundleKey productInformation = null;
    private BrandingSupport.BundleKey mainWindowTitle = null;
    private BrandingSupport.BundleKey splashWindowTitle = null;    
    private BrandingSupport.BundleKey mainWindowTitleNoProject = null;
    private BrandingSupport.BundleKey currentVersion = null;
    
    /** representation of bundle keys for splash section */
    private BrandingSupport.BrandedFile splash = null;
    
    private BrandingSupport.BundleKey splashWidth = null;
    private BrandingSupport.BundleKey splashHeight = null;
    private BrandingSupport.BundleKey splashShowProgressBar = null;
    private BrandingSupport.BundleKey splashRunningTextBounds = null;
    private BrandingSupport.BundleKey splashProgressBarBounds = null;
    private BrandingSupport.BundleKey splashRunningTextFontSize = null;
    private BrandingSupport.BundleKey splashRunningTextColor = null;
    private BrandingSupport.BundleKey splashProgressBarColor = null;
    private BrandingSupport.BundleKey splashProgressBarEdgeColor = null;
    private BrandingSupport.BundleKey splashProgressBarCornerColor = null;
    
    /**all above splash BundleKeys in set*/
    private final Set<BrandingSupport.BundleKey> splashKeys = new HashSet<BrandingSupport.BundleKey>();
    
    /** representation of bundle keys for window system section */
    private BrandingSupport.BundleKey wsEnableDragAndDrop = null;
    private BrandingSupport.BundleKey wsEnableFloating = null;
    private BrandingSupport.BundleKey wsEnableSliding = null;
    private BrandingSupport.BundleKey wsEnableClosingViews = null;
    private BrandingSupport.BundleKey wsEnableClosingEditors = null;
    private BrandingSupport.BundleKey wsEnableResizing = null;
    private BrandingSupport.BundleKey wsEnableMinimumSize = null;
    private BrandingSupport.BundleKey wsEnableMaximization = null;
    
    /**all above splash BundleKeys in set*/
    private final Set<BrandingSupport.BundleKey> winsysKeys = new HashSet<BrandingSupport.BundleKey>();
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    /** Creates a new instance of ApplicationDetails */
    public BasicBrandingModel(final SuiteProperties suiteProps) {
        this.suiteProps = suiteProps;
        init();
    }
    
    public boolean isBrandingEnabled() {
        return brandingEnabled;
    }
    
    public void setBrandingEnabled(boolean brandingEnabled) {
        if (this.brandingEnabled != brandingEnabled) {
            this.brandingEnabled = brandingEnabled;
            brandingChanged = true;
            changeSupport.fireChange();
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) /*throws IllegalArgumentException*/ {
        /*if (name != null && !name.matches("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*")) { // NOI18N
            throw new IllegalArgumentException("Malformed name: " + name); // NOI18N
        }*/
     
        if (isBrandingEnabled()) {
            this.name = name;
            suiteProps.setProperty(NAME_PROPERTY, getName());
            suiteProps.setProperty(BRANDING_TOKEN_PROPERTY, "${" + NAME_PROPERTY + "}");//NOI18N
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    public void setTitle(String title) {
        if (isBrandingEnabled()) {
            this.title = title;
            if (productInformation != null) {
                productInformation.setValue(title);
            }
            if (mainWindowTitle != null) {
                mainWindowTitle.setValue(title + " {0}"); //NOI18N
            }
            if (splashWindowTitle != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(NbBundle.getMessage(BasicBrandingModel.class, "LBL_splash_window_title_prefix"));//NOI18N
                sb.append(" ").append(title);//NOI18N
                splashWindowTitle.setValue(sb.toString());//NOI18N
            }
            if (mainWindowTitleNoProject != null) {
                mainWindowTitleNoProject.setValue(title + " {0}"); //NOI18N
            }
            if (currentVersion != null) {
                currentVersion.setValue(title + " {0}"); //NOI18N
            }
            suiteProps.setProperty(TITLE_PROPERTY, getTitle());
        }
    }
    
    public URL getIconSource() {
        return icon != null ? icon.getBrandingSource() : null;
    }
    
    public void setIconSource(final URL url) {
        if (isBrandingEnabled()) {
            icon.setBrandingSource(url);
            suiteProps.setProperty(ICON_LOCATION_PROPERTY, getIconLocation());
        }
    }
    
    public String getIconLocation() {
        File prj = suiteProps.getProjectDirectoryFile();
        String relativePath = PropertyUtils.relativizeFile(prj ,icon.getFileLocation());
        
        return relativePath;
    }
    
    public String getSplashLocation() {
        File prj = suiteProps.getProjectDirectoryFile();
        String relativePath = PropertyUtils.relativizeFile(prj ,splash.getFileLocation());
        
        return relativePath;
    }
    
    public void store() throws IOException {
        if (brandingEnabled) {
            getBranding().brandBundleKey(productInformation);
            getBranding().brandBundleKey(mainWindowTitle);
            getBranding().brandBundleKey(splashWindowTitle);
            getBranding().brandBundleKey(mainWindowTitleNoProject);
            getBranding().brandBundleKey(currentVersion);
            
            boolean isModified = icon.isModified();
            getBranding().brandFile(icon, 
                    getScaleAndStoreIconTask(icon, BasicBrandingModel.ICON_WIDTH,BasicBrandingModel.ICON_HEIGHT));
            
            if (isModified) {
                icon16.setBrandingSource(icon.getBrandingSource());
                getBranding().brandFile(icon16, 
                        getScaleAndStoreIconTask(icon16, 16,16));
            }
                                    
            getBranding().brandBundleKeys(splashKeys);
            getBranding().brandFile(splash);                        
            getBranding().brandBundleKeys(winsysKeys);
        } else {
            if (brandingChanged) {//#115737
                suiteProps.removeProperty(BasicBrandingModel.BRANDING_TOKEN_PROPERTY);
                suiteProps.removeProperty(BasicBrandingModel.NAME_PROPERTY);
                suiteProps.removeProperty(BasicBrandingModel.TITLE_PROPERTY);
                suiteProps.removeProperty(BasicBrandingModel.ICON_LOCATION_PROPERTY);
            }
        }
    }
    
    private static Runnable getScaleAndStoreIconTask(final BrandedFile icon, final int width, final int height) throws IOException {
        return new Runnable() {
            public void run() {
                BufferedImage bi = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_ARGB);
                
                Graphics2D g2 = bi.createGraphics();
                ImageIcon image = new ImageIcon(icon.getBrandingSource());
                g2.drawImage(image.getImage(),0, 0, 
                        width, height, null);
                
                g2.dispose();
                try {
                    ImageIO.write(bi,"png",icon.getFileLocation());//NOI18N
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        };
    }
    
    private BrandingSupport getBranding() {
        if (branding == null) {
            try {
                branding = BrandingSupport.getInstance(suiteProps);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
                throw new IllegalStateException(ex.getLocalizedMessage());
            }
        }
        return branding;
    }
    
    private void init() {
        initBundleKeys();
        initName(false);
        initTitle(false);
        brandingEnabledRefresh();
        brandingChanged = false;
    }
    
    void brandingEnabledRefresh() {
        brandingEnabled = (suiteProps.getProperty(BRANDING_TOKEN_PROPERTY) != null);
    }
    
    private String getSimpleName() {
        Element nameEl = Util.findElement(suiteProps.getProject().getHelper().getPrimaryConfigurationData(true), "name", SuiteProjectType.NAMESPACE_SHARED); // NOI18N
        String text = (nameEl != null) ? Util.findText(nameEl) : null;
        return (text != null) ? text : "???"; // NOI18N
    }

    void initName(boolean reread)  {
        if (name == null || reread) {
            name = suiteProps.getProperty(NAME_PROPERTY);
        }
        
        if (name == null) {
            name = getSimpleName().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]", "_"); // NOI18N
            if (!name.matches("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*")) { // NOI18N
                // Too far from a decent name, forget it.
                name = "app"; // NOI18N
            }
        }
        
        assert name != null;
    }
    
    void initTitle(boolean reread)  {
        if (title == null || reread) {
            String initTitle = suiteProps.getProperty(TITLE_PROPERTY);
            
            if (initTitle == null) {
                initTitle = getSimpleName();
                // Just make a rough attempt to uppercase it, to hint that it can be a display name.
                if (Character.isLowerCase(initTitle.charAt(0))) {
                    initTitle = String.valueOf(Character.toLowerCase(initTitle.charAt(0))) + initTitle.substring(1);
                }
            }
            assert initTitle != null;
            title = initTitle;
        }
    }
    
    private void initBundleKeys() {
        productInformation = getBranding().getBundleKey(
                "org.netbeans.core",//NOI18N
                "org/netbeans/core/ui/Bundle.properties" ,//NOI18N
                "LBL_ProductInformation");//NOI18N
        
        mainWindowTitle = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/view/ui/Bundle.properties", // NOI18N
                "CTL_MainWindow_Title");//NOI18N

        splashWindowTitle = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "LBL_splash_window_title");//NOI18N                
        
        mainWindowTitleNoProject = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/view/ui/Bundle.properties",//NOI18N
                "CTL_MainWindow_Title_No_Project");//NOI18N
        
        currentVersion = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "currentVersion");//NOI18N
        
        icon = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame48.gif");//NOI18N

        icon16 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame.gif");//NOI18N               
        
        splash = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/splash.gif");//NOI18N
        
        // init of splash keys
        
        splashWidth = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SPLASH_WIDTH");//NOI18N
        
        splashHeight = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SPLASH_HEIGHT");//NOI18N
        
        splashShowProgressBar = getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashShowProgressBar");//NOI18N
        
        splashRunningTextFontSize= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashRunningTextFontSize");//NOI18N
        
        splashProgressBarBounds= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashProgressBarBounds");//NOI18N
        
        splashRunningTextBounds= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashRunningTextBounds");//NOI18N
        
        splashRunningTextColor= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashRunningTextColor");//NOI18N
        
        splashProgressBarColor= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashProgressBarColor");//NOI18N
        
        splashProgressBarEdgeColor= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashProgressBarEdgeColor");//NOI18N
        
        splashProgressBarCornerColor= getBranding().getBundleKey(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/Bundle.properties",//NOI18N
                "SplashProgressBarCornerColor");//NOI18N
        
        splashKeys.clear();
        
        if (splashWidth != null) {
            splashKeys.add(splashWidth);
        }
        if (splashHeight != null) {
            splashKeys.add(splashHeight);
        }
        if (splashShowProgressBar != null) {
            splashKeys.add(splashShowProgressBar);
        }
        if (splashRunningTextBounds != null) {
            splashKeys.add(splashRunningTextBounds);
        }
        if (splashProgressBarBounds != null) {
            splashKeys.add(splashProgressBarBounds);
        }
        if (splashRunningTextFontSize != null) {
            splashKeys.add(splashRunningTextFontSize);
        }
        if (splashRunningTextColor != null) {
            splashKeys.add(splashRunningTextColor );
        }
        if (splashProgressBarColor != null) {
            splashKeys.add(splashProgressBarColor);
        }
        if (splashProgressBarEdgeColor != null) {
            splashKeys.add(splashProgressBarEdgeColor);
        }
        if (splashProgressBarCornerColor != null) {
            splashKeys.add(splashProgressBarCornerColor);
        }
        splashKeys.remove(null);

            
        wsEnableClosingEditors = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Editor.TopComponent.Closing.Enabled");//NOI18N
            
        wsEnableClosingViews = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "View.TopComponent.Closing.Enabled");//NOI18N
            
        wsEnableDragAndDrop = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.DragAndDrop.Enabled");//NOI18N
            
        wsEnableFloating = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Undocking.Enabled");//NOI18N
            
        wsEnableMinimumSize = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "Splitter.Respect.MinimumSize.Enabled");//NOI18N
            
        wsEnableResizing = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Resizing.Enabled");//NOI18N
            
        wsEnableSliding = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Sliding.Enabled");//NOI18N
            
        wsEnableMaximization = getBranding().getBundleKey(
                "org.netbeans.core.windows",//NOI18N
                "org/netbeans/core/windows/Bundle.properties",//NOI18N
                "TopComponent.Maximization.Enabled");//NOI18N
        
        winsysKeys.clear();
        
        if (wsEnableClosingEditors != null) {
            winsysKeys.add(wsEnableClosingEditors);
        }
        if (wsEnableClosingViews != null) {
            winsysKeys.add(wsEnableClosingViews);
        }
        if (wsEnableDragAndDrop != null) {
            winsysKeys.add(wsEnableDragAndDrop);
        }
        if (wsEnableFloating != null) {
            winsysKeys.add(wsEnableFloating);
        }
        if (wsEnableMaximization != null) {
            winsysKeys.add(wsEnableMaximization);
        }
        if (wsEnableMinimumSize != null) {
            winsysKeys.add(wsEnableMinimumSize);
        }
        if (wsEnableResizing != null) {
            winsysKeys.add(wsEnableResizing);
        }
        if (wsEnableSliding != null) {
            winsysKeys.add(wsEnableSliding);
        }
        winsysKeys.remove(null);
}
    
    public BrandingSupport.BundleKey getSplashWidth() {
        return splashWidth;
    }
    
    public BrandingSupport.BundleKey getSplashHeight() {
        return splashHeight;
    }
    
    public BrandingSupport.BundleKey getSplashShowProgressBar() {
        return splashShowProgressBar;
    }
    
    public BrandingSupport.BundleKey getSplashRunningTextBounds() {
        return splashRunningTextBounds;
    }
    
    public BrandingSupport.BundleKey getSplashProgressBarBounds() {
        return splashProgressBarBounds;
    }
    
    public BrandingSupport.BundleKey getSplashRunningTextFontSize() {
        return splashRunningTextFontSize;
    }
    
    public BrandingSupport.BundleKey getSplashRunningTextColor() {
        return splashRunningTextColor;
    }
    
    public BrandingSupport.BundleKey getSplashProgressBarColor() {
        return splashProgressBarColor;
    }
    
    public BrandingSupport.BundleKey getSplashProgressBarEdgeColor() {
        return splashProgressBarEdgeColor;
    }
    
    public BrandingSupport.BundleKey getSplashProgressBarCornerColor() {
        return splashProgressBarCornerColor;
    }
    
    public BrandingSupport.BrandedFile getSplash() {
        return splash;
    }

    public BundleKey getWsEnableClosingEditors() {
        return wsEnableClosingEditors;
    }

    public BundleKey getWsEnableClosingViews() {
        return wsEnableClosingViews;
    }

    public BundleKey getWsEnableDragAndDrop() {
        return wsEnableDragAndDrop;
    }

    public BundleKey getWsEnableFloating() {
        return wsEnableFloating;
    }

    public BundleKey getWsEnableMaximization() {
        return wsEnableMaximization;
    }

    public BundleKey getWsEnableMinimumSize() {
        return wsEnableMinimumSize;
    }

    public BundleKey getWsEnableResizing() {
        return wsEnableResizing;
    }

    public BundleKey getWsEnableSliding() {
        return wsEnableSliding;
    }
}
