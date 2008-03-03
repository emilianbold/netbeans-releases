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


package org.netbeans.editor.ext;

import java.awt.Color;
import java.awt.Rectangle;
import java.lang.Comparable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.StringBuffer;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import javax.swing.Timer;
import org.netbeans.editor.BaseKit;

import org.netbeans.editor.WeakTimerListener;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.Settings;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsUtil;

import java.util.*;
import org.openide.util.NbBundle;


/**
 *  Support for javadoc in code completion.
 *  Contains also static utilities methods for preparing javadoc HTML content
 *
 *  @author  Martin Roskanin
 *  @since   03/2002
 */
public abstract class CompletionJavaDoc implements ActionListener, SettingsChangeListener, PropertyChangeListener  {
    
    /** Editor UI supporting this completion */
    protected ExtEditorUI extEditorUI;
    
    // javadoc browser history
    private List history = new ArrayList(5);
    
    private int curHistoryItem = -1;
    
    private JavaDocPane pane;
    private JavaDocView view;
    private int javaDocDelay;
    private Timer timer;
    protected Object currentContent;
    protected boolean addToHistory;
    private ListSelectionListener completionListener;    
    private boolean javaDocAutoPopup;
    private CaretListener caretL;

    public static final String BUNDLE_PREFIX = "javadoc-tag-"; //NOI18N
    public static final String LOADING = "javadoc-loading"; //NOI18N
    
    private static final int POPUP_DELAY = 200;
    
    
    /** Creates a new instance of CompletionJavaDoc */
    public CompletionJavaDoc(ExtEditorUI extEditorUI) {
        this.extEditorUI = extEditorUI;
        if (extEditorUI == null)
            return;
        
        // Initialize timer
        timer = new Timer(0, new WeakTimerListener(this)); // delay will be set later
        timer.setRepeats(false);
        Settings.addSettingsChangeListener(this);
        
        javaDocDelay = getJavaDocDelay();
        javaDocAutoPopup = getJavaDocAutoPopup();
        
        
        /**
         * Hides JavaDoc if completion is hidden.
         */
        final ExtEditorUI extUI = extEditorUI;
        class MyCaretListener implements CaretListener {
            public void caretUpdate( CaretEvent e ) {
                Completion com = extUI.getCompletion();
                if (com == null) return;
                JDCPopupPanel panel = com.getJDCPopupPanelIfExists();
                if (panel == null) return;
                if (panel.isVisible() && !com.isPaneVisible()){
                    setJavaDocVisible(false);
                }
            }            
        }
        caretL = new MyCaretListener();
        
        synchronized (extEditorUI.getComponentLock()) {
            // if component already installed in ExtEditorUI simulate installation
            JTextComponent component = extEditorUI.getComponent();
            if (component != null) {
                propertyChange(new PropertyChangeEvent(extEditorUI,
                                                       ExtEditorUI.COMPONENT_PROPERTY, null, component));
            }

            extEditorUI.addPropertyChangeListener(this);
        }
        
    }


    protected Object convertCompletionObject(Object obj){
        return obj;
    }
    
    /** If true, the javadoc popup will remain open during completion item change 
     *  and "Searching..." dialog will be shown. If the javadoc item will not be found, 
     *  the "Javadoc Not Found" message will be also shown
     *  If false, then only valid javadoc content will be shown
     */
    protected boolean alwaysDisplayPopup(){
        return true;
    }

    protected Comparator getContentComparator() {
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        // add completion listener
        final ExtEditorUI extUI = extEditorUI;
        class ClearTask implements Runnable {
            public void run(){
                Completion com = extUI.getCompletion();
                if (com != null && com.isPaneVisible()){
                    Object selectedCompletionObject = com.getSelectedValue();
                    CompletionJavaDoc completionJavaDoc = extUI.getCompletionJavaDoc();                    
                    if (selectedCompletionObject == null) {
                        if ( completionJavaDoc != null && isVisible()){
                            completionJavaDoc.setContent(null);
                        }
                        return;
                    }
                    Object selectedValue = convertCompletionObject(selectedCompletionObject);

                    if (alwaysDisplayPopup() == false) setJavaDocVisible(false);

                    if (completionJavaDoc!=null){
                        if(completionJavaDoc.autoPopup()){
                            Comparator comparator = getContentComparator();
                            if (currentContent!=null && !LOADING.equals(currentContent) && (comparator != null ? comparator.compare(currentContent, selectedValue) == 0 : currentContent.equals(selectedValue))){
                                if (!isVisible() && alwaysDisplayPopup()) setJavaDocVisible(true);
                                return;
                            }
                            if (!LOADING.equals(currentContent) && alwaysDisplayPopup())
                                completionJavaDoc.setContent(NbBundle.getBundle(BaseKit.class).getString(LOADING));
                            clearHistory();
                            completionJavaDoc.setContent(selectedValue);
                            addToHistory(selectedValue);
                        }else{
                            if (isVisible()) completionJavaDoc.setContent(null);
                        }
                    }
                }
            }                    
        }

        class SelectionObserver implements ListSelectionListener {
            public void valueChanged(ListSelectionEvent e){
                SwingUtilities.invokeLater(new ClearTask());
            }
        };
    
        if (ExtEditorUI.COMPONENT_PROPERTY.equals(propName)) {
            JTextComponent component = (JTextComponent)evt.getNewValue();
            if (component != null) { // just installed
                component.addCaretListener( caretL );

                completionListener = new SelectionObserver();

                Completion completion = extEditorUI.getCompletion();
                if (completion != null){
                    if (completion.getView() instanceof JList){
                        JList completionList = (JList)completion.getView();
                        completionList.addListSelectionListener(completionListener);
                    }
                }
                
            } else { // just deinstalled
                
                cancelPerformingThread();
                component = (JTextComponent)evt.getOldValue();

                if( component != null ) {
                    component.removeCaretListener( caretL );
                }
                
                Completion completion = extEditorUI.getCompletion();
                if (completion != null){
                    if (completion.getView() instanceof JList){
                        JList completionList = (JList)completion.getView();
                        completionList.removeListSelectionListener(completionListener);
                    }
                }
                
            }

        }
    }
    
    private JDCPopupPanel getJDCPopupPanel(){
        Completion completion = extEditorUI.getCompletion();
        if (completion != null){
            return completion.getJDCPopupPanelIfExists();
        }
        return null;
    }

    /** Returns JavaDoc popup pane */
    public JavaDocPane getJavaDocPane(){
        Completion completion = extEditorUI.getCompletion();
        if (completion != null){
            return completion.getJDCPopupPanel().getJavaDocPane();
        }
        
        if (pane == null){
            pane = new ScrollJavaDocPane(extEditorUI);
        }
        return pane;
     }
    
    /** Returns JavaDoc View */
    public JavaDocView getJavaDocView(){
        if (view == null) {
            view = new HTMLJavaDocView(getJavaDocBGColor());
        }
        return view;        
    }
    
    /** Sets javadoc popup window visibility */
    public void setJavaDocVisible(final boolean visible){
        final JDCPopupPanel jdc = getJDCPopupPanel();
        if (jdc!=null){
            if (visible) getJavaDocPane().setShowWebEnabled(isExternalJavaDocMounted());
            if (!SwingUtilities.isEventDispatchThread()){
                SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        jdc.setJavaDocVisible(visible);
                    }
                });
            }else{
                jdc.setJavaDocVisible(visible);
            }
        }
    }
    
    public synchronized void addToHistory(Object url){
        int histSize = history.size();
        for (int i=curHistoryItem+1; i<histSize; i++){
            history.remove(history.size()-1);
        }
        history.add(url);
        curHistoryItem = history.size()-1;
        if (curHistoryItem > 0) getJavaDocPane().setBackEnabled(true);
        getJavaDocPane().setForwardEnabled(false);
    }
    
    public synchronized void backHistory(){
        if (curHistoryItem > 0) {
            curHistoryItem--;
            setContent(history.get(curHistoryItem), false, false);            
            if (curHistoryItem == 0) getJavaDocPane().setBackEnabled(false);
            getJavaDocPane().setForwardEnabled(true);
        }
    }
    
    public synchronized void forwardHistory(){
        if (curHistoryItem <history.size()-1){
            curHistoryItem++;
            setContent(history.get(curHistoryItem), false, false);
            if (curHistoryItem == history.size()-1) getJavaDocPane().setForwardEnabled(false); 
            getJavaDocPane().setBackEnabled(true);
        }
    }
    
    public synchronized void clearHistory(){
        curHistoryItem = -1;
        history.clear();
        getJavaDocPane().setBackEnabled(false);
        getJavaDocPane().setForwardEnabled(false);
    }
    
    public boolean isVisible(){
        return getJavaDocPane().getComponent().isVisible();
    }
    
    /** Interrupts timer that is responsible for delayed popup of javadoc window */
    public void cancelPerformingThread(){
        timer.stop();
    }
    
    protected Object getCurrentContent(){
        if (currentContent instanceof CompletionQuery.ResultItemAssociatedObject){
            return ((CompletionQuery.ResultItemAssociatedObject)currentContent).getAssociatedObject();
        }
        return currentContent;
    }

    synchronized void clearContent() {
        cancelPerformingThread();
        currentContent = null;
    }

    /** Sets content of javadoc
     *  @param content it is Object of the java member such as JCClass, JCMethod, JCField or JCConstructor
     *  @param postRequest if false, javadoc window is popuped without delay
     *  @param addToHistory if true, the content item will be added to history queue
     */
    public synchronized void setContent(Object content, boolean postRequest, boolean addToHistory){
        timer.stop();
        if (content == null) {
            currentContent = null;
            setJavaDocVisible(false);
            return;
        }
        currentContent = content;
        this.addToHistory = addToHistory;
        
        if (postRequest){
            //timer.setInitialDelay(javaDocDelay);
            timer.setInitialDelay(POPUP_DELAY);
            //timer.setDelay(javaDocDelay);
            timer.setDelay(POPUP_DELAY);
            timer.start();
        }else{
            actionPerformed(null);
        }
        
        
    }
    
    /** Sets content of javadoc 
     *  @param content it is Object of the java member such as JCClass, JCMethod, JCField or JCConstructor
     *  @param postRequest if false, javadoc window is popuped without delay
     */
    public synchronized void setContent(Object content, boolean postRequest){
        setContent(content, postRequest, true);
    }
    
    /** Sets content of javadoc with postRequest setted to true
     *  @see #setContent(java.lang.Object, boolean) 
     */
    public void setContent(Object content){
        setContent(content, true);
    }
    
    /** Immediately sets Content of javadoc withou popup delay 
     *  @param content String representation of the displayed text. 
     *      In the case of current implementation it is an HTML document
     *      Can be <code>null</code> in this case javaDoc popup will be hidden
     */
    public void setContent(String content){
        if (content == null){
            setJavaDocVisible(false);
            return;
        }
        getJavaDocView().setContent(content);
    }
    
    /**
     * Invoked when an action occurs.
     */
    public synchronized void actionPerformed(ActionEvent e) {
        //[PENDING] - javaDoc for standalone editor
    }    

    /** Retrieve a background color of javadoc from options */
    private Color getJavaDocBGColor(){
        Class kitClass = Utilities.getKitClass(extEditorUI.getComponent());
        if (kitClass != null) {
            return (Color)SettingsUtil.getValue(kitClass,
                      ExtSettingsNames.JAVADOC_BG_COLOR,
                      ExtSettingsDefaults.defaultJavaDocBGColor);
        }
        return ExtSettingsDefaults.defaultJavaDocBGColor;
    }

    /** Retrieve a javadoc popup delay from options */
    private int getJavaDocDelay(){
        Class kitClass = Utilities.getKitClass(extEditorUI.getComponent());
        if (kitClass != null) {
            return ((Integer)SettingsUtil.getValue(kitClass,
                      ExtSettingsNames.JAVADOC_AUTO_POPUP_DELAY,
                      ExtSettingsDefaults.defaultJavaDocAutoPopupDelay)).intValue();
        }
        return ExtSettingsDefaults.defaultJavaDocAutoPopupDelay.intValue();
    }

    /** Retrieve a auto popup of javadoc property from options */    
    private boolean  getJavaDocAutoPopup(){
        Class kitClass = Utilities.getKitClass(extEditorUI.getComponent());
        if (kitClass != null) {
            return ((Boolean)SettingsUtil.getValue(kitClass,
                      ExtSettingsNames.JAVADOC_AUTO_POPUP,
                      ExtSettingsDefaults.defaultJavaDocAutoPopup)).booleanValue();
        }
        return ExtSettingsDefaults.defaultJavaDocAutoPopup.booleanValue();
    }
    
    /** Returns whether javadoc window should be invoked automatically */
    public boolean autoPopup(){
        return javaDocAutoPopup;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        if (ExtSettingsNames.JAVADOC_BG_COLOR.equals(evt.getSettingName())){
            getJavaDocView().setBGColor(getJavaDocBGColor());
        }
        
        if (ExtSettingsNames.JAVADOC_AUTO_POPUP_DELAY.equals(evt.getSettingName())){
            javaDocDelay = getJavaDocDelay();
        }

        if (ExtSettingsNames.JAVADOC_AUTO_POPUP.equals(evt.getSettingName())){
            javaDocAutoPopup = getJavaDocAutoPopup();
        }
        
    }
    

    
    /** Parses given link such as <code>java.awt.Component#addHierarchyListener</code>
     *  and returns parsed Object
     *  @return Object of JCClass, JCMethod, JCConstructor or JCField 
     */
    public Object parseLink(String link, Object baseObj){
        return null;
    }

    
    protected String getTagName(CompletionJavaDoc.JavaDocTagItem tag){
        try {
            return NbBundle.getBundle(BaseKit.class).getString(BUNDLE_PREFIX+tag.getName());
        } catch (MissingResourceException e) {
            return tag.getName();
        }
    }
    
    
    public void goToSource(){
    }
    
    public void openInExternalBrowser(){
    }
    
    public boolean isExternalJavaDocMounted(){
        return false;
    }
    
    public interface JavaDocTagItem extends Comparable{
        public String getName();
        public String getText();
    }

}
