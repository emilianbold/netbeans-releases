/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.components.panels;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.Version.VersionDistance;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;

/**
 *
 * @author Kirill Sorokin
 */
public class JdkLocationPanel extends ApplicationLocationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Version minimumVersion;
    private Version maximumVersion;
    private Version preferredVersion;
    private String  vendorAllowed;
    private List<File>   jdkLocations;
    private List<String> jdkLabels;
    
    public JdkLocationPanel() {
        setProperty(MINIMUM_JDK_VERSION_PROPERTY,
                DEFAULT_MINIMUM_JDK_VERSION);
        setProperty(MAXIMUM_JDK_VERSION_PROPERTY,
                DEFAULT_MAXIMUM_JDK_VERSION);
        setProperty(VENDOR_JDK_ALLOWED_PROPERTY,
                DEFAULT_VENDOR_JDK_ALLOWED);
        
        setProperty(LOCATION_LABEL_TEXT_PROPERTY,
                DEFAULT_LOCATION_LABEL_TEXT);
        setProperty(LOCATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_LOCATION_BUTTON_TEXT);
        setProperty(LIST_LABEL_TEXT_PROPERTY,
                DEFAULT_LIST_LABEL_TEXT);
        
        setProperty(ERROR_NULL_PROPERTY,
                DEFAULT_ERROR_NULL);
        setProperty(ERROR_NOT_VALID_PATH_PROPERTY,
                DEFAULT_ERROR_NOT_VALID_PATH);
        setProperty(ERROR_PATH_NOT_EXISTS_PROPERTY,
                DEFAULT_ERROR_PATH_NOT_EXISTS);
        setProperty(ERROR_NOT_JAVAHOME_PROPERTY,
                DEFAULT_ERROR_NOT_JAVAHOME);
        setProperty(ERROR_NOT_JDK_PROPERTY,
                DEFAULT_ERROR_NOT_JDK);
        setProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY,
                DEFAULT_ERROR_WRONG_VERSION_OLDER);
        setProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY,
                DEFAULT_ERROR_WRONG_VERSION_NEWER);
        setProperty(ERROR_WRONG_VENDOR_PROPERTY,
                DEFAULT_ERROR_WRONG_VENDOR);        
        setProperty(ERROR_UNKNOWN_PROPERTY,
                DEFAULT_ERROR_UNKNOWN);
        setProperty(ERROR_NOTHING_FOUND_PROPERTY,
                DEFAULT_ERROR_NOTHING_FOUND);
        
        setProperty(USEDBY_LABEL_PROPERTY,
                DEFAULT_USEDBY_LABEL);
    }
    
    @Override
    public void initialize() {
        minimumVersion = Version.getVersion(
                getProperty(MINIMUM_JDK_VERSION_PROPERTY));
        maximumVersion = Version.getVersion(
                getProperty(MAXIMUM_JDK_VERSION_PROPERTY));
        vendorAllowed = getProperty(VENDOR_JDK_ALLOWED_PROPERTY);
        
        if (getProperty(PREFERRED_JDK_VERSION_PROPERTY) != null) {
            preferredVersion = Version.getVersion(
                    getProperty(PREFERRED_JDK_VERSION_PROPERTY));
        }
        
        jdkLocations = new LinkedList<File>();
        jdkLabels = new LinkedList<String>();
        
        final Registry registry = Registry.getInstance();
        for (int i = 0; i < SearchForJavaAction.javaLocations.size(); i++) {
            final File location = SearchForJavaAction.javaLocations.get(i);
            
            String label = SearchForJavaAction.javaLabels.get(i);
            Version version = null;
            
            // initialize the version; if the location exists, it must be an
            // already installed jdk and we should fetch the version in a
            // "traditional" way; otherwise the jdk is only planned for
            // installation and we should try to get its version from the
            // registry
            if (location.exists()) {
                version = JavaUtils.getVersion(location);
            } else {
                for (Product jdk: registry.getProducts(JDK_PRODUCT_UID)) {
                    if ((jdk.getStatus() == Status.TO_BE_INSTALLED) &&
                            jdk.getInstallationLocation().equals(location)) {
                        version = jdk.getVersion();
                    }
                }
            }
            
            // if we could not fetch the version, we should skip this jdk
            // installation
            if (version == null) {
                continue;
            }
            
            // run through the installed and to-be-installed products and check
            // whether this location is already used somewhere
            final RegistryFilter filter = new OrFilter(
                    new ProductFilter(Status.INSTALLED),
                    new ProductFilter(Status.TO_BE_INSTALLED));
            final List<Product> products = new LinkedList<Product>();
            for (Product product: registry.queryProducts(filter)) {
                final String jdk = product.getProperty(JDK_LOCATION_PROPERTY);
                
                if ((jdk != null) && jdk.equals(location.getAbsolutePath())) {
                    products.add(product);
                }
            }
            
            final Product product = (Product) getWizard().
                    getContext().
                    get(Product.class);
            
            if (products.contains(product)) {
                products.remove(product);
            }
            if (products.size() > 0) {
                label = StringUtils.format(
                        getProperty(USEDBY_LABEL_PROPERTY),
                        label,
                        StringUtils.asString(products));
            }
            
            // if the location exists and is a jdk installation (or if the
            // location does not exist - in this case we're positive that it
            // WILL be a jdk) and if version satisfies the requirements - add
            // the location to the list
            if ((!location.exists() || JavaUtils.isJdk(location))) {
                String vendor = JavaUtils.getInfo(location).getVendor();
                
                if(!version.olderThan(minimumVersion) &&
                        !version.newerThan(maximumVersion) &&
                        vendor.matches(vendorAllowed)) {
                    jdkLocations.add(location);
                    jdkLabels.add(label);
                }
                
            }
        }
    }
    
    public List<File> getLocations() {
        return jdkLocations;
    }
    
    public List<String> getLabels() {
        return jdkLabels;
    }
    
    public File getSelectedLocation() {
        // the first obvious choice is the jdk that has already been selected for
        // this product; if it has not yet been set, there are still lots of
        // choices:
        // - reuse the location which was selected on another jdk location panel if
        //   it fits the requirements
        // - reuse the location which has been used for an installed product if
        //   it fits the requirements
        // - choose the closest one to the preferred version if it is defined and
        //   a valid closest version exists
        // - use the first item in the list
        // - use an empty path
        final String jdkLocation =
                getWizard().getProperty(JDK_LOCATION_PROPERTY);
        if (jdkLocation != null) {
            return new File(jdkLocation);
        }
        
        if ((SearchForJavaAction.lastSelectedJava != null) &&
                jdkLocations.contains(SearchForJavaAction.lastSelectedJava)) {
            return SearchForJavaAction.lastSelectedJava;
        }
        
        for (Product product: Registry.getInstance().queryProducts(new OrFilter(
                new ProductFilter(Status.INSTALLED),
                new ProductFilter(Status.TO_BE_INSTALLED)))) {
            final String jdk = product.getProperty(JDK_LOCATION_PROPERTY);
            
            if (jdk != null) {
                final File jdkFile = new File(jdk);
                
                if (jdkLocations.contains(jdkFile)) {
                    return jdkFile;
                }
            }
        }
        
        if (preferredVersion != null) {
            File closestLocation = null;
            VersionDistance closestDistance = null;
            
            for (File location: jdkLocations) {
                final Version currentVersion =
                        JavaUtils.getVersion(location);
                final VersionDistance currentDistance =
                        currentVersion.getDistance(preferredVersion);
                
                if ((closestDistance == null) ||
                        currentDistance.lessThan(closestDistance)) {
                    closestLocation = location;
                    closestDistance = currentDistance;
                }
            }
            
            if (closestLocation != null) {
                return closestLocation;
            }
        }
        
        if (jdkLocations.size() != 0) {
            return jdkLocations.get(0);
        }
        
        return new File("");
    }
    
    public String validateLocation(final String path) {
        final File file = new File(path);
        
        if (path.equals("")) {
            return StringUtils.format(
                    getProperty(ERROR_NULL_PROPERTY));
        }
        
        if (!SystemUtils.isPathValid(path)) {
            return StringUtils.format(
                    getProperty(ERROR_NOT_VALID_PATH_PROPERTY), path);
        }
        
        if (!file.exists()) {
            return StringUtils.format(
                    getProperty(ERROR_PATH_NOT_EXISTS_PROPERTY), path);
        }
        
        if (!JavaUtils.isJavaHome(file)) {
            return StringUtils.format(
                    getProperty(ERROR_NOT_JAVAHOME_PROPERTY), path);
        }
        
        if (!JavaUtils.isJdk(file)) {
            return StringUtils.format(
                    getProperty(ERROR_NOT_JDK_PROPERTY), path);
        }
        
        Version version = JavaUtils.getVersion(file);
        if (version == null) {
            for (Product jdk: Registry.getInstance().getProducts(JDK_PRODUCT_UID)) {
                if ((jdk.getStatus() == Status.TO_BE_INSTALLED) &&
                        jdk.getInstallationLocation().equals(file)) {
                    version = jdk.getVersion();
                }
            }
        }
        
        if (version == null) {
            return StringUtils.format(getProperty(ERROR_UNKNOWN_PROPERTY), path);
        }
        
        if (version.olderThan(minimumVersion)) {
            return StringUtils.format(
                    getProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY),
                    path,
                    version,
                    minimumVersion);
        }
        
        if (version.newerThan(maximumVersion)) {
            return StringUtils.format(
                    getProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY),
                    path,
                    version,
                    maximumVersion);
        }
        String vendor = JavaUtils.getInfo(file).getVendor();
        if(!vendor.matches(vendorAllowed)) {
            return StringUtils.format(
                    getProperty(ERROR_WRONG_VENDOR_PROPERTY),
                    path,
                    vendor,
                    vendorAllowed);
        }
    
        return null;
    }
    
    public void setLocation(final File location) {
        SearchForJavaAction.lastSelectedJava = location;
        
        if (!SearchForJavaAction.javaLocations.contains(location)) {
            SearchForJavaAction.javaLocations.add(location);
            SearchForJavaAction.javaLabels.add(
                    SearchForJavaAction.getLabel(location));
        }
        
        getWizard().setProperty(JDK_LOCATION_PROPERTY, location.getAbsolutePath());
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String JDK_LOCATION_PROPERTY =
            "jdk.location"; // NOI18N
    
    public static final String MINIMUM_JDK_VERSION_PROPERTY =
            "minimum.jdk.version"; // NOI18N
    public static final String MAXIMUM_JDK_VERSION_PROPERTY =
            "maximum.jdk.version"; // NOI18N
    public static final String PREFERRED_JDK_VERSION_PROPERTY =
            "preferred.jdk.version"; // NOI18N
    public static final String VENDOR_JDK_ALLOWED_PROPERTY =
            "vendor.jdk.allowed.pattern"; // NOI18N
    
    public static final String DEFAULT_LOCATION_LABEL_TEXT =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.location.label.text"); // NOI18N
    public static final String DEFAULT_LOCATION_BUTTON_TEXT =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.location.button.text"); // NOI18N
    public static final String DEFAULT_LIST_LABEL_TEXT =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.list.label.text"); // NOI18N
    
    public static final String ERROR_NULL_PROPERTY =
            "error.null"; // NOI18N
    public static final String ERROR_NOT_VALID_PATH_PROPERTY =
            "error.not.valid.path"; // NOI18N
    public static final String ERROR_PATH_NOT_EXISTS_PROPERTY =
            "error.path.not.exists"; // NOI18N
    public static final String ERROR_NOT_JAVAHOME_PROPERTY =
            "error.not.javahome"; // NOI18N
    public static final String ERROR_NOT_JDK_PROPERTY =
            "error.not.jdk"; // NOI18N
    public static final String ERROR_WRONG_VERSION_OLDER_PROPERTY =
            "error.wrong.version.older"; // NOI18N
    public static final String ERROR_WRONG_VERSION_NEWER_PROPERTY =
            "error.wrong.version.newer"; // NOI18N
    public static final String ERROR_WRONG_VENDOR_PROPERTY =
            "error.wrong.vendor"; // NOI18N
    public static final String ERROR_UNKNOWN_PROPERTY =
            "error.unknown"; // NOI18N
    
    public static final String DEFAULT_ERROR_NULL =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.null"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_VALID_PATH =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.not.valid.path"); // NOI18N
    public static final String DEFAULT_ERROR_PATH_NOT_EXISTS =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.path.not.exists"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_JAVAHOME =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.not.javahome"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_JDK =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.not.jdk"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VERSION_OLDER =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.version.older"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VERSION_NEWER =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.version.newer"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VENDOR =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.wrong.vendor"); // NOI18N
    
    public static final String DEFAULT_ERROR_UNKNOWN =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.unknown"); // NOI18N
    public static final String DEFAULT_ERROR_NOTHING_FOUND =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.error.nothing.found"); // NOI18N
    
    public static final String DEFAULT_MINIMUM_JDK_VERSION =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.minimum.jdk.version"); // NOI18N
    public static final String DEFAULT_MAXIMUM_JDK_VERSION =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.maximum.jdk.version"); // NOI18N
    public static final String DEFAULT_VENDOR_JDK_ALLOWED =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.vendor.jdk.allowed");
    
    public static final String DEFAULT_USEDBY_LABEL =
            ResourceUtils.getString(JdkLocationPanel.class,
            "JLP.usedby.label"); //NOI18N
    public static final String USEDBY_LABEL_PROPERTY =
            "usedby.label"; //NOI18N
    
    private static final String JDK_PRODUCT_UID =
            "jdk"; //NOI18N
}
