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
package org.netbeans.modules.php.rt.providers.impl.local.apache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.php.rt.providers.impl.AbstractProvider;



/**
 * @author ads
 *
 */
class HttpdConfig {
    
    public static final String ALL_IPV4_IP_ADDRESS  = "0.0.0.0";        // NOI18N
    
    public static final String VERSION_5            = "5";              // NOI18N
    
    public static final String VERSION_4            = "4";              // NOI18N
    
    public static final String PHP                  = "php";            // NOI18N
    
    private static final String VIRTUAL_HOST_END    = "</VirtualHost";  // NOI18N

    private static final String VIRTUAL_HOST_START  = "<VirtualHost";   // NOI18N

    private static final String SERVER_ROOT         = "ServerRoot";     // NOI18N

    private static final String INCLUDE             = "Include";        // NOI18N

    private static final String COMMENT             = "#";              // NOI18N
    
    private static final String DOCUMENT_ROOT       = "DocumentRoot";   // NOI18N
    
    private static final String SERVER_NAME         = "ServerName";     // NOI18N

    private static final String PORT                = "Port";     // NOI18N

    private static final String LISTEN              = "Listen";     // NOI18N

    private static final String LOAD_MODULE         = "LoadModule";     // NOI18N
    
    private static final String ADD_TYPE            = "AddType";        // NOI18N
    
    private static final String MODULE_NAME_END     = "_module";        // NOI18N
    
    private static final String PHP_TYPE            = 
                        "application/x-httpd-";                         // NOI18N

    private static final String COLON               = ":";              // NOI18N

    private static final String IPV6_BRACKET        = "]";              // NOI18N

    private static final String IPV6_COLON          = IPV6_BRACKET+COLON;              // NOI18N
    
    private static Logger LOGGER = Logger.getLogger(AbstractProvider.class.getName());

    //public HttpdConfig( String platformPath , String configPath ) {
    public HttpdConfig( String configPath ) {
        init();

        myPlatformConfig = configPath;
        if ( configPath == null ) {
            myHttpdConf = null;
        }
        else {
            myHttpdConf = new File( configPath );
        }
    }
    
    /**
     * @return list of all available hosts in config file 
     */
    public HttpdHost[] getHosts() {
        if ( myHttpdConf != null ) {
            if ( !myHttpdConf.exists() ) {
                return new HttpdHost[] {};
            }
            List<HttpdHost> list = new LinkedList<HttpdHost>();
            try {
                loadHostsFromFile( myHttpdConf , list );
                if ( myGlobalDocRoot != null ) {
                    list.add( 
                        new HttpdHost( myGlobalHostName, myGlobalPort, myGlobalDocRoot, myPlatformConfig) 
                         
                        );
                }
            }
            catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
            return list.toArray( new HttpdHost[ list.size()] );
        }
        return new HttpdHost[] {};
    }
    
    /**
     * @param version version of PHP language
     * @return full path to php .so file ( as it defined in config file ) 
     */
    public String getPhpSOPath( String version ) {
        if ( myServerRoot == null ) {
            getHosts();
        }
        String key = PHP + version + MODULE_NAME_END;
        String path = myPhpModules.get( key );
        if ( path == null ) {
            return null;
        }
        else {
            if (fileExists(path)){
                return path;
            } else {
                return myServerRoot == null ? path : myServerRoot +
                        File.separator +path; 
            }
        }
    }

    private boolean fileExists(String path){
        File file = new File(path);
        return file.exists();
    }
    
    /**
     * @return collection of extensions that are recognized as php files by Apache
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getPhpExtensions() {
        if ( myServerRoot == null ) {
            getHosts();
        }
        String key = PHP_TYPE + PHP;
        String value = myPhpTypes.get(key);
        if ( value == null ) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer tokenizer = new StringTokenizer( value );
        Collection<String> coll = new LinkedList<String>();
        while ( tokenizer.hasMoreTokens() ) {
            String token = tokenizer.nextToken();
            coll.add( token );
        }
        return coll;
    }

    private void loadHostsFromFile( File file, List<HttpdHost> list )
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            handleLine(line, list);
        }
        configureGlobalPort();
    }
    
    /**
     * If default server port (myGlobalPort) wasn't loaded from 
     * Port or ServerName directives, tries to calculate it.
     * Will remove from myListenPorts all ports used by virtualhosts
     * (stored in myVirtualHostsPorts ) and take the first element
     */
    private void configureGlobalPort(){
        if (myGlobalPort != null)
            return;
        
        myListenPorts.removeAll(myVirtualHostsPorts);
        myListenPorts.remove(null);
        if (myListenPorts.size()>0){
            myGlobalPort = myListenPorts.get(0);
        }
    }
    
    private void handleLine( String line , List<HttpdHost> list ) {
        if ( line.startsWith(COMMENT)) {                // NOI18N
            return;
        }
        // catch ServerRoot directive for determening relative paths
        else if ( line.startsWith( SERVER_ROOT) ) {
            handleServerRoot(line);
        }
        else if ( line.startsWith( DOCUMENT_ROOT) ) {
            handleDocumentRoot(line);
        }
        else if ( line.startsWith( INCLUDE ) ) {
            String value = line.substring( INCLUDE.length() );
            if ( !isDirective(value) ){
                return;
            }
            value = clearValue(value.trim());
            handleInclude( value, list );
        }
        else if ( line.startsWith( SERVER_NAME ) ) {
            handleServerName(line);
        }
        else if ( line.startsWith( PORT ) ) {
            handlePort(line);
        }
        else if ( line.startsWith( LISTEN ) ) {
            handleListen(line);
        }
        else if ( line.startsWith( VIRTUAL_HOST_START )) {
            handleVirtualHostBegin(line);
        }
        else if ( line.startsWith( VIRTUAL_HOST_END )) {
            handleVirtualHostEnd(line, list);
        }
        else if ( line.startsWith( LOAD_MODULE )) {
            handleLoadModule( line );
        }
        else if (line.startsWith( ADD_TYPE) ) {
            handleAddType( line );
        }
    }

    private void handleAddType( String line ) {
        String value = line.substring( ADD_TYPE.length() );
        if ( !isDirective(value) ){
            return;
        }
        value = clearValue(value.trim());
        
        String type = null;
        
        StringTokenizer tokenizer = new StringTokenizer( value );
        if ( tokenizer.hasMoreTokens() ) {
            type = tokenizer.nextToken();
        }
        else {
            return;
        }
        
        if ( type.contains( PHP ) ) {
            String typeValue = value.substring( type.length() ).trim(); 
            myPhpTypes.put( type , typeValue );
        }
        else {
            return;
        }
    }

    private void handleLoadModule( String line ) {
        String value = line.substring( LOAD_MODULE.length() );
        if ( !isDirective(value) ){
            return;
        }
        value = clearValue(value.trim());
        
        String moduleName = null;
        String modulePath = null;
        
        StringTokenizer tokenizer = new StringTokenizer( value );
        if ( tokenizer.hasMoreTokens() ) {
            moduleName = tokenizer.nextToken();
        }
        else {
            return;
        }
        
        if ( tokenizer.hasMoreTokens() ) {
            modulePath = clearValue(tokenizer.nextToken());
        }
        else {
            return;
        }
        
        myPhpModules.put( moduleName , modulePath );
    }

    private void handleVirtualHostEnd( String line, List<HttpdHost> list ) {
        String value = line.substring( VIRTUAL_HOST_END.length() );
        if ( value.length()!= 0 && value.charAt( value.length()-1) == '>'
            && value.substring( 0, value.length()-1 ).trim().length()==0 ) 
        {
            //myCurrentHost = null;
            if ( myCurrentDocRoot != null && myCurrentHost != null ) {
                list.add( new HttpdHost( myCurrentHost , myCurrentPort, myCurrentDocRoot, myPlatformConfig)  );
            }
            isVirtualHost = false;
        }
    }


    private void handleVirtualHostBegin( String line ) {
        String value = line.substring( VIRTUAL_HOST_START.length() );
        if ( !isDirective(value) ){
            return;
        }
        myCurrentHost = null;
        myCurrentPort = null;
        
        value = clearValue(value.trim());
        if (value.indexOf('>') > 0){
            value = value.substring(0, value.indexOf('>'));
            // if port will be specified in ServerName directive 
            // inside this <VirtualHost> tag,
            // this myCurrentPort value will be rewritten
            myCurrentPort = handleVirtualHostBeginHostPatterns(value);
        }
        /*if ( value.length() >0 && value.charAt( value.length()-1) =='>') {
            myCurrentHost = value.substring( 0 , value.length() -1 ).trim(); 
        }*/
        
        isVirtualHost = true;
    }

    /** 
     * parses list of ports specified in &lt;VirtualHost&gt; tag.
     * Stores them in myVirtualHostsPorts.
     * @returns the first occured port
     */
    private String handleVirtualHostBeginHostPatterns( String line ) {
        String firstPort = null;
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        while (tokenizer.hasMoreTokens()){
            String value = tokenizer.nextToken();
            String port = null;
            String colon = defineIPPortSeparatorByIPV(value);
        
            if (value.indexOf(colon) != -1){
                if (value.indexOf(colon) < value.length()-1 )
                    port = value.substring(value.indexOf(colon)+1);
            }
            if (port != null){
                if (firstPort == null)
                    firstPort = port;
                if (!myVirtualHostsPorts.contains(port))
                    myVirtualHostsPorts.add(port);
            }
        }
        return firstPort;
    }
    
    /**
     * checks which IP version is used in string value and returns 
     * IP and Port separator suitable for this version.
     * 
     * @param value
     *          string value with ip address and port. 
     *          e.g. 120.0.0.1:80 from Listen directive.
     * @returns separator
     *          Strng used to separate ipfrom port, if any.
     *          Returns ']:' for IPv6, ':' for IPv4.
     *          Returns ':' if there is no separator in given string.
     */
    private String defineIPPortSeparatorByIPV(String value){
        if (value == null){
            return COLON;
        }
        return value.indexOf(IPV6_BRACKET) != -1
                    ? IPV6_COLON
                    : COLON;
    }
    
    private void handleServerName( String line ) {
        String value = line.substring( SERVER_NAME.length() );
        if ( !isDirective(value) ){
            return;
        }
        value = clearValue(value.trim());
        
        String name = null;
        String port = null;
        if (value.indexOf(COLON) != -1){
            if (value.indexOf(COLON) > 0 )
                name = value.substring(0, value.indexOf(COLON));
            if (value.indexOf(COLON) < value.length()-1 )
                port = value.substring(value.indexOf(COLON)+1, value.length());
        } else {
            name = value;
        }

        if ( isVirtualHost ) {
            myCurrentHost = name;
            if (port != null)
                myCurrentPort = port;
        }
        else {
            myGlobalHostName = name;
            if (port != null)
                myGlobalPort = port;
        }
    }

    private void handleListen( String line ) {
        String value = line.substring( LISTEN.length() );
        if ( !isDirective(value) ){
            return;
        }
        value = clearValue(value.trim());
        
        String ip = grabIpFromListen(value);
        String port = grabPortFromListen(value);
        if (ip != null && myGlobalHostName == null){
            myGlobalHostName = ip;
        }
        if (port != null){
            myListenPorts.add(port);
        }
        // 
    }
    
    private String grabIpFromListen(String value){
        String ip = null;
        String colon = defineIPPortSeparatorByIPV(value);
        if (!colon.equals(IPV6_COLON)){
            if (value.indexOf(colon) > 0 ){
                ip = value.substring(0, value.indexOf(colon));
            }
        }
        if (ALL_IPV4_IP_ADDRESS.equals(ip)){
            ip = null;
        }
        return ip;
    }

    private String grabPortFromListen(String value){
        String port = null;
        String colon = defineIPPortSeparatorByIPV(value);
        
        if (value.indexOf(colon) != -1){
            if (value.indexOf(colon) < value.length()-1 ){
                port = value.substring(value.indexOf(colon)+1, value.length());
            }
        } else {
            port = value;
        }
        return port;
    }
    
    private void handlePort( String line ) {
        String value = line.substring( PORT.length() );
        if ( !isDirective(value) ){
            return;
        }
        value = clearValue(value.trim());
            // PORT is not allowed inside virtual host
            // if it was already loaded from ServerName, do not load from Port

        if (myGlobalPort == null){
            myGlobalPort = value;
        }
    }

    private void handleDocumentRoot( String line ) {
        String value = line.substring( DOCUMENT_ROOT.length() );
        if ( !isDirective( value) ){
            return;
        }
        value = clearValue(value.trim());
        /*if ( myCurrentHost == null ) {
            list.add( new HttpdHost( value ) );
        }
        else {
            list.add( new HttpdHost( myCurrentHost, value ) );
        }*/
        if ( isVirtualHost ) {
            myCurrentDocRoot = value;
        }
        else {
            myGlobalDocRoot = value;
        }
    }

    private void handleServerRoot( String line ) {
        String value = line.substring( SERVER_ROOT.length() );
        if ( !isDirective( value) ){
            return;
        }
        if ( myServerRoot == null ) {  // ServerRoot should be handled only in main config file
            myServerRoot = clearValue(value.trim());
        }
    }
    
    /*
     * - Include directory => read all files in that directory and any subdirectory
     * - Include using wildcad => read all maching files
     *
     */
    private void handleInclude(String value, List<HttpdHost> list) {
        String includePath;
        if (value.startsWith(File.separator)) {
            includePath = value;
        } else {
            includePath = myServerRoot == null ? value : myServerRoot + File.separator + value;
        }
        List<File> files = loadFilesByIncludePath(includePath);

        if (files == null) {
            return;
        } else {
            for (File file : files) {
                try {
                    loadHostsFromFile(file, list);
                } catch (IOException e) {
                    // ignore it
                }
            }
        }
    }

    private List<File> loadFilesByIncludePath(String includePath) {
        List<File> filesList = new ArrayList<File>();
        File file = new File(includePath);
        if (file.exists()) {
            if (file.isDirectory()) {
                loadFilesRecursively(file, filesList);
            } else {
                filesList.add(file);
            }
        } else {
            loadFilesByPattern(file, filesList);
        }

        return filesList;
    }

    /**
     * processed case:
     * Include directory => read all files in that directory and any subdirectory
     */
    private void loadFilesRecursively(File dir, List<File> filesList) {
        loadFilesRecursively(dir, filesList, null);
    }
    
    /**
     * inits colletions to store values we will grab from conf file
     */
    private void init(){
        myPhpModules = new HashMap<String, String>();
        myPhpTypes = new HashMap<String, String>();
        myVirtualHostsPorts = new ArrayList<String>();
        myListenPorts = new ArrayList<String>();
    }

    private void loadFilesRecursively(File dir, List<File> filesList, FilenameFilter filter) {
        File[] children = dir.listFiles(filter);
        for (File file : children) {
            if (file.isDirectory()) {
                // it is allowed specify pattern for elements inside one directory.
                // all deeper elements should be loadded without pattern.
                loadFilesRecursively(file, filesList);
            } else {
                filesList.add(file);
            }
        }
    }

    /** 
     * processed case:
     * Include using wildcad => read all maching files.
     * <br/>
     * Unlike apache, doesn't care about loading files in alphabetical order.
     */
    private void loadFilesByPattern(File pattern, List<File> filesList) {
        String path = pattern.getPath();
        int index = RegexpFileFilter.wildcardIndex(path);
        if (index < 0){
            return;
        }
        // get the most deep dir without wildcard symbols
        File parent = new File(path.substring(0, index)).getParentFile();
        if (!parent.exists()) {
            return;
        }
        try{
            FilenameFilter filter = new RegexpFileFilter(pattern.getPath());
            loadFilesRecursively(parent, filesList, filter);
        } catch (PatternSyntaxException e){
            LOGGER.log(Level.WARNING, null, e.getMessage());
        }
    }
    
    private boolean isDirective( String rest ) {
        if ( rest.length() >0 && 
                rest.charAt(0) != ' '  && rest.charAt(0) != '\t' )
        {
            return false;
        }
        return true;
    }
    
    private String clearValue( String value) {
        if ( value == null || value.length() == 0) {
            return value;
        }
        String result = value; 
        char first = value.charAt(0);
        if ( first == '"' ) {
            result = value.substring( 1 );
        }
        char last = result.charAt( result.length() -1 );
        if ( last == '"' ) {
            result = result.substring( 0 , result.length() -1 );
        }
        return result;
    }

    
    private File myHttpdConf;
    
    private String myServerRoot;
    
    // contains currenly handled ServerName directive inside virtual host descr 
    private String myCurrentHost;
    
    // contains currenly handled port value inside virtual host descr 
    // (from ServerName or <VirtualHost *:port> )
    private String myCurrentPort;

    // contains currenly handled DocumentRoot directive inside virtual host descr
    private String myCurrentDocRoot;
    
    private boolean isVirtualHost;
    
    // variables for keeping globally defined DocumentRoot and Server Name
    private String myGlobalHostName;
    
    private String myGlobalPort;

    private String myGlobalDocRoot;
    
    private String myPlatformConfig;
    
    private Map<String,String> myPhpModules;
    
    private Map<String,String> myPhpTypes;

    // list to store all ports supported by server.
    private List<String> myListenPorts;
    
    // list to store ports used for virtual hosts
    private List<String> myVirtualHostsPorts;
    
}
