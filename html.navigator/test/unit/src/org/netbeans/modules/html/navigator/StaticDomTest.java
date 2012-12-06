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
package org.netbeans.modules.html.navigator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.junit.Test;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Jan Becicka
 */
public class StaticDomTest extends TestBase {

    private static String source1 = "<!DOCTYPE html><html><head><title></title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body><div>TODO write content</div></body></html>";
    private static String[] gold1 = {"root", "html", "head", "title", "meta", "body", "div"};

    public StaticDomTest(String name) {
        super(name);
    }

    @Test
    public void testDOM1() throws InterruptedException, IOException, ParseException, ExecutionException, InterruptedException, InterruptedException, InvocationTargetException {
        performDOMTest(source1, gold1);
        HtmlNavigatorPanel.ui.setCaretOffset("<!DOCTYPE html>".length());
        assertEquals(getSelectedNode().getName(), "html");
    }
    
    @Test
    public void testDOM2() throws InterruptedException, IOException, ParseException, ExecutionException, InterruptedException, InterruptedException, InvocationTargetException {
        performDOMTest(source1, gold1);
        HtmlNavigatorPanel.ui.setCaretOffset("<!DOCTYPE html><html>".length());
        assertEquals(getSelectedNode().getName(), "head");
    }

    @Test
    public void testDOM3() throws InterruptedException, IOException, ParseException, ExecutionException, InterruptedException, InterruptedException, InvocationTargetException {
        performDOMTest(source1, gold1);
        HtmlNavigatorPanel.ui.setCaretOffset("<!DOCTYPE html><html><head>".length());
        assertEquals(getSelectedNode().getName(), "title");
    }

    @Test
    public void testDOM4() throws InterruptedException, IOException, ParseException, ExecutionException, InterruptedException, InterruptedException, InvocationTargetException {
        performDOMTest(source1, gold1);
        HtmlNavigatorPanel.ui.setCaretOffset("<!DOCTYPE html><html><head><title>".length());
        assertEquals(getSelectedNode().getName(), "title");
    }

    @Test
    public void testDOM5() throws InterruptedException, IOException, ParseException, ExecutionException, InterruptedException, InterruptedException, InvocationTargetException {
        performDOMTest(source1, gold1);
        HtmlNavigatorPanel.ui.setCaretOffset("<!DOCTYPE html><html><head><title></title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">".length());
        assertEquals(getSelectedNode().getName(), "meta");
    }
    

    private void performDOMTest(String src, final String[] golden) throws InterruptedException, IOException, ParseException, ExecutionException {
        final FileObject file = createFile("test.html", src);

        Source source = Source.create(file);
        final Future<Node> nodeFuture[] = new Future[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                HtmlParserResult result = (HtmlParserResult) resultIterator.getParserResult();
                HtmlNavigatorPanel.ui.setParserResult(result);
                nodeFuture[0] = HtmlNavigatorPanel.ui.performTest(new Callable<Node>() {
                    @Override
                    public Node call() throws Exception {
                        return HtmlNavigatorPanel.ui.getExplorerManager().getRootContext();
                    }
                });
            }
        });

        final Node node = nodeFuture[0].get();
        compare(node, golden);

    }

    private void compare(Node node, String[] golden) {
        String[] array = dump(node);
        assertTrue(Arrays.deepEquals(array, golden));
    }

    private String[] dump(Node node) {
        ArrayList<String> l = new ArrayList<String>();
        l.add(node.getName());
        for (Node ch : node.getChildren().getNodes()) {
            l.addAll(Arrays.asList((dump(ch))));
        }
        return l.toArray(new String[]{});
    }

    private Node getSelectedNode() throws InterruptedException, ExecutionException, InvocationTargetException {
        HtmlNavigatorPanel.ui.performTest(new Callable() {

            @Override
            public Object call() throws Exception {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });
                return null;
            }
        });
        
        
        final Future<Node> nodeFuture = HtmlNavigatorPanel.ui.performTest(new Callable<Node>() {
            @Override
            public Node call() throws Exception {
                return HtmlNavigatorPanel.ui.getExplorerManager().getSelectedNodes()[0];
            }
        });
        return nodeFuture.get();
    }
}
