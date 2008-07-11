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

package org.netbeans.modules.visualweb.gravy.model.project.components;

import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Bundle;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.model.components.*;
import org.netbeans.modules.visualweb.gravy.model.project.JavaFile;

import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.JemmyException;

import java.awt.event.KeyEvent;

/**
 * Common class for all components on web page.
 */

public class VisualEventHandlingComponent extends VisualComponent {

    private final static String bundle = "org.netbeans.modules.visualweb.gravy.model.project.components.Bundle";
    private final static String viewOutline = Bundle.getStringTrimmed(
                                              Bundle.getStringTrimmed(bundle, "OutlineBundle"),
                                              Bundle.getStringTrimmed(bundle, "ViewOutline"));
    private final static String popupEventHandler = Bundle.getStringTrimmed(
                                                    Bundle.getStringTrimmed(bundle, "InsyncBundle"),
                                                    Bundle.getStringTrimmed(bundle, "EditEventHandler"));
    private final static String[] action = {"action"};
    private final static String popupActionEventHandler = Bundle.getStringTrimmed(
                                                          Bundle.getStringTrimmed(bundle, "InsyncBundle"),
                                                          Bundle.getStringTrimmed(bundle, "EditDefEventHandler"), action);
    private final static String openOutline = "Window|" + viewOutline;
    
    /**
     * Create visual component on web page using web component from palette.
     * @param webcomponent Web component from palette.
     * @param name Name of component.
     */
    public VisualEventHandlingComponent(WebComponent webcomponent, String name) {
        super(webcomponent, name);
    }
    
    /**
     * Set event handler for component.
     * @param handlerText Text of event handler.
     */
    public void setEventHandler(String handlerText) {
        setEventHandler("", handlerText);
    }
    
    /**
     * Set event handler for component.
     * @param eventType Type of event.
     * @param handlerText Text of event handler.
     */
    public void setEventHandler(String eventType, String handlerText) {
        Page.open();
        try {
            Util.getMainMenu().pushMenu(openOutline);
        }
        catch(Exception e) {
            throw new JemmyException(openOutline + " menu can't be found!", e);
        }
        DocumentOutlineOperator outline = new DocumentOutlineOperator(RaveWindowOperator.getDefaultRave());
        TestUtils.wait(1000);
        try {
            JTreeOperator aotree = outline.getStructTreeOperator();
            aotree.callPopupOnPath(aotree.findPath(Page.getName() + "|page1|html1|body1|form1|" + getName()));
            TestUtils.wait(1000);
            if (!eventType.equals("")) new JPopupMenuOperator().pushMenu(popupEventHandler + "|" + eventType);
            else new JPopupMenuOperator().pushMenu(popupActionEventHandler);
            TestUtils.wait(1000);
        }
        catch(Exception e) {
            throw new JemmyException("Item for action handler in popup menu can't be found!", e);
        }
        try {
            JavaFile jf = Page.getJavaFile();
            jf.insert(handlerText);
            TestUtils.wait(1000);
            jf.pushKey(KeyEvent.VK_DOWN);
            TestUtils.wait(500);
            jf.pushKey(KeyEvent.VK_END);
            TestUtils.wait(500);
            if (!jf.getText().substring(jf.getCaretPosition() - 1, jf.getCaretPosition()).equals("}")) {
                jf.pushKey(KeyEvent.VK_HOME, KeyEvent.SHIFT_MASK);
                TestUtils.wait(500);
                jf.pushKey(KeyEvent.VK_DELETE);
            }
            
            //Workaround: switch to design gives an error
            //Workaround start
            TestUtils.wait(500);
            jf.pushKey(KeyEvent.VK_LEFT);
            TestUtils.wait(500);
            jf.pushKey(KeyEvent.VK_SPACE);
            TestUtils.wait(500);
            jf.pushKey(KeyEvent.VK_BACK_SPACE);
            //Workaround end
            
            TestUtils.wait(1000);
            jf.reformatCode();
        }
        catch(Exception e) {
            throw new JemmyException("Event Handler can't be added!", e);
        }
        DesignerPaneOperator.switchToDesignerPane();
        TestUtils.wait(1000);
    }
}
