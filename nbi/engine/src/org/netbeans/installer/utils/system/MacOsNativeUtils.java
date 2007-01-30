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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */

package org.netbeans.installer.utils.system;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.Shortcut;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Dmitry Lipin
 */
public class MacOsNativeUtils extends UnixNativeUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String    LIBRARY_PATH_MACOSX     = "native/macosx.dylib";
    
    public static final String    APP_SUFFIX              = ".app";
    
    private static final String   DOCK_PROPERIES         = "com.apple.dock.plist";
    
    private static final String   PLUTILS                = "plutil";
    private static final String   PLUTILS_CONVERT        = "-convert";
    private static final String   PLUTILS_CONVERT_XML    = "xml1";
    private static final String   PLUTILS_CONVERT_BINARY = "binary1";
    
    private static final String[] UPDATE_DOCK_COMMAND  = new String[] {"killall", "-HUP", "Dock"};
    public static final String[] FORBIDDEN_DELETING_FILES_MACOSX = {
        "/Applications",
        "/Developer",
        "/Library",
        "/Network",
        "/System",
        "/Users"};
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    
    // constructor //////////////////////////////////////////////////////////////////
    MacOsNativeUtils() {
        loadNativeLibrary(LIBRARY_PATH_MACOSX);
        initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_MACOSX);
    }
    
    // NativeUtils implementation/override //////////////////////////////////////////
    public File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) {
        String fileName = shortcut.getFileName();
        
        if (fileName == null) {
            fileName = shortcut.getExecutable().getName();
            if (fileName!=null && fileName.endsWith(APP_SUFFIX)) {
                fileName = fileName.substring(0,fileName.lastIndexOf(APP_SUFFIX));
            }
        }
        
        switch (locationType) {
            case CURRENT_USER_DESKTOP:
                return new File(SystemUtils.getUserHomeDirectory(), "Desktop/" + fileName);
            case ALL_USERS_DESKTOP:
                return new File(SystemUtils.getUserHomeDirectory(), "Desktop/" + fileName);
            case CURRENT_USER_START_MENU:
                return getDockPropertiesFile();
            case ALL_USERS_START_MENU:
                return getDockPropertiesFile();
        }
        return null;
    }
    
    public File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        final File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        try {
            
            if (locationType == ShortcutLocationType.CURRENT_USER_DESKTOP ||
                    locationType == ShortcutLocationType.ALL_USERS_DESKTOP ) {
                // create a symlink on desktop
                if(!shortcutFile.exists()) {
                    SystemUtils.executeCommand(null,new String[] {
                        "ln", "-s", shortcut.getExecutablePath(),  //NOI18N
                        shortcutFile.getPath()});
                }
            } else {
                //create link in the Dock
                if(convertDockProperties(true)==0) {
                    if (modifyDockLink(shortcut, shortcutFile, true)) {
                        LogManager.log(ErrorLevel.DEBUG,
                                "    Updating Dock");
                        convertDockProperties(false);
                        SystemUtils.executeCommand(null,UPDATE_DOCK_COMMAND);
                    }
                }
            }
            return shortcutFile;
        } catch (IOException e) {
            throw new NativeException("Cannot create shortcut", e);
        }
    }
    
    public void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean cleanupParents) throws NativeException {
        final File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        try {
            if (locationType == ShortcutLocationType.CURRENT_USER_DESKTOP ||
                    locationType == ShortcutLocationType.ALL_USERS_DESKTOP ) {
                // create a symlink on desktop
                if(shortcutFile.exists()) {
                    FileUtils.deleteFile(shortcutFile,false);
                }
            } else {
                //create link in the Dock
                if(convertDockProperties(true)==0) {
                    if(modifyDockLink(shortcut,shortcutFile,false)) {
                        LogManager.log(ErrorLevel.DEBUG,
                                "    Updating Dock");
                        if(convertDockProperties(false)==0) {
                            SystemUtils.executeCommand(null,UPDATE_DOCK_COMMAND);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new NativeException("Cannot remove shortcut", e);
        }
    }
    
    // mac os x specific ////////////////////////////////////////////////////////////
    public boolean isCheetah() {
        return (getOSVersion().startsWith("10.0"));
    }
    
    public boolean isPuma() {
        return (getOSVersion().startsWith("10.1"));
    }
    
    public boolean isJaguar() {
        return (getOSVersion().startsWith("10.2"));
    }
    
    public boolean isPanther() {
        return (getOSVersion().startsWith("10.3"));
    }
    
    public boolean isTiger() {
        return (getOSVersion().startsWith("10.4"));
    }
    
    public boolean isLeopard() {
        return (getOSVersion().startsWith("10.5"));
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private String getOSVersion() {
        return System.getProperty("os.version");
    }
    
    private File upToApp(File exec) {
        File executable = exec;
        while ((executable != null) && !executable.getPath().endsWith(APP_SUFFIX)) {
            executable = executable.getParentFile();
        }
        
        return executable;
    }
    
    private void modifyShortcutExecutable(Shortcut shortcut) {
        if (shortcut.canModifyExecutablePath()) {
            File executable = upToApp(new File(shortcut.getExecutablePath()));
            
            if (executable != null) {
                shortcut.setExecutable(executable);
            }
        }
    }
    
    private boolean modifyDockLink(Shortcut shortcut, File dockFile, boolean adding) {
        OutputStream outputStream = null;
        try {
            
            modifyShortcutExecutable(shortcut);
            
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    parsing xml file...");
            Document document = documentBuilder.parse(dockFile);
            LogManager.log(ErrorLevel.DEBUG,
                    "    ...complete");
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    getting root element");
            Node root = document.getDocumentElement();
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    getting root/dict element");
            Node dict = XMLUtils.getChildNode(root,"./dict");
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    getting root/dict/[key=persistent-apps] element. dict = " +
                    dict.getNodeName());
            LogManager.log(ErrorLevel.DEBUG,"Get Keys");
            
            
            List <Node> keys = XMLUtils.getChildList(dict, "./key");
            LogManager.log(ErrorLevel.DEBUG,"Length = " + keys.size());
            Node persistentAppsKeyNode = null;
            int index = 0;
            while(keys.get(index)!=null) {
                if(keys.get(index).getTextContent().equals("persistent-apps")) {
                    persistentAppsKeyNode = keys.get(index);
                    break;
                }
                index++;
            }
            
            LogManager.log(ErrorLevel.DEBUG,
                    "    done. KeyNode = " + persistentAppsKeyNode.getTextContent());
            
            if(persistentAppsKeyNode==null) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    Not found.. strange.. Create new one");
                persistentAppsKeyNode = XMLUtils.addChildNode(dict,"key","persistent-apps");
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "    Getting next element.. expecting it to be array element");
            Node array = keys.get(index);
            index = 0 ;
            while(!array.getNodeName().equals("array") && index < 10) {
                array = array.getNextSibling();
                index++;
            }
            if(index==10) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    is not an array element... very strange");
                return false;
            }
            
            if(array==null) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    null... very strange");
                return false;
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "    OK. Content = " + array.getNodeName());
            if(!array.getNodeName().equals("array")) {
                LogManager.log(ErrorLevel.DEBUG,
                        "    Not an array element");
                return false;
            }
            if(adding) {                
                LogManager.log(ErrorLevel.DEBUG,
                        "Adding shortcut with the following properties: ");                
                LogManager.log(ErrorLevel.DEBUG, "    executable = " + shortcut.getExecutablePath());
                LogManager.log(ErrorLevel.DEBUG, "    name = " + shortcut.getName());
                
                dict = XMLUtils.addChildNode(array,"dict",null);
                XMLUtils.addChildNode(dict,"key","tile-data");
                Node dictChild = XMLUtils.addChildNode(dict,"dict",null);
                XMLUtils.addChildNode(dictChild,"key","file-data");
                Node dictCC = XMLUtils.addChildNode(dictChild,"dict",null);
                XMLUtils.addChildNode(dictCC,"key","_CFURLString");
                XMLUtils.addChildNode(dictCC,"string",shortcut.getExecutablePath());
                XMLUtils.addChildNode(dictCC,"key","_CFURLStringType");
                XMLUtils.addChildNode(dictCC,"integer","0");
                XMLUtils.addChildNode(dictChild,"key","file-label");
                XMLUtils.addChildNode(dictChild,"string",shortcut.getName());
                XMLUtils.addChildNode(dictChild,"key","file-type");
                XMLUtils.addChildNode(dictChild,"integer","41");
                XMLUtils.addChildNode(dict,"key","tile-type");
                XMLUtils.addChildNode(dict, "string","file-tile");                                
                LogManager.log(ErrorLevel.DEBUG,
                        "... adding shortcut to Dock XML finished");
            } else {
                LogManager.log(ErrorLevel.DEBUG,
                        "Removing shortcut with the following properties: ");
                LogManager.indent();
                LogManager.log(ErrorLevel.DEBUG,
                        "executable = " + shortcut.getExecutablePath());
                LogManager.log(ErrorLevel.DEBUG,
                        "name = " + shortcut.getName());
                
                String location = shortcut.getExecutablePath();
                List <Node> dcts = XMLUtils.getChildList(array,
                        "./dict/dict/dict/string");
                index = 0;
                Node dct = null;
                LogManager.log(ErrorLevel.DEBUG,
                        "Total dict/dict/dict/string items = " + dcts.size());
                LogManager.log(ErrorLevel.DEBUG,
                        "        location = " + location);
                
                File locationFile = new File(location);
                
                while(index < dcts.size() && dcts.get(index)!=null) {
                    Node item = dcts.get(index);
                    String content = item.getTextContent();
                    LogManager.log(ErrorLevel.DEBUG, "        content = " + content);
                    if(content!=null && !content.equals("")) {                        
                        File contentFile = new File(content);
                        if(locationFile.equals(contentFile)) {
                            dct = item;
                            break;
                        }
                    }
                    index++;
                };
                
                if(dct!=null) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "Shortcut exists in the dock.plist");
                    array.removeChild(dct.getParentNode().getParentNode().getParentNode());
                } else {
                    LogManager.log(ErrorLevel.DEBUG,
                            "hortcut doesn`t exist in the dock.plist");
                }
                LogManager.unindent();
                LogManager.log(ErrorLevel.DEBUG,
                        "... removing shortcut from Dock XML finished");
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "    Saving XML... ");
            XMLUtils.saveXMLDocument(document,dockFile);
            LogManager.log(ErrorLevel.DEBUG,
                    "    Done (saving xml)");
            
        } catch (ParserConfigurationException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        }  catch (ParseException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (XMLException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (SAXException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (IOException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (NullPointerException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } finally {
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    LogManager.log(ErrorLevel.WARNING,
                            "Can`t close stream for Dock properties file");
                }
            }
        }
        LogManager.log(ErrorLevel.DEBUG,
                "    Return true from modifyDockLink");
        return true;
        
    }
    
    private File getDockPropertiesFile() {
        return new File(SystemUtils.getUserHomeDirectory(),
                "Library/Preferences/" + DOCK_PROPERIES);//NOI18N
    }
    
    private int convertDockProperties(boolean decode) {
        File dockFile = getDockPropertiesFile();
        int returnResult = 0;
        try {
            if(!isCheetah() && !isPuma()) {
                if((!decode && (isTiger() || isLeopard())) || decode) {
                    // decode for all except Cheetah and Puma
                    // code only for Tiger and Leopard
                    
                    ExecutionResults result = SystemUtils.executeCommand(null,
                            new String[] { PLUTILS,PLUTILS_CONVERT,(decode)? PLUTILS_CONVERT_XML :
                                PLUTILS_CONVERT_BINARY,dockFile.getPath()});
                    returnResult = result.getErrorCode();
                }
            }
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING);
            returnResult = -1;
        }
        return returnResult;
    }
}
