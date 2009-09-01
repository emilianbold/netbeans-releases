/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform.loader;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesBasedDataObject;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.platform.BrokenJavacardPlatform;
import org.netbeans.modules.javacard.platform.JavacardPlatformImpl;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.*;
import java.io.*;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.BorderFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * This class is a workaround for a bit of complexity in the Java Platform
 * API.  The Java Platform API does not want an instance of JavaPlatform
 * registered in the layer;  it wants an instance of some DataObject that
 * has a JavaPlatform in its Node's Lookup registered in a layer.
 * <p>
 * The traditional way of doing this is a ferociously complex mess of
 * registering a DTD for a specific XML type, and writing sax parsers, etc.
 * to read and write what is in our case two or three properties.
 * <p>
 * Registering a file type has more overhead (?) than a DTD, but is
 * hundreds of lines less code.
 *
 * @author Tim Boudreau
 */
public class JavacardPlatformDataObject extends PropertiesBasedDataObject<JavacardPlatform> {

    private static final String ICON_BASE = "org/netbeans/modules/javacard/resources/ri.png"; //NOI18N

    public JavacardPlatformDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader, JavacardPlatform.class);
    }

    @Override
    protected Node createNodeDelegate() {
        return new ND(this);
    }

    @Override
    protected void onDelete(FileObject parentFolder) throws Exception {
        EditableProperties props = PropertyUtils.getGlobalProperties();
        props.remove(JCConstants.GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX + getName());
        props.remove(JCConstants.GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX + getName()
                + JCConstants.GLOBAL_PROPERTIES_DEVICE_FOLDER_PATH_KEY_SUFFIX);
        PropertyUtils.putGlobalProperties(props);
    }

    @Override
    protected JavacardPlatform createFrom(ObservableProperties properties) {
        String old = properties.getProperty(JavacardPlatformKeyNames.PLATFORM_ID);
        boolean idPropMatch = getName().equals(old);
        if (!idPropMatch) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, JavacardPlatformKeyNames.PLATFORM_ID
                        + " in " + getPrimaryFile().getPath() + " was set to " + old //NOI18N
                        + " - resetting to default, triggering save"); //NOI18N
            }
            //Ensure there is always a property from which the DataObject can
            //be looked up without having to iterate all platforms.
            properties.setProperty(JavacardPlatformKeyNames.PLATFORM_ID, getName());
        }
        if (properties.isEmpty()) {
            return new BrokenJavacardPlatform(getName());
        } else {
            JavacardPlatform result = new JavacardPlatformImpl(properties);
            return result;
        }
    }

    private static final class ND extends DataNode {

        ND(JavacardPlatformDataObject ob) {
            super(ob, Children.LEAF, ob.getLookup());
            setIconBaseWithExtension(ICON_BASE); //NOI18N
        }

        @Override
        public String getDisplayName() {
            JavacardPlatform platform = getLookup().lookup(JavacardPlatform.class);
            return platform == null ? super.getDisplayName() : platform.getDisplayName();
        }

        @Override
        public String getHtmlDisplayName() {
            JavacardPlatform platform = getLookup().lookup(JavacardPlatform.class);
            if (!platform.isValid()) {
                return "<font color='!nb.errorForeground'>" + //NOI18N
                        NbBundle.getMessage(JavacardPlatformDataObject.class,
                        "MSG_INVALID_PLATFORM", platform.getDisplayName()); //NOI18N
            }
            return null;
        }

        @Override
        public Action[] getActions (boolean ignored) {
            Action[] supers = super.getActions(ignored);
            Action[] result = new Action[supers.length + 1];
            System.arraycopy (supers, 0, result, 1, supers.length);
            result[0] = new AddDeviceAction();
            return result;
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set set = new Sheet.Set();
            set.setName (NbBundle.getMessage(JavacardPlatformDataObject.class,
                    "PROPERTY_SET_CLASSPATH")); //NOI18N

            set.put (new CPProp (ProjectKind.CLASSIC_APPLET));
            set.put (new CPProp (ProjectKind.EXTENDED_APPLET));

            JavacardPlatform pl = getLookup().lookup(JavacardPlatform.class);
            try {
                PropertySupport.Reflection<Boolean> isRiProp =
                    new PropertySupport.Reflection<Boolean>(pl, Boolean.class,
                    "isRI", null); //NOI18N
                isRiProp.setName ("isRI"); //NOI18N
                isRiProp.setDisplayName (NbBundle.getMessage(JavacardPlatformDataObject.class,
                        "PROP_IS_RI")); //NOI18N
                set.put (isRiProp);
                PropertySupport.Reflection<Boolean> isValid =
                    new PropertySupport.Reflection<Boolean>(pl, Boolean.class,
                    "isValid", null); //NOI18N
                isValid.setName ("isValid"); //NOI18N
                isValid.setDisplayName (NbBundle.getMessage(JavacardPlatformDataObject.class,
                        "PROP_IS_VALID")); //NOI18N
                set.put (isValid);
                PropertySupport.Reflection<String> kindProp =
                    new PropertySupport.Reflection<String>(pl, String.class,
                    "getPlatformKind", null); //NOI18N
                kindProp.setName ("kind");
                kindProp.setDisplayName (NbBundle.getMessage(JavacardPlatformDataObject.class,
                        "PROP_KIND")); //NOI18N
                set.put (kindProp);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            sheet.put (set);
            set.setDisplayName(set.getName());
            PropertiesBasedDataObject<?> ob = getLookup().lookup(PropertiesBasedDataObject.class);
            sheet.put(ob.getPropertiesAsPropertySet());
            return sheet;
        }

        private final class CPProp extends PropertySupport.ReadOnly<String> {
            private final ProjectKind kind;
            CPProp (ProjectKind kind) {
                super (propNameForKind(kind), String.class, propNameForKind(kind), null);
                this.kind = kind;
            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                JavacardPlatform p = getLookup().lookup(JavacardPlatform.class);
                ClassPath cp = p.getBootstrapLibraries(kind);
                StringBuilder sb = new StringBuilder();
                for (FileObject fo : cp.getRoots()) {
                    if (sb.length() > 0) {
                        sb.append (File.pathSeparator);
                    }
                    sb.append (fo.getPath());
                }
                return sb.toString();
            }
        }

        private static String propNameForKind(ProjectKind kind) {
            switch (kind) {
                case CLASSIC_APPLET :
                case CLASSIC_LIBRARY :
                    return NbBundle.getMessage(JavacardPlatformDataObject.class,
                            "PROP_NAME_CLASSIC_CLASSPATH"); //NOI18N
                default :
                    return NbBundle.getMessage(JavacardPlatformDataObject.class,
                            "PROP_NAME_EXT_CLASSPATH"); //NOI18N
            }
        }

        @Override
        public Image getIcon(int ignored) {
            return ImageUtilities.loadImage(ICON_BASE);
        }

        @Override
        public Image getOpenedIcon(int kind) {
            return getIcon(kind);
        }

        @Override
        public boolean hasCustomizer() {
            return true;
        }

        @Override
        public Component getCustomizer() {
            PlatformCustomizerPanel result = new
                    PlatformCustomizerPanel(getLookup().lookup(
                    JavacardPlatformDataObject.class));
            int i = (Utilities.getOperatingSystem() & Utilities.OS_MAC) == 0 ?
                12 : 5;
            result.setBorder (BorderFactory.createEmptyBorder(i,i,i,i));
            return result;
        }
    }
}
