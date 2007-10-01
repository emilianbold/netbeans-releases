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

package org.netbeans.modules.java.editor;

import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;
import org.netbeans.editor.ext.java.JavaSettingsInitializer;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.java.NbJavaSettingsInitializer;
import org.netbeans.modules.editor.NbLocalizer;
import org.netbeans.modules.java.editor.options.JavaOptions;
import org.openide.modules.ModuleInstall;

/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class JavaEditorModule extends ModuleInstall {

    private NbLocalizer settingsNamesLocalizer;
    private NbLocalizer optionsLocalizer;
//    private JavaIndentationSettingsProvider jisProvider = null;
    static boolean inited = false;
    
    public static void init(){
        if (inited) return;
        inited = true;
        Settings.addInitializer(new JavaSettingsInitializer(JavaKit.class));
        Settings.addInitializer(new NbJavaSettingsInitializer());
        Settings.reset();
    }
    
    /** Module installed again. */
    public void restored () {
        init();

//        JMManager.setDocumentLocksCounter(BaseDocument.THREAD_LOCAL_LOCK_DEPTH);

        settingsNamesLocalizer = new NbLocalizer(JavaSettingsNames.class);
        optionsLocalizer = new NbLocalizer(JavaOptions.class);
        LocaleSupport.addLocalizer(settingsNamesLocalizer);
        LocaleSupport.addLocalizer(optionsLocalizer);
//        if (jisProvider == null) {
//            jisProvider = new JavaIndentationSettingsProvider();
//            JMManager.setIndentationSettingsProvider(jisProvider);
//        }

    }

    /** Called when module is uninstalled. Overrides superclass method. */
    public void uninstalled() {
        
//        if (jisProvider != null) {
//            jisProvider.release();
//            JMManager.setIndentationSettingsProvider(null);
//            jisProvider = null;
//        }
        
//        JMManager.setDocumentLocksCounter(null);

        Settings.removeInitializer(JavaSettingsInitializer.NAME);
        Settings.removeInitializer(NbJavaSettingsInitializer.NAME);
        Settings.reset();

        LocaleSupport.removeLocalizer(settingsNamesLocalizer);
        settingsNamesLocalizer = null;
        LocaleSupport.removeLocalizer(optionsLocalizer);
        optionsLocalizer = null;
        
    }
    
//    private static class JavaIndentationSettingsProvider implements IndentationSettingsProvider, PropertyChangeListener{
//        
//        private static final Map indentSettings2propertyName
//                = new HashMap();
//        
//        static {
//            indentSettings2propertyName.put(
//                    JavaIndentEngine.JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP,
//                    JavaSettingsNames.JAVA_FORMAT_LEADING_STAR_IN_COMMENT
//            );
//            indentSettings2propertyName.put(
//                    JavaIndentEngine.JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP,
//                    JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE
//            );
//            indentSettings2propertyName.put(
//                    JavaIndentEngine.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
//                    JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS
//            );
//            indentSettings2propertyName.put(
//                    JavaIndentEngine.JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP,
//                    JavaSettingsNames.JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT
//            );
//            indentSettings2propertyName.put(
//                    FormatterIndentEngine.EXPAND_TABS_PROP,
//                    SettingsNames.EXPAND_TABS
//            );
//            indentSettings2propertyName.put(
//                    FormatterIndentEngine.SPACES_PER_TAB_PROP,
//                    SettingsNames.SPACES_PER_TAB
//            );
//        }
//        
//        private JavaIndentEngine indentEngine = null;
//
//        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//        
//        public JavaIndentationSettingsProvider(){
//        }
//        
//        private synchronized JavaIndentEngine getIndentEngine() {
//            if (indentEngine == null) {
//                BaseOptions javaOptions = BaseOptions.getOptions(JavaKit.class);
//                if (javaOptions instanceof JavaOptions) {
//                    IndentEngine eng = javaOptions.getIndentEngine();
//                    if (eng instanceof JavaIndentEngine) {
//                        indentEngine = (JavaIndentEngine)eng;
//                        indentEngine.addPropertyChangeListener(this);
//                    }
//                }
//            }
//            return indentEngine;
//        }
//
//        public Object getPropertyValue(String propertyName) {
//            JavaIndentEngine eng = getIndentEngine();
//            if (eng != null){
//                String settingsPropertyName = (String)indentSettings2propertyName.get(propertyName);
//                if (settingsPropertyName != null) {
//                    return eng.getValue(settingsPropertyName); 
//                }
//            }
//
//            return null;
//        }
//
//        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
//            getIndentEngine(); // possibly init engine to listen on it
//            pcs.removePropertyChangeListener(l);
//        }
//
//        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
//            getIndentEngine(); // possibly init engine to listen on it
//            pcs.addPropertyChangeListener(l);
//        }
//
//        public void propertyChange(java.beans.PropertyChangeEvent evt) {
//            if (evt == null) return;
//            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
//        }
//
//        public synchronized void release() {
//            if (indentEngine != null) {
//                indentEngine.removePropertyChangeListener(this);
//            }
//        }
//
//    }
}
