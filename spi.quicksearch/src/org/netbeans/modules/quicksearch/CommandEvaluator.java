/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.quicksearch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.quicksearch.ProviderModel.Category;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Command Evaluator. It evaluates commands from toolbar and creates results.
 * 
 * @author Jan Becicka, Dafe Simonek
 */
public class CommandEvaluator {
    
    final static String RECENT = "Recent";
    
    /**
     * command pattern is:
     * "command arguments"
     */
    private static Pattern COMMAND_PATTERN = Pattern.compile("(\\w+)(\\s+)(.+)");

    /** Narrow evaluation only to specified category if non null.
     * Evaluate all categories otherwise
     */
    private static ProviderModel.Category evalCat;

    /** Temporary narrow evaluation to only specified category **/
    private static boolean isCatTemporary;

    private static final RequestProcessor RP = new RequestProcessor("QuickSearch Command Evaluator"); // NOI18N
    
    /**
     * Runs evaluation.
     *
     * @param command text to evauate, to search for
     *
     * @return task of this evaluation, which waits for all providers to complete
     * execution. Use returned instance to recognize if this evaluation still
     * runs and when it actually will finish.
     */
    public static org.openide.util.Task evaluate (String command, ResultsModel model) {
        List<CategoryResult> l = new ArrayList<CategoryResult>();
        String[] commands = parseCommand(command);
        SearchRequest sRequest = Accessor.DEFAULT.createRequest(commands[1], null);
        List<Task> tasks = new ArrayList<Task>();

        List<Category> provCats = new ArrayList<Category>();
        boolean allResults = getProviderCategories(commands, provCats);

        for (ProviderModel.Category curCat : provCats) {
            CategoryResult catResult = new CategoryResult(curCat, allResults);
            SearchResponse sResponse = Accessor.DEFAULT.createResponse(catResult, sRequest);
            for (SearchProvider provider : curCat.getProviders()) {
                Task t = runEvaluation(provider, sRequest, sResponse, curCat);
                if (t != null) {
                    tasks.add(t);
                }
            }
            l.add(catResult);
        }

        model.setContent(l);

        return new Wait4AllTask(tasks);
    }

    public static Category getEvalCat () {
        return evalCat;
    }

    public static void setEvalCat (Category cat) {
        CommandEvaluator.evalCat = cat;
    }

    public static boolean isCatTemporary () {
        return isCatTemporary;
    }

    public static void setCatTemporary (boolean isCatTemporary) {
        CommandEvaluator.isCatTemporary = isCatTemporary;
    }

    private static String[] parseCommand (String command) {
        String[] results = new String[2];

        Matcher m = COMMAND_PATTERN.matcher(command);

        if (m.matches()) {
            results[0] = m.group(1);
            if (ProviderModel.getInstance().isKnownCommand(results[0])) {
                results[1] = m.group(3);
            } else {
                results[0] = null;
                results[1] = command;
            }
        } else {
            results[1] = command;
        }
                
        return results;
    }

    /** Returns array of providers to ask for evaluation according to
     * current evaluation rules.
     *
     * @return true if providers are expected to return all results, false otherwise
     */
    private static boolean getProviderCategories (String[] commands, List<Category> result) {
        List<Category> cats = ProviderModel.getInstance().getCategories();

        // always include recent searches
        for (Category cat : cats) {
            if (RECENT.equals(cat.getName())) {
                result.add(cat);
            }
        }

        // skip all but recent if empty string came
        if (commands[1] == null || commands[1].trim().equals("")) {
            return false;
        }

        // command string has biggest priority for narrow evaluation to category
        if (commands[0] != null) {
            for (Category curCat : cats) {
                String commandPrefix = curCat.getCommandPrefix();
                if (commandPrefix != null && commandPrefix.equalsIgnoreCase(commands[0])) {
                    result.add(curCat);
                    return true;
                }
            }
        }

        // evaluation narrowed to category perhaps?
        if (evalCat != null) {
            result.add(evalCat);
            return true;
        }

        // no narrowing
        result.clear();
        result.addAll(cats);

        return false;
    }

    private static Task runEvaluation (final SearchProvider provider, final SearchRequest request,
                                final SearchResponse response, final ProviderModel.Category cat) {
        // actions are not happy outside EQ at all
        if ("Actions".equals(cat.getName())) {
            provider.evaluate(request, response);
            return null;
        }
        
        return RP.post(new Runnable() {
            @Override
            public void run() {
                provider.evaluate(request, response);
            }
        });
    }

    /** Task implementation that computes nothing itself, it just waits
     * for all given RequestProcessor tasks to finish and then it finishes as well.
     */
    private static class Wait4AllTask extends org.openide.util.Task implements Runnable {

        private static final long TIMEOUT = 60000;

        private List<Task> tasks;

        private Wait4AllTask (List<Task> tasks) {
            super();
            this.tasks = tasks;
        }

        @Override
        public void run () {
            try {
                notifyRunning();
                for (Task task : tasks) {
                    try {
                        // wait no longer then one minute
                        task.waitFinished(TIMEOUT);
                    } catch (InterruptedException ex) {
                        // ignore, we are not interested
                    }
                }
            } finally {
                notifyFinished();
            }
        }
    }

}
