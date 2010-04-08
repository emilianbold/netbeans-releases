/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jackpot.spi.CustomizerProvider;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.jackpot.spi.HintProvider;
import org.netbeans.modules.java.hints.jackpot.spi.support.ErrorDescriptionFactory;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.Rule;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

import static org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription;

/** Manages rules read from the system filesystem.
 *
 * @author Petr Hrebejk
 */
public class RulesManager implements FileChangeListener {

    // The logger
    public static Logger LOG = Logger.getLogger("org.netbeans.modules.java.hints"); // NOI18N

    // Extensions of files
    private static final String INSTANCE_EXT = ".instance";

    // Non GUI attribute for NON GUI rules
    private static final String NON_GUI = "nonGUI"; // NOI18N
    
    private static final String RULES_FOLDER = "org-netbeans-modules-java-hints/rules/";  // NOI18N
    private static final String ERRORS = "errors"; // NOI18N
    private static final String HINTS = "hints"; // NOI18N
    private static final String SUGGESTIONS = "suggestions"; // NOI18N

    // Maps of registered rules
    private final Map<String, Map<String,List<ErrorRule>>> mimeType2Errors = new HashMap<String, Map<String, List<ErrorRule>>>();
    private final Map<HintMetadata, Collection<? extends HintDescription>> metadata = new HashMap<HintMetadata, Collection<? extends HintDescription>>();

    private static RulesManager INSTANCE;

    private RulesManager() {
        doInit();
    }

    public static synchronized RulesManager getInstance() {
        if ( INSTANCE == null ) {
            INSTANCE = new RulesManager();
        }
        return INSTANCE;
    }

    public synchronized Map<String,List<ErrorRule>> getErrors(String mimeType) {
        Map<String,List<ErrorRule>> res = mimeType2Errors.get(mimeType);

        if (res == null) {
            res = Collections.emptyMap();
        }

        return res;
    }

    private synchronized void doInit() {
        initErrors();
        initHints();
        initSuggestions();
    }

    // Private methods ---------------------------------------------------------

    private void initErrors() {
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + ERRORS );
        List<Pair<Rule,FileObject>> rules = readRules( folder );
        categorizeErrorRules(rules, mimeType2Errors, folder);
    }

    private void initHints() {
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + HINTS );
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeTreeRules( rules, HintMetadata.Kind.HINT, HintMetadata.Kind.HINT_NON_GUI, metadata);
    }


    private void initSuggestions() {
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + SUGGESTIONS );
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeTreeRules( rules, HintMetadata.Kind.SUGGESTION, HintMetadata.Kind.SUGGESTION_NON_GUI, metadata);
    }

    /** Read rules from system filesystem */
    private List<Pair<Rule,FileObject>> readRules( FileObject folder ) {
        List<Pair<Rule,FileObject>> rules = new LinkedList<Pair<Rule,FileObject>>();
        
        if (folder == null) {
            return rules;
        }

        Queue<FileObject> q = new LinkedList<FileObject>();
        
        q.offer(folder);
        
        while(!q.isEmpty()) {
            FileObject o = q.poll();
            
            o.removeFileChangeListener(this);
            o.addFileChangeListener(this);
            
            if (o.isFolder()) {
                q.addAll(Arrays.asList(o.getChildren()));
                continue;
            }
            
            if (!o.isData()) {
                continue;
            }
            
            String name = o.getNameExt().toLowerCase();

            if ( o.canRead() ) {
                Rule r = null;
                if ( name.endsWith( INSTANCE_EXT ) ) {
                    r = instantiateRule(o);
                }
                if ( r != null ) {
                    rules.add( new Pair<Rule,FileObject>( r, o ) );
                }
            }
        }
        return rules;
    }

    private static void categorizeErrorRules( List<Pair<Rule,FileObject>> rules,
                                             Map<String, Map<String,List<ErrorRule>>> dest,
                                             FileObject rootFolder) {
        dest.clear();

        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();
            String mime = FileUtil.getRelativePath(rootFolder, fo.getParent());

            if (mime.length() == 0) {
                mime = Utilities.JAVA_MIME_TYPE;
            }

            if ( rule instanceof ErrorRule ) {
                Map<String, List<ErrorRule>> map = dest.get(mime);

                if (map == null) {
                    dest.put(mime, map = new HashMap<String, List<ErrorRule>>());
                }

                addRule( (ErrorRule)rule, map );
            }
            else {
                LOG.log( Level.WARNING, "The rule defined in " + fo.getPath() + "is not instance of ErrorRule" );
            }
        }
    }

    private static void categorizeTreeRules( List<Pair<Rule,FileObject>> rules,
                                             HintMetadata.Kind kind,
                                             HintMetadata.Kind kindNonGui,
                                             Map<HintMetadata, Collection<? extends HintDescription>> metadata) {
        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();

            if ( rule instanceof TreeRule ) {
                final TreeRule tr = (TreeRule) rule;
                Object nonGuiObject = fo.getAttribute(NON_GUI);
                boolean toGui = true;
                
                if ( nonGuiObject != null &&
                     nonGuiObject instanceof Boolean &&
                     ((Boolean)nonGuiObject).booleanValue() ) {
                    toGui = false;
                }

                HintMetadata hm;

                FileObject parent = fo.getParent();

                if (rule instanceof AbstractHint) {
                    final AbstractHint h = (AbstractHint) rule;
                    hm = HintMetadata.create(h.getId(),
                                             toGui ? h.getDisplayName() : "",
                                             toGui ? h.getDescription() : "",
                                             parent.getName(),
                                             HintsSettings.HINTS_ACCESSOR.isEnabledDefault(h),
                                             toGui ? kind : kindNonGui,
                                             HintsSettings.HINTS_ACCESSOR.severiryDefault(h),
                                             new CustomizerProviderImpl(h),
                                             Arrays.asList(HintsSettings.HINTS_ACCESSOR.getSuppressBy(h)));
                } else {
                    hm = HintMetadata.create(tr.getId(),
                                             toGui ? tr.getDisplayName() : "",
                                             toGui ? tr.getDisplayName() : "",
                                             parent.getName(),
                                             true,
                                             toGui ? kind : kindNonGui,
                                             AbstractHint.HintSeverity.WARNING,
                                             Arrays.<String>asList());
                }

                List<HintDescription> hd = new LinkedList<HintDescription>();

                for (Kind k : tr.getTreeKinds()) {
                    hd.add(HintDescriptionFactory.create()
                                                 .setTriggerKind(k)
                                                 .setMetadata(hm)
                                                 .setWorker(new WorkerImpl(tr))
                                                 .produce());
                }

                metadata.put(hm, hd);
            }
            else {
                LOG.log( Level.WARNING, "The rule defined in " + fo.getPath() + "is not instance of TreeRule" );
            }

        }
    }

    private static void addRule( TreeRule rule, Map<Tree.Kind,List<TreeRule>> dest ) {

        for( Tree.Kind kind : rule.getTreeKinds() ) {
            List<TreeRule> l = dest.get( kind );
            if ( l == null ) {
                l = new LinkedList<TreeRule>();
                dest.put( kind, l );
            }
            l.add( rule );
        }

    }

    @SuppressWarnings("unchecked")
    private static void addRule( ErrorRule rule, Map<String,List<ErrorRule>> dest ) {

        for(String code : (Set<String>) rule.getCodes()) {
            List<ErrorRule> l = dest.get( code );
            if ( l == null ) {
                l = new LinkedList<ErrorRule>();
                dest.put( code, l );
            }
            l.add( rule );
        }

    }

    private static Rule instantiateRule( FileObject fileObject ) {
        try {
            DataObject dobj = DataObject.find(fileObject);
            InstanceCookie ic = dobj.getCookie( InstanceCookie.class );
            Object instance = ic.instanceCreate();
            
            if (instance instanceof Rule) {
                return (Rule) instance;
            } else {
                return null;
            }
        } catch( IOException e ) {
            LOG.log(Level.INFO, null, e);
        } catch ( ClassNotFoundException e ) {
            LOG.log(Level.INFO, null, e);
        }

        return null;
    }

    public void fileFolderCreated(FileEvent fe) {
        hintsChanged();
    }

    public void fileDataCreated(FileEvent fe) {
        hintsChanged();
    }

    public void fileChanged(FileEvent fe) {
        hintsChanged();
    }

    public void fileDeleted(FileEvent fe) {
        hintsChanged();
    }

    public void fileRenamed(FileRenameEvent fe) {
        hintsChanged();
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        hintsChanged();
    }

    private void hintsChanged() {
        refreshHints.cancel();
        refreshHints.schedule(50);
    }
    
    private final RequestProcessor.Task refreshHints = new RequestProcessor(RulesManager.class.getName()).create(new Runnable() {
        public void run() {
            doInit();
        }
    });

    private static final class CustomizerProviderImpl implements CustomizerProvider {
        private final AbstractHint hint;

        public CustomizerProviderImpl(AbstractHint hint) {
            this.hint = hint;
        }

        public JComponent getCustomizer(Preferences prefs) {
            return hint.getCustomizer(prefs);
        }
    }

    private static class WorkerImpl implements Worker {
        private final TreeRule tr;

        public WorkerImpl(TreeRule tr) {
            this.tr = tr;
        }

        public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
            Collection<? extends ErrorDescription> result = tr.run(ctx.getInfo(), ctx.getPath());

            if (result == null) return result;

            Collection<ErrorDescription> wrapped = new LinkedList<ErrorDescription>();

            for (ErrorDescription ed : result) {
                if (ed == null) continue;
                List<Fix> fixesForED = ErrorDescriptionFactory.resolveDefaultFixes(ctx, ed.getFixes().getFixes().toArray(new Fix[0]));

                ErrorDescription nue = createErrorDescription(ed.getSeverity(),
                                                              ed.getDescription(),
                                                              fixesForED,
                                                              ed.getFile(),
                                                              ed.getRange().getBegin().getOffset(),
                                                              ed.getRange().getEnd().getOffset());

                wrapped.add(nue);
            }

            return wrapped;
        }
    }
    
    @ServiceProvider(service=HintProvider.class)
    public static final class HintProviderImpl implements HintProvider {

        public Map<HintMetadata, Collection<? extends HintDescription>> computeHints() {
            return RulesManager.getInstance().metadata;
        }
        
    }

}
