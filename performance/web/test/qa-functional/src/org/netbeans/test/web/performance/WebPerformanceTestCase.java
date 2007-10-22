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

package org.netbeans.test.web.performance;
import org.netbeans.jemmy.EventTool;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.html.editor.options.HTMLOptions;
import org.netbeans.modules.java.editor.options.JavaOptions;
import org.netbeans.modules.web.core.syntax.JSPKit;
import org.netbeans.modules.web.core.syntax.settings.JSPOptions;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.text.syntax.XMLOptions;
import org.netbeans.performance.test.utilities.PerformanceTestCase;

/**
 *
 * @author ms113234
 */
public abstract class WebPerformanceTestCase extends PerformanceTestCase {
    // Options
    private JSPOptions jspOptions = null;
    private JavaOptions javaOptions = null;
    private HTMLOptions htmlOptions = null;
    private XMLOptions xmlOptions = null;
    /* TODO doesn't work after retouche integration
    private JavaSettings javaSettings = null;
     */
    // jsp options
    private int defCaretBlinkingRate;
    private boolean defCodeFoldindEnabled;
    private int defCompletionAutoPopupDelayJsp;
    private int defFontSize;
    private boolean defJavaDocAutoPopupJsp;
    private int defStatusBarCaretDelay;
    // java options
    private int defCompletionAutoPopupDelayJava;
    private boolean defJavaDocAutoPopupJava;
    // java settings
    private int defParsingErrors;
    private EventTool eventTool = null;
    
    /**
     * Creates a new instance of WebPerformanceTestCase
     * @param testName name of the test
     */
    public WebPerformanceTestCase(String testName) {
        super(testName);
        init();
    }
    
    /**
     * Creates a new instance of WebPerformanceTestCase
     * @param testName name of the test
     * @param performanceDataName name for measured performance data, measured values are stored to results under this name
     */
    public WebPerformanceTestCase(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        init();
    }
    
    protected void init() {
        // timeouts
        WAIT_AFTER_PREPARE = 1500;
        WAIT_AFTER_OPEN = 2000;
        HEURISTIC_FACTOR = -1;
        // init options
        jspOptions = (JSPOptions) BaseOptions.getOptions(JSPKit.class);
        javaOptions = (JavaOptions) BaseOptions.getOptions(JavaKit.class);
        htmlOptions = (HTMLOptions) BaseOptions.getOptions(HTMLKit.class);
        xmlOptions = (XMLOptions) BaseOptions.getOptions(XMLKit.class);
        /* TODO doesn't work after retouche integration
        javaSettings = JavaSettings.getDefault();
         */
        // TODO replace by store/reload whole settings impl
        // jsp options
        defCaretBlinkingRate = jspOptions.getCaretBlinkRate();
//        defCodeFoldindEnabled = jspOptions.getCodeFoldingEnable();
        defCompletionAutoPopupDelayJsp = jspOptions.getCompletionAutoPopupDelay();
        defFontSize = jspOptions.getFontSize();
        defJavaDocAutoPopupJsp = jspOptions.getJavaDocAutoPopup();
        defStatusBarCaretDelay = jspOptions.getStatusBarCaretDelay();
        // java options
        defCompletionAutoPopupDelayJava = javaOptions.getCompletionAutoPopupDelay();
        defJavaDocAutoPopupJava = javaOptions.getJavaDocAutoPopup();
        // java settings
        /* TODO doesn't work after retouche integration
        defParsingErrors = javaSettings.getParsingErrors();
         */ 
        // turn off caret blinking
        jspOptions.setCaretBlinkRate(0);
        javaOptions.setCaretBlinkRate(0);
        htmlOptions.setCaretBlinkRate(0);
        xmlOptions.setCaretBlinkRate(0);
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            // save default JSP Options
//            ObjectOutputStream out = new ObjectOutputStream(baos);
//            jspOptions.writeExternal(out);
//            defJspOptions = baos.toByteArray();
//            out.reset();
//            // save default Java Options
//            javaOptions.writeExternal(out);
//            defJavaOptions = baos.toByteArray();
//            out.reset();
//            // save default Java Settings
//            javaSettings.writeExternal(out);
//            defJavaSettings = baos.toByteArray();
//            out.reset();
//        } catch (IOException ioe) {
//            fail(ioe);
//        }
        
    }
    
    protected void resetOptions() {
        // TODO replace by store/reload whole settings impl
//        jspOptions.setCodeFoldingEnable(defCodeFoldindEnabled);
        jspOptions.setCompletionAutoPopupDelay(defCompletionAutoPopupDelayJsp);
        jspOptions.setFontSize(defFontSize);
        jspOptions.setJavaDocAutoPopup(defJavaDocAutoPopupJsp);
        jspOptions.setStatusBarCaretDelay(defStatusBarCaretDelay);
        // java options
        javaOptions.setCompletionAutoPopupDelay(defCompletionAutoPopupDelayJava);
        javaOptions.setJavaDocAutoPopup(defJavaDocAutoPopupJava);
        // java settings
        /* TODO doesn't work after retouche integration
        javaSettings.setParsingErrors(defParsingErrors);
         */ 
//        try {
//            System.out.println("defJavaSettings= " + defJavaSettings.length);
//            System.out.println("defJavaOptions= " + defJavaOptions.length);
//            System.out.println("defJspOptions= " + defJspOptions.length);
//            
//            // java settings
//            ByteArrayInputStream bais = new ByteArrayInputStream(defJavaSettings);
//            ObjectInputStream in = new ObjectInputStream(bais);
//            javaSettings.readExternal(in);
//            // java options
//            bais = new ByteArrayInputStream(defJavaOptions);
//            in = new ObjectInputStream(bais);
//            javaOptions.readExternal(in);
//            // jsp options
//            bais = new ByteArrayInputStream(defJspOptions);
//            in = new ObjectInputStream(bais);
//            jspOptions.readExternal(in);
//        } catch (Exception e) {
//            fail(e);
//        }
    }
    
    protected JSPOptions jspOptions() {
        return jspOptions;
    }
    
    protected JavaOptions javaOptions() {
        return javaOptions;
    }
    
    /* TODO doesn't work after retouche integration   
    protected JavaSettings javaSettings() {
        return javaSettings;
    }
    */ 
    
    protected void shutdown() {
        resetOptions();
        repaintManager().resetRegionFilters();
    }
    
    protected EventTool eventTool() {
        if (eventTool == null) {
            eventTool = new EventTool();
        }
        return eventTool;
    }
    
    public String toString() {
        return renamedTestCaseName.toString();
    }
}
