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

import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.features.ActionCreator;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.modules.languages.features.ColorsManager;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Jan Jancura
 */
public class LanguagesManager extends org.netbeans.api.languages.LanguagesManager {
    
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

    public boolean createDataObjectFor (String mimeType) {
        if(!isSupported(mimeType)) {
            return false;
        }
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject fo = fs.findResource ("Editors/" + mimeType);
        if (fo == null) return false;
        Boolean b = (Boolean) fo.getAttribute ("createDataObject");
        if (b == null) return true;
        return b.booleanValue ();
    }
    
    private Language parsingLanguage = new Language ("parsing...");
    
    private Map<String,Language> mimeTypeToLanguage = new HashMap<String,Language> ();
    
    public synchronized Language getLanguage (String mimeType) 
    throws LanguageDefinitionNotFoundException {
        if (!mimeTypeToLanguage.containsKey (mimeType)) {
            mimeTypeToLanguage.put (mimeType, parsingLanguage);
            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
            FileObject fo = fs.findResource ("Editors/" + mimeType + "/language.nbs");
            if (fo == null) {
                mimeTypeToLanguage.remove (mimeType);
                throw new LanguageDefinitionNotFoundException 
                    ("Language definition for " + mimeType + " not found.");
            }
            addListener (fo);
            Language l = null;
            try {
                l = NBSLanguageReader.readLanguage (fo, mimeType);
                l.getAnalyser ();
                initLanguage (l);
            } catch (ParseException ex) {
                l = new Language (mimeType);
                Utils.message (ex.getMessage ());
            } catch (IOException ex) {
                l = new Language (mimeType);
                Utils.message ("Editors/" + mimeType + "/language.nbs: " + ex.getMessage ());
            }
            //l.print ();
            mimeTypeToLanguage.put (mimeType, l);
        }
        if (parsingLanguage == mimeTypeToLanguage.get (mimeType))
            throw new IllegalArgumentException ();
        return mimeTypeToLanguage.get (mimeType);
    }
    
    public void addLanguage (Language l) {
        mimeTypeToLanguage.put (l.getMimeType (), l);
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
        Vector<LanguagesManagerListener> v = new Vector<LanguagesManagerListener>(listeners);
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
                            Utils.notify (ex);
                        }
                    }
                });

            // init code folding bar
            if (root.getFileObject ("SideBar/org-netbeans-modules-languages-features-CodeFoldingSideBarFactory.instance") == null &&
                l.getFeatures("FOLD").size () > 0
            ) {
                FileUtil.createData (root, "FoldManager/org-netbeans-modules-languages-features-LanguagesFoldManager$Factory.instance");
                FileUtil.createData(root, "SideBar/org-netbeans-modules-languages-features-CodeFoldingSideBarFactory.instance").
                        // Can tune position to whatever seems right; at least put after org-netbeans-editor-GlyphGutter.instance:
                        setAttribute("position", 1000);
            }

            // init error stripe
            if (root.getFileObject ("UpToDateStatusProvider/org-netbeans-modules-languages-features-UpToDateStatusProviderFactoryImpl.instance") == null
                //l.supportsCodeFolding ()  does not work if you first open language without folding than no languages will have foding.
            )
                FileUtil.createData (root, "UpToDateStatusProvider/org-netbeans-modules-languages-features-UpToDateStatusProviderFactoryImpl.instance");

                
            initPopupMenu (root, l);

            // init navigator
            if (l.getFeatures ("NAVIGATOR").size () > 0) {
                String foldFileName = "Navigator/Panels/" + l.getMimeType () + 
                    "/org-netbeans-modules-languages-features-LanguagesNavigator.instance";
                if (fs.findResource (foldFileName) == null)
                    FileUtil.createData (fs.getRoot (), foldFileName);
            }

            // init tooltips
            if (l.getFeatures ("TOOLTIP").size () > 0)
                FileUtil.createData (root, "ToolTips/org-netbeans-modules-languages-features-ToolTipAnnotation.instance");

            if (l.getFeature("COMMENT_LINE") != null) {
                // init editor toolbar
                FileObject toolbarDefault = FileUtil.createFolder(root, "Toolbars/Default");
                createSeparator(
                        toolbarDefault,
                        "Separator-before-comment",
                        3000 // can tune to whatever; want after stop-macro-recording
                );
                FileUtil.createData(toolbarDefault, "comment").setAttribute("position", 3100);
                FileUtil.createData(toolbarDefault, "uncomment").setAttribute("position", 3200);
                
                if (root.getFileObject("Keybindings/NetBeans/Defaults/keybindings.xml") == null) {
                    fs.runAtomicAction (new AtomicAction () {
                        public void run () {
                            try {
                                InputStream is = getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/languages/resources/DefaultKeyBindings.xml");
                                try {
                                    FileObject fo = root.getFileObject("Keybindings/NetBeans/Defaults");
                                    if (fo == null) {
                                        fo = root.createFolder("Keybindings");
                                        fo = fo.createFolder("NetBeans");
                                        fo = fo.createFolder("Defaults");
                                    }
                                    FileObject bindings = fo.createData("keybindings.xml");
                                    OutputStream os = bindings.getOutputStream();
                                    try {
                                        FileUtil.copy(is, os);
                                    } finally {
                                        os.close();
                                    }
                                } finally {
                                    is.close();
                                }
                            } catch (IOException ex) {
                                Utils.notify (ex);
                            }
                        }
                    });
                }
            }
        } catch (IOException ex) {
            Utils.notify (ex);
        }
        
        // init coloring
        ColorsManager.initColorings (l);
    }

    private static void initPopupMenu (FileObject root, Language l) throws IOException {
            List<Feature> actions = l.getFeatures("ACTION");
            // Could probably use fixed anchor points if these positions settle down:
            int selectInPos = findPositionOfDefaultPopupAction("org-netbeans-modules-editor-NbSelectInPopupAction.instance", 1000);
            int increment = (findPositionOfDefaultPopupAction("org-openide-actions-CutAction.instance", 2000) - selectInPos) / (actions.size() + 3);
            FileObject popup = FileUtil.createFolder (root, "Popup");
            int pos = selectInPos + increment;
            createSeparator(popup, "SeparatorAfterSelectInPopupAction", pos);
            boolean actionAdded = false;
            if (l.getFeatures("SEMANTIC_USAGE").size() > 0) {
                actionAdded = true;
                pos += increment;
                FileUtil.createData (popup, "org-netbeans-modules-languages-features-GoToDeclarationAction.instance").setAttribute("position", pos);
            }
            for (Feature action : actions) {
                if (action.getBoolean ("explorer", false))
                    continue;
                actionAdded = true;
                pos += increment;
                String name = action.getSelector ().getAsString ();
                String displayName= l.localize((String)action.getValue ("name"));
                String performer = action.getMethodName ("performer");
                String enabler = action.getMethodName ("enabled");
                /* XXX disabled for now; could use numeric position key if desired:
                String installAfter = (String) action.getValue ("install_after");
                String installBefore = (String) action.getValue ("install_before");
                 */
                boolean separatorBefore = action.getBoolean ("separator_before", false);
                boolean separatorAfter = action.getBoolean ("separator_after", false);
                FileObject fobj = FileUtil.createData (popup, name + ".instance"); // NOI18N
                fobj.setAttribute("instanceCreate", new ActionCreator (new Object[] {displayName, performer, enabler})); // NOI18N
                fobj.setAttribute("instanceClass", "org.netbeans.modules.languages.features.GenericAction"); // NOI18N
                fobj.setAttribute("position", pos);
                if (separatorBefore) {
                    createSeparator(popup, name + "_separator_before", pos - increment / 3);
                }
                if (separatorAfter) {
                    createSeparator(popup, name + "_separator_after", pos + increment / 3);
                }
            }
            //FileUtil.createData (popup, "org-netbeans-modules-languages-features-FormatAction.instance").setAttribute("position", ...);
            if (actionAdded) {
                createSeparator(popup, "SeparatorBeforeCut", pos + increment);
            }
            if (l.getFeatures("FOLD").size() > 0) {
                FileUtil.createData (popup, "generate-fold-popup").setAttribute(
                    "position",
                    findPositionOfDefaultPopupAction("org-openide-actions-PasteAction.instance", 3000) + 50
                );
            }
            // init actions
    }
    
    private static int findPositionOfDefaultPopupAction(String name, int fallback) {
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Editors/Popup/" + name);
        if (f != null) {
            Object pos = f.getAttribute("position");
            if (pos instanceof Integer) {
                return (Integer) pos;
            }
        }
        return fallback;
    }
    
    private static void createSeparator (
        FileObject      folder,
        String          name,
        int position
    ) throws IOException {
        FileObject separator = FileUtil.createData(folder, name + ".instance");
        separator.setAttribute ("instanceClass", "javax.swing.JSeparator");
        separator.setAttribute("position", position);
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



