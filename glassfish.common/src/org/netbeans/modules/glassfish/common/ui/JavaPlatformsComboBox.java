/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 * Combo box containing all Java SE platforms registered in NetBeans
 * or subset of them.
 * <p/>
 * @author Tomas Kraus
 */
public class JavaPlatformsComboBox extends JComboBox {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Encapsulate {@see JavaPlatform} object and provide human readable
     * <code>toString()</code> output for combo box.
     */
    public static class Platform {

        /** Java SE platform reference. */
        private final JavaPlatform platform;

        /**
         * Creates an instance of <code>Platform</code> object and sets provided
         * {@see JavaPlatform} reference.
         * <p/>
         * @param platform Java SE platform reference.
         */
        Platform(JavaPlatform platform) {
            this.platform = platform;
        }

        /**
         * Get Java SE platform reference.
         * <p/>
         * @return Java SE platform reference.
         */
        public JavaPlatform getPlatform() {
            return platform;
        }

        /**
         * Get {@see String} representation of this object.
         * <p/>
         * @return {@see String} representation of this object.
         */
        @Override
        public String toString() {
            return platform.getDisplayName();
        }
    }

    /**
     * Empty platform class to represent empty selection on the combo box.
     */
    private static class EmptyJavaPlatform extends JavaPlatform {

        /** Empty class path to be always returned. */
        private static final ClassPath EMPTY_CLASSPATH
                = ClassPathSupport.createClassPath(new URL[0]);

        /** Empty specification to be always returned. */
        private static final Specification EMPTY_SPECIFICATION
                = new Specification("j2se", new SpecificationVersion("0.0"));

        @Override
        public String getDisplayName() {
            return EMPTY_DISPLAY_NAME;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.EMPTY_MAP;
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return EMPTY_CLASSPATH;
        }

        @Override
        public ClassPath getStandardLibraries() {
            return EMPTY_CLASSPATH;
        }

        @Override
        public String getVendor() {
            return System.getProperty("java.vm.vendor");
        }

        @Override
        public Specification getSpecification() {
            return EMPTY_SPECIFICATION;
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public ClassPath getSourceFolders() {
            return EMPTY_CLASSPATH;
        }

        @Override
        public List<URL> getJavadocFolders() {
            return Collections.EMPTY_LIST;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Exception message for disabled constructors. */
    private static final String CONSTRUCTOR_EXCEPTION_MSG =
            "Data model for a combo box shall not be supplied in constructor.";

    /** Empty platform display name from properties. */
    public static final String EMPTY_DISPLAY_NAME = NbBundle.getMessage(
            JavaPlatformsComboBox.class,
            "JavaPlatformsComboBox.emptyDisplayName");
    
    /** Empty platform to represent empty selection on the combo box. */
    public static final Platform EMPTY_PLATFORM
            = new Platform(new EmptyJavaPlatform());

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert array of {@see JavaPlatform} objects to array of {@see Platform}
     * objects.
     * <p/>
     * 
     * <p/>
     * @param platformsIn An array of {@see JavaPlatform} objects
     *        to be converted.
     * @param addEmpty Add an empty platform representing no selection
     *                 at the beginning of the list.
     * @return An array of {@see Platform} objects containing
     *         <code>platformsIn</code>.
     */
    private static Platform[] toPlatform(JavaPlatform[] platformsIn,
            boolean addEmpty) {
        int size = platformsIn != null ? platformsIn.length : 0;
        Platform[] platformsOut = new Platform[addEmpty ? size + 1 : size];
        if (addEmpty) {
            platformsOut[0] = EMPTY_PLATFORM;
        }
        for(int i = 0; i < size; i++)
            platformsOut[addEmpty ? i + 1 : i] = new Platform(platformsIn[i]);
        return platformsOut;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see JavaPlatform}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(JavaPlatform[])} instead.
     * @param comboBoxModel Data model for this combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public JavaPlatformsComboBox(final ComboBoxModel comboBoxModel)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see JavaPlatform}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(JavaPlatform[])} instead.
     * @param items An array of objects to insert into the combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public JavaPlatformsComboBox(final Object items[])
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see JavaPlatform}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(JavaPlatform[])} instead.
     * @param items {@see Vector} of objects to insert into the combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public JavaPlatformsComboBox(final Vector<?> items)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Creates an instance of <code>JavaPlatformsComboBox</code> that contains
     * all Java SE platforms registered in NetBeans.
     * <p/>
     * @param addEmpty Add an empty platform representing no selection
     *                 at the beginning of the list.
     */
    public JavaPlatformsComboBox(boolean addEmpty) {
        super(new DefaultComboBoxModel(toPlatform(
                JavaPlatformManager.getDefault().getInstalledPlatforms(),
                addEmpty)));
    }

    /**
     * Creates an instance of <code>JavaPlatformsComboBox</code> that contains
     * supplied list of Java SE platforms.
     * <p/>
     * @param platforms Java SE platforms to be set as data model for combo box.
     * @param addEmpty Add an empty platform representing no selection
     *                 at the beginning of the list.
     */
    public JavaPlatformsComboBox(final JavaPlatform[] platforms,
            boolean addEmpty) {
        super(new DefaultComboBoxModel(toPlatform(platforms, addEmpty)));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Update content of data model to contain all Java SE platforms currently
     * registered in NetBeans
     * <p/>
     * @param addEmpty Add an empty platform representing no selection
     *                 at the beginning of the list.
     */
    public void updateModel(boolean addEmpty) {
        setModel(new DefaultComboBoxModel(toPlatform(
                JavaPlatformManager.getDefault().getInstalledPlatforms(),
                addEmpty)));
    }

    /**
     * Update content of data model to contain supplied list
     * of Java SE platforms.
     * <p/>
     * @param platforms Java SE platforms to be set as data model for combo box.
     * @param addEmpty Add an empty platform representing no selection
     *                 at the beginning of the list.
     */
    public void updateModel(final JavaPlatform[] platforms, boolean addEmpty) {
        setModel(new DefaultComboBoxModel(toPlatform(platforms, addEmpty)));
    }

    /**
     * Set selected item in the combo box display area to the provided Java SE
     * platform.
     * <p/>
     * @param platform Java SE platform to be set as selected. Empty platform
     *                 will be used when <code>null</code> value is supplied.
     */
    @Override
    public void setSelectedItem(Object platform) {
        if (platform == null)
            platform = EMPTY_PLATFORM.getPlatform();
        if (platform instanceof JavaPlatform) {
        int i, count = dataModel.getSize();
        for (i = 0; i < count; i++) {
            if (((JavaPlatform)platform).getDisplayName().equals((
                    (Platform)dataModel.getElementAt(i))
                    .getPlatform().getDisplayName())) {
                super.setSelectedItem(dataModel.getElementAt(i));
                break;
            }
        }
        } else {
            super.setSelectedItem(platform);
        }
    }

}
