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
package org.netbeans.test.editor.app.core;

import java.util.ArrayList;
import org.netbeans.test.editor.app.core.TestGroup;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Vector;
import org.netbeans.test.editor.app.core.actions.ActionRegistry;
import org.netbeans.test.editor.app.core.cookies.LoggingCookie;
import org.netbeans.test.editor.app.core.cookies.PackCookie;
import org.netbeans.test.editor.app.core.cookies.PerformCookie;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.actions.TestPackAction;
import org.netbeans.test.editor.app.gui.actions.TreeNewType;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestStep extends TestGroup {
    
    /** Creates new TestStep */
    public TestStep(int num) {
        this("step"+Integer.toString(num));
    }
    
    public TestStep(String name) {
        super(name);
    }
    
    public TestStep(Element node) {
        super(node);
    }
    
    public Element toXML(Element node) {
        return super.toXML(node);
    }
    
    public void startLogging() {
        System.err.println("Start Logging");
        getLogger().clear();
        getLogger().startLogging();
    }
    
    public void stopLogging() {
        System.err.println("Stop Logging");
        getLogger().stopLogging();
        TestNode[] nodes=getLogger().saveActions(this);
        getLogger().clear();
        firePropertyChange(ADD_CHILDS,null,nodes);
    }
    
    public boolean isLogging() {
        return getLogger().isLogging();
    }
    
    public void perform() {
        isPerforming=true;
        for(int i=0;i < getChildCount();i++) {
            if (!isPerforming) break;
            get(i).perform();
        }
        isPerforming=false;
    }
    
    public void stop() {
        isPerforming=false;
    }
    
    public void delete() {
        TestNode n;
        TestCallAction ca;
        
        for(int i=0;i < owner.getChildCount();i++) {
            n=owner.get(i);
            if (n instanceof TestCallAction) {
                ca=(TestCallAction)n;
                if (name.compareTo(ca.getToCall()) == 0) {
                    ca.setToCall("");
                }
                if (name.compareTo(ca.getToSet()) == 0) {
                    ca.setToSet("");
                }
            }
        }
        owner.remove(this);
    }
    
    public boolean isPacked() {
        TestNode tn;
        for (int i=0;i < getChildCount();i++) {
            tn=(TestNode)(get(i));
            if (tn instanceof TestLogAction && tn.getName().compareTo(TestStringAction.STRINGED_NAME) == 0) {
                return false;
            }
        }
        return true;
    }
    
    public void pack() {
        TestAction[] tas=TestStringAction.generate(getChildNodes());
        removeAll();
        addNodes(tas);
    }
    
    protected void registerNewTypes() {
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestLogAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestStringAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestCompletionAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSetKitAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSetIEAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSetJavaIEAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestSetCompletionAction.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestAddAbbreviationAction.class);
    }
    
    protected void registerCookies() {
        getCookieSet().put(PerformCookie.class,new PerformCookie() {
            public void perform() {
                TestStep.this.perform();
            }
            
            public boolean isPerforming() {
                return TestStep.this.isPerforming;
            }
        });
        getCookieSet().put(LoggingCookie.class,new LoggingCookie() {
            public void start() {
                TestStep.this.startLogging();
            }
            public void stop() {
                TestStep.this.stopLogging();
            }
            public boolean isLogging() {
                return TestStep.this.isLogging();
            }
        });
        getCookieSet().put(PackCookie.class,new PackCookie() {
            public void pack() {
                TestStep.this.pack();
            }
            public boolean isPacked() {
                return TestStep.this.isPacked();
            }
        });
    }
}
