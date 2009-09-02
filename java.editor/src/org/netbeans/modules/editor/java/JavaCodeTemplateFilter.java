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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class JavaCodeTemplateFilter implements CodeTemplateFilter, Task<CompilationController> {
    
    private static final Logger LOG = Logger.getLogger(JavaCodeTemplateFilter.class.getName());
    
    private int startOffset;
    private int endOffset;
    private Tree.Kind ctx = null;
    
    private JavaCodeTemplateFilter(JTextComponent component, int offset) {
        this.startOffset = offset;
        this.endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : -1;            
        final JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            ScanDialog.runWhenScanFinished (new Runnable () {
                public void run () {
                    try {
                        js.runUserActionTask (new Task<CompilationController> () {
                            public void run (CompilationController controller ) throws Exception {
                                JavaCodeTemplateFilter.this.run (controller);
                            }
                        }, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, "JavaCodeTemplateFilter");
        }
    }

    public synchronized boolean accept(CodeTemplate template) {
        return ctx != null && getTemplateContexts(template).contains(ctx);
    }
    

    public synchronized void run(CompilationController controller) throws IOException {
        controller.toPhase(Phase.PARSED);
        Tree tree = controller.getTreeUtilities().pathFor(startOffset).getLeaf();
        if (endOffset >= 0 && startOffset != endOffset) {
            if (controller.getTreeUtilities().pathFor(endOffset).getLeaf() != tree)
                return;
        }
        ctx = tree.getKind();
    }

    private EnumSet<Tree.Kind> getTemplateContexts(CodeTemplate template) {
        List<String> contexts = template.getContexts();
        List<Tree.Kind> kinds = new ArrayList<Tree.Kind>();
        
        if (contexts != null) {
            for(String ctx : contexts) {
                Tree.Kind kind = Tree.Kind.valueOf(ctx);
                if (kind != null) {
                    kinds.add(kind);
                } else {
                    LOG.warning("Invalid code template context '" + ctx + "', ignoring."); //NOI18N
                }
            }
        }
        
        if (kinds.size() > 0) {
            return EnumSet.copyOf(kinds);
        } else {
            return EnumSet.noneOf(Tree.Kind.class);
        }
    }

    public static final class Factory implements CodeTemplateFilter.Factory {
        
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new JavaCodeTemplateFilter(component, offset);
        }
    }
}
