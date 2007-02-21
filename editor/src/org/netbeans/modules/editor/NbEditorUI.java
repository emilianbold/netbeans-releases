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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.windows.TopComponent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.CustomizableSideBar.SideBarPosition;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
* Editor UI
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorUI extends ExtEditorUI {

    private FocusListener focusL;

    private boolean attached = false;
    private ChangeListener listener;
    private FontColorSettings fontColorSettings;    
    private LookupListener weakLookupListener;    
    private Lookup.Result result;
    private LookupListener lookupListener;    
    
    private static final Map /*<mimeType, map of colorings>*/mime2Coloring = new HashMap(5);
    
    /**
     *
     * @deprecated - use {@link attachSystemActionPerformer(String)} instead
     */
    protected SystemActionUpdater createSystemActionUpdater(
        String editorActionName, boolean updatePerformer, boolean syncEnabling) {
        return new SystemActionUpdater(editorActionName, updatePerformer, syncEnabling);
    }

    public NbEditorUI() {
        focusL = new FocusAdapter() {
                     public void focusGained(FocusEvent evt) {
                         // Refresh file object when component made active
                         Document doc = getDocument();
                         if (doc != null) {
                             DataObject dob = NbEditorUtilities.getDataObject(doc);
                             if (dob != null) {
                                 final FileObject fo = dob.getPrimaryFile();
                                 if (fo != null) {
                                     // Fixed #48151 - posting the refresh outside of AWT thread
                                     RequestProcessor.getDefault().post(new Runnable() {
                                         public void run() {
                                             fo.refresh();
                                         }
                                     });
                                 }
                             }
                         }
                     }
                 };

    }
    
    
    private static Lookup getContextLookup(java.awt.Component component){
        Lookup lookup = null;
        for (java.awt.Component c = component; c != null; c = c.getParent()) {
            if (c instanceof Lookup.Provider) {
                lookup = ((Lookup.Provider)c).getLookup ();
                if (lookup != null) {
                    break;
                }
            }
        }
        return lookup;
    }
    
    protected void attachSystemActionPerformer(String editorActionName){
        new NbEditorUI.SystemActionPerformer(editorActionName);
    }

    private String getDocumentContentType(){
        JTextComponent c = getComponent();
        if (c == null){
            return null;
        }
        Document doc = c.getDocument();
        String mimeType = (String) doc.getProperty("mimeType");  //NOI18N
        if (mimeType == null){
            return null;
        }
        return mimeType;
    }
    
    private FontColorSettings getFontColorSettings(){
        synchronized (Settings.class){
            if (fontColorSettings == null){
                final String mimeType = getDocumentContentType();
                if (mimeType == null){
                    return null;
                }
                Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
                result = lookup.lookup(new Lookup.Template(FontColorSettings.class));
                Collection inst = result.allInstances();
                lookupListener = new MyLookupListener(mimeType);
                weakLookupListener = (LookupListener) WeakListeners.create(
                        LookupListener.class, lookupListener, result);
  
                result.addLookupListener(weakLookupListener);
                if (inst.size() > 0){
                    fontColorSettings = (FontColorSettings)inst.iterator().next();
                }
            }
        }
        return fontColorSettings;
    }

    
    protected Map createColoringMap(){
        FontColorSettings fcs = getFontColorSettings();
        String mimeType = getDocumentContentType();
        if (fcs == null || mimeType == null){
            return super.createColoringMap();
        }
        synchronized (Settings.class){
            Map cm = (Map)mime2Coloring.get(mimeType);
            if (cm != null){
                return cm;
            }
            cm = new HashMap();
            cm.putAll(super.createColoringMap());

            for(Iterator it = cm.keySet().iterator(); it.hasNext(); ) {

                Object nameObj = it.next();
                if (!(nameObj instanceof String)) {
                    continue;
                }
                
                String name = (String) nameObj;
                
                AttributeSet as = fcs.getTokenFontColors(name);
                if (as == null){
                    as = fcs.getFontColors(name);
                    if (as == null){
                        continue;
                    }
                }

                cm.put(name, Coloring.fromAttributeSet(as));
            }
            
            mime2Coloring.put(mimeType, cm);
            return cm;
        }
    }
    
    protected void installUI(JTextComponent c) {
        super.installUI(c);

        if (!attached){
            attachSystemActionPerformer(ExtKit.findAction);
            attachSystemActionPerformer(ExtKit.replaceAction);
            attachSystemActionPerformer(ExtKit.gotoAction);
            attachSystemActionPerformer(ExtKit.showPopupMenuAction);

            // replacing DefaultEditorKit.deleteNextCharAction by BaseKit.removeSelectionAction
            // #41223
            // attachSystemActionPerformer(BaseKit.removeSelectionAction);
            
            attached = true;
        }
        
        c.addFocusListener(focusL);
    }


    protected void uninstallUI(JTextComponent c) {
        super.uninstallUI(c);

        c.removeFocusListener(focusL);
    }
    
    protected JComponent createExtComponent() {

        final JTextComponent component = getComponent();
//        setLineNumberEnabled(true); // enable line numbering

        // extComponent will be a panel
        final JComponent ec = new JPanel(new BorderLayout());
        ec.putClientProperty(JTextComponent.class, component);

        // Add the scroll-pane with the component to the center
        final JScrollPane scroller = new JScrollPane(component);
        
        scroller.getViewport().setMinimumSize(new Dimension(4,4));

        // remove default scroll-pane border, winsys will handle borders itself 
        Border empty = BorderFactory.createEmptyBorder();
        // Important:  Do not delete or use null instead, will cause
        //problems on GTK L&F.  Must set both scroller border & viewport
        //border! - Tim
        scroller.setBorder(empty);
        scroller.setViewportBorder(empty);

        String mimeType = NbEditorUtilities.getMimeType(component);
        
        Map/*<SideBarPosition, JComponent>*/ sideBars = CustomizableSideBar.createSideBars(component);
        if (listener == null){
            listener = new ChangeListener(){
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    if (Utilities.getEditorUI(component) == null) {
                        return; //#63146
                    }
                    Map newMap = CustomizableSideBar.createSideBars(component);
                    processSideBars(newMap, scroller, ec);
                    ec.revalidate();
                    ec.repaint();
                    
                }
            };
            CustomizableSideBar.addChangeListener(mimeType, listener);
        }
        
        processSideBars(sideBars, scroller, ec);
        
        initGlyphCorner(scroller);

        ec.add(scroller);
        return ec;
    }
    

    public boolean isLineNumberEnabled() {
        return AllOptionsFolder.getDefault().getLineNumberVisible();
    }

    public void setLineNumberEnabled(boolean lineNumberEnabled) {
        AllOptionsFolder.getDefault().setLineNumberVisible(lineNumberEnabled);
    }
    
    private void processSideBars(Map sideBars, JScrollPane scroller, JComponent ec){
        ec.removeAll();
        scroller.setRowHeader(null);
        scroller.setColumnHeaderView(null);
        for (Iterator entries = sideBars.entrySet().iterator(); entries.hasNext(); ) {
            Map.Entry entry = (Map.Entry) entries.next();
            SideBarPosition position = (SideBarPosition) entry.getKey();
            JComponent sideBar = (JComponent) entry.getValue();
            
            if (position.isScrollable()) {
                if (position.getPosition() == SideBarPosition.WEST) {
                    scroller.setRowHeaderView(sideBar);
                } else {
                    if (position.getPosition() == SideBarPosition.NORTH) {
                        scroller.setColumnHeaderView(sideBar);
                    } else {
                        throw new IllegalArgumentException("Unsupported side bar position, scrollable = true, position=" + position.getBorderLayoutPosition()); // NOI18N
                    }
                }
            } else {
                ec.add(sideBar, position.getBorderLayoutPosition());
            }
        }
        ec.add(scroller);
    }
    
    protected JToolBar createToolBarComponent() {
        return new NbEditorToolBar(getComponent());
    }

    private class SystemActionPerformer implements PropertyChangeListener{

        private String editorActionName;

        private Action editorAction;

        private Action systemAction;
        
        
        SystemActionPerformer(String editorActionName) {
            this.editorActionName = editorActionName;

            synchronized (NbEditorUI.this.getComponentLock()) {
                // if component already installed in EditorUI simulate installation
                JTextComponent component = getComponent();
                if (component != null) {
                    propertyChange(new PropertyChangeEvent(NbEditorUI.this,
                                                           EditorUI.COMPONENT_PROPERTY, null, component));
                }

                NbEditorUI.this.addPropertyChangeListener(this);
            }
        }
        
        private void attachSystemActionPerformer(JTextComponent c){
            if (c == null) return;

            Action editorAction = getEditorAction(c);
            if (editorAction == null) return;

            Action globalSystemAction = getSystemAction(c);
            if (globalSystemAction == null) return;

            if (globalSystemAction instanceof CallbackSystemAction){
                Object key = ((CallbackSystemAction)globalSystemAction).getActionMapKey();
                c.getActionMap ().put (key, editorAction);
            }                        
        }
        
        private void detachSystemActionPerformer(JTextComponent c){
            if (c == null) return;

            Action editorAction = getEditorAction(c);
            if (editorAction == null) return;

            Action globalSystemAction = getSystemAction(c);
            if (globalSystemAction == null) return;

            if (globalSystemAction instanceof CallbackSystemAction){
                Object key = ((CallbackSystemAction)globalSystemAction).getActionMapKey();
                Object ea = c.getActionMap ().get (key);
                if (editorAction.equals(ea)){
                    c.getActionMap ().remove(key);
                }
            }                        
                                
        }
        
        
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();

            if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
                JTextComponent component = (JTextComponent)evt.getNewValue();

                if (component != null) { // just installed
                    component.addPropertyChangeListener(this);
                    attachSystemActionPerformer(component);
                } else { // just deinstalled
                    component = (JTextComponent)evt.getOldValue();
                    component.removePropertyChangeListener(this);
                    detachSystemActionPerformer(component);
                }
            }
        }   

        private synchronized Action getEditorAction(JTextComponent component) {
            if (editorAction == null) {
                BaseKit kit = Utilities.getKit(component);
                if (kit != null) {
                    editorAction = kit.getActionByName(editorActionName);
                }
            }
            return editorAction;
        }

        private Action getSystemAction(JTextComponent c) {
            if (systemAction == null) {
                Action ea = getEditorAction(c);
                if (ea != null) {
                    String saClassName = (String)ea.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
                    if (saClassName != null) {
                        Class saClass;
                        try {
                            saClass = Class.forName(saClassName);
                        } catch (Throwable t) {
                            saClass = null;
                        }

                        if (saClass != null) {
                            systemAction = SystemAction.get(saClass);
                            if (systemAction instanceof ContextAwareAction){
                                Lookup lookup = getContextLookup(c);
                                if (lookup!=null){
                                    systemAction = ((ContextAwareAction)systemAction).createContextAwareInstance(lookup);
                                }
                            }
                            
                        }
                    }
                }
            }
            return systemAction;
        }
        
    }
    

    /**
     *
     * @deprecated use SystemActionPerformer instead
     */
    public final class SystemActionUpdater
        implements PropertyChangeListener, ActionPerformer {

        private String editorActionName;

        private boolean updatePerformer;

        private boolean syncEnabling;

        private Action editorAction;

        private Action systemAction;

        private PropertyChangeListener enabledPropertySyncL;
        
        private boolean listeningOnTCRegistry;


        SystemActionUpdater(String editorActionName, boolean updatePerformer,
                            boolean syncEnabling) {
            this.editorActionName = editorActionName;
            this.updatePerformer = updatePerformer;
            this.syncEnabling = syncEnabling;

            synchronized (NbEditorUI.this.getComponentLock()) {
                // if component already installed in EditorUI simulate installation
                JTextComponent component = getComponent();
                if (component != null) {
                    propertyChange(new PropertyChangeEvent(NbEditorUI.this,
                                                           EditorUI.COMPONENT_PROPERTY, null, component));
                }

                NbEditorUI.this.addPropertyChangeListener(this);
            }
        }

        public void editorActivated() {
            Action ea = getEditorAction();
            Action sa = getSystemAction();
            if (ea != null && sa != null) {
                if (updatePerformer) {
                    if (ea.isEnabled() && sa instanceof CallbackSystemAction) {
                        ((CallbackSystemAction)sa).setActionPerformer(this);
                    }
                }

                if (syncEnabling) {
                    if (enabledPropertySyncL == null) {
                        enabledPropertySyncL = new EnabledPropertySyncListener(sa);
                    }
                    ea.addPropertyChangeListener(enabledPropertySyncL);
                }
            }
        }

        public void editorDeactivated() {
            Action ea = getEditorAction();
            Action sa = getSystemAction();
            if (ea != null && sa != null) {
                /*        if (sa instanceof CallbackSystemAction) {
                          CallbackSystemAction csa = (CallbackSystemAction)sa;
                          if (csa.getActionPerformer() == this) {
                            csa.setActionPerformer(null);
                          }
                        }
                */

                if (syncEnabling && enabledPropertySyncL != null) {
                    ea.removePropertyChangeListener(enabledPropertySyncL);
                }
            }
        }

        private void reset() {
            if (enabledPropertySyncL != null) {
                editorAction.removePropertyChangeListener(enabledPropertySyncL);
            }

            /*      if (systemAction != null) {
                    if (systemAction instanceof CallbackSystemAction) {
                      CallbackSystemAction csa = (CallbackSystemAction)systemAction;
                      if (!csa.getSurviveFocusChange() || csa.getActionPerformer() == this) {
                        csa.setActionPerformer(null);
                      }
                    }
                  }
            */

            editorAction = null;
            systemAction = null;
            enabledPropertySyncL = null;
        }

        /** Perform the callback action */
        public void performAction(SystemAction action) {
            JTextComponent component = getComponent();
            Action ea = getEditorAction();
            if (component != null && ea != null) {
                ea.actionPerformed(new ActionEvent(component, 0, "")); // NOI18N
            }
        }
        
        private void startTCRegistryListening() {
            if (!listeningOnTCRegistry) {
                listeningOnTCRegistry = true;
                TopComponent.getRegistry().addPropertyChangeListener(this);
            }
        }
        
        private void stopTCRegistryListening() {
            if (listeningOnTCRegistry) {
                listeningOnTCRegistry = false;
                TopComponent.getRegistry().removePropertyChangeListener(this);
            }
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();

            if (TopComponent.Registry.PROP_ACTIVATED.equals (propName)) {
                TopComponent activated = (TopComponent)evt.getNewValue();

                if(activated instanceof CloneableEditorSupport.Pane)
                    editorActivated();
                else
                    editorDeactivated();
            } else if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
                JTextComponent component = (JTextComponent)evt.getNewValue();

                if (component != null) { // just installed
                    component.addPropertyChangeListener(this);
                    if (component.isDisplayable()) {
                        startTCRegistryListening();
                    }

                } else { // just deinstalled
                    component = (JTextComponent)evt.getOldValue();
                    component.removePropertyChangeListener(this);
                    stopTCRegistryListening();
                }

                reset();

            } else if ("editorKit".equals(propName)) { // NOI18N
                reset();

            } else if ("ancestor".equals(propName)) { // NOI18N
                if (((Component)evt.getSource()).isDisplayable()) { // now displayable
                    startTCRegistryListening();
                } else { // not displayable
                    stopTCRegistryListening();
                }
            }
        }

        private synchronized Action getEditorAction() {
            if (editorAction == null) {
                BaseKit kit = Utilities.getKit(getComponent());
                if (kit != null) {
                    editorAction = kit.getActionByName(editorActionName);
                }
            }
            return editorAction;
        }

        private Action getSystemAction() {
            if (systemAction == null) {
                Action ea = getEditorAction();
                if (ea != null) {
                    String saClassName = (String)ea.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
                    if (saClassName != null) {
                        Class saClass;
                        try {
                            saClass = Class.forName(saClassName);
                        } catch (Throwable t) {
                            saClass = null;
                        }

                        if (saClass != null) {
                            systemAction = SystemAction.get(saClass);
                        }
                    }
                }
            }
            return systemAction;
        }

        protected void finalize() throws Throwable {
            reset();
        }

    }

    /** Listener that listen on changes of the "enabled" property
    * and if changed it changes the same property of the action
    * given in constructor.
    */
    static class EnabledPropertySyncListener implements PropertyChangeListener {

        Action action;

        EnabledPropertySyncListener(Action actionToBeSynced) {
            this.action = actionToBeSynced;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                action.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
            }
        }

    }

    public Map getColoringMap() {
        return createColoringMap();
    }

    private class MyLookupListener implements LookupListener {

        private String mimeType;

        public MyLookupListener(String mimeType) {
            super();
            this.mimeType = mimeType;
        }

        public void resultChanged(LookupEvent ev) {
            synchronized (Settings.class){
                mime2Coloring.remove(mimeType);
                Lookup.Result result = ((Lookup.Result)ev.getSource());
                // refresh fontColorSettings
                Collection newInstances = result.allInstances();
                if (newInstances.size() > 0){
                    fontColorSettings = (FontColorSettings)newInstances.iterator().next();
                }
            }
            settingsChangeImpl(null);
        }
    }

}
