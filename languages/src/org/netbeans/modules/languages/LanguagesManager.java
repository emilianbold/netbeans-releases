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

import java.io.OutputStream;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.languages.parser.LanguageDefinitionNotFoundException;
import org.netbeans.modules.languages.parser.ParseException;
import org.netbeans.modules.languages.parser.SToken;
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

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesManager {
    
    private static LanguagesManager manager;
    
    public static LanguagesManager getDefault () {
        if (manager == null) {
            manager = new LanguagesManager ();
        }
        return manager;
    }
    
    private Set mimeTypes = null;
    
    public Set getSupportedMimeTypes () {
        if (mimeTypes == null)
            mimeTypes = MimeTypesReader.loadMimeTypes ();
        return Collections.unmodifiableSet (mimeTypes);
    }
//    
//    private Map mimeTypeToName = new HashMap ();
//    
//    public String getLanguageName (String mimeType) {
//        if (!mimeTypeToName.containsKey (mimeType)) {
//            FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
//            FileObject fo = fs.findResource ("Editors/" + mimeType);
//            if (fo == null) return "???";
//            String bundleName = (String) fo.getAttribute ("SystemFileSystem.localizingBundle");
//            String name = mimeType;
//            if (bundleName != null)
//                try {
//                    name = NbBundle.getBundle (bundleName).getString (mimeType);
//                } catch (MissingResourceException ex) {}
//            mimeTypeToName.put (mimeType, name);
//        }
//        return (String) mimeTypeToName.get (mimeType);
//    }
    
    private Map mimeTypeToLanguage = new HashMap ();
    
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
    
    private Vector listeners = new Vector ();
    
    public void addLanguagesManagerListener (LanguagesManagerListener l) {
        listeners.add (l);
    }
    
    public void removeLanguagesManagerListener (LanguagesManagerListener l) {
        listeners.remove (l);
    }
    
    void languageChanged (String mimeType) {
        mimeTypeToLanguage.remove (mimeType);
        Vector v = (Vector) listeners.clone ();
        Iterator it = v.iterator ();
        while (it.hasNext ()) {
            LanguagesManagerListener l = (LanguagesManagerListener) it.next ();
            l.languageChanged (mimeType);
        }
    }

    private Set listeningOn = new HashSet ();
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
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        final FileObject root = fs.findResource ("Editors/" + l.getMimeType ());
        
        // init old options
        if (root.getFileObject ("Settings.settings") == null) {
            try {
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
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        }
        
        // init code folding bar
        if (root.getFileObject ("SideBar/org-netbeans-modules-languages-fold-CodeFoldingSideBarFactory.instance") == null
            //l.supportsCodeFolding ()  does not work if you first open language without folding than no languages will have foding.
        ) {
            try {
                FileUtil.createData (root, "FoldManager/org-netbeans-modules-languages-fold-LanguagesFoldManager$Factory.instance");
                FileUtil.createData (root, "SideBar/org-netbeans-modules-languages-fold-CodeFoldingSideBarFactory.instance");
                FileObject fo = root.getFileObject ("SideBar");
                fo.setAttribute ("org-netbeans-editor-GlyphGutter.instance/org-netbeans-modules-languages-fold-CodeFoldingSideBarFactory.instance", Boolean.TRUE);
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        }
        
        // init error stripe
        if (root.getFileObject ("UpToDateStatusProvider/org-netbeans-modules-languages-fold-UpToDateStatusProviderFactoryImpl.instance") == null
            //l.supportsCodeFolding ()  does not work if you first open language without folding than no languages will have foding.
        ) {
            try {
                FileUtil.createData (root, "UpToDateStatusProvider/org-netbeans-modules-languages-fold-UpToDateStatusProviderFactoryImpl.instance");
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        }
        
        // init FormatAction
        try {
            FileUtil.createData (root, "Popup/org-netbeans-modules-languages-FormatAction.instance");
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
        
        // init actions
        List[] actionsList = (List[])l.getProperty(l.getMimeType(), Language.ACTION);
        if (actionsList != null && actionsList[0] != null) {
            for (Iterator iter = actionsList[0].iterator(); iter.hasNext(); ) {
                Evaluator[] evals = (Evaluator[]) iter.next();
                try {
                    String actionName = (String) evals[0].evaluate();
                    String performer = ((Evaluator.Method)evals[1]).getMethodName();
                    String enabler = evals[2] instanceof Evaluator.Method ? 
                        ((Evaluator.Method)evals[2]).getMethodName() : null;
                    FileObject fobj = FileUtil.createData (root, 
                            "Popup/" + spacesToDashes(actionName) + ".instance"); // NOI18N
                    fobj.setAttribute("instanceCreate", new ActionCreator(new Object[] {
                        actionName, performer, enabler})); // NOI18N
                    fobj.setAttribute("instanceClass", "org.netbeans.modules.languages.GenericAction"); // NOI18N
                } catch (IOException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            }
        }
        
        // init navigator
        if (l.supportsFeature (Language.NAVIGATOR)) {
            String foldFileName = "Navigator/Panels/" + l.getMimeType () + 
                "/org-netbeans-modules-languages-fold-LanguagesNavigator.instance";
            if (fs.findResource (foldFileName) == null) {
                try {
                    FileUtil.createData (fs.getRoot (), foldFileName);
                } catch (IOException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            }
        }
        
        // init tooltips
        if (l.supportsFeature (Language.TOOLTIP)) {
            try {
                FileUtil.createData (root, "ToolTips/org-netbeans-modules-languages-fold-ToolTipAnnotation.instance");
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        }
        
        // init coloring
    // default categories
        Collection defaults = EditorSettings.getDefault ().
            getDefaultFontColorDefaults ("NetBeans");
        Map defaultsMap = new HashMap ();
        Iterator it = defaults.iterator (); // check if IDE Defaults module is installed
        while (it.hasNext ()) {
            AttributeSet as = (AttributeSet) it.next ();
            defaultsMap.put (
                as.getAttribute (StyleConstants.NameAttribute),
                as
            );
        }
        // current colors
        FontColorSettingsFactory fcsf = EditorSettings.getDefault ().
            getFontColorSettings (new String[] {l.getMimeType ()});
        Collection colors = fcsf.getAllFontColors ("NetBeans");
        Map colorsMap = new HashMap ();
        it = colors.iterator ();
        while (it.hasNext ()) {
            AttributeSet as = (AttributeSet) it.next ();
            colorsMap.put (
                as.getAttribute (StyleConstants.NameAttribute),
                as
            );
        }
        it = l.getParser ().getTokens ().iterator ();
        while (it.hasNext ()) {
            SToken token = (SToken) it.next ();
            SimpleAttributeSet as = (SimpleAttributeSet) l.getFeature 
                (Language.COLOR, token);
            String colorName = as == null ? token.getType () :
                (String) as.getAttribute (StyleConstants.NameAttribute);
            addColor (colorName, as, l, colorsMap, defaultsMap);
        }
        addColor ("error", null, l, colorsMap, defaultsMap);
        fcsf.setAllFontColorsDefaults ("NetBeans", colorsMap.values ());
        fcsf.setAllFontColors ("NetBeans", colorsMap.values ());
    }
    
    private void addColor (
        String colorName, 
        SimpleAttributeSet sas,
        Language l, 
        Map colorsMap, 
        Map defaultsMap
    ) {
        String color = sas == null ? 
            colorName :
            (String) sas.getAttribute (StyleConstants.NameAttribute);
        if (sas == null)
            sas = new SimpleAttributeSet ();
        else
            sas = new SimpleAttributeSet (sas);
        sas.addAttribute (StyleConstants.NameAttribute, color);
        sas.addAttribute (EditorStyleConstants.DisplayName, color);
        if (!sas.isDefined (EditorStyleConstants.Default)) {
            String def = color;
            int i = def.lastIndexOf ('-');
            if (i > 0) def = def.substring (i + 1);
            if (defaultsMap.containsKey (def))
                sas.addAttribute (EditorStyleConstants.Default, def);
        }
        colorsMap.put (color, sas);
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



