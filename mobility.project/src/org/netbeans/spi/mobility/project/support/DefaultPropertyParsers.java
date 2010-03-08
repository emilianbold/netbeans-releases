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
package org.netbeans.spi.mobility.project.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.mobility.project.PropertyParser;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class DefaultPropertyParsers {
    
    public static final PropertyParser STRING_PARSER = new StringParser();
    public static final PropertyParser BOOLEAN_PARSER = new BooleanParser();
    public static final PropertyParser INVERSE_BOOLEAN_PARSER = new InverseBooleanParser();
    public static final PropertyParser PATH_PARSER = new PathParser();
    public static final PropertyParser PLATFORM_PARSER = new PlatformParser();
    public static final PropertyParser INTEGER_PARSER = new IntegerParser();
    public static final PropertyParser MANIFEST_PROPERTY_PARSER = new ManifestPropertyParser();
    public static final PropertyParser FILE_REFERENCE_PARSER = new FileReferenceParser();
    public static final PropertyParser ABILITIES_PARSER = new AbilitiesParser();
    public static final PropertyParser DEPLOYMENT_TYPE_PARSER = new DeploymentTypeParser();
    public static final PropertyParser DEBUG_LEVEL_PARSER = new DebugLevelParser();
    
    
    
    
    private static class StringParser implements PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            return raw;
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return value.toString();
        }
        
    }
    
    private static class DebugLevelParser implements PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return raw == null || raw.trim().length() == 0 ? "debug" : raw.trim(); //NOI18N
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            String s = ((String)value).trim();
            return s.length() == 0 ? "debug" : s; //NOI18N
        }
        
    }
    
    private static class FileReferenceParser implements PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            if (raw == null || !raw.startsWith("${") || !raw.endsWith("}")) return raw;//NOI18N
            String ref = raw.substring(2, raw.length()-1);
            String val = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ref);
            if (val == null) val = antProjectHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(ref);
            if (val == null) return raw;
            File f = new File(val);
            return FileUtil.normalizeFile(f.isAbsolute() ? f : new File(FileUtil.toFile(antProjectHelper.getProjectDirectory()), val)).getAbsolutePath();
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            File f = new File((String)value);
            return refHelper.createForeignFileReference(FileUtil.normalizeFile(f.isAbsolute() ? f : new File(FileUtil.toFile(antProjectHelper.getProjectDirectory()), (String)value)), "anyfile"); //NOI18N
        }
        
    }
    
    private static class BooleanParser implements PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            if ( raw != null ) {
                String lowecaseRaw = raw.toLowerCase();
                if ("true".equals(lowecaseRaw ) ||  //NOI18N
                        "yes".equals(lowecaseRaw) ||  //NOI18N
                        "enabled".equals(lowecaseRaw) ) //NOI18N
                    return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        
        public String encode(Object value,  AntProjectHelper antProjectHelper,  ReferenceHelper refHelper ) {
            return value.toString();
        }
        
    }
    
    private static class IntegerParser implements PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            try {
                //#155005 - Integer.decode("00001") will throw an NFE.  Leaving
                //it here because maybe something out there actually needs this?
                return Integer.decode(raw);
            } catch (NumberFormatException e) {
                return Integer.parseInt(raw.trim());
            }
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return value.toString();
        }
        
    }
    
    private static class InverseBooleanParser extends BooleanParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return ((Boolean)super.decode( raw, antProjectHelper, refHelper )).booleanValue() ? Boolean.FALSE : Boolean.TRUE;
        }
        
        public String encode(Object value, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            return super.encode( ((Boolean)value).booleanValue() ? Boolean.FALSE : Boolean.TRUE, antProjectHelper, refHelper );
        }
        
    }
    
    private static class PathParser implements PropertyParser {
        
        // XXX Define in the LibraryManager
        private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
        private static final String ARTIFACT_PREFIX = "${reference.";
        
        // Contains well known paths in the J2MEProject
        private static final String[][] WELL_KNOWN_PATHS = new String[][] {
            { "libs.classpath", NbBundle.getMessage( J2MEProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) }, //NOI18N
        };
        
        
        public List<VisualClassPathItem> decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper ) {
            String pe[] = PropertyUtils.tokenizePath( raw );
            List<VisualClassPathItem> cpItems = new ArrayList<VisualClassPathItem>( pe.length );
            for( String pei:pe) {
                VisualClassPathItem cpItem = null;
                
                // First try to find out whether the item is well known classpath
                // in the J2ME project type
                int wellKnownPathIndex = -1;
                for( int j = 0; j < WELL_KNOWN_PATHS.length; j++ ) {
                    if ( WELL_KNOWN_PATHS[j][0].equals( getAntPropertyName( pei ) ) )  {
                        wellKnownPathIndex = j;
                        break;
                    }
                }
                
                if ( wellKnownPathIndex != - 1 ) {
                    cpItem = new VisualClassPathItem( pei, VisualClassPathItem.TYPE_CLASSPATH, pei, WELL_KNOWN_PATHS[wellKnownPathIndex][1] );
                } else if ( pei.startsWith( LIBRARY_PREFIX ) ) {
                    // Library from library manager
                    String eval = pei.substring( LIBRARY_PREFIX.length(), pei.lastIndexOf('.') ); //NOI18N
                    Library lib = LibraryManager.getDefault().getLibrary(eval);
                    String dName;
                    if (lib != null)
                        dName=lib.getDisplayName();
                    else
                    {
                        dName=eval;
                    }
                    cpItem = new VisualClassPathItem( lib, VisualClassPathItem.TYPE_LIBRARY, pei, dName );
                } else if (pei.startsWith(ARTIFACT_PREFIX)) {
                    Object o[] = refHelper.findArtifactAndLocation( pei );
                    File f = null;
                    if ( o[0] != null && o[1] != null ) {
                        // Sub project artifact
                        String eval = antProjectHelper.getStandardPropertyEvaluator().getProperty(getAntPropertyName(pei));
                        if (eval != null) {
                            f = FileUtil.normalizeFile(antProjectHelper.resolveFile(eval));
                        }
                    }
                    cpItem = new VisualClassPathItem((AntArtifact)o[0], (URI)o[1], VisualClassPathItem.TYPE_ARTIFACT, pei, f != null ? f.getPath() : pei.substring(ARTIFACT_PREFIX.length(), pei.lastIndexOf('.'))); //NOI18N
                } else {
                    // Standalone jar or property
                    String eval = antProjectHelper.getStandardPropertyEvaluator().getProperty(getAntPropertyName(pei));
                    File f = null;
                    if (eval != null) {
                        f = FileUtil.normalizeFile(antProjectHelper.resolveFile(eval));
                    }
                    String name = f != null ? f.getName() : getAntPropertyName(pei);
                    String displayName = f != null ? f.getPath() : name;
                    cpItem = new VisualClassPathItem(f, isJar(name) ? VisualClassPathItem.TYPE_JAR : VisualClassPathItem.TYPE_FOLDER, pei, displayName);
                }
                cpItems.add( cpItem );
            }
            
            return cpItems;
        }
        
        private boolean isJar(String s) {
            if (s == null) return false;
            s = s.toLowerCase();
            return s.endsWith(".jar") || s.endsWith(".zip"); //NOI18N
        }
        
        public String encode( Object value, AntProjectHelper helper, ReferenceHelper refHelper ) {
            Iterator<VisualClassPathItem> it = ((List<VisualClassPathItem>)value).iterator();
            StringBuffer sb = new StringBuffer();
            while (it.hasNext()) {
                VisualClassPathItem vcpi = it.next();
                
                if (vcpi.getElement() == null) {
                    if (vcpi.getRawText() != null) {
                        sb.append(vcpi.getRawText());
                        if (it.hasNext()) {
                            sb.append(File.pathSeparatorChar);
                        }
                    }
                    continue;
                }
                
                switch( vcpi.getType() ) {
                    case VisualClassPathItem.TYPE_JAR:
                    case VisualClassPathItem.TYPE_FOLDER:
                        File f = (File)vcpi.getElement();
                        String reference = refHelper.createForeignFileReference(f, JavaProjectConstants.ARTIFACT_TYPE_JAR);
                        sb.append(reference);
                        break;
                    case VisualClassPathItem.TYPE_LIBRARY:
                        sb.append(vcpi.getRawText()); //NOI18N
                        break;
                    case VisualClassPathItem.TYPE_ARTIFACT:
                        AntArtifact aa = (AntArtifact)vcpi.getElement();
                        URI u = vcpi.getURI();
                        reference = refHelper.addReference(aa, u);
                        sb.append(reference);
                        break;
                    case VisualClassPathItem.TYPE_CLASSPATH:
                        sb.append((String)vcpi.getElement());
                        break;
                }
                
                if ( it.hasNext() ) {
                    sb.append( File.pathSeparatorChar );
                }
            }
            return sb.toString();
        }
        
        static String getAntPropertyName( final String property ) {
            if ( property != null &&
                    property.startsWith( "${" ) && // NOI18N
                    property.endsWith( "}" ) ) { // NOI18N
                return property.substring( 2, property.length() - 1 );
            }
            return property;
        }
        
    }
    
    private static class PlatformParser implements PropertyParser {
        
        public Object decode(String raw,
                AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper) {
            
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            for( int i = 0; i < platforms.length; i++ ) {
                String normalizedName = (String)platforms[i].getProperties().get("platform.ant.name"); //NOI18N
                if ( normalizedName != null && normalizedName.equals( raw ) ) {
                    return platforms[i].getDisplayName();
                }
            }
            
            return JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName();
        }
        
        
        public String encode(Object value,
                AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms((String)value, null);
            if (platforms.length == 0) {
                return null;
            }
            return (String) platforms[0].getProperties().get("platform.ant.name");  //NOI18N
        }
        
    }
    
    private static class DeploymentTypeParser implements PropertyParser {
        
        public Object decode(String raw,
                AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper) {
            Iterator it = Lookup.getDefault().lookup(new Lookup.Template<DeploymentPlugin>(DeploymentPlugin.class)).allInstances().iterator();
            while (it.hasNext()) {
                DeploymentPlugin p = (DeploymentPlugin)it.next();
                if (p.getDeploymentMethodName().equals(raw))
                    return p.getDeploymentMethodDisplayName();
            }
            return "";//NOI18N
        }
        
        
        public String encode(Object value,
                AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper) {
            Iterator it = Lookup.getDefault().lookup(new Lookup.Template<DeploymentPlugin>(DeploymentPlugin.class)).allInstances().iterator();
            while (it.hasNext()) {
                DeploymentPlugin p = (DeploymentPlugin)it.next();
                if (p.getDeploymentMethodDisplayName().equals(value))
                    return p.getDeploymentMethodName();
            }
            return null;//NOI18N
        }
        
    }
    
    private static class ManifestPropertyParser implements PropertyParser {
        
        public Object decode(String raw, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            try {
                if (raw == null)
                    return null;
                BufferedReader br = new BufferedReader(new StringReader(raw));
                HashMap<String,String> map = new HashMap<String,String>();
                for (;;) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    int i = line.indexOf(':');
                    if (i < 0)
                        continue;
                    map.put(line.substring(0, i), line.substring(i + 1).trim());
                }
                return map;
            } catch (IOException ioe) {
                assert false: ioe;
                return null;
            }
        }
        
        public String encode(Object val, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
            HashMap<String,String> map = (HashMap<String,String>)val;
            if (map == null) return null;
            StringBuffer buffer = new StringBuffer();
            for (String key:map.keySet()) {
                if (key == null)
                    continue;
                String value = map.get(key);
                if (value == null)
                    continue;
                buffer.append(key).append(": ").append(value).append('\n'); //NOI18N
            }
            return buffer.toString();
        }
    }
    
    private static class AbilitiesParser implements PropertyParser { //XXX escape/unescape equal and comma
        public Object decode(String raw,
                AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper) {
            return CommentingPreProcessor.decodeAbilitiesMap(raw);
        }
        
        public String encode(Object value,
                AntProjectHelper antProjectHelper,
                ReferenceHelper refHelper) {
            return CommentingPreProcessor.encodeAbilitiesMap((Map<String,String>)value);
        }
    }
    
}
