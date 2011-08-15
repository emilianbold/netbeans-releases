/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * StyleEditor.java
 *
 * Created on October 13, 2004, 12:26 PM
 */

package org.netbeans.modules.css.visual.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.visual.CssEditorSupport;
import org.netbeans.modules.css.visual.CssRuleContent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.openide.util.Exceptions;

/**
 * Super class for all Style editors
 * @author  Winston Prakash, Marek Fukala
 * @version 1.0
 */
abstract public class StyleEditor extends JPanel {

    private PropertyChangeSupport cssPropertyChangeSupport;

    CssPropertyChangeListener cssPropertyChangeListener = new CssPropertyChangeListener();

    boolean listenerAdded = false;

    private CssRuleContext content;

    private final Object LOCK = new Object();
    private Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final AtomicBoolean IN_PROPERTY_VALUES_INITIALIZATION = new AtomicBoolean(false);

    protected StyleEditor(String name, String dispName) {
        setName(name); //NOI18N
        setDisplayName(dispName);
        
        armPanel();
    }

    /** Called by StyleBuilderPanel to set the UI panel property values. */
    public synchronized void setContent(final CssRuleContext content) {
        this.content = content;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        //Bug 187818 - CSS Style Editor sometimes broken after update to 6.9
                        //
                        //The CssEditorSupport.updateSelectedRule() method calls setContent() on the
                        //StyleBuilderTopComponent which then triggers this code.
                        //In the StyleEditor.setCssPropertyValues() method there might be the
                        //aggregated events handling which then causes the CssEditorSupport to stop listening
                        //on the UI.
                        //It is caused by the lazy call of this Runnable. First the
                        //CssEditorSupport.updateSelectedRule()
                        //triggers the StyleEditor.setContent() method, then adds the listener to the
                        //selected rule in the same thread. Then after some time this Runnable lazily runs
                        //and possibly (for example in the MarginStyleEditor) calls
                        //the CssEditorSupport.lastAggregatedEventFired
                        //which detaches the property change listener.
                        //
                        //The workaround is not to call the CssEditorSupport.firstAggregatedEventWillFire() and
                        //CssEditorSupport.lastAggregatedEventFired() during the panel initialization but only
                        //upon the UI modifications
                        //
                        IN_PROPERTY_VALUES_INITIALIZATION.set(true);
                        setCssPropertyValues(content.selectedRuleContent());
                        IN_PROPERTY_VALUES_INITIALIZATION.set(false);
                        //once the instance executor is used, we can release it and run the code directly
                        EXECUTOR = null;
                    }
                    
                });
            }
        };

        if(EXECUTOR != null) {
            EXECUTOR.execute(task);
        } else {
            task.run();
        }

    }

    protected void startAggregatedEventsSession() {
        if(!IN_PROPERTY_VALUES_INITIALIZATION.get()) {
            CssEditorSupport.getDefault().firstAggregatedEventWillFire();
        }
    }

    protected void closeAggregatedEventsSession() {
        if(!IN_PROPERTY_VALUES_INITIALIZATION.get()) {
            CssEditorSupport.getDefault().lastAggregatedEventFired();
        }
    }
    
    protected CssRuleContext content() {
        return content;
    }

    protected abstract void lazyInitializePanel();
    
    public void initializePanel() {
        synchronized (LOCK) {
            LOCK.notifyAll(); //AWT
        }
    }

    private StyleEditor armPanel() {
        EXECUTOR.execute(new Runnable() {

            @Override
            public void run() {
                //this blocks the execution until the editor panel is selected
                synchronized (LOCK) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        
                        @Override
                        public void run() {
                            lazyInitializePanel();

                            revalidate();
                            repaint();
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        return this;
    }

    /**
     * Overriden by the subclasses
     * - Remove the property change listener
     * - Set the values from CSS data to GUI elements
     * - Set back the CSS property change listener
     */
    abstract protected void setCssPropertyValues(CssRuleContent styleData);

    PropertyChangeSupport cssPropertyChangeSupport() {
        if(cssPropertyChangeSupport == null) {
            cssPropertyChangeSupport =  new PropertyChangeSupport(this);
        }
        return cssPropertyChangeSupport;
    }
    
    /**
     * Set the CSS property change listener
     */
    public void setCssPropertyChangeListener(CssRuleContent styleData){
        // We don't want the property change listener added more than
        // once accidently
        synchronized(StyleEditor.class){
            if (!listenerAdded){
                listenerAdded = true;
                cssPropertyChangeListener.setCssStyleData(styleData);
                cssPropertyChangeSupport().addPropertyChangeListener(cssPropertyChangeListener);
            }
        }
    }

    /**
     * Remove the CSS property change listener
     */
    public void removeCssPropertyChangeListener(){
        synchronized(StyleEditor.class){
            if (listenerAdded){
                listenerAdded = false;
                cssPropertyChangeSupport().removePropertyChangeListener(cssPropertyChangeListener);
            }
        }
    }


    /**
     * Holds value of property displayName.
     */
    private String displayName;

    /**
     * Holds value of property icon.
     */
    private Icon icon;

    /**
     * Getter for property displayName.
     * @return Value of property displayName.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Setter for property displayName.
     * @param displayName New value of property displayName.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for property icon.
     * @return Value of property icon.
     */
    public Icon getIcon() {
        return this.icon;
    }

    /**
     * Setter for property icon.
     * @param icon New value of property icon.
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    static class CssPropertyChangeListener implements PropertyChangeListener{
        CssRuleContent cssStyleData;

        public CssPropertyChangeListener(){
        }

        public CssPropertyChangeListener(CssRuleContent styleData){
            cssStyleData = styleData;
        }

        public void setCssStyleData(CssRuleContent styleData){
            cssStyleData = styleData;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                cssStyleData.modifyProperty(evt.getPropertyName(), (String) evt.getNewValue());
            } catch (BadLocationException ex) {
                Logger.getLogger("global").log(Level.WARNING, "CssModel inconsistency!", ex); //NOI18N
            }
        }
    }
}
