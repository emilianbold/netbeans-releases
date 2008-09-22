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

package org.netbeans.modules.web.client.tools.common.dbgp;

import org.w3c.dom.Node;

/**
 *
 * @author ads, jdeva
 */
public class Feature {
    public enum Name {
        LANGUAGE_SUPPORTS_THREADS(false),
        LANGUAGE_NAME(false),
        LANGUAGE_VERSION(false),
        ENCODING(false),
        PROTOCOL_VERSION(false),
        SUPPORTS_ASYNC(false),
        DATA_ENCODING(false),
        BREAKPOINT_LANGUAGES(false),
        MULTIPLE_SESSIONS(true),
        MAX_CHILDREN(true),
        MAX_DATA(true),
        MAX_DEPTH(true),
        SUPPORTS_POSTMORTEM(false),
        SHOW_HIDDEN(true),
        NOTIFY_OK(true),
        HTTP_MONITOR(false) { @Override public String toDBGPFeatureName() { return "http_monitor"; }},
        
        // Javascript debugger options
    	SUSPEND_ON_FIRST_LINE(true)      { @Override public String toDBGPFeatureName() { return "suspendOnFirstLine"; }},
    	SUSPEND_ON_EXCEPTIONS(true)      { @Override public String toDBGPFeatureName() { return "suspendOnExceptions"; }},
    	SUSPEND_ON_ERRORS(true)          { @Override public String toDBGPFeatureName() { return "suspendOnErrors"; }},
    	SUSPEND_ON_DEBUGGERKEYWORD(true) { @Override public String toDBGPFeatureName() { return "suspendOnDebuggerKeyword"; }},
        SHOW_FUNCTIONS(true)             { @Override public String toDBGPFeatureName() { return "showFunctions"; }},
    	SHOW_CONSTANTS(true)             { @Override public String toDBGPFeatureName() { return "showConstants"; }},
        IGNORE_QUERY_STRINGS(true)       { @Override public String toDBGPFeatureName() { return "ignoreQueryStrings"; }},

    	ENABLE(true)                     { @Override public String toDBGPFeatureName() { return "enable"; }}
        ;

        boolean isSettable;
        Name(boolean isSettable) {
            this.isSettable = isSettable;
        }
        
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
        
        public String toDBGPFeatureName() {
            return toString();
        }
    }
    
    public static class FeatureGetCommand extends Command {
        private static final String NAME_ARG = "-n ";                // NOI18N

        public FeatureGetCommand(int transactionId, Name feature) {
            this(CommandMap.FEATURE_GET.getCommand(), transactionId, feature);
        }

        protected FeatureGetCommand(String command, int transactionId, Name feature) {
            super(command, transactionId);
            this.name = feature.toDBGPFeatureName();
        }

        public void setFeature(Name feature) {
            name = feature.toDBGPFeatureName();
        }

        public void setFeature(String name) {
            this.name = name;
        }

        @Override
        protected String getArguments() {
            return NAME_ARG + name;
        }

        private String name;
    }
    
    public static class FeatureGetResponse extends FeatureSetResponse {
        private static final String SUPPORTED = "supported";         // NOI18N

        FeatureGetResponse(Node node) {
            super(node);
        }

        /**
         * This method does NOT mean that the feature is supported, 
         * this is encoded in the text child of the response tag. 
         * The 'supported' attribute informs whether the feature with 
         * 'feature_name' is supported by feature_get in the engine, 
         * or when the command with name 'feature_get' is supported by the engine.
         * @return
         */
        public boolean isSupportedFeatureName() {
            String value = getAttribute(getNode(), SUPPORTED);
            try {
                return Integer.parseInt(value) > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public String getDetails() {
            return getNodeValue(getNode());
        }
    }
    
    public static class FeatureSetCommand extends FeatureGetCommand {
        private static final String VALUE_ARG = "-v ";                // NOI18N

        public FeatureSetCommand(int transactionId, Name feature, String value) {
            super(CommandMap.FEATURE_SET.getCommand(), transactionId, feature);
            this.value = value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder(super.getArguments());

            builder.append(Command.SPACE);
            builder.append(VALUE_ARG);
            builder.append(value);

            return builder.toString();
        }
        private String value;
    }
    
    public static class FeatureSetResponse extends ResponseMessage {
        private static final String FEATURE = "feature";      // NOI18N

        FeatureSetResponse(Node node) {
            super(node);
        }

        public String getFeature() {
            return getAttribute(getNode(), FEATURE);
        }

        public boolean isSuccess() {
            return getBoolean(getNode(), SUCCESS);
        }
    }
}
