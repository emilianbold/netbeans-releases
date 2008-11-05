/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.languages;

import java.util.Collections;
import javax.swing.text.Document;
import org.netbeans.modules.languages.ASTEvaluatorFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.util.Lookup;


/**
 *
 * @author hanz
 */
public class ParserListenerFactoryImpl extends TaskFactory {


    @Override
    public Collection<SchedulerTask> create (Snapshot snapshot) {
        Document document = snapshot.getSource ().getDocument ();
        String mimeType = snapshot.getMimeType ();
        if (!LanguagesManager.getDefault ().isSupported (mimeType)) return null;
        //System.out.println("\nASTEvaluators: ");
        Map<String,Set<ASTEvaluator>> evaluators = new HashMap<String,Set<ASTEvaluator>> ();
        Iterator<? extends ASTEvaluatorFactory> it = getASTEvaluatorFactories ().iterator ();
        while (it.hasNext()) {
            ASTEvaluatorFactory factory = it.next ();
            ASTEvaluator evaluator = factory.create (document);
            String featureName = evaluator.getFeatureName ();
            Set<ASTEvaluator> set = evaluators.get (featureName);
            if (set == null) {
                set = new HashSet<ASTEvaluator> ();
                evaluators.put (featureName, set);
            }
            //System.out.println("  " + featureName + " : " + evaluator);
            set.add (evaluator);
        }
        if (evaluators.isEmpty ()) return null;
        return Collections.<SchedulerTask>singleton (new ParserListenerImpl (evaluators));
    }

    private static Lookup.Result<ASTEvaluatorFactory>   evaluatorFactoriesLookupResult;
    
    private static Collection<? extends ASTEvaluatorFactory> getASTEvaluatorFactories () {
        if (evaluatorFactoriesLookupResult == null) {
            evaluatorFactoriesLookupResult = Lookup.getDefault ().lookupResult (ASTEvaluatorFactory.class);
        }
        return evaluatorFactoriesLookupResult.allInstances ();
    }
    
    private static class ParserListenerImpl extends ParserResultTask<ParserResult> {

        private Map<String,Set<ASTEvaluator>> evaluators;
        
        ParserListenerImpl (Map<String,Set<ASTEvaluator>> evaluators) {
            this.evaluators = evaluators;
        }
        
        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel () {
        }

        @Override
        public void run (ParserResult parserResult) {
            Iterator<Set<ASTEvaluator>> it = evaluators.values ().iterator ();
            while (it.hasNext ()) {
                Iterator<ASTEvaluator> it2 = it.next ().iterator ();
                while (it2.hasNext ()) {
                    ASTEvaluator e = it2.next ();
                    e.beforeEvaluation (parserResult);
                    //if (cancel [0]) return;
                }
            }
                                                                            //times = new HashMap<Object,Long> ();
            evaluate (
                parserResult.getRootNode (), 
                new ArrayList<ASTItem> (), 
                evaluators                                                  //, times
            );                                                              //iit = times.keySet ().iterator ();while (iit.hasNext()) {Object object = iit.next();S ystem.out.println("  Evaluator " + object + " : " + times.get (object));}
            //if (cancel [0]) return;
            it = evaluators.values ().iterator ();
            while (it.hasNext ()) {
                Iterator<ASTEvaluator> it2 = it.next ().iterator ();
                while (it2.hasNext ()) {
                    ASTEvaluator e = it2.next ();
                    e.afterEvaluation (parserResult);
                    //if (cancel [0]) return;
                }
            }
        }

        @Override
        public int getPriority () {
            return 100;
        }
        
        private void evaluate (
            ASTItem                         item, 
            List<ASTItem>                   path,
            Map<String,Set<ASTEvaluator>>   evaluatorsMap2                            //, Map<Object,Long> times                                         
        ) {
            path.add (item);
            Language language = (Language) item.getLanguage ();
            if (language != null)
                language.getFeatureList ().evaluate (
                     path, 
                     evaluatorsMap2                                                 //, times
                );
            Iterator<ASTItem> it2 = item.getChildren ().iterator ();
            while (it2.hasNext ()) {
//                if (cancel [0]) return;
                evaluate (
                    it2.next (), 
                    path, 
                    evaluatorsMap2                                                  //, times
                );
            }
            path.remove (path.size () - 1);
        }
    }
}
