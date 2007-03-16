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
 */

package org.netbeans.modules.languages;

import java.io.IOException;
import org.netbeans.modules.languages.features.ActionCreator;
import org.netbeans.api.languages.ParseException;
import java.io.OutputStream;
import java.util.HashSet;
import org.netbeans.modules.languages.parser.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.netbeans.modules.languages.features.ColorsManager;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesManager {
    
    private static LanguagesManager languagesManager;
    
    public static LanguagesManager getDefault () {
        if (languagesManager == null)
            languagesManager = new LanguagesManager ();
        return languagesManager;
    }

    public boolean isSupported (String mimeType) {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        return fs.findResource ("Editors/" + mimeType + "/language.nbs") != null;
    }
    
    private Map<String,Object> mimeTypeToLanguage = new HashMap<String,Object> ();
    
    public synchronized Language getLanguage (String mimeType) 
    throws ParseException {
        if (!mimeTypeToLanguage.containsKey (mimeType)) {
            mimeTypeToLanguage.put (mimeType, new ParseException ("Already parisng " + mimeType));
            try {
                FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
                FileObject fo = fs.findResource ("Editors/" + mimeType + "/language.nbs");
                if (fo == null) 
                    throw new LanguageDefinitionNotFoundException 
                        ("Language definition for " + mimeType + " not found.");
                addListener (fo);
                Language l = NBSLanguageReader.readLanguage (fo, mimeType);
                initLanguage (l);
                //l.print ();
                mimeTypeToLanguage.put (mimeType, l);
            } catch (ParseException ex) {
                mimeTypeToLanguage.put (mimeType, ex);
                throw ex;
            } catch (Exception ex) {
                ParseException pe = new ParseException (ex);
                mimeTypeToLanguage.put (mimeType, pe);
                throw pe;
            }
        }
        if (mimeTypeToLanguage.get (mimeType) instanceof ParseException)
            throw (ParseException) mimeTypeToLanguage.get (mimeType);
        return (Language) mimeTypeToLanguage.get (mimeType);
    }
    
    private Vector<LanguagesManagerListener> listeners = new Vector<LanguagesManagerListener> ();
    
    public void addLanguagesManagerListener (LanguagesManagerListener l) {
        listeners.add (l);
    }
    
    public void removeLanguagesManagerListener (LanguagesManagerListener l) {
        listeners.remove (l);
    }

    
    // helper methods .....................................................................................................
    
    private void languageChanged (String mimeType) {
        mimeTypeToLanguage.remove (mimeType);
        Vector<LanguagesManagerListener> v = (Vector<LanguagesManagerListener>) listeners.clone ();
        Iterator<LanguagesManagerListener> it = v.iterator ();
        while (it.hasNext ()) {
            LanguagesManagerListener l = it.next ();
            l.languageChanged (mimeType);
        }
    }

    private Set<FileObject> listeningOn = new HashSet<FileObject> ();
    private Listener listener;
    
    private void addListener (FileObject fo) {
        if (!listeningOn.contains (fo)) {
            if (listener == null)
                listener = new Listener ();
            fo.addFileChangeListener (listener);
            listeningOn.add (fo);
        }
    }
    
    private void initLanguage (Language l) {
        try {
            
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            final FileObject root = fs.findResource ("Editors/" + l.getMimeType ());

            // init old options
            if (root.getFileObject ("Settings.settings") == null)
                fs.runAtomicAction (new AtomicAction () {
                    public void run () {
                        try {
                            InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/languages/resources/LanguagesOptions.settings");
                            try {
                                FileObject fo = root.createData("Settings.settings");
                                OutputStream os = fo.getOutputStream();
                                try {
                                    FileUtil.copy(is, os);
    //                                    System.out.println("@@@ Successfully created " + fo.getPath());
                                } finally {
                                    os.close();
                                }
                            } finally {
                                is.close();
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault ().notify (ex);
                        }
                    }
                });

            // init code folding bar
            if (root.getFileObject ("SideBar/org-netbeans-modules-languages-features-CodeFoldingSideBarFactory.instance") == null
                //l.supportsCodeFolding ()  does not work if you first open language without folding than no languages will have foding.
            ) {
                FileUtil.createData (root, "FoldManager/org-netbeans-modules-languages-features-LanguagesFoldManager$Factory.instance");
                FileUtil.createData (root, "SideBar/org-netbeans-modules-languages-features-CodeFoldingSideBarFactory.instance");
                FileObject fo = root.getFileObject ("SideBar");
                fo.setAttribute ("org-netbeans-editor-GlyphGutter.instance/org-netbeans-modules-languages-features-CodeFoldingSideBarFactory.instance", Boolean.TRUE);
            }

            // init error stripe
            if (root.getFileObject ("UpToDateStatusProvider/org-netbeans-modules-languages-features-UpToDateStatusProviderFactoryImpl.instance") == null
                //l.supportsCodeFolding ()  does not work if you first open language without folding than no languages will have foding.
            )
                FileUtil.createData (root, "UpToDateStatusProvider/org-netbeans-modules-languages-features-UpToDateStatusProviderFactoryImpl.instance");

                
            initPopupMenu (root, l);

            // init navigator
            if (l.getFeatures ("NAVIGATOR") != null) {
                String foldFileName = "Navigator/Panels/" + l.getMimeType () + 
                    "/org-netbeans-modules-languages-features-LanguagesNavigator.instance";
                if (fs.findResource (foldFileName) == null)
                    FileUtil.createData (fs.getRoot (), foldFileName);
            }

            // init tooltips
            if (l.getFeatures ("TOOLTIP") != null)
                FileUtil.createData (root, "ToolTips/org-netbeans-modules-languages-features-ToolTipAnnotation.instance");

            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        
        // init coloring
        ColorsManager.initColorings (l);
    }

    private static void initPopupMenu (FileObject root, Language l) throws IOException {
            FileObject popup = FileUtil.createFolder (root, "Popup");
            createSeparator (popup, "SeparatorAfterSelectInPopupAction", "org-netbeans-modules-editor-NbSelectInPopupAction.instance", null);
            List<Feature> actions = l.getFeatures ("ACTION");
            String lastAction = "SeparatorAfterSelectInPopupAction.instance";
            boolean actionAdded = false;
            Iterator<Feature> it = actions.iterator ();
            while (it.hasNext ()) {
                Feature action = it.next ();
                if (action.getBoolean ("explorer", false))
                    continue;
                actionAdded = true;
                String name = action.getSelector ().getAsString ();
                String displayName= l.localize((String)action.getValue ("name"));
                String performer = action.getMethodName ("performer");
                String enabler = action.getMethodName ("enabled");
                String installAfter = (String) action.getValue ("install_after");
                String installBefore = (String) action.getValue ("install_before");
                boolean separatorBefore = action.getBoolean ("separator_before", false);
                boolean separatorAfter = action.getBoolean ("separator_after", false);
                FileObject fobj = FileUtil.createData (popup, name + ".instance"); // NOI18N
                fobj.setAttribute("instanceCreate", new ActionCreator (new Object[] {displayName, performer, enabler})); // NOI18N
                fobj.setAttribute("instanceClass", "org.netbeans.modules.languages.features.GenericAction"); // NOI18N
                if (separatorBefore) {
                    createSeparator (popup, name + "_separator_before", installBefore, name + ".instance");
                    popup.setAttribute (name + "_separator_before/" + name + ".instance", Boolean.TRUE);
                } else
                if (installBefore != null)
                    popup.setAttribute (installBefore + "/" + name + ".instance", Boolean.TRUE);
                else
                popup.setAttribute (lastAction + "/" + name + ".instance", Boolean.TRUE);
                if (separatorAfter) {
                    createSeparator (popup, name + "_separator_after", installAfter, name + ".instance");
                    popup.setAttribute (name + "_separator_after/" + name + ".instance", Boolean.TRUE);
                } else
                if (installAfter != null)
                    popup.setAttribute (installAfter + "/" + name + ".instance", Boolean.TRUE);
                if (installAfter == null && installBefore == null)
                    lastAction = name + ".instance";
            }
            //popup.setAttribute (lastAction + "/org-netbeans-modules-languages-features-FormatAction.instance", Boolean.TRUE);
            //FileUtil.createData (popup, "org-netbeans-modules-languages-features-FormatAction.instance");
            
            if (actionAdded) {
                createSeparator (popup, "SeparatorBeforeCut", lastAction, "org-openide-actions-CutAction.instance");
            }
            createSeparator (popup, "SeparatorAfterPaste", "org-openide-actions-PasteAction.instance", "generate-fold-popup");
            FileUtil.createData (popup, "generate-fold-popup");
            // init actions
    }
    
    private String spacesToDashes(String text) {
        StringBuffer buf = new StringBuffer();
        int length = text.length();
        for (int x = 0; x < length; x++) {
            char c = text.charAt(x);
            buf.append(Character.isWhitespace(c) ? '_' : c);
        }
        return buf.toString();
    }

    private static void createSeparator (
        FileObject      folder,
        String          name,
        String          after,
        String          before
    ) throws IOException {
        name += ".instance";
        FileObject separator = FileUtil.createData (folder, name);
        separator.setAttribute ("instanceClass", "javax.swing.JSeparator");
        if (after != null)
            folder.setAttribute (after + "/" + name, Boolean.TRUE);
        if (before != null)
            folder.setAttribute (name + "/" + before, Boolean.TRUE);
    }
    
    
    // innerclasses ............................................................
    
    public static interface LanguagesManagerListener {
        
        public void languageChanged (String mimeType);
    }
    
    private class Listener implements FileChangeListener {
        
        public void fileAttributeChanged (FileAttributeEvent fe) {
        }
        public void fileChanged (FileEvent fe) {
            FileObject fo = fe.getFile ();
            String mimeType = fo.getParent ().getParent ().getName () + 
                '/' + fo.getParent ().getName ();
            languageChanged (mimeType);
        }
        public void fileDataCreated (FileEvent fe) {
        }
        public void fileDeleted (FileEvent fe) {
            FileObject fo = fe.getFile ();
            String mimeType = fo.getParent ().getName ();
            languageChanged (mimeType);
        }
        public void fileFolderCreated (FileEvent fe) {
        }
        public void fileRenamed (FileRenameEvent fe) {
        }
    }
}



