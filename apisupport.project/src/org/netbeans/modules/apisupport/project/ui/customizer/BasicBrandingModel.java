/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.BrandingSupport;
import org.netbeans.modules.apisupport.project.suite.BrandingSupport.BrandedFile;
import org.netbeans.modules.apisupport.project.suite.BrandingSupport.BundleKey;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectType;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * @author Radek Matous, S. Aubrecht
 */
public class BasicBrandingModel {
    
    private BrandingSupport branding;
    private final SuiteProperties suiteProps;
    private final Project project;
    private final String brandingPath;
    
    /** generated properties*/
    public static final String NAME_PROPERTY = "app.name";//NOI18N
    public static final String TITLE_PROPERTY = "app.title";//NOI18N
    public static final String ICON_LOCATION_PROPERTY = "app.icon";//NOI18N
    
    public static final String BRANDING_TOKEN_PROPERTY = "branding.token";//NOI18N
    
    /** for generating property branding.token*/
    private boolean brandingEnabled;
    private boolean brandingChanged = false;
    
    /** for properties (app.name, app.title, app.icon)*/
    private String name;
    private String title;
    private @NullAllowed BrandingSupport.BrandedFile icon48 = null;
    private BrandingSupport.BrandedFile icon16 = null;
    private BrandingSupport.BrandedFile icon32 = null;
    
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

    /**all BundleKeys the user may have modified through Resource Bundle editor panel */
    private final Set<BrandingSupport.BundleKey> generalResourceBundleKeys = new HashSet<BrandingSupport.BundleKey>();
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    public BasicBrandingModel(SuiteProperties suiteProps) {
        assert null != suiteProps;
        this.suiteProps = suiteProps;
        this.project = null;
        this.brandingPath = null;
        init();
    }

    /**
     * Create branding model for a generic project, e.g. Maven branding module.
     * @param p Project to be branded
     * @param brandingPath Path relative to project's dir where branded resources are stored in.
     */
    public BasicBrandingModel(Project p, String brandingPath) {
        assert null != p;
        assert null != brandingPath;
        assert !brandingPath.isEmpty();
        this.suiteProps = null;
        this.project = p;
        this.brandingPath = brandingPath;
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
            if( null != suiteProps ) {
                suiteProps.setProperty(NAME_PROPERTY, getName());
                suiteProps.setProperty(BRANDING_TOKEN_PROPERTY, "${" + NAME_PROPERTY + "}");//NOI18N
            }
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
            if( null != suiteProps ) {
                suiteProps.setProperty(TITLE_PROPERTY, getTitle());
            }
        }
    }
    
    public URL getIconSource(int size) {
        switch( size ) {
            case 16:
                return icon16 != null ? icon16.getBrandingSource() : null;
            case 32:
                return icon32 != null ? icon32.getBrandingSource() : null;
            case 48:
                return icon48 != null ? icon48.getBrandingSource() : null;
        }
        throw new IllegalArgumentException("Invalid icon size: " + size);
    }
    
    public void setIconSource(int size, final URL url) {
        if (isBrandingEnabled()) {
            BrandingSupport.BrandedFile icon = null;
            switch( size ) {
                case 16:
                    icon = icon16;
                    break;
                case 32:
                    icon = icon32;
                    break;
                case 48:
                    icon = icon48;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid icon size: " + size);
            }
            if (icon != null) {
                icon.setBrandingSource(url);
            }
            if( null != suiteProps ) {
                suiteProps.setProperty(ICON_LOCATION_PROPERTY, getIconLocation());
            }
        }
    }
    
    public @CheckForNull String getIconLocation() {
        if (icon48 == null) {
            return null;
        }
        File prj = getProjectDirectoryFile();
        String relativePath = PropertyUtils.relativizeFile(prj, icon48.getFileLocation());
        
        return relativePath;
    }
    
    public String getSplashLocation() {
        File prj = getProjectDirectoryFile();
        String relativePath = PropertyUtils.relativizeFile(prj ,splash.getFileLocation());
        
        return relativePath;
    }

    public Project getProject() {
        return null != suiteProps ? suiteProps.getProject() : project;
    }

    private File getProjectDirectoryFile() {
        if( null == suiteProps ) {
            return FileUtil.toFile(project.getProjectDirectory());
        }
        return suiteProps.getProjectDirectoryFile();
    }
    
    public void store() throws IOException {
        if (brandingEnabled) {
            getBranding().brandBundleKey(productInformation);
            getBranding().brandBundleKey(mainWindowTitle);
            getBranding().brandBundleKey(splashWindowTitle);
            getBranding().brandBundleKey(mainWindowTitleNoProject);
            getBranding().brandBundleKey(currentVersion);

            if (icon48 != null) { // #176423
                getBranding().brandFile(icon48, getScaleAndStoreIconTask(icon48, 48, 48));
            }

            if (icon16 != null) {
                getBranding().brandFile(icon16, getScaleAndStoreIconTask(icon16, 16, 16));
            }

            if (icon32 != null) {
                getBranding().brandFile(icon32, getScaleAndStoreIconTask(icon32, 32, 32));
            }
                                    
            getBranding().brandBundleKeys(splashKeys);
            if (splash != null) {
                getBranding().brandFile(splash);
            }
            getBranding().brandBundleKeys(winsysKeys);

            getBranding().brandBundleKeys(generalResourceBundleKeys);
            
        } else {
            if (brandingChanged && null != suiteProps) {//#115737
                suiteProps.removeProperty(BasicBrandingModel.BRANDING_TOKEN_PROPERTY);
                suiteProps.removeProperty(BasicBrandingModel.NAME_PROPERTY);
                suiteProps.removeProperty(BasicBrandingModel.TITLE_PROPERTY);
                suiteProps.removeProperty(BasicBrandingModel.ICON_LOCATION_PROPERTY);
            }
        }
    }
    
    private static Runnable getScaleAndStoreIconTask(final BrandedFile icon, final int width, final int height) throws IOException {
        return new Runnable() {
            @Override
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
                if( null == suiteProps ) {
                    branding = BrandingSupport.getInstance(project, brandingPath);
                } else {
                    branding = BrandingSupport.getInstance(suiteProps);
                }
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
        brandingEnabled = null == suiteProps || (suiteProps.getProperty(BRANDING_TOKEN_PROPERTY) != null);
    }
    
    private String getSimpleName() {
        if( null == suiteProps ) {
            String res = mainWindowTitle.getValue();
            if( null != res && res.endsWith(" {0}") ) { //NOI18N
                res = res.substring(0, res.lastIndexOf(" {0}")); //NOI18N
            }
            if( null == res )
                res = getProjectDirectoryFile().getName();
            return res;
        }
        Element nameEl = Util.findElement(suiteProps.getProject().getHelper().getPrimaryConfigurationData(true), "name", SuiteProjectType.NAMESPACE_SHARED); // NOI18N
        String text = (nameEl != null) ? Util.findText(nameEl) : null;
        return (text != null) ? text : "???"; // NOI18N
    }

    void initName(boolean reread)  {
        if (name == null || reread) {
            if( null != suiteProps )
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
            String initTitle = null == suiteProps ? null : suiteProps.getProperty(TITLE_PROPERTY);
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
        
        icon48 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame48.gif");//NOI18N

        icon16 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame.gif");//NOI18N               
        
        icon32 = getBranding().getBrandedFile(
                "org.netbeans.core.startup",//NOI18N
                "org/netbeans/core/startup/frame32.gif");//NOI18N

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

        generalResourceBundleKeys.clear();
}

    private String backslashesToSlashes (String text) {
        return text.replace('\\', '/'); // NOI18N
    }

    private BrandingSupport.BundleKey findInModifiedGeneralBundleKeys (String codenamebase, String bundlepath, String key) {
        for (BundleKey bundleKey : generalResourceBundleKeys) {
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return bundleKey;
        }
        return null;
    }

    public void addModifiedGeneralBundleKey (BrandingSupport.BundleKey key) {
        generalResourceBundleKeys.add (key);
    }

    public BrandingSupport.BundleKey getGeneralBundleKeyForModification
            (String codenamebase, String bundlepath, String key) {
        BrandingSupport.BundleKey bKey = findInModifiedGeneralBundleKeys(codenamebase, bundlepath, key);
        return null != bKey ? bKey : getBranding().getBundleKey(codenamebase, bundlepath, key);
    }

    public String getKeyValue (String bundlepath, String codenamebase, String key) {
        BrandingSupport.BundleKey bKey = findInModifiedGeneralBundleKeys(codenamebase, bundlepath, key);
        return null != bKey ? bKey.getValue()
                : getBranding().getBundleKey(codenamebase, bundlepath, key).getValue();
    }

    public boolean isKeyBranded (String bundlepath, String codenamebase, String key) {
        // in modified keys?
        for (BundleKey bundleKey : generalResourceBundleKeys) {
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        // in branded but not modified keys?
        Set<BundleKey> bundleKeys = getBranding().getBrandedBundleKeys();
        for (BundleKey bundleKey : bundleKeys) {
            if (key.equals(bundleKey.getKey()) &&
                backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }

    public boolean isBundleBranded (String bundlepath, String codenamebase) {
        // in modified keys?
        for (BundleKey bundleKey : generalResourceBundleKeys) {
            if (backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        // in branded but not modified keys?
        Set<BundleKey> bundleKeys = getBranding().getBrandedBundleKeys();
        for (BundleKey bundleKey : bundleKeys) {
            if (backslashesToSlashes(bundleKey.getBundleFilePath()).endsWith(bundlepath) &&
                codenamebase.equals(bundleKey.getModuleEntry().getCodeNameBase()))
                return true;
        }
        return false;
    }

    public @CheckForNull BrandingSupport.BundleKey getSplashWidth() {
        return splashWidth;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashHeight() {
        return splashHeight;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashShowProgressBar() {
        return splashShowProgressBar;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashRunningTextBounds() {
        return splashRunningTextBounds;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashProgressBarBounds() {
        return splashProgressBarBounds;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashRunningTextFontSize() {
        return splashRunningTextFontSize;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashRunningTextColor() {
        return splashRunningTextColor;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashProgressBarColor() {
        return splashProgressBarColor;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashProgressBarEdgeColor() {
        return splashProgressBarEdgeColor;
    }
    
    public @CheckForNull BrandingSupport.BundleKey getSplashProgressBarCornerColor() {
        return splashProgressBarCornerColor;
    }
    
    public @CheckForNull BrandingSupport.BrandedFile getSplash() {
        return splash;
    }

    public @CheckForNull BundleKey getWsEnableClosingEditors() {
        return wsEnableClosingEditors;
    }

    public @CheckForNull BundleKey getWsEnableClosingViews() {
        return wsEnableClosingViews;
    }

    public @CheckForNull BundleKey getWsEnableDragAndDrop() {
        return wsEnableDragAndDrop;
    }

    public @CheckForNull BundleKey getWsEnableFloating() {
        return wsEnableFloating;
    }

    public @CheckForNull BundleKey getWsEnableMaximization() {
        return wsEnableMaximization;
    }

    public @CheckForNull BundleKey getWsEnableMinimumSize() {
        return wsEnableMinimumSize;
    }

    public @CheckForNull BundleKey getWsEnableResizing() {
        return wsEnableResizing;
    }

    public @CheckForNull BundleKey getWsEnableSliding() {
        return wsEnableSliding;
    }
}
