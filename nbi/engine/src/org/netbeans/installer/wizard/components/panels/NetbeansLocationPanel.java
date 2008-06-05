/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.components.panels;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.dependencies.InstallAfter;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.Version.VersionDistance;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;

/**
 *
 * @author Jeff Lin
 */
public class NetbeansLocationPanel extends ApplicationLocationPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Version minimumVersion;
    private Version maximumVersion;
    private List<File>   netbeansLocations;
    private List<String> netbeansLabels;
    private static File lastSelectedNetbeans = null;


    public NetbeansLocationPanel() {        
        setProperty(MINIMUM_NETBEANS_VERSION_PROPERTY,
                DEFAULT_MINIMUM_NETBEANS_VERSION);
        setProperty(MAXIMUM_NETBEANS_VERSION_PROPERTY,
                DEFAULT_MAXIMUM_NETBEANS_VERSION);

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
        setProperty(ERROR_WRONG_VERSION_OLDER_PROPERTY,
                DEFAULT_ERROR_WRONG_VERSION_OLDER);
        setProperty(ERROR_WRONG_VERSION_NEWER_PROPERTY,
                DEFAULT_ERROR_WRONG_VERSION_NEWER);
        setProperty(ERROR_UNKNOWN_PROPERTY,
                DEFAULT_ERROR_UNKNOWN);
        setProperty(ERROR_NOTHING_FOUND_PROPERTY,
                DEFAULT_ERROR_NOTHING_FOUND);
        
    }

    @Override
    public void initialize() {
        minimumVersion = Version.getVersion(
                getProperty(MINIMUM_NETBEANS_VERSION_PROPERTY));
        maximumVersion = Version.getVersion(
                getProperty(MAXIMUM_NETBEANS_VERSION_PROPERTY));


        //addJavaLocationsFromProductDependencies();

        netbeansLocations = new LinkedList<File>();
        netbeansLabels = new LinkedList<String>();

/************************
        
        final Registry registry = Registry.getInstance();
        for (int i = 0; i < SearchForJavaAction.getJavaLocations().size(); i++) {
            final File location = SearchForJavaAction.getJavaLocations().get(i);

            String label = SearchForJavaAction.getJavaLabels().get(i);
            Version version = null;

            // initialize the version; if the location exists, it must be an
            // already installed jdk and we should fetch the version in a
            // "traditional" way; otherwise the jdk is only planned for
            // installation and we should try to get its version from the
            // registry
            if (location.exists()) {
                version = JavaUtils.getVersion(location);
            } else {
                for (Product jdk: registry.getProducts(NETBEANS_PRODUCT_UID)) {
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
 **************************/
    }

    public List<File> getLocations() {
        return netbeansLocations;
    }

    public List<String> getLabels() {
        return netbeansLabels;
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
        final String netbeansLocation =
                getWizard().getProperty(NETBEANS_LOCATION_PROPERTY);
        if (netbeansLocation != null /*&& netbeansLocations.contains(new File(netbeansLocation))*/) {
            return new File(netbeansLocation);
        }
        
/*************************
        if ((lastSelectedJava != null) &&
                jdkLocations.contains(lastSelectedJava)) {
            return lastSelectedJava;
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
        *************************/
        if (netbeansLocations.size() != 0) {
            return netbeansLocations.get(0);
        }

        //return new File(StringUtils.EMPTY_STRING);
        return new File("C:\\temp\\Netbeans 6.0");
    }
    
    
    public String validateLocation(final String path) {
        
        final File file = new File(path);

        if (path.equals(StringUtils.EMPTY_STRING)) {
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
        } else {
            // check that NetBeans is there...
        }
        // also check that it is the NetBeans of required version...
        // Version version = NetBeansUtils.getNetBeansVersion(file);
        Version version = Version.getVersion("6.1");
        
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
        
        return null;
    }
    
    

    public void setLocation(final File location) {
        //lastSelectedJava = location;
        //SearchForJavaAction.addJavaLocation(location);
        getWizard().setProperty(NETBEANS_LOCATION_PROPERTY, location.getAbsolutePath());
    }
/**************************
    private void addJavaLocationsFromProductDependencies() {
        // finally we should scan the registry for jdks planned for installation, if
        // the current product is scheduled to be installed after 'jdk', i.e. has
        // an install-after dependency on 'jdk' uid

        final Object objectContext = getWizard().getContext().get(Product.class);
        boolean sort = false;
        if(objectContext != null && objectContext instanceof Product) {
            final Product product = (Product) objectContext;
            for (Dependency dependency: product.getDependencies(
                    InstallAfter.class)) {
                if (dependency.getUid().equals(JDK_PRODUCT_UID)) {
                    for (Product jdk: Registry.getInstance().getProducts(JDK_PRODUCT_UID)) {
                        if (jdk.getStatus() == Status.TO_BE_INSTALLED &&
                                !SearchForJavaAction.getJavaLocations().
                                contains(jdk.getInstallationLocation())) {
                            SearchForJavaAction.addJavaLocation(
                                    jdk.getInstallationLocation(),
                                    jdk.getVersion(),
                                    SUN_MICROSYSTEMS_VENDOR);
                            sort = true;
                        }
                    }

                    break;
                }
            }
        }
        if(sort) {
            SearchForJavaAction.sortJavaLocations();
        }
    }
 ******************************/
/////////////////////////////////////////////////////////////////////////////////
// Constants
    private static final String NETBEANS_PRODUCT_UID =
            "NetBeans"; //NOI18N
    public static final String NETBEANS_LOCATION_PROPERTY =
            "netbeans.location"; // NOI18N
    
    public static final String MINIMUM_NETBEANS_VERSION_PROPERTY =
            "minimum.netbeans.version"; // NOI18N
    public static final String MAXIMUM_NETBEANS_VERSION_PROPERTY =
            "maximum.netbeans.version"; // NOI18N
    public static final String ERROR_NULL_PROPERTY =
            "error.null"; // NOI18N
    public static final String ERROR_NOT_VALID_PATH_PROPERTY =
            "error.not.valid.path"; // NOI18N
    public static final String ERROR_PATH_NOT_EXISTS_PROPERTY =
            "error.path.not.exists"; // NOI18N
    public static final String ERROR_WRONG_VERSION_OLDER_PROPERTY =
            "error.wrong.version.older"; // NOI18N
    public static final String ERROR_WRONG_VERSION_NEWER_PROPERTY =
            "error.wrong.version.newer"; // NOI18N
    public static final String ERROR_UNKNOWN_PROPERTY =
            "error.unknown"; // NOI18N
    

    public static final String DEFAULT_LOCATION_LABEL_TEXT =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.location.label.text"); // NOI18N
    public static final String DEFAULT_LOCATION_BUTTON_TEXT =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.location.button.text"); // NOI18N
    public static final String DEFAULT_LIST_LABEL_TEXT =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.list.label.text"); // NOI18N
    public static final String DEFAULT_ERROR_NULL =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.error.null"); // NOI18N
    public static final String DEFAULT_ERROR_NOT_VALID_PATH =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.error.not.valid.path"); // NOI18N
    public static final String DEFAULT_ERROR_PATH_NOT_EXISTS =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.error.path.not.exists"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VERSION_OLDER =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.error.wrong.version.older"); // NOI18N
    public static final String DEFAULT_ERROR_WRONG_VERSION_NEWER =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.error.wrong.version.newer"); // NOI18N
    public static final String DEFAULT_ERROR_UNKNOWN =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.error.unknown"); // NOI18N
    public static final String DEFAULT_ERROR_NOTHING_FOUND =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.error.nothing.found"); // NOI18N
    public static final String DEFAULT_MINIMUM_NETBEANS_VERSION =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.minimum.netbeans.version"); // NOI18N
    public static final String DEFAULT_MAXIMUM_NETBEANS_VERSION =
            ResourceUtils.getString(NetbeansLocationPanel.class,
            "NLP.maximum.netbeans.version"); // NOI18N

}
