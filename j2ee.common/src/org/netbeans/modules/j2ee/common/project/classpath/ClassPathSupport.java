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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.common.project.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hrebejk, Radko Najman, David Konecny
 */
public final class ClassPathSupport {
                
    // Prefixes and suffixes of classpath
    private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    private static final String LIBRARY_SUFFIX = ".classpath}"; // NOI18N

    private PropertyEvaluator evaluator;
    private ReferenceHelper referenceHelper;
    private AntProjectHelper antProjectHelper;
    private UpdateHelper updateHelper;
    private static Set<String> wellKnownPaths = new HashSet<String>(Arrays.asList(ProjectProperties.WELL_KNOWN_PATHS));
    private static String antArtifactPrefix = ProjectProperties.ANT_ARTIFACT_PREFIX;
        
    private Callback callback;

    /** Creates a new instance of ClassPathSupport */
    public ClassPathSupport( PropertyEvaluator evaluator, 
                              ReferenceHelper referenceHelper,
                              AntProjectHelper antProjectHelper,
                              UpdateHelper updateHelper,
                              Callback callback) {
        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
        this.updateHelper = updateHelper;
        this.callback = callback;
    }

    /** Creates list of <CODE>Items</CODE> from given property.
     */    
    public Iterator<Item> itemsIterator(String propertyValue) {
        return itemsIterator(propertyValue, null);
    }
    
    public Iterator<Item> itemsIterator( String propertyValue, String webModuleLibraries ) {
        // XXX More performance frendly impl. would retrun a lazzy iterator.
        return itemsList( propertyValue, webModuleLibraries ).iterator();
    }
    
    public List<Item> itemsList(String propertyValue) {
        return itemsList(propertyValue, null);
    }
    
    public List<Item> itemsList( String propertyValue, String projectXMLElement ) {    
        // Get the list of items which are included in deployment
        //Map warMap = ( webModuleLibraries != null ) ? callback.createWarIncludesMap( antProjectHelper, webModuleLibraries) : new LinkedHashMap();
        
        String pe[] = PropertyUtils.tokenizePath( propertyValue == null ? "": propertyValue ); // NOI18N        
        List items = new ArrayList( pe.length );        
        for( int i = 0; i < pe.length; i++ ) {
            //String property = ProjectProperties.getAntPropertyName( pe[i] );
            
            Item item;

            // First try to find out whether the item is well known classpath
            if ( isWellKnownPath( pe[i] ) ) {
                // Some well know classpath
                item = Item.create( pe[i]);
            } 
            else if ( isLibrary( pe[i] ) ) {
                //Library from library manager
                String libraryName = getLibraryNameFromReference(pe[i]);
                assert libraryName != null : "Not a library reference: "+pe[i];
                Library library = referenceHelper.findLibrary(libraryName);
                if ( library == null ) {
                    item = Item.createBroken( Item.TYPE_LIBRARY, pe[i]);
                }
                else {
                    item = Item.create( library, pe[i]);
                }
            } 
            else if ( isAntArtifact( pe[i] ) ) {
                // Ant artifact from another project
                Object[] ret = referenceHelper.findArtifactAndLocation(pe[i]);
                if ( ret[0] == null || ret[1] == null ) {
                    item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i]);
                }
                else {
                    //item = Item.create( (AntArtifact)ret[0], (URI)ret[1], pe[i], (String) warMap.get(property));
                    //fix of issue #55368
                    AntArtifact artifact = (AntArtifact)ret[0];
                    URI uri = (URI)ret[1];
                    File usedFile = antProjectHelper.resolveFile(evaluator.evaluate(pe[i]));
                    File artifactFile = new File (artifact.getScriptLocation().toURI().resolve(uri).normalize());
                    if (usedFile.equals(artifactFile)) {
                        item = Item.create( artifact, uri, pe[i]);
                    }
                    else {
                        item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i]);
                    }
                }
            } else {
                // Standalone jar or property
                String eval = evaluator.evaluate( pe[i] );
                File f = null;
                if (eval != null) {
                    f = antProjectHelper.resolveFile( eval );
                }                    
                
                if ( f == null || !f.exists() ) {
                    item = Item.createBroken( eval, FileUtil.toFile(antProjectHelper.getProjectDirectory()), pe[i]);
                }
                else {
                    item = Item.create( eval, FileUtil.toFile(antProjectHelper.getProjectDirectory()), pe[i]);
                }
                
                //TODO these should be encapsulated in the Item class 
                // but that means we need to pass evaluator and antProjectHelper there.
                String ref = item.getSourceReference();
                eval = evaluator.evaluate( ref );
                ref = item.getJavadocReference();
                String eval2 = evaluator.evaluate( ref );
                item.setInitialSourceAndJavadoc(eval, eval2);
            }
            
            items.add( item );
        }
        if (projectXMLElement != null) {
            callback.readAdditionalProperties(items, projectXMLElement);
        }

        return items;        
    }
    
    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     * !! This method may add <included-library> items to project.xml !!
     */
    public String[] encodeToStrings(List<Item> classpath) {
        return encodeToStrings(classpath, null);
    }
    
    public String[] encodeToStrings( List<Item> classpath, String projectXMLElement ) {
        List<String> items = new ArrayList<String>();
        for (Item item : classpath) {
            String reference = null;
            
            switch( item.getType() ) {

                case Item.TYPE_JAR:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }
                    if (reference == null) {
                        // pass null as expected artifact type to always get file reference
                        reference = referenceHelper.createForeignFileReferenceAsIs(item.getFilePath(), null);
                        item.setReference(reference);
                    }
                    if (item.hasChangedSource()) {
                        if (item.getSourceFilePath() != null) {
                            referenceHelper.createExtraForeignFileReferenceAsIs(item.getSourceFilePath(), item.getSourceProperty());
                        } else {
                            //oh well, how do I do this otherwise??
                            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            ep.remove(item.getSourceProperty());
                            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                        }
                    }
                    if (item.hasChangedJavadoc()) {
                        if (item.getJavadocFilePath() != null) {
                            referenceHelper.createExtraForeignFileReferenceAsIs(item.getJavadocFilePath(), item.getJavadocProperty());
                        } else {
                            //oh well, how do I do this otherwise??
                            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            ep.remove(item.getJavadocProperty());
                            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                        }
                    }
                    break;
                case Item.TYPE_LIBRARY:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }                    
                    Library library = item.getLibrary();                                       
                    if (reference == null) {
                        if ( library == null ) {
                            break;
                        }
                        reference = getLibraryReference( item );
                        item.setReference(reference);
                    }
                    break;    
                case Item.TYPE_ARTIFACT:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }
                    AntArtifact artifact = item.getArtifact();                                       
                    if ( reference == null) {
                        if ( artifact == null ) {
                            break;
                        }
                        reference = referenceHelper.addReference( item.getArtifact(), item.getArtifactURI());
                        item.setReference(reference);
                    }
                    break;
                case Item.TYPE_CLASSPATH:
                    reference = item.getReference();
                    break;
            }
            items.add(reference + ":");
        }

        if ( projectXMLElement != null ) {
            callback.storeAdditionalProperties(classpath, projectXMLElement );
        }
        String arr[] = items.toArray(new String[items.size()]);
        // remove ":" from last item:
        if (arr.length != 0) {
            arr[arr.length-1] = arr[arr.length-1].substring(0, arr[arr.length-1].length()-1);
        }
        return arr;
    }
    
    public void updateJarReference(Item item) {
        String eval = evaluator.evaluate( item.getReference() );

        item.object = eval;

        //TODO these should be encapsulated in the Item class 
        // but that means we need to pass evaluator and antProjectHelper there.
        String ref = item.getSourceReference();
        eval = evaluator.evaluate( ref );
        if (eval != null && !eval.contains(Item.SOURCE_START)) {
            item.setSourceFilePath(eval);
        }
        ref = item.getJavadocReference();
        eval = evaluator.evaluate( ref );
        File f2 = null;
        if (eval != null && !eval.contains(Item.JAVADOC_START)) {
            item.setJavadocFilePath(eval);
        }
        
    }
    
    public String getLibraryReference( Item item ) {
        if ( item.getType() != Item.TYPE_LIBRARY ) {
            throw new IllegalArgumentException( "Item must be of type LIBRARY" );
        }
        return referenceHelper.createLibraryReference(item.getLibrary(), "classpath"); // NOI18N
    }
    
    // Private methods ---------------------------------------------------------

    private boolean isWellKnownPath( String property ) {
        return wellKnownPaths == null ? false : wellKnownPaths.contains( property );
    }
    
    private boolean isAntArtifact( String property ) {        
        return antArtifactPrefix == null ? false : property.startsWith( antArtifactPrefix );
    }
    
    private static boolean isLibrary( String property ) {
        return property.startsWith(LIBRARY_PREFIX) && property.endsWith(LIBRARY_SUFFIX);
    }
        
    // Innerclasses ------------------------------------------------------------
    
    /** Item of the classpath.
     */    
    public static class Item {
        
        // Types of the classpath elements
        public static final int TYPE_JAR = 0;
        public static final int TYPE_LIBRARY = 1;
        public static final int TYPE_ARTIFACT = 2;
        public static final int TYPE_CLASSPATH = 3;

        private static final String REF_START = "${file.reference."; //NOI18N
        private static final int REF_START_INDEX = REF_START.length();
        private static final String JAVADOC_START = "${javadoc.reference."; //NOI18N
        private static final String SOURCE_START = "${source.reference."; //NOI18N
        
        private Object object;
        private URI artifactURI;
        private int type;
        private String property;
        private String raw;
        private String eval;
        private boolean broken;
        private String sourceFilePath;
        private String javadocFilePath;
        
        private String initialSourceFilePath;
        private String initialJavadocFilePath;
        private String libraryName;
        
        private Map<String, String> additionalProperties = new HashMap<String, String>();

        private Item( int type, Object object, String raw, String eval, String property, boolean broken) {
            this.type = type;
            this.object = object;
            this.broken = broken;
            if (object == null || type == TYPE_CLASSPATH || broken ||
                    (type == TYPE_JAR && object instanceof RelativePath) ||
                    (type == TYPE_ARTIFACT && (object instanceof AntArtifact)) ||
                    (type == TYPE_LIBRARY && (object instanceof Library))) {
                this.raw = raw;
                this.eval = eval;
                this.property = property;
            } else {
                throw new IllegalArgumentException ("invalid classpath item, type=" + type + " object type:" + object.getClass().getName());
            }
        }
        
        private Item( int type, Object object, String raw, String eval, URI artifactURI, String property) {
            this( type, object, raw, eval, property);
            this.artifactURI = artifactURI;
        }
        
        private Item(int type, Object object, String raw, String eval, String property) {
            this(type, object, raw, eval, property, false);
        }
              
        public String getAdditionalProperty(String key) {
            return additionalProperties.get(key);
        }
        
        public void setAdditionalProperty(String key, String value) {
            additionalProperties.put(key, value);
        }
        
        public void setRaw(String raw) {
            this.raw = raw;
        }

        public String getRaw() {
            return raw;
        }
        
        // Factory methods -----------------------------------------------------
        
        
        public static Item create( Library library, String property) {
            if ( library == null ) {
                throw new IllegalArgumentException( "library must not be null" ); // NOI18N
            }
                        
            String libraryName = library.getName();
            Item itm = new Item( TYPE_LIBRARY, library, "${libs."+libraryName+".classpath}", libraryName, property); //NOI18N
            itm.libraryName = libraryName;
            itm.reassignLibraryManager( library.getManager() );
            return itm;
        }
        
        public static Item create( AntArtifact artifact, URI artifactURI, String property) {
            if ( artifactURI == null ) {
                throw new IllegalArgumentException( "artifactURI must not be null" ); // NOI18N
            }
            if ( artifact == null ) {
                throw new IllegalArgumentException( "artifact must not be null" ); // NOI18N
            }
            return new Item( TYPE_ARTIFACT, artifact, null, artifact.getArtifactLocations()[0].toString(), artifactURI, property);
        }
        
        public static Item create( String filePath, File base, String property) {
            if ( filePath == null ) {
                throw new IllegalArgumentException( "file path must not be null" ); // NOI18N
            }
            return new Item( TYPE_JAR, new RelativePath(filePath, base), null, filePath, property);
        }
        
        public static Item create( String property) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null" ); // NOI18N
            }
            return new Item ( TYPE_CLASSPATH, null, null, null, property);
        }
        
        public static Item createBroken( int type, String property) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null in broken items" ); // NOI18N
            }
            Item itm = new Item( type, null, null, null, property, true);
            if (type == TYPE_LIBRARY) {
                Pattern LIBRARY_REFERENCE = Pattern.compile("\\$\\{libs\\.([a-zA-Z0-9_\\-\\.]+)\\.([^.]+)\\}"); // NOI18N
                Matcher m = LIBRARY_REFERENCE.matcher(property);
                if (m.matches()) {
                    itm.libraryName = m.group(1);
                } else {
                    assert false : property;
                }
            }
            return itm;
        }
        
        public static Item createBroken(String filePath, File base, String property) {
            return new Item(TYPE_JAR, new RelativePath(filePath, base), null, null, property, true);
        }
        
        // Instance methods ----------------------------------------------------
        
        public int getType() {
            return type;
        }
        
        public Library getLibrary() {
            if ( getType() != TYPE_LIBRARY ) {
                throw new IllegalArgumentException( "Item is not of required type - LIBRARY" ); // NOI18N
            }
            if (isBroken()) {
                return null;
            }
            assert object == null || object instanceof Library :
                "Invalid object type: "+object.getClass().getName()+" instance: "+object.toString()+" expected type: Library";   //NOI18N
            return (Library)object;
        }
        
        public File getResolvedFile() {
            if ( getType() != TYPE_JAR ) {
                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
            }
            // for broken item: one will get java.io.File or null (#113390)
            return ((RelativePath)object).getResolvedFile();
        }
        
        public String getFilePath() {
            if ( getType() != TYPE_JAR ) {
                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
            }
            // for broken item: one will get java.io.File or null (#113390)
            return ((RelativePath)object).getFilePath();
        }
        
        public AntArtifact getArtifact() {
            if ( getType() != TYPE_ARTIFACT ) {
                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
            }
            if (isBroken()) {
                return null;
            }
            return (AntArtifact)object;
        }
        
        public URI getArtifactURI() {
            if ( getType() != TYPE_ARTIFACT ) {
                throw new IllegalArgumentException( "Item is not of required type - ARTIFACT" ); // NOI18N
            }
            return artifactURI;
        }
        
        public void reassignLibraryManager(LibraryManager newManager) {
            if (getType() != TYPE_LIBRARY) {
                throw new IllegalArgumentException(" reassigning only works for type - LIBRARY");
            }
            assert libraryName != null;
            if (getLibrary() == null || newManager != getLibrary().getManager()) {
                Library lib = newManager.getLibrary(libraryName);
                if (lib == null) {
                    broken = true;
                    object = null;
                } else {
                    object = lib;
                    broken = false;
                }
            }
        }
        
        public Object getObject() {
            return object;
        }

        public boolean canDelete() {
            return getType() != TYPE_CLASSPATH;
        }

        public String getReference() {
            return property;
        }
        
        public void setReference(String property) {
            this.property = property;
        }

        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        public String getSourceReference() {
            return property != null ? (SOURCE_START + property.substring(REF_START_INDEX)) : property;
        }
        
        public String getSourceProperty() {
            return property != null ? (getSourceReference().substring(2, getSourceReference().length() - 1)) : null;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        public String getJavadocReference() {
            return property != null ? (JAVADOC_START + property.substring(REF_START_INDEX)) : property;
        }
        
        public String getJavadocProperty() {
            return property != null ? (getJavadocReference().substring(2, getJavadocReference().length() - 1)) : null;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        public String getSourceFilePath() {
            return sourceFilePath;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        public String getJavadocFilePath() {
            return javadocFilePath;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        public void setJavadocFilePath(String javadoc) {
            javadocFilePath = javadoc;
        }
        
        /**
         * only applicable to TYPE_JAR
         * 
         * @return
         */
        public void setSourceFilePath(String source) {
            sourceFilePath = source;
        }
        
        public void setInitialSourceAndJavadoc(String source, String javadoc) {
            initialSourceFilePath = source;
            initialJavadocFilePath = javadoc;
            sourceFilePath = source;
            javadocFilePath = javadoc;
        }
        
        public boolean hasChangedSource() {
            if ((initialSourceFilePath == null) != (sourceFilePath == null)) {
                return true;
            }
            if (initialSourceFilePath != null && sourceFilePath != null) {
                return ! initialSourceFilePath.equals(sourceFilePath);
            }
            return true;
        }
        
        public boolean hasChangedJavadoc() {
            if ((initialJavadocFilePath == null) != (javadocFilePath == null)) {
                return true;
            }
            if (initialJavadocFilePath != null && javadocFilePath != null) {
                return ! initialJavadocFilePath.equals(javadocFilePath);
            }
            return true;
        }
        
        public boolean isBroken() {
            return broken;
        }
                        
        @Override
        public int hashCode() {
        
            int hash = getType();

            if (broken) {
                return 42;
            }
            
            switch ( getType() ) {
                case TYPE_ARTIFACT:
                    hash += getArtifact().getType().hashCode();                
                    hash += getArtifact().getScriptLocation().hashCode();
                    hash += getArtifactURI().hashCode();
                    break;
                case TYPE_CLASSPATH:
                    hash += property.hashCode();
                    break;
                default:
                    hash += object.hashCode();
            }

            return hash;
        }
    
        @Override
        public boolean equals( Object itemObject ) {

            if ( !( itemObject instanceof Item ) ) {
                return false;
            }
            
            Item item = (Item)itemObject;

            if ( getType() != item.getType() ) {
                return false;
            }
            
            if ( isBroken() != item.isBroken() ) {
                return false;
            }
            
            if ( isBroken() ) {
                return getReference().equals( item.getReference() );
            }

            switch ( getType() ) {
                case TYPE_ARTIFACT:
                    if (!(getArtifact().getType()).equals(item.getArtifact().getType())) {
                        return false;
                    }

                    if ( !getArtifact().getScriptLocation().equals( item.getArtifact().getScriptLocation() ) ) {
                        return false;
                    }

                    if ( !getArtifactURI().equals( item.getArtifactURI() ) ) {
                        return false;
                    }
                    return true;
                case TYPE_CLASSPATH:
                    return property.equals( item.property );
                default:
                    return object.equals( item.object );
            }

        }
     
        @Override
        public String toString() {
            return "artifactURI=" + artifactURI
                    + ", type=" + type 
                    + ", property=" + property
                    + ", raw=" + raw
                    + ", eval=" + eval
                    + ", object=" + object
                    + ", broken=" + broken
                    + ", additional=" + additionalProperties;
        }
        
    }
            
    /**
     * Returns library name if given property represents library reference 
     * otherwise return null.
     * 
     * @param property property to test
     * @return library name or null
     */
    public static String getLibraryNameFromReference(String property) {
        if (!isLibrary(property)) {
            return null;
        }
        return property.substring(LIBRARY_PREFIX.length(), property.lastIndexOf('.')); //NOI18N
    }
    
    private static final class RelativePath {
        private final String filePath;
        private final File base;
        private final File resolvedFile;

        public RelativePath(String filePath, File base) {
            Parameters.notNull("filePath", filePath);
            Parameters.notNull("base", base);
            this.filePath = filePath;
            this.base = base;
            resolvedFile = PropertyUtils.resolveFile(base, filePath);
        }

        public File getBase() {
            return base;
        }

        public String getFilePath() {
            return filePath;
        }

        public File getResolvedFile() {
            return resolvedFile;
        }

        @Override
        public int hashCode() {
            return filePath.hashCode() + base.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RelativePath)) {
                return false;
            }
            RelativePath other = (RelativePath)obj;
            return filePath.equals(other.filePath) && base.equals(other.base);
        }
        
    }
    
    /**
     * Callback to customize classpath support behaviour.
     */
    public static interface Callback {
        
        /**
         * Reads additional information from project XML for classpath items.
         */
        void readAdditionalProperties(List<Item> items, String projectXMLElement);
        
        /**
         * Writes additional information from classpath items to project XML.
         */
        void storeAdditionalProperties(List<Item> items, String projectXMLElement);
        
        /**
         * Initializes additional information to a default state.
         */
        void initAdditionalProperties(Item item);
    }
    
}
