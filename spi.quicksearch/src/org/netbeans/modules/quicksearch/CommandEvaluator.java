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
    
    private final static String RECENT = "Recent";
    
    /**
     * command pattern is:
     * "command arguments"
     */
    private static Pattern COMMAND_PATTERN = Pattern.compile("(\\w+)(\\s+)(.+)");
    
    /**
     * if command is in form "command arguments" then only providers registered 
     * for given command are called. Otherwise all providers are called.
     * @param command
     * @return 
     */
    public static void evaluate (String command, ResultsModel model) {
        
        List<CategoryResult> l = new ArrayList<CategoryResult>();
        Matcher m = COMMAND_PATTERN.matcher(command);
        String commandString = null;
        String text = null;
        if (m.matches()) {
            commandString = m.group(1);
            if (ProviderRegistry.getInstance().getProviders().isKnownCommand(commandString)) {
                text = m.group(3);
            } else {
                commandString = null;
                text = command;
            }
        } else {
            text = command;
        }
        
        boolean onlyRecent = text == null || text.trim().equals("");
        
        SearchRequest sRequest = Accessor.DEFAULT.createRequest(text, null);
        
        for (ProviderModel.Category cat : ProviderRegistry.getInstance().getProviders().getCategories()) {
            // skip all but recent if empty string came
            if (onlyRecent && !RECENT.equals(cat.getName())) {
                continue;
            }
            
            CategoryResult catResult = new CategoryResult(cat);
            SearchResponse sResponse = Accessor.DEFAULT.createResponse(catResult);
            for (SearchProvider provider : cat.getProviders()) {
                if (commandString != null) {
                    String commandPrefix = cat.getCommandPrefix();
                    if (commandPrefix != null && commandPrefix.equalsIgnoreCase(commandString)) {
                        runEvaluation(provider, sRequest, sResponse, cat);
                    }
                } else {
                    runEvaluation(provider, sRequest, sResponse, cat);
                }
            }
            l.add(catResult);
        }

        model.setContent(l);
    }
    
    private static Task runEvaluation (final SearchProvider provider, final SearchRequest request,
                                final SearchResponse response, ProviderModel.Category cat) {
        // actions are not happy outside EQ at all
        if ("Actions".equals(cat.getName())) {
            provider.evaluate(request, response);
            return null;
        }
        
        return RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                provider.evaluate(request, response);
            }
        });
    }

}
