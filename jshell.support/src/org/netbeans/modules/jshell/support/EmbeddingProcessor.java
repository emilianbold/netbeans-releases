/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.model.ConsoleModel;
import java.util.ArrayList;
import java.util.List;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import org.netbeans.modules.jshell.model.ConsoleModel.SnippetHandle;
import org.netbeans.modules.jshell.support.InternalJShell.SnippetInfo;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public class EmbeddingProcessor {
    /**
     * The result list of embeddings
     */
    private List<Embedding> embeddings = new ArrayList<>();
    
    private ConsoleModel    model;
    private JShell shell;
    private Snapshot snapshot;
    private ShellSession session;
    
    private ConsoleSection  section;

    public EmbeddingProcessor(ShellSession session, ConsoleModel model, Snapshot snapshot) {
        this.session = session;
        this.model = model;
        this.snapshot = snapshot;
        
        this.shell = model.getShell();
    }
    
    public List<Embedding> process() {
        model.getSections().stream().filter(s -> s.getType().java).forEach(this::processSection);
        return embeddings;
    }
    
    private void defineEmbedding(SnippetHandle info, Rng posInfo) {
        String contents = info.getWrappedCode();
        int s = 0;
        int e = info.getSource().trim().length() - 1;
        
        int ts = info.getWrappedPosition(s);
        int te = info.getWrappedPosition(e);
        
        if (ts == -1 || te == -1) {
            // fall back: tell that the snippet text itself is the embedding
            embeddings.add(snapshot.create(posInfo.start, posInfo.len(), "text/x-java"));
            return;
        }
        te++;
        FileObject snipFile = session.snippetFile(info, model.getInputSection() == section);
        if (snipFile == null) {
            return;
        }
        Source src = Source.create(snipFile);
        Snapshot generatedSnapshot = src.createSnapshot();
        
        Embedding prolog = snapshot.create(contents.substring(0, ts), "text/x-java");
        Embedding epilog = snapshot.create(contents.substring(te), "text/x-java");
        
        List<Embedding> embs = new ArrayList<>();
        embs.add(prolog);
        int l = snapshot.getText().length() + 1;
        Rng[] fragments = info.getFragments();
        for (int i = 0; i < fragments.length; i++) {
            Rng r = fragments[i];
            // the document may have changed, and the console model has already
            // accommodated the change.
            if (r.end > l) {
                continue;
            }
//            System.err.println("Adding: " + r.start + "-" + r.end);
            embs.add(snapshot.create(r.start, r.len(), "text/x-java"));
        }
        embs.add(epilog);
        Embedding emb = Embedding.create(embs);
//        System.err.println("define embedding: " + emb.getSnapshot());
        embeddings.add(emb);
    }
    
    // a section may contain one or more snippets
    private void processSection(ConsoleSection section) {
        this.section = section;
        List<SnippetHandle> snippets = model.getSnippets(section);
        Rng[] ranges = section.getAllSnippetBounds();
        if (snippets == null) {
            return;
        }
        int index = 0;
        for (SnippetHandle s : snippets) {
            defineEmbedding(s, ranges[index++]);
        }
    }
}
