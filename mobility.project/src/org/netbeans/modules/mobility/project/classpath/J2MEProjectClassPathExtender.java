/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.mobility.project.classpath;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;

/**
 * @author Adam Sotona
 **/
public class J2MEProjectClassPathExtender implements ProjectClassPathExtender {
    
    //ToDo - this condition map should be constructed dynamically from each library definition
    private static final Map<String, String> CONDITIONS = new HashMap(); 
    static {
        CONDITIONS.put("swing-layout", "javax/swing/AbstractCellEditor.class"); //NOI18N
        CONDITIONS.put("cdc-agui-swing-layout", "javax/swing/JComponent.class,-javax/swing/AbstractCellEditor.class"); //NOI18N
        CONDITIONS.put("cdc-pp-awt-layout", "java/awt/Component.class,-javax/swing/JComponent.class"); //NOI18N
        CONDITIONS.put("NetBeans MIDP Components", "javax/microedition/lcdui/Screen.class"); //NOI18N
        CONDITIONS.put("nb_svg_midp_components", "javax/microedition/m2g/SVGImage.class"); //NOI18N
        CONDITIONS.put("J2MEUnit", "javax/microedition/midlet/MIDlet.class"); //NOI18N
        CONDITIONS.put("JMUnit4CLDC10", "javax/microedition/midlet/MIDlet.class"); //NOI18N
        CONDITIONS.put("JMUnit4CLDC11", "javax/microedition/midlet/MIDlet.class,java/lang/Double.class"); //NOI18N
    }
    
    protected Project project;
    protected AntProjectHelper helper;
    protected ReferenceHelper refHelper;
    protected ProjectConfigurationsHelper confHelper;
    
    public J2MEProjectClassPathExtender(Project project, AntProjectHelper helper, ReferenceHelper refHelper, ProjectConfigurationsHelper confHelper) {
        this.project = project;
        this.helper = helper;
        this.refHelper = refHelper;
        this.confHelper = confHelper;
    }
    
    public boolean addLibrary(Library library) throws IOException {
        if (library == null) return false;
        boolean modified = false;

        //swing layout hack for CDC - try to add all possible swing layout libraries
        if ("swing-layout".equals(library.getName())) { //NOI18N
            modified = addLibrary(LibraryManager.getDefault().getLibrary("cdc-agui-swing-layout")) //NOI18N
                     | addLibrary(LibraryManager.getDefault().getLibrary("cdc-pp-awt-layout")); //NOI18N
        }
        
        return addCPItemToAllCfg(VisualClassPathItem.create(library), CONDITIONS.get(library.getName())) | modified;
    }
    
    public boolean addArchiveFile(FileObject archiveFile) throws IOException {
        assert archiveFile != null : "Parameter cannot be null";       //NOI18N
        final File f = FileUtil.toFile(archiveFile);
        if (f == null ) {
            throw new IllegalArgumentException("The file must exist on disk");     //NOI18N
        }
        return addCPItemToAllCfg(VisualClassPathItem.create(f), null);
    }

    public boolean addAntArtifact(final AntArtifact artifact, final URI artifactElement) throws IOException {
        assert artifact != null : "Parameter cannot be null";       //NOI18N
        return addCPItemToAllCfg(VisualClassPathItem.create(artifact, artifactElement), null);
    }
    
    private boolean addCPItemToAllCfg(final VisualClassPathItem item, final String condition) throws IOException {
        try {
            return (ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws Exception {
                    EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    boolean modified = false;
                    final boolean defaultFits = checkCondition(props, null, condition);
                    final String defaultRaw = props.getProperty(DefaultPropertiesDescriptor.LIBS_CLASSPATH);
                    if (defaultFits) {
                        List<VisualClassPathItem> resources = (List<VisualClassPathItem>)DefaultPropertyParsers.PATH_PARSER.decode(defaultRaw, helper, refHelper);
                        if (!resources.contains(item)) {
                            resources.add(item);
                            String itemRefs = DefaultPropertyParsers.PATH_PARSER.encode(resources, helper, refHelper);
                            props.setProperty(DefaultPropertiesDescriptor.LIBS_CLASSPATH, itemRefs);
                            modified = true;
                        }
                    }
                    for (ProjectConfiguration cfg : confHelper.getConfigurations().toArray(new ProjectConfiguration[0])) {
                        if (!confHelper.getDefaultConfiguration().equals(cfg)) { 
                            String propName = VisualPropertySupport.prefixPropertyName(cfg.getDisplayName(), DefaultPropertiesDescriptor.LIBS_CLASSPATH);
                            boolean fits = checkCondition(props, cfg, condition);
                            String raw = props.getProperty(propName);
                            if (raw == null && fits != defaultFits) {
                                raw = defaultRaw;
                                props.put(propName, raw);
                            }
                            if (fits && raw != null) {
                                List<VisualClassPathItem> resources = (List<VisualClassPathItem>)DefaultPropertyParsers.PATH_PARSER.decode(raw, helper, refHelper);
                                if (!resources.contains(item)) {
                                    resources.add(item);
                                    final String itemRefs = DefaultPropertyParsers.PATH_PARSER.encode(resources, helper, refHelper);
                                    props.setProperty(propName, itemRefs);
                                    modified = true;
                                }
                            }
                        }
                    }
                    if (modified) {
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        ProjectManager.getDefault().saveProject(project);
                    }
                    return Boolean.valueOf(modified);
                }
            }
            )).booleanValue();
        } catch (Exception e) {
                final Exception t = new IOException();
                throw (IOException) ErrorManager.getDefault().annotate(t,e);
        }
    }
    
    protected boolean checkCondition(EditableProperties props, ProjectConfiguration cfg, String condition) {
        if (condition == null) return true;
        String platformName = props.getProperty(cfg == null ? DefaultPropertiesDescriptor.PLATFORM_ACTIVE : VisualPropertySupport.prefixPropertyName(cfg.getDisplayName(), DefaultPropertiesDescriptor.PLATFORM_ACTIVE));
        if (platformName == null) platformName = props.getProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE);
        if (platformName == null) return false;
        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()){
            String antName = (String) platform.getProperties().get("platform.ant.name");        //NOI18N
            if (antName != null && antName.equals(platformName)) {
                ClassPath cp = platform.getBootstrapLibraries();
                boolean ret = true;
                for (String s : condition.split(",")) //NOI18N 
                    if (s.startsWith("-")) ret &= platform.getBootstrapLibraries().findResource(s.substring(1)) == null; //NOI18N
                    else ret &= platform.getBootstrapLibraries().findResource(s) != null;
                return ret;
            }
        }
        return true;
    }
}
