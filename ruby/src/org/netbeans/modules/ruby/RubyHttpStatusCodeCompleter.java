/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.ruby.elements.CommentElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.RubyElement;
import org.openide.util.NbBundle;

/**
 * Code completer for HTTP status codes.
 *
 * @author Erno Mononen
 */
final class RubyHttpStatusCodeCompleter extends RubyBaseCompleter {

    private static final String[] STATUS_CODE_KEYS = {"100", "101",
        "200", "201", "202", "203", "204", "205", "206",
        "300", "301", "302", "303", "304", "305", "307",
        "400", "401", "403", "404", "405", "406", "407", "408", "409", "410",
        "411", "412", "413", "414", "415", "416", "417",
        "500", "501", "502", "503", "504", "505"};

    private static final List<StatusCode> STATUS_CODES = initStatusCodes();

    private final IndexedMethod target;

    private RubyHttpStatusCodeCompleter(List<? super CompletionProposal> proposals,
            CompletionRequest request, int anchor, boolean caseSensitive, IndexedMethod target) {
        super(proposals, request, anchor, caseSensitive);
        this.target = target;

    }

    static boolean complete(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final int anchor,
            final boolean caseSensitive,
            IndexedMethod target) {
        RubyHttpStatusCodeCompleter rsc = new RubyHttpStatusCodeCompleter(proposals, request, anchor, caseSensitive, target);
        return rsc.complete();
    }

    private boolean complete() {
        String prefix = request.prefix;
        boolean completed = false;
        for (StatusCode statusCode : STATUS_CODES) {
            if (statusCode.code.startsWith(prefix)) {
                RubyElement element = new CommentElement(statusCode.longDesc);
                RubyCompletionItem.ParameterItem item =
                        new RubyCompletionItem.ParameterItem(element, statusCode.shortDesc, statusCode.code, statusCode.code, anchor, request);
                item.setSmart(true);
                proposals.add(item);
                completed = true;
            }
        }

        return completed;
    }

    private static List<StatusCode> initStatusCodes() {
        List<StatusCode> result = new ArrayList<StatusCode>(STATUS_CODE_KEYS.length);
        NbBundle.getBundle(RubyHttpStatusCodeCompleter.class);
        for (String key : STATUS_CODE_KEYS) {
            String shortDesc = NbBundle.getMessage(RubyHttpStatusCodeCompleter.class, "SC_" + key);
            String longDesc = NbBundle.getMessage(RubyHttpStatusCodeCompleter.class, "SC_" + key + "_DESC");
            result.add(new StatusCode(key, shortDesc, longDesc));
        }
        return result;
    }

    private static class StatusCode {
        private final String code;
        private final String shortDesc;
        private final String longDesc;

        public StatusCode(String code, String shortDesc, String longDesc) {
            this.code = code;
            this.shortDesc = shortDesc;
            this.longDesc = longDesc;
        }

    }

}
