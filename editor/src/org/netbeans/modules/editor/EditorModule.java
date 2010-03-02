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

package org.netbeans.modules.editor;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
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
import org.netbeans.modules.editor.lib2.EditorApiPackageAccessor;
import org.netbeans.modules.editor.options.AnnotationTypesFolder;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.openide.cookies.EditorCookie;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
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
    private PropertyChangeListener topComponentRegistryListener;

    /** Module installed again. */
    public @Override void restored () {
        LocaleSupport.addLocalizer(new NbLocalizer(BaseKit.class));

        // register loader for annotation types
        AnnotationTypes.getTypes().registerLoader( new AnnotationTypes.Loader() {
                public void loadTypes() {
                    AnnotationTypesFolder.getAnnotationTypesFolder();
                }
                public void loadSettings() {
                    // AnnotationType properties are stored in BaseOption, so let's read them now
                    Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);

                    int i = prefs.getInt(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA, Integer.MIN_VALUE);
                    if (i != Integer.MIN_VALUE) {
                        AnnotationTypes.getTypes().setBackgroundGlyphAlpha(i);
                    }
                    
                    boolean b = prefs.getBoolean(AnnotationTypes.PROP_BACKGROUND_DRAWING, false);
                    AnnotationTypes.getTypes().setBackgroundDrawing(b);
                    
                    b = prefs.getBoolean(AnnotationTypes.PROP_COMBINE_GLYPHS, true);
                    AnnotationTypes.getTypes().setCombineGlyphs(b);
                    
                    b = prefs.getBoolean(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS, true);
                    AnnotationTypes.getTypes().setGlyphsOverLineNumbers(b);
                    
                    b = prefs.getBoolean(AnnotationTypes.PROP_SHOW_GLYPH_GUTTER, true);
                    AnnotationTypes.getTypes().setShowGlyphGutter(b);
                }
                public void saveType(AnnotationType type) {
                    AnnotationTypesFolder.getAnnotationTypesFolder().saveAnnotationType(type);
                }
                public void saveSetting(String settingName, Object value) {
                    // AnnotationType properties are stored to BaseOption
                    Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
                    if (value instanceof Integer) {
                        prefs.putInt(settingName, (Integer) value);
                    } else if (value instanceof Boolean) {
                        prefs.putBoolean(settingName, (Boolean) value);
                    } else if (value != null) {
                        prefs.put(settingName, value.toString());
                    } else {
                        prefs.remove(settingName);
                    }
                }
            } );

        // ------------------------------------------------------------
        // Autoregistration
            
        // First, initialize JDK's editor kit types registry
        initAndCheckEditorKitTypeRegistry("text/plain", null); //NOI18N
        initAndCheckEditorKitTypeRegistry("text/html", HTMLEditorKit.class.getName()); //NOI18N
        initAndCheckEditorKitTypeRegistry("text/rtf", RTFEditorKit.class.getName()); //NOI18N
        initAndCheckEditorKitTypeRegistry("application/rtf", RTFEditorKit.class.getName()); //NOI18N
            
        // Now hook up to the JDK's editor kit registry
        // XXX: This all should be removed, see IZ #80110
        try {
            Field keyField = JEditorPane.class.getDeclaredField("kitRegistryKey");  // NOI18N
            keyField.setAccessible(true);
            Object key = keyField.get(JEditorPane.class);

            Class<?> appContextClass = ClassLoader.getSystemClassLoader().loadClass("sun.awt.AppContext"); //NOI18N
            Method getAppContext = appContextClass.getDeclaredMethod("getAppContext"); //NOI18N
            Method get = appContextClass.getDeclaredMethod("get", Object.class); //NOI18N
            Method put = appContextClass.getDeclaredMethod("put", Object.class, Object.class); //NOI18N
            
            Object appContext = getAppContext.invoke(null);
            Hashtable<?,?> kitMapping = (Hashtable<?,?>) get.invoke(appContext, key);
            put.invoke(appContext, key, new HackMap(kitMapping));

// REMOVE: we should not depend on sun.* classes
//            Hashtable kitMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
//            sun.awt.AppContext.getAppContext().put(key, new HackMap(kitMapping));
        } catch (Throwable t) {
            if (debug) {
                LOG.log(Level.WARNING, "Can't hack in to the JEditorPane's registry for kits.", t);
            } else {
                LOG.log(Level.WARNING, "Can''t hack in to the JEditorPane''s registry for kits: {0}", new Object[] {t});
            }
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

         if (GraphicsEnvironment.isHeadless()) {
             return;
         }

         EditorApiPackageAccessor.get().setIgnoredAncestorClass(TabbedContainer.class);
         if (topComponentRegistryListener == null) {
             topComponentRegistryListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
                        TopComponent tc = (TopComponent)evt.getNewValue();
                        // Limit checking to editors and multiviews only - should suffice
                        // if not then assign: doClose = true;
                        boolean doNotify = (tc instanceof CloneableEditorSupport.Pane);
                        LOG.finest("CLOSE-TC: doClose=" + doNotify + ", TC=" + tc + "\n");
                        if (doNotify) {
                            EditorApiPackageAccessor.get().notifyClose(tc);
                        }
                    }
                }
            };
            TopComponent.getRegistry().addPropertyChangeListener(topComponentRegistryListener);
         }

         if (LOG.isLoggable(Level.INFO)) {
             WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                public void run() {
                    try {
                        Field kitsField = BaseKit.class.getDeclaredField("kits");
                        kitsField.setAccessible(true);
                        Map kitsMap = (Map) kitsField.get(null);
                        LOG.fine("Number of loaded editor kits: " + kitsMap.size());
                    } catch (Exception e) {
                        // ignore
                    }
                }
            });
         }
    }

    /** Called when module is uninstalled. Overrides superclass method. */
    public @Override void uninstalled() {

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
            
            Class appContextClass = getClass().getClassLoader().loadClass("sun.awt.AppContext"); //NOI18N
            Method getAppContext = appContextClass.getDeclaredMethod("getAppContext"); //NOI18N
            Method get = appContextClass.getDeclaredMethod("get", Object.class); //NOI18N
            Method put = appContextClass.getDeclaredMethod("put", Object.class, Object.class); //NOI18N
            Method remove = appContextClass.getDeclaredMethod("remove", Object.class, Object.class); //NOI18N
            
            Object appContext = getAppContext.invoke(null);
            Hashtable kitMapping = (Hashtable) get.invoke(appContext, key);

            if (kitMapping instanceof HackMap) {
                if (((HackMap) kitMapping).getOriginal() != null) {
                    put.invoke(appContext, key, new HackMap(kitMapping));
                } else {
                    remove.invoke(appContext, key);
                }
            }
            
// REMOVE: we should not depend on sun.* classes
//            HackMap kitMapping = (HackMap)sun.awt.AppContext.getAppContext().get(key);
//            if (kitMapping.getOriginal() != null) {
//                sun.awt.AppContext.getAppContext().put(key, kitMapping.getOriginal());
//            } else {
//                sun.awt.AppContext.getAppContext().remove(key);
//            }
        } catch (Throwable t) {
            if (debug) {
                LOG.log(Level.WARNING, "Can't release the hack from the JEditorPane's registry for kits.", t);
            } else {
                LOG.log(Level.WARNING, "Can't release the hack from the JEditorPane's registry for kits.");
            }
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
                
                // Remove listening on top component registry
                if (topComponentRegistryListener != null) {
                    TopComponent.getRegistry().removePropertyChangeListener(topComponentRegistryListener);
                    topComponentRegistryListener = null;
                }
            }
        });
    }

    
    private static class HackMap extends Hashtable {
        
        private final Object LOCK = new String("EditorModule.HackMap.LOCK"); //NOI18N
        
	private Hashtable delegate;

        HackMap(Hashtable h) {
            delegate = h;
            
            if (debug) {
                LOG.log(Level.INFO, "Original kit mappings: " + h); //NOI18N

                try {
                    Field keyField = JEditorPane.class.getDeclaredField("kitTypeRegistryKey");  // NOI18N
                    keyField.setAccessible(true);
                    Object key = keyField.get(JEditorPane.class);
                    
                    Class appContextClass = getClass().getClassLoader().loadClass("sun.awt.AppContext"); //NOI18N
                    Method getAppContext = appContextClass.getDeclaredMethod("getAppContext"); //NOI18N
                    Method get = appContextClass.getDeclaredMethod("get", Object.class); //NOI18N
                    Method put = appContextClass.getDeclaredMethod("put", Object.class, Object.class); //NOI18N

                    Object appContext = getAppContext.invoke(null);
                    Hashtable kitTypeMapping = (Hashtable) get.invoke(appContext, key);

                    if (kitTypeMapping != null) {
                        put.invoke(appContext, key, new DebugHashtable(kitTypeMapping));
                    }
                    
// REMOVE: we should not depend on sun.* classes
//                    Hashtable kitTypeMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
//                    if (kitTypeMapping != null) {
//                        sun.awt.AppContext.getAppContext().put(key, new DebugHashtable(kitTypeMapping));
//                    }
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Can't hack in to the JEditorPane's registry for kit types.", t);
                }
            }
        }

        private String getKitClassName(String type) {
            try {
                Field keyField = JEditorPane.class.getDeclaredField("kitTypeRegistryKey");  // NOI18N
                keyField.setAccessible(true);
                Object key = keyField.get(JEditorPane.class);
                
                Class appContextClass = getClass().getClassLoader().loadClass("sun.awt.AppContext"); //NOI18N
                Method getAppContext = appContextClass.getDeclaredMethod("getAppContext"); //NOI18N
                Method get = appContextClass.getDeclaredMethod("get", Object.class); //NOI18N

                Object appContext = getAppContext.invoke(null);
                Hashtable kitTypeMapping = (Hashtable) get.invoke(appContext, key);

                if (kitTypeMapping != null) {
                    return (String) kitTypeMapping.get(type);
                }

// REMOVE: we should not depend on sun.* classes
//                Hashtable kitTypeMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
//                if (kitTypeMapping != null) {
//                    return (String)kitTypeMapping.get(type);
//                }
            } catch (Throwable t) {
                if (debug) {
                    LOG.log(Level.WARNING, "Can't hack in to the JEditorPane's registry for kit types.", t);
                } else {
                    LOG.log(Level.WARNING, "Can't hack in to the JEditorPane's registry for kit types.");
                }
            }
            
            return null;
        }
        
        public @Override Object get(Object key) {
            synchronized (LOCK) {
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
        
        public @Override Object put(Object key, Object value) {
            synchronized (LOCK) {
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

        public @Override Object remove(Object key) {
            synchronized (LOCK) {
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
            if (!MimePath.validate(mimeType)) // #146276 - exclude invalid mime paths
                return null;
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
        
        public @Override Object put(Object key, Object value) {
            Object ret = super.put(key, value);
            LOG.log(Level.INFO, "registering mimeType=" + key //NOI18N
                + " -> kitClassName=" + value // NOI18N
                + " original was " + ret); // NOI18N
            return ret;
        }
        
        public @Override Object remove(Object key) {
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
