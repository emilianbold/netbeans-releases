/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual.ui.preview;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.IntrospectionException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.visual.v2.RuleNode;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class UITest extends CslTestBase {

    public UITest(String testName) {
        super(testName);
    }

//    public void test() throws IntrospectionException {
//        String code = ".threelinesarticle {\n"
//                + "\tbackground-image:url(\"/images_www/v4/tl.gif\");\n"
//                + "\tbackground-repeat:no-repeat;\n"
//                + "\tbackground-position: 0px 4px;\n"
//                + "\tpadding-left:15px;\n"
//                + "\tfont-weight:bold;\n"
//                + "}";
//
//        CssParserResult result = TestUtil.parse(code);
//        Model model = new Model(result);
//
//        final AtomicReference<Rule> ruleRef = new AtomicReference<Rule>();
//        model.runReadTask(new Model.ModelTask() {
//
//            @Override
//            public void run(Model model) {
//                List<Rule> rules = model.getStyleSheet().getBody().getRules();
//                ruleRef.set(rules.get(0));
//            }
//        });
//
//        //leaking model out of the lock!!!! just for test purposes!!!!!
//        Rule rule = ruleRef.get();
//
//        JFrame f = new JFrame("CSS Properties");
//
//        PropertySheet sheet = new PropertySheet();
// 
////        Node node = new BeanNode(new JFrame());
//        Node node = new RuleNode(result.getSnapshot(), model, rule);
////        node.
//        sheet.setNodes(new Node[]{node});
//
//        f.setContentPane(sheet);
//
//        f.pack();
//
//        f.setVisible(true);
//
//        final Object lock = new Object();
//        f.addWindowListener(new WindowAdapter() {
//
//            @Override
//            public void windowClosing(WindowEvent we) {
//                super.windowClosing(we);
//                synchronized (lock) {
//                    lock.notifyAll();
//                }
//            }
//        });
//        try {
//            synchronized (lock) {
//                lock.wait();
//            }
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//
//        System.out.println("modified source:");
//        System.out.println(model.getModelSource());
//        
//    }
}
