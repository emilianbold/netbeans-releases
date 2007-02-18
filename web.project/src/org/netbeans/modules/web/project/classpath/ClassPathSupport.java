/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.netbeans.modules.web.project.WebProjectType;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;

/**
 *
 * @author Petr Hrebejk, Radko Najman
 */
public class ClassPathSupport {
                
    private PropertyEvaluator evaluator;
    private ReferenceHelper referenceHelper;
    private AntProjectHelper antProjectHelper;
    private Set /*<String>*/ wellKnownPaths;
    private String libraryPrefix;
    private String librarySuffix;
    private String antArtifactPrefix;
        
    public final static String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public final static String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; // NOI18N

    /** Creates a new instance of ClassPathSupport */
    public ClassPathSupport( PropertyEvaluator evaluator, 
                              ReferenceHelper referenceHelper,
                              AntProjectHelper antProjectHelper,
                              String wellKnownPaths[],
                              String libraryPrefix,
                              String librarySuffix,
                              String antArtifactPrefix) {
        this.evaluator = evaluator;
        this.referenceHelper = referenceHelper;
        this.antProjectHelper = antProjectHelper;
        this.wellKnownPaths = wellKnownPaths == null ? null : new HashSet( Arrays.asList( wellKnownPaths ) );
        this.libraryPrefix = libraryPrefix;
        this.librarySuffix = librarySuffix;
        this.antArtifactPrefix = antArtifactPrefix;
    }

    /** Creates list of <CODE>Items</CODE> from given property.
     */    
    public Iterator /*<Item>*/ itemsIterator( String propertyValue, String webModuleLibraries ) {
        // XXX More performance frendly impl. would retrun a lazzy iterator.
        return itemsList( propertyValue, webModuleLibraries ).iterator();
    }
    
    public List /*<Item>*/ itemsList( String propertyValue, String webModuleLibraries ) {    
        
        // Get the list of items which are included in deployment
        Map warMap = ( webModuleLibraries != null ) ? createWarIncludesMap( antProjectHelper, webModuleLibraries) : new LinkedHashMap();
        
        String pe[] = PropertyUtils.tokenizePath( propertyValue == null ? "": propertyValue ); // NOI18N        
        List items = new ArrayList( pe.length );        
        for( int i = 0; i < pe.length; i++ ) {
            String property = WebProjectProperties.getAntPropertyName( pe[i] );
            
            Item item;

            // First try to find out whether the item is well known classpath
            if ( isWellKnownPath( pe[i] ) ) {
                // Some well know classpath
                item = Item.create( pe[i], Item.PATH_IN_WAR_NONE);
            } 
            else if ( isLibrary( pe[i] ) ) {
                //Library from library manager
                String libraryName = pe[i].substring( libraryPrefix.length(), pe[i].lastIndexOf('.') ); //NOI18N
                Library library = LibraryManager.getDefault().getLibrary( libraryName );
                if ( library == null ) {
                    item = Item.createBroken( Item.TYPE_LIBRARY, pe[i], (String) warMap.get(property));
                }
                else {
                    item = Item.create( library, pe[i], (String) warMap.get(property));
                }
            } 
            else if ( isAntArtifact( pe[i] ) ) {
                // Ant artifact from another project
                Object[] ret = referenceHelper.findArtifactAndLocation(pe[i]);
                if ( ret[0] == null || ret[1] == null ) {
                    item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i], (String) warMap.get(property));
                }
                else {
                    //item = Item.create( (AntArtifact)ret[0], (URI)ret[1], pe[i], (String) warMap.get(property));
                    //fix of issue #55368
                    AntArtifact artifact = (AntArtifact)ret[0];
                    URI uri = (URI)ret[1];
                    File usedFile = antProjectHelper.resolveFile(evaluator.evaluate(pe[i]));
                    File artifactFile = new File (artifact.getScriptLocation().toURI().resolve(uri).normalize());
                    if (usedFile.equals(artifactFile)) {
                        item = Item.create( artifact, uri, pe[i], (String) warMap.get(property) );
                    }
                    else {
                        item = Item.createBroken( Item.TYPE_ARTIFACT, pe[i], (String) warMap.get(property) );
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
                    item = Item.createBroken( Item.TYPE_JAR, pe[i], (String) warMap.get(property));
                }
                else {
                    item = Item.create( f, pe[i], (String) warMap.get(property));
                }
            }
            
            items.add( item );
        }

        return items;        
    }
    
    /** Converts list of classpath items into array of Strings.
     * !! This method creates references in the project !!
     * !! This method may add <included-library> items to project.xml !!
     */
    public String[] encodeToStrings( Iterator /*<Item>*/ classpath, String webModuleLibraries ) {
        
        ArrayList result = new ArrayList();
        ArrayList includedLibraries = new ArrayList();
        
        List cp = new LinkedList();
        
        while( classpath.hasNext() ) {

            Item item = (Item)classpath.next();
            cp.add(item);
            String reference = null;
            
            switch( item.getType() ) {

                case Item.TYPE_JAR:
                    reference = item.getReference();
                    if ( item.isBroken() ) {
                        break;
                    }
                    if (reference == null) {
                        // New file
                        File file = item.getFile();
                        // pass null as expected artifact type to always get file reference
                        reference = referenceHelper.createForeignFileReference(file, null);
                        item.setReference(reference);
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
                    AntArtifact artifact = (AntArtifact)item.getArtifact();                                       
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
            
            if ( reference != null ) {
                result.add( reference );

                if (webModuleLibraries != null) {
                    includedLibraries.add( WebProjectProperties.getAntPropertyName( reference ) );
                }
            }

        }

        if ( webModuleLibraries != null )
            putIncludedLibraries( includedLibraries, cp, antProjectHelper, webModuleLibraries );
        
        String[] items = new String[ result.size() ];
        for( int i = 0; i < result.size(); i++) {
            if ( i < result.size() - 1 ) {
                items[i] = result.get( i ) + ":";
            }
            else  {       
                items[i] = (String)result.get( i );    //NOI18N
            }
        }
        
        return items;
    }
    
    public String getLibraryReference( Item item ) {
        if ( item.getType() != Item.TYPE_LIBRARY ) {
            throw new IllegalArgumentException( "Item must be of type LIBRARY" );
        }
        return libraryPrefix + item.getLibrary().getName() + librarySuffix;        
    }
    
    // Private methods ---------------------------------------------------------

    private boolean isWellKnownPath( String property ) {
        return wellKnownPaths == null ? false : wellKnownPaths.contains( property );
    }
    
    private boolean isAntArtifact( String property ) {        
        return antArtifactPrefix == null ? false : property.startsWith( antArtifactPrefix );
    }
    
    private boolean isLibrary( String property ) {
        if ( libraryPrefix != null && property.startsWith( libraryPrefix ) ) {
            return librarySuffix == null ? true : property.endsWith( librarySuffix );
        }
        else {
            return false;
        }
        
    }
        
    // Private static methods --------------------------------------------------

    private static final String TAG_PATH_IN_WAR = "path-in-war"; //NOI18N
    private static final String TAG_FILE = "file"; //NOI18N
    private static final String TAG_LIBRARY = "library"; //NOI18N
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N

    private static Map createWarIncludesMap(AntProjectHelper uh, String webModuleLibraries) {
        Map warIncludesMap = new LinkedHashMap();
        //try all supported namespaces starting with the newest one
        for(int idx = WebProjectType.PROJECT_CONFIGURATION_NAMESPACE_LIST.length - 1; idx >= 0; idx--) {
            String ns = WebProjectType.PROJECT_CONFIGURATION_NAMESPACE_LIST[idx];
            Element data = uh.createAuxiliaryConfiguration().getConfigurationFragment("data",ns,true);
            if(data != null) {
                Element webModuleLibs = (Element) data.getElementsByTagNameNS(ns, webModuleLibraries).item(0);
                if (webModuleLibs != null) {
                    NodeList ch = webModuleLibs.getChildNodes();
                    for (int i = 0; i < ch.getLength(); i++) {
                        if (ch.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element library = (Element) ch.item(i);
                            Node webFile = library.getElementsByTagNameNS(ns, TAG_FILE).item(0);
                            NodeList pathInWarElements = library.getElementsByTagNameNS(ns, TAG_PATH_IN_WAR);
                            //remove ${ and } from the beginning and end
                            String webFileText = findText(webFile);
                            webFileText = webFileText.substring(2, webFileText.length() - 1);
                            
                            //#86522
                            if (webModuleLibraries.equals(TAG_WEB_MODULE__ADDITIONAL_LIBRARIES)) {
                                String pathInWar = Item.PATH_IN_WAR_NONE;
                                if (pathInWarElements.getLength() > 0) {
                                    pathInWar = findText((Element) pathInWarElements.item(0));
                                    if (pathInWar == null)
                                        pathInWar = Item.PATH_IN_WAR_APPLET;
                                }
                                warIncludesMap.put(webFileText, pathInWar);
                            } else
                                warIncludesMap.put(webFileText, pathInWarElements.getLength() > 0 ? findText((Element) pathInWarElements.item(0)) : Item.PATH_IN_WAR_NONE);
                        }
                    }
                    return warIncludesMap;
                }
            }
        }
        return warIncludesMap; //return an empy map
    }

    /**
     * Updates the project helper with the list of classpath items which are to be
     * included in deployment.
     */
    private static void putIncludedLibraries( List /*<String>*/ libraries, List /*<Item>*/ classpath, AntProjectHelper antProjectHelper, String webModuleLibraries ) {
        assert libraries != null;
        assert antProjectHelper != null;
        assert webModuleLibraries != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        Document doc = data.getOwnerDocument();
        Element webModuleLibs = (Element) data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, webModuleLibraries).item(0);
        if (webModuleLibs == null) {
            webModuleLibs = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, webModuleLibraries); //NOI18N
            data.appendChild(webModuleLibs);
        }
        while (webModuleLibs.hasChildNodes()) {
            webModuleLibs.removeChild(webModuleLibs.getChildNodes().item(0));
        }
        
        Iterator cp = classpath.iterator();
        for (Iterator i = libraries.iterator(); i.hasNext();)
            webModuleLibs.appendChild(createLibraryElement(doc, (String)i.next(), (Item) cp.next()));

        antProjectHelper.putPrimaryConfigurationData( data, true );
    }
    
    private static Element createLibraryElement(Document doc, String pathItem, Item item) {
        Element libraryElement = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_LIBRARY);
        ArrayList files = new ArrayList ();
        ArrayList dirs = new ArrayList ();
        WebProjectProperties.getFilesForItem(item, files, dirs);
        if (files.size() > 0) {
            libraryElement.setAttribute(ATTR_FILES, "" + files.size());
        }
        if (dirs.size() > 0) {
            libraryElement.setAttribute(ATTR_DIRS, "" + dirs.size());
        }
        Element webFile = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_FILE);
        libraryElement.appendChild(webFile);
        webFile.appendChild(doc.createTextNode("${" + pathItem + "}"));
        if (item.getPathInWAR() != null) {
            Element pathInWar = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, TAG_PATH_IN_WAR);
            pathInWar.appendChild(doc.createTextNode(item.getPathInWAR()));
            libraryElement.appendChild(pathInWar);
        }
        return libraryElement;
    }

    /**
     * Extracts <b>the first</b> nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    private static String findText( Element parent ) {
        NodeList l = parent.getChildNodes();
        for ( int i = 0; i < l.getLength(); i++ ) {
            if ( l.item(i).getNodeType() == Node.TEXT_NODE ) {
                Text text = (Text)l.item( i );
                return text.getNodeValue();
            }
        }
        return null;
    }
    
    /**
     * Extract nested text from a node.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent node
     * @return the nested text, or null if none was found
     */
    private static String findText(Node parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
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

        public static final String PATH_IN_WAR_LIB = "WEB-INF/lib"; //NOI18N
        public static final String PATH_IN_WAR_DIR = "WEB-INF/classes"; //NOI18N
        public static final String PATH_IN_WAR_APPLET = ""; //NOI18N
        public static final String PATH_IN_WAR_NONE = null;
    
        // Reference to a broken object
        private static final String BROKEN = "BrokenReference"; // NOI18N
        
        private Object object;
        private URI artifactURI;
        private int type;
        private String property;
        private String pathInWar;
        private String raw;
        private String eval;

        private Item( int type, Object object, String raw, String eval, String property, String pathInWar) {
            this.type = type;
            this.object = object;
            if (object == null || type == TYPE_CLASSPATH || object == BROKEN ||
                    (type == TYPE_JAR && object instanceof File) ||
                    (type == TYPE_ARTIFACT && (object instanceof AntArtifact)) ||
                    (type == TYPE_LIBRARY && (object instanceof Library))) {
                this.raw = raw;
                this.eval = eval;
                this.property = property;
                this.pathInWar = pathInWar;
            } else {
                throw new IllegalArgumentException ("invalid classpath item, type=" + type + " object type:" + object.getClass().getName());
            }
        }
        
        private Item( int type, Object object, String raw, String eval, URI artifactURI, String property, String pathInWar) {
            this( type, object, raw, eval, property, pathInWar);
            this.artifactURI = artifactURI;
        }
              
        public String getPathInWAR () {
            return pathInWar;
        }

        public void setPathInWAR (String pathInWar) {
            this.pathInWar = pathInWar;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }

        public String getRaw() {
            return raw;
        }
        
        public String getEvaluated() {
            return eval == null ? getRaw() : eval;
        }

        // Factory methods -----------------------------------------------------
        
        
        public static Item create( Library library, String property, String pathInWar) {
            if ( library == null ) {
                throw new IllegalArgumentException( "library must not be null" ); // NOI18N
            }
                        
            String libraryName = library.getName();
            return new Item( TYPE_LIBRARY, library, "${libs."+libraryName+".classpath}", libraryName, property, pathInWar); //NOI18N
        }
        
        public static Item create( AntArtifact artifact, URI artifactURI, String property, String pathInWar) {
            if ( artifactURI == null ) {
                throw new IllegalArgumentException( "artifactURI must not be null" ); // NOI18N
            }
            if ( artifact == null ) {
                throw new IllegalArgumentException( "artifact must not be null" ); // NOI18N
            }
            return new Item( TYPE_ARTIFACT, artifact, null, artifact.getArtifactLocations()[0].toString(), artifactURI, property, pathInWar);
        }
        
        public static Item create( File file, String property, String pathInWar) {
            if ( file == null ) {
                throw new IllegalArgumentException( "file must not be null" ); // NOI18N
            }
            return new Item( TYPE_JAR, file, null, file.getPath(), property, pathInWar);
        }
        
        public static Item create( String property, String pathInWar) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null" ); // NOI18N
            }
            return new Item ( TYPE_CLASSPATH, null, null, null, property, pathInWar);
        }
        
        public static Item createBroken( int type, String property, String pathInWar) {
            if ( property == null ) {
                throw new IllegalArgumentException( "property must not be null in broken items" ); // NOI18N
            }
            return new Item( type, BROKEN, null, null, property, pathInWar);
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
            return (Library)object;
        }
        
        public File getFile() {
            if ( getType() != TYPE_JAR ) {
                throw new IllegalArgumentException( "Item is not of required type - JAR" ); // NOI18N
            }
            if (isBroken()) {
                return null;
            }
            return (File)object;
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
        
        public boolean isBroken() {
            return object == BROKEN;
        }
                        
        public int hashCode() {
        
            int hash = getType();

            if ( object == BROKEN ) {
                return BROKEN.hashCode();
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
     
        public String toString() {
            return "artifactURI=" + artifactURI
                    + ", type=" + type 
                    + ", property=" + property
                    + ", pathInWar=" + pathInWar
                    + ", raw=" + raw
                    + ", eval=" + eval
                    + ", object=" + object;
        }
        
    }
            
}
