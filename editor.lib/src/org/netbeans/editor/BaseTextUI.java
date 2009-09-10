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

package org.netbeans.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.TextUI;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.Position.Bias;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.lib2.EditorApiPackageAccessor;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.openide.util.WeakListeners;

/**
* Text UI implementation
* 
* @author  Miloslav Metelka, Martin Roskanin
* @version 1.00
*/

public class BaseTextUI extends BasicTextUI implements PropertyChangeListener, DocumentListener {

    /* package */ static final String PROP_DEFAULT_CARET_BLINK_RATE = "nbeditor-default-swing-caret-blink-rate"; //NOI18N
    
    /** Extended UI */
    private EditorUI editorUI;

    private boolean foldingEnabled;
    
    private boolean needsRefresh = false;
    
    /** ID of the component in registry */
    int componentID = -1;
    
    private AbstractDocument lastDocument;
    
    /** Instance of the <tt>GetFocusedComponentAction</tt> */
    private static final GetFocusedComponentAction gfcAction
    = new GetFocusedComponentAction();

    private Preferences prefs = null;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt == null || SimpleValueNames.CODE_FOLDING_ENABLE.equals(evt.getKey())) {
                foldingEnabled = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, EditorPreferencesDefaults.defaultCodeFoldingEnable);
                JTextComponent component = getComponent();
                if (component != null) {
                    component.putClientProperty(SimpleValueNames.CODE_FOLDING_ENABLE, foldingEnabled);
                    needsRefresh = true;
                    Utilities.runInEventDispatchThread(new Runnable() {
                        public void run() {
                            refresh();
                        }
                    });
                }
            }
        }
    };
    private PreferenceChangeListener weakListener = null;
    
    public BaseTextUI() {
    }
    
    protected String getPropertyPrefix() {
        return "EditorPane"; //NOI18N
    }

    public static JTextComponent getFocusedComponent() {
        return gfcAction.getFocusedComponent2();
    }

    protected boolean isRootViewReplaceNecessary() {
        boolean replaceNecessary = false;
        
        Document doc = getComponent().getDocument();
        if (doc != lastDocument) {
            replaceNecessary = true;
        }
        
        return replaceNecessary;
    }

    protected void rootViewReplaceNotify() {
        // update the newly used document
        lastDocument = (AbstractDocument)getComponent().getDocument();
    }

    /** Called when the model of component is changed */
    protected @Override void modelChanged() {
        JTextComponent component = getComponent();
        Document doc = component != null ? component.getDocument() : null;
        
        if (component != null && doc != null) {
            boolean documentReplaced = isRootViewReplaceNecessary();

            component.removeAll();
            if (documentReplaced) {
                ViewFactory f = getRootView(component).getViewFactory();
                rootViewReplaceNotify();
                Element elem = doc.getDefaultRootElement();
                View v = f.create(elem);
                setView(v);
            }
            component.revalidate();
            
            if (documentReplaced) {
                // Execute actions related to document installaction into the component
                BaseKit baseKit = Utilities.getKit(component);
                if (baseKit != null && prefs != null) {
                    List<String> actionNamesList = new  ArrayList<String>();
                    String actionNames = prefs.get(EditorPreferencesKeys.DOC_INSTALL_ACTION_NAME_LIST, ""); //NOI18N
                    for(StringTokenizer t = new StringTokenizer(actionNames, ","); t.hasMoreTokens(); ) { //NOI18N
                        String actionName = t.nextToken().trim();
                        actionNamesList.add(actionName);
                    }
                
                    List<Action> actionsList = baseKit.translateActionNameList(actionNamesList); // translate names to actions
                    for(Action a : actionsList) {
                        a.actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    }
                }
            }
        }
    }

    
    /* XXX - workaround bugfix of issue #45487 and #45678 
     * The hack can be removed if JDK bug
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5067948
     * will be fixed.
     */
    protected @Override void installKeyboardActions() {
        String mapName = getPropertyPrefix() + ".actionMap"; //NOI18N
        // XXX - workaround bugfix of issue #45487
        // Because the ActionMap is cached in method BasicTextUI.getActionMap()
        // the property 'mapName' is set to null to force new actionMap creation
        UIManager.getLookAndFeelDefaults().put(mapName, null);        
        UIManager.getDefaults().put(mapName, null); //#45678
        super.installKeyboardActions();
    }

    /** Installs the UI for a component. */
    public @Override void installUI(JComponent c) {
        super.installUI(c);
        
        if (!(c instanceof JTextComponent)) {
            return;
        }
        
        JTextComponent component = getComponent();
        prefs = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component)).lookup(Preferences.class);
        weakListener = WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs);
        prefs.addPreferenceChangeListener(weakListener);
        
        // set margin
        String value = prefs.get(SimpleValueNames.MARGIN, null);
        Insets margin = value != null ? SettingsConversions.parseInsets(value) : null;
        component.setMargin(margin != null ? margin : EditorUI.NULL_INSETS);

        getEditorUI().installUI(component);
        foldingEnabled  = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, EditorPreferencesDefaults.defaultCodeFoldingEnable);
        component.putClientProperty(SimpleValueNames.CODE_FOLDING_ENABLE, foldingEnabled);
        
        // attach to the model and component
        //component.addPropertyChangeListener(this); already done in super class
        if (component.getClientProperty(UIWatcher.class) == null) {
            UIWatcher uiWatcher = new UIWatcher(this.getClass());
            component.addPropertyChangeListener(uiWatcher);
            component.putClientProperty(UIWatcher.class, uiWatcher);
        }
        
        BaseKit kit = (BaseKit)getEditorKit(component);
        ViewFactory vf = kit.getViewFactory();
        // Create and attach caret
        Caret defaultCaret = component.getCaret();
        Caret caret = kit.createCaret();
        component.setCaretColor(Color.black); // will be changed by settings later
        component.setCaret(caret);
        component.putClientProperty(PROP_DEFAULT_CARET_BLINK_RATE, defaultCaret.getBlinkRate());
        
        // assign blink rate
        int br = prefs.getInt(SimpleValueNames.CARET_BLINK_RATE, -1);
        if (br == -1) {
            br = defaultCaret.getBlinkRate();
        }
        caret.setBlinkRate(br);

        SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_FOCUSED, null);
        
        Registry.addComponent(component);
        Registry.activate(component);
        EditorApiPackageAccessor.get().register(component);
        component.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    /** Deinstalls the UI for a component */
    public @Override void uninstallUI(JComponent c) {
        super.uninstallUI(c);

        if (prefs != null && weakListener != null) {
            prefs.removePreferenceChangeListener(weakListener);
            prefs = null;
            weakListener = null;
        }
        
        if (c instanceof JTextComponent){        
            JTextComponent comp = (JTextComponent)c;
            BaseDocument doc = Utilities.getDocument(comp);
            if (doc != null) {
                doc.removeDocumentListener(this);
            }

            comp.setKeymap(null);
            comp.setCaret(null);

            getEditorUI().uninstallUI(comp);
            Registry.removeComponent(comp);
        }
        
        // Clear the editorUI so it will be recreated according to the kit
        // of the component for which the installUI() is called
        editorUI = null;
    }
    
    public int getYFromPos(int pos) throws BadLocationException {
        JTextComponent component = getComponent();
        if (component != null) {
            Document doc = component.getDocument();
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readLock();
            }
            try {
                View rootView = getRootView(component);
                if (rootView.getViewCount() > 0) {
                    View view = rootView.getView(0);
                    if (view instanceof LockView) {
                        LockView lockView = (LockView) view;
                        lockView.lock();
                        try {
                            DrawEngineDocView docView = (DrawEngineDocView)view.getView(0);
                            Rectangle alloc = getVisibleEditorRect();
                            if (alloc != null) {
                                rootView.setSize(alloc.width, alloc.height);
                                return docView.getYFromPos(pos, alloc);
                            }
                        } finally {
                            lockView.unlock();
                        }
                    }
                }
            } finally {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readUnlock();
                }
            }
        }
        Rectangle ret = modelToView(component, pos);
        return (ret == null) ? 0 : ret.y;
    }

    public int getPosFromY(int y) throws BadLocationException {
        return viewToModel(getComponent(), 0, y);
    }

    public int getBaseX(int y) {
        return getEditorUI().getTextMargin().left;
    }

    public int viewToModel(JTextComponent c, int x, int y) {
        return viewToModel(c, new Point(x, y));
    }

    @Override
    public void damageRange(JTextComponent t, int p0, int p1, Bias p0Bias, Bias p1Bias) {
        View rootView = getRootView(getComponent());
        boolean doDamageRange = true;
        if (rootView.getViewCount() > 0) {
            View view = rootView.getView(0);
            if (view instanceof LockView) {
                LockView lockView = (LockView) view;
                lockView.lock();
                try {
                    DrawEngineDocView docView = (DrawEngineDocView)view.getView(0);
                    doDamageRange = docView.checkDamageRange(p0, p1, p0Bias, p1Bias);
                } finally {
                    lockView.unlock();
                }
            }
        }
        if (doDamageRange) {
            super.damageRange(t, p0, p1, p0Bias, p1Bias);
        }
    }

    /** Next visually represented model location where caret can be placed.
    * This version works without placing read lock on the document.
    */
    public @Override int getNextVisualPositionFrom(JTextComponent t, int pos,
                                         Position.Bias b, int direction, Position.Bias[] biasRet)
    throws BadLocationException{
        if (biasRet == null) {
            biasRet = new Position.Bias[1];
            biasRet[0] = Position.Bias.Forward;
        }
        return super.getNextVisualPositionFrom(t, pos, b, direction, biasRet);
    }



    /** Fetches the EditorKit for the UI.
    *
    * @return the component capabilities
    */
    public @Override EditorKit getEditorKit(JTextComponent c) {
        JEditorPane pane = (JEditorPane)getComponent();
        return (pane == null) ? null : pane.getEditorKit();
    }


    /** Get extended UI. This is called from views to get correct extended UI. */
    public EditorUI getEditorUI() {
        if (editorUI == null) {
            JTextComponent c = getComponent();
            BaseKit kit = (BaseKit)getEditorKit(c);
            if (kit != null) {
                editorUI = kit.createEditorUI();
                editorUI.initLineHeight(c);
            }
        }
        return editorUI;
    }

    /**
    * This method gets called when a bound property is changed.
    * We are looking for document changes on the component.
    */
    public @Override void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if ("document".equals(propName)) { // NOI18N
            BaseDocument oldDoc = (evt.getOldValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getOldValue() : null;
                                  
            if (oldDoc != null) {
                oldDoc.removeDocumentListener(this);
            }

            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
                                  
            if (newDoc != null) {
                newDoc.addDocumentListener(this);
                Registry.activate(newDoc); // Activate the new document
            }
        } else if ("ancestor".equals(propName)) { // NOI18N
            JTextComponent comp = (JTextComponent)evt.getSource();
            if (comp.isDisplayable() && editorUI != null && editorUI.hasExtComponent()) {
                // #41209: In case extComponent was retrieved set the ancestorOverride
                // to true and expect that the editor kit that installed
                // this UI will be deinstalled explicitly.
                if (!Boolean.TRUE.equals(comp.getClientProperty("ancestorOverride"))) { // NOI18N
                    comp.putClientProperty("ancestorOverride", Boolean.TRUE); // NOI18N
                }
            }
        }
    }

    /** Insert to document notification. */
    public void insertUpdate(DocumentEvent evt) {
        try {
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            EditorUI eui = getEditorUI();
            int y = getYFromPos(evt.getOffset());
            int lineHeight = eui.getLineHeight();
            int syntaxY = getYFromPos(bevt.getSyntaxUpdateOffset());
            // !!! patch for case when DocMarksOp.eolMark is at the end of document
            if (bevt.getSyntaxUpdateOffset() == evt.getDocument().getLength()) {
                syntaxY += lineHeight;
            }
            if (getComponent().isShowing()) {
                eui.repaint(y, Math.max(lineHeight, syntaxY - y));
            }
        } catch (BadLocationException ex) {
            Utilities.annotateLoggable(ex);
        }
    }
    
    /** Remove from document notification. */
    public void removeUpdate(DocumentEvent evt) {
        try {
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            EditorUI eui = getEditorUI();
            int y = getYFromPos(evt.getOffset());
            int lineHeight = eui.getLineHeight();
            int syntaxY = getYFromPos(bevt.getSyntaxUpdateOffset());
            // !!! patch for case when DocMarksOp.eolMark is at the end of document
            if (bevt.getSyntaxUpdateOffset() == evt.getDocument().getLength()) {
                syntaxY += lineHeight;
            }
            if (getComponent().isShowing()) {
                eui.repaint(y, Math.max(lineHeight, syntaxY - y));
            }

        } catch (BadLocationException ex) {
            Utilities.annotateLoggable(ex);
        }
    }

    /** The change in document notification.
    *
    * @param evt  The change notification from the currently associated document.
    */
    public void changedUpdate(DocumentEvent evt) {
        if (evt instanceof BaseDocumentEvent) {
            BaseDocumentEvent bdevt = (BaseDocumentEvent)evt;
            BaseDocument doc = (BaseDocument)bdevt.getDocument();
            String layerName = bdevt.getDrawLayerName();
            if (layerName != null) {
                getEditorUI().addLayer(doc.findLayer(layerName),
                        bdevt.getDrawLayerVisibility());
            }else{ //temp
                try {
                    JTextComponent comp = getComponent();
                    if (comp!=null && comp.isShowing()) {
                        getEditorUI().repaintBlock(evt.getOffset(), evt.getOffset() + evt.getLength());
                    }
                } catch (BadLocationException ex) {
                    Utilities.annotateLoggable(ex);
                }
            }
        }
    }

    
    
    /** Creates a view for an element.
    *
    * @param elem the element
    * @return the newly created view or null
    */
    public @Override View create(Element elem) {
	    String kind = elem.getName();
            /*
            if (foldingEnabled){
                Element parent = elem.getParentElement();
                if (parent!=null){
                    int index = parent.getElementIndex(elem.getStartOffset());
                    if (index >=3 && index <=6){
                        return  new CollapsedView(parent.getElement(3), parent.getElement(6));
                    }
                }
            }
            */
            
	    if (kind != null) {
		if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
		} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
//                    System.out.println("creating DrawEngineLineView for elem=" + elem);
		    return new DrawEngineLineView(elem);//.createFragment(elem.getStartOffset()+10,elem.getStartOffset()+30);
		} else if (kind.equals(AbstractDocument.SectionElementName)) {
//                   return new LockView(new EditorUIBoxView(elem, View.Y_AXIS));
//                    System.out.println("creating DrawEngineDocView for elem=" + elem);
//		    return new DrawEngineDocView(getComponent()); // EditorUIBoxView(elem, View.Y_AXIS);
		    return new LockView(new DrawEngineDocView(elem)); // EditorUIBoxView(elem, View.Y_AXIS);
		} else if (kind.equals(StyleConstants.ComponentElementName)) {
		    return new ComponentView(elem);
		} else if (kind.equals(StyleConstants.IconElementName)) {
		    return new IconView(elem);
		}
	    }
	
	    // default to text display
            return new DrawEngineLineView(elem);        
    }

    /** Creates a view for an element.
    * @param elem the element
    * @param p0 the starting offset >= 0
    * @param p1 the ending offset >= p0
    * @return the view
    */
    public @Override View create(Element elem, int p0, int p1) {
        return null;
    }

    /** Specifies that some preference has changed. */
    public void preferenceChanged(boolean width, boolean height) {
        modelChanged();
    }

    public void invalidateStartY() {
        // no longer available
    }

    boolean isFoldingEnabled() {
        return foldingEnabled;
    }

    protected void refresh(){
        if (getComponent().isShowing() && needsRefresh){
            modelChanged();
            needsRefresh = false;
        }
    }
    
    private static class GetFocusedComponentAction extends TextAction {

        private GetFocusedComponentAction() {
            super("get-focused-component"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt) {
        }

        JTextComponent getFocusedComponent2() {
            return super.getFocusedComponent();
        }

    }
    
    static void uninstallUIWatcher(JTextComponent c) {
        UIWatcher uiWatcher = (UIWatcher)c.getClientProperty(UIWatcher.class);
        if (uiWatcher != null) {
            c.removePropertyChangeListener(uiWatcher);
            c.putClientProperty(UIWatcher.class, null);
        }
    }
    
    /** Class that returns back BaseTextUI after its change
     * by changing look-and-feel.
     */
    static class UIWatcher implements PropertyChangeListener {
        
        private Class uiClass;
        
        UIWatcher(Class uiClass) {
            this.uiClass = uiClass;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            Object newValue = evt.getNewValue();
            if ("UI".equals(evt.getPropertyName())
                && (newValue != null) && !(newValue instanceof BaseTextUI)
            ) {
                JTextComponent c = (JTextComponent)evt.getSource();
                EditorKit kit = ((TextUI)newValue).getEditorKit(c);
                if (kit instanceof BaseKit) {
                    // BaseKit but not BaseTextUI -> restore BaseTextUI
                    try {
                        c.setUI((BaseTextUI)uiClass.newInstance());
                    } catch (InstantiationException e) {
                    } catch (IllegalAccessException e) {
                    }
                }
            }
        }
        
    }
    
}
