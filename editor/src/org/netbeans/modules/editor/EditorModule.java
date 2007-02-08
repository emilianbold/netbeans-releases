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

package org.netbeans.modules.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.FindSupport;
import org.netbeans.editor.FindSupport.SearchPatternWrapper;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;
import org.netbeans.modules.editor.options.AllOptions;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.modules.editor.options.AnnotationTypesFolder;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.cookies.EditorCookie;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openidex.search.SearchHistory;
import org.openidex.search.SearchPattern;

/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class EditorModule extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(EditorModule.class.getName());
    
    private static final boolean debug = Boolean.getBoolean("netbeans.debug.editor.kits");

    private PropertyChangeListener searchSelectedPatternListener;
    private PropertyChangeListener editorHistoryChangeListener;

    /** Module installed again. */
    public void restored () {
        LocaleSupport.addLocalizer(new NbLocalizer(AllOptions.class));
        LocaleSupport.addLocalizer(new NbLocalizer(BaseKit.class));

        // register loader for annotation types
        AnnotationTypes.getTypes().registerLoader( new AnnotationTypes.Loader() {
                public void loadTypes() {
                    AnnotationTypesFolder.getAnnotationTypesFolder();
                }
                public void loadSettings() {
                    // AnnotationType properties are stored in BaseOption, so let's read them now
                    BaseOptions bo = (BaseOptions)BaseOptions.findObject(BaseOptions.class, true);

                    Integer i = (Integer)bo.getSettingValue(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA);
                    if (i != null)
                        AnnotationTypes.getTypes().setBackgroundGlyphAlpha(i.intValue());
                    Boolean b = (Boolean)bo.getSettingValue(AnnotationTypes.PROP_BACKGROUND_DRAWING);
                    if (b != null)
                        AnnotationTypes.getTypes().setBackgroundDrawing(b);
                    b = (Boolean)bo.getSettingValue(AnnotationTypes.PROP_COMBINE_GLYPHS);
                    if (b != null)
                        AnnotationTypes.getTypes().setCombineGlyphs(b);
                    b = (Boolean)bo.getSettingValue(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS);
                    if (b != null)
                        AnnotationTypes.getTypes().setGlyphsOverLineNumbers(b);
                    b = (Boolean)bo.getSettingValue(AnnotationTypes.PROP_SHOW_GLYPH_GUTTER);
                    if (b != null)
                        AnnotationTypes.getTypes().setShowGlyphGutter(b);
                }
                public void saveType(AnnotationType type) {
                    AnnotationTypesFolder.getAnnotationTypesFolder().saveAnnotationType(type);
                }
                public void saveSetting(String settingName, Object value) {
                    // AnnotationType properties are stored to BaseOption
                    BaseOptions bo = (BaseOptions)BaseOptions.findObject(BaseOptions.class, true);
                    bo.setSettingValue(settingName, value);
                }
            } );

        // ------------------------------------------------------------
        // Autoregistration
            
        // First, initialize JDK's editor kit types registry
        initAndCheckEditorKitTypeRegistry("text/plain", null);
        initAndCheckEditorKitTypeRegistry("text/html", HTMLEditorKit.class.getName());
        initAndCheckEditorKitTypeRegistry("text/rtf", RTFEditorKit.class.getName());
        initAndCheckEditorKitTypeRegistry("application/rtf", RTFEditorKit.class.getName());
            
        // Now hook up to the JDK's editor kit registry
        try {
            Field keyField = JEditorPane.class.getDeclaredField("kitRegistryKey");  // NOI18N
            keyField.setAccessible(true);
            Object key = keyField.get(JEditorPane.class);
            // XXX this is illegal! Must use reflection and have a proper fallback.
            Hashtable kitMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
            sun.awt.AppContext.getAppContext().put(key, new HackMap(kitMapping));
        } catch (Throwable t) {
            t.printStackTrace();
        }
            
        // Registration of the editor kits to JEditorPane
//        for (int i = 0; i < replacements.length; i++) {
//            JEditorPane.registerEditorKitForContentType(
//                replacements[i].contentType,
//                replacements[i].newKitClassName,
//                getClass().getClassLoader()
//            );
//        }

        // ------------------------------------------------------------
         
         searchSelectedPatternListener = new PropertyChangeListener(){
             
             public void propertyChange(PropertyChangeEvent evt){
                 if (evt == null){
                     return;
                 }

                 FindSupport fs = FindSupport.getFindSupport();                 
                 if (SearchHistory.LAST_SELECTED.equals(evt.getPropertyName())){
                     //System.out.println("API -> editor:");                
                     SearchPattern sp = SearchHistory.getDefault().getLastSelected();
                     if (sp==null){
                         return;
                     }

                     FindSupport.SearchPatternWrapper spw = new FindSupport.SearchPatternWrapper(sp.getSearchExpression(),
                             sp.isWholeWords(), sp.isMatchCase(), sp.isRegExp());
                     //System.out.println("spw:"+spw);
                     fs.setLastSelected(spw);
                 } else if (SearchHistory.ADD_TO_HISTORY.equals(evt.getPropertyName())){
                     List searchPatterns = SearchHistory.getDefault().getSearchPatterns();

                     List history = new ArrayList();
                     for (int i = 0; i<searchPatterns.size(); i++){
                         SearchPattern sptr = ((SearchPattern)searchPatterns.get(i));
                         SearchPatternWrapper spwrap = new SearchPatternWrapper(sptr.getSearchExpression(),
                                 sptr.isWholeWords(), sptr.isMatchCase(), sptr.isRegExp());
                         history.add(spwrap);
                     }

                     fs.setHistory(history);
                 }
             }
         };
         
         editorHistoryChangeListener =  new PropertyChangeListener(){
             public void propertyChange(PropertyChangeEvent evt){
                 
                 if (evt == null || !FindSupport.FIND_HISTORY_PROP.equals(evt.getPropertyName())){
                     return;
                 }
 
                 //System.out.println("");
                 //System.out.println("editor -> API");
                 
                 SearchPatternWrapper spw = (SearchPatternWrapper)evt.getNewValue();
                 SearchPattern spLast = SearchHistory.getDefault().getLastSelected();
                 
                 if (spw == null || spw.getSearchExpression()==null || "".equals(spw.getSearchExpression()) ){ //NOI18N
                     return;
                 }
                 //System.out.println("spw:"+spw);
                 
                 SearchPattern sp = SearchPattern.create(spw.getSearchExpression(),
                         spw.isWholeWords(), spw.isMatchCase(), spw.isRegExp());
                 
                 if (sp == null || (sp.equals(spLast))){
                     return;
                 }
                 
                 SearchHistory.getDefault().add(sp);
                 SearchHistory.getDefault().setLastSelected(sp);
             }
         };
 
         SearchHistory.getDefault().addPropertyChangeListener(searchSelectedPatternListener);
         FindSupport.getFindSupport().addPropertyChangeListener(editorHistoryChangeListener);
         
         // TEMP start
         /*
         final int fired[] = new int[1];
         fired[0] = 0;
         
         Timer timer;
         timer = new Timer(15000, new ActionListener(){
                 public void actionPerformed(ActionEvent e){
                    SearchPattern p;
                     p = SearchPattern.create(String.valueOf(fired[0]),false,false,false);
                     SearchHistory.getDefault().add(p);                    
                     SearchHistory.getDefault().setLastSelected(p);
                     fired[0] ++;
                 }
         });
         timer.start();
         */
         //TEMP end

            
    }

    /** Called when module is uninstalled. Overrides superclass method. */
    public void uninstalled() {

        AllOptionsFolder.unregisterModuleRegListener();

        /* [TEMP]
        if (searchSelectedPatternListener!=null){
            SearchHistory.getDefault().removePropertyChangeListener(searchSelectedPatternListener);
        }
        */
         
        // unregister our registry
        try {
            Field keyField = JEditorPane.class.getDeclaredField("kitRegistryKey");  // NOI18N
            keyField.setAccessible(true);
            Object key = keyField.get(JEditorPane.class);
            HackMap kitMapping = (HackMap)sun.awt.AppContext.getAppContext().get(key);
            if (kitMapping.getOriginal() != null) {
                sun.awt.AppContext.getAppContext().put(key, kitMapping.getOriginal());
            } else {
                sun.awt.AppContext.getAppContext().remove(key);
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        // #42970 - Possible closing of opened editor top components must happen in AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                // issue #16110
                // close all TopComponents which contain editor based on BaseKit
                HashSet set = new HashSet();
                set.addAll(TopComponent.getRegistry().getOpened());

                for (Iterator it = set.iterator(); it.hasNext(); ) {
                    TopComponent topComp = (TopComponent)it.next();
                    // top components in which we are interested must be of type CloneableEditor
                    if (!(topComp instanceof CloneableEditor))
                        continue;
                    Node[] arr = topComp.getActivatedNodes();
                    if (arr == null)
                        continue;
                    for (int i=0; i<arr.length; i++) {
                        EditorCookie ec = (EditorCookie)arr[i].getCookie(EditorCookie.class);
                        if (ec == null)
                            continue;
                        JEditorPane[] pane = ec.getOpenedPanes();
                        if (pane == null) 
                            continue;
                        for (int j=0; j<pane.length; j++) {
                            if (pane[j].getEditorKit() instanceof BaseKit) {
                                topComp.close();
                            }
                        }
                    }
                }

            }
        });
    }

    
    private static class HackMap extends Hashtable {
        
	private Hashtable delegate;

        HackMap(Hashtable h) {
            delegate = h;
            
            if (debug) {
                LOG.log(Level.INFO, "Original kit mappings: " + h); //NOI18N

                try {
                    Field keyField = JEditorPane.class.getDeclaredField("kitTypeRegistryKey");  // NOI18N
                    keyField.setAccessible(true);
                    Object key = keyField.get(JEditorPane.class);
                    Hashtable kitTypeMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
                    if (kitTypeMapping != null) {
                        sun.awt.AppContext.getAppContext().put(key, new DebugHashtable(kitTypeMapping));
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        private String getKitClassName(String type) {
            try {
                Field keyField = JEditorPane.class.getDeclaredField("kitTypeRegistryKey");  // NOI18N
                keyField.setAccessible(true);
                Object key = keyField.get(JEditorPane.class);
                Hashtable kitTypeMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
                if (kitTypeMapping != null) {
                    return (String)kitTypeMapping.get(type);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            
            return null;
        }
        
        public Object get(Object key) {
            synchronized (Settings.class) {
            if (debug) LOG.log(Level.INFO, "HackMap.get key=" + key); //NOI18N
            
            Object retVal = null;
            
            if (delegate != null) {
                retVal = delegate.get(key);
                if (debug && retVal != null) {
                    LOG.log(Level.INFO, "Found cached instance kit=" + retVal + " for mimeType=" + key); //NOI18N
                }
            }

            if (key instanceof String) {
                String mimeType = (String) key;
                if (retVal == null || shouldUseNbKit(retVal.getClass().getName(), mimeType)) {
                    // first check the type registry
                    String kitClassName = getKitClassName(mimeType);
                    if (debug) {
                        LOG.log(Level.INFO, "Found kitClassName=" + kitClassName + " for mimeType=" + mimeType); //NOI18N
                    }

                    if (kitClassName == null || shouldUseNbKit(kitClassName, mimeType)) {
                        Object kit = findKit(mimeType);
                        if (kit != null) {
                            retVal = kit;
                            if (debug) {
                                LOG.log(Level.INFO, "Found kit=" + retVal + " in xml layers for mimeType=" + mimeType); //NOI18N
                            }
                        }
                    }
                }
            }
            
            return retVal;
            } // synchronized (Settings.class)
        }
        
        public Object put(Object key, Object value) {
            synchronized (Settings.class) {
            if (debug) LOG.log(Level.INFO, "HackMap.put key=" + key + " value=" + value); //NOI18N
            
            if (delegate == null) {
                delegate = new Hashtable();
            }

            Object ret = delegate.put(key,value);
            
            if (debug) {
                LOG.log(Level.INFO, "registering mimeType=" + key //NOI18N
                    + " -> kitInstance=" + value // NOI18N
                    + " original was " + ret); // NOI18N
            }
             
            return ret;
            } // synchronized (Settings.class)
        }

        public Object remove(Object key) {
            synchronized (Settings.class) {
            if (debug) LOG.log(Level.INFO, "HackMap.remove key=" + key); //NOI18N
            
            Object ret = (delegate != null) ? delegate.remove(key) : null;
            
            if (debug) {
                LOG.log(Level.INFO, "removing kitInstance=" + ret //NOI18N
                    + " for mimeType=" + key); // NOI18N
            }
            
            return ret;
            } // synchronized (Settings.class)
        }
        
        Hashtable getOriginal() {
            return delegate;
        }

        private boolean shouldUseNbKit(String kitClass, String mimeType) {
            if (mimeType.startsWith("text/html") || //NOI18N
                mimeType.startsWith("text/rtf") || //NOI18N
                mimeType.startsWith("application/rtf")) //NOI18N
            {
                return false;
            } else {
                return kitClass.startsWith("javax.swing."); //NOI18N
            }
        }

        // Don't use CloneableEditorSupport.getEditorKit so that it can safely
        // fallback to JEP.createEKForCT if it doesn't find Netbeans kit.
        private EditorKit findKit(String mimeType) {
            Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
            EditorKit kit = (EditorKit) lookup.lookup(EditorKit.class);
            return kit == null ? null : (EditorKit) kit.clone();
        }
    }
    
    private static final class DebugHashtable extends Hashtable {
        
        DebugHashtable(Hashtable h) {
            if (h != null) {
                putAll(h);
                LOG.log(Level.INFO, "Existing kit classNames mappings: " + this); //NOI18N
            }
        }
        
        public Object put(Object key, Object value) {
            Object ret = super.put(key, value);
            LOG.log(Level.INFO, "registering mimeType=" + key //NOI18N
                + " -> kitClassName=" + value // NOI18N
                + " original was " + ret); // NOI18N
            return ret;
        }
        
        public Object remove(Object key) {
            Object ret = super.remove(key);
            LOG.log(Level.INFO, "removing kitClassName=" + ret //NOI18N
                + " for mimeType=" + key); // NOI18N
            return ret;
        }
        
    }

    private void initAndCheckEditorKitTypeRegistry(String mimeType, String expectedKitClass) {
        String kitClass = JEditorPane.getEditorKitClassNameForContentType(mimeType);
        if (kitClass == null) {
            LOG.log(Level.WARNING, "Can't find JDK editor kit class for " + mimeType); //NOI18N
        } else if (expectedKitClass != null && !expectedKitClass.equals(kitClass)) {
            LOG.log(Level.WARNING, "Wrong JDK editor kit class for " + mimeType + //NOI18N
                ". Expecting: " + expectedKitClass + //NOI18N
                ", but was: " + kitClass); //NOI18N
        }
    }
}
