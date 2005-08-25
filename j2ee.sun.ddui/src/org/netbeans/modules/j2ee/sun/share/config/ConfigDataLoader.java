/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.config;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.enterprise.deploy.shared.ModuleType;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;


/** Loader for deployment plan files
 * Permits viewing/editing of them.
 * @author Pavel Buzek
 */
public class ConfigDataLoader extends UniFileLoader implements FileChangeListener {

    /** Generated serial version UID. */
//    private static final long serialVersionUID = ;

    private static final String GENERIC_EXTENSION = "dpf"; //NOI18N
    private static final String PRIMARY = "primary"; //NOI18N
    private static final String SECONDARY = "secondary"; //NOI18N
//    private static final String SERVER = "server"; //NOI18N
    
    /** Creates loader. */
    public ConfigDataLoader () {
        super("org.netbeans.modules.j2ee.sun.share.config.ConfigDataObject"); // NOI18N
    }

    /** Initizalized loader, i.e. its extension list. Overrides superclass method. */
    protected void initialize () {
        super.initialize();
        checkCache ();
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getMessage (ConfigDataLoader.class, "LBL_LoaderName");
    }
    
    /** Gets default system actions. Overrides superclass method. */
    protected SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get (OpenAction.class),
            SystemAction.get (EditAction.class),
            SystemAction.get (FileSystemAction.class),
            null,
            SystemAction.get (CutAction.class),
            SystemAction.get (CopyAction.class),
            SystemAction.get (PasteAction.class),
            null,
            SystemAction.get (DeleteAction.class),
            null,
            SystemAction.get (ToolsAction.class),
            SystemAction.get (PropertiesAction.class),
        };
    }

    /** Creates multi data object for specified primary file.
     * Implements superclass abstract method. */
    protected MultiDataObject createMultiObject (FileObject fo)
    throws DataObjectExistsException, IOException {
        if (isPrimaryDescriptor(fo)) {
            return new ConfigDataObject(fo, this);
        } else {
            return new SecondaryConfigDataObject(fo, this);
        }
    }
    
    private boolean isPrimaryDescriptor(FileObject fo) {
        String filename = fo.getNameExt();
        return getPrimaryByName(filename) != null;
    }
    
    protected FileObject findPrimaryFile (FileObject fo) {
        // never recognize folders.
        if (fo.isFolder()) {
            return null;
        }
        
        String ext = fo.getExt();
        String filename = fo.getNameExt ();
        FileObject primaryFO = null;
        String secondaryName = null;
        if (getPrimaryByName(filename) != null || ext.equals(GENERIC_EXTENSION)) {
            primaryFO = fo;
        } else if (getPrimaryBySecondaryName (filename) != null) { // check for secondary file 
            secondaryName = filename;
        }
        
        if (primaryFO == null && secondaryName == null) {
            return null;
        }
        
        Project owner = FileOwnerQuery.getOwner(fo);
        if (owner != null) {
            Lookup l = owner.getLookup();
            J2eeModuleProvider projectModule = (J2eeModuleProvider) l.lookup(J2eeModuleProvider.class);
            if (projectModule != null) {
                if (primaryFO != null) {
                    primaryFO = projectModule.findDeploymentConfigurationFile(filename);
                    if (primaryFO != null) {
                        File primary = FileUtil.toFile(primaryFO);
                        if (primary != null && primary.equals(FileUtil.toFile(fo))) {
                            return fo;
                        }
                    }
                } else { // look for secondary FO 
                    return projectModule.findDeploymentConfigurationFile(secondaryName);
                }
            }
        }
        return null;
    }
    
    private String getPrimaryByName (String name) {
        HashMap p = (HashMap) getProperty (PRIMARY);
        return (String) p.get (name);
    }
    
    private String getPrimaryBySecondaryName (String name) {
        HashMap p = (HashMap) getProperty (SECONDARY);
        return (String) p.get (name);
    }
    
//    private Set getServersByFileName(String name) {
//        Map m = (Map)getProperty(SERVER);
//        Set serversByName = (Set)m.get(name);
//        return serversByName != null ? serversByName : Collections.EMPTY_SET;
//    }
    
    //this is only a workaround for getStringTable being protected in ModuleType 
//    private static final class MT extends ModuleType {
//        private MT (int i) {
//            super (i);
//        }
//        //return map of MT name in upperCase -> MT instance
//        private HashMap getMTMap () {
//            String t [] = getStringTable ();
//            HashMap m = new HashMap ();
//            for (int i = 0; i < t.length; i ++) {
//                m.put (t [i].toUpperCase (), ModuleType.getModuleType (i));
//            }
//            return m;
//        }
//    }
    
    private void checkCache () {
//        HashMap serversByName = new HashMap ();
        HashMap primaryByName = new HashMap ();
        HashMap secondaryByName = new HashMap ();

        // Sun Application Server specific files for 4.2.  !PW FIXME add appclient and connector in NB 5.0/Glassfish support
        primaryByName.put("sun-web.xml", "WEB-INF/sun-web.xml");
        primaryByName.put("sun-ejb-jar.xml", "META-INF/sun-ejb-jar.xml");
        secondaryByName.put("sun-cmp-mappings.xml", "META-INF/sun-cmp-mappings.xml");
        primaryByName.put("sun-application.xml", "META-INF/sun-application.xml");
        
        // Need to dynamically determine location of server specific files... dunno 
        // how we're going to do that...
//        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
//        assert rep != null;
//        FileObject dir = null; //= rep.getDefaultFileSystem().findResource(ServerRegistry.DIR_JSR88_PLUGINS);
//        if (dir != null) {
//            dir.addFileChangeListener(this);
//            FileObject[] servers = dir.getChildren();
//            for(int _servers = 0; _servers < servers.length; _servers ++) {
//                String serverName = servers[_servers].getName ();
//                FileObject deplFNames = servers [_servers].getFileObject ("DeploymentFileNames"); //NOI18N
//                if (deplFNames != null) {
//                    FileObject mTypes [] = deplFNames.getChildren ();
//                    HashMap mtMap = new MT (0).getMTMap ();
//                    for (int _mTypes = 0; _mTypes < mTypes.length; _mTypes ++) {
//                        String mTypeName = mTypes [_mTypes].getName ();
//                        ModuleType mt = (ModuleType) mtMap.get (mTypeName.toUpperCase ());
//
//                        FileObject allNames [] = mTypes [_mTypes].getChildren ();
//                        String primName = null;
//                        for (int i = 0; i < allNames.length; i++) {
//                            String fname = allNames [i].getNameExt ();
//                            int lastSlash = fname.lastIndexOf ('/');
//                            if (lastSlash == -1) {
//                                lastSlash = fname.lastIndexOf ('\\'); //try both variants
//                            }
//                            lastSlash ++;
//                            String shortName = fname.substring (lastSlash);
//                            if (i == 0) {
//                                primName = shortName;
//                                primaryByName.put (shortName, fname.replace ('\\', '/')); //just in case..
//                            } else {
//                                secondaryByName.put (shortName, primName); //just in case..
//                            }
//                            HashSet hs = (HashSet)serversByName.get(shortName);
//                            if (hs == null) {
//                                hs = new HashSet();
//                                serversByName.put(shortName, hs);
//                            }
//                            hs.add(serverName);
//                        }
//                    }
//                }
//            }
//        }
        putProperty (PRIMARY, primaryByName);
        putProperty (SECONDARY, secondaryByName);
//        putProperty (SERVER, serversByName);
    }
    
    private FileObject findPrimary (FileObject fo, String secondaryName, String primaryName) {
        String secPath = fo.getPath ();
        String primPath = secPath.substring (0, secPath.length () - secondaryName.length ()) + primaryName;
        try {
            return fo.getFileSystem ().findResource (primPath);
        } catch (FileStateInvalidException e) {
            org.openide.ErrorManager.getDefault ().log (e.getLocalizedMessage ());
            return null;
        }
    }
    
    public void fileAttributeChanged (FileAttributeEvent fe) {
    }
    
    public void fileChanged (FileEvent fe) {
        checkCache ();
    }
    
    public void fileDataCreated (FileEvent fe) {
        checkCache ();
    }
    
    public void fileDeleted (FileEvent fe) {
        checkCache ();
    }
    
    public void fileFolderCreated (FileEvent fe) {
        checkCache ();
    }
    
    public void fileRenamed (FileRenameEvent fe) {
        checkCache ();
    }
    
//    public static String getStandardDeploymentPlanName(Server server) {
//        return server.getShortName() + "." + GENERIC_EXTENSION; //NOI18N
//    }
}
