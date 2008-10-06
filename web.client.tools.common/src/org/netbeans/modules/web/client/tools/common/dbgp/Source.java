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

package org.netbeans.modules.web.client.tools.common.dbgp;

import org.w3c.dom.Node;

/**
 * @author ads, jdeva
 *
 */
public class Source{
     public static class SourceCommand extends Command {
        private static final String BEGIN_ARG = "-b ";         // NOI18N
        private static final String END_ARG = "-e ";         // NOI18N

        public SourceCommand(int transactionId, String uri) {
            super(CommandMap.SOURCE.getCommand(), transactionId);
            fileUri = uri;
            begin = -1;
            end = -1;
        }

        public void setFile(String uri) {
            fileUri = uri;
        }

        public void setBeginLine(int line) {
            begin = line;
        }

        public void setEndLine(int line) {
            end = line;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder(FILE_ARG);
            builder.append(fileUri);

            if (begin != -1) {
                builder.append(Command.SPACE);
                builder.append(BEGIN_ARG);
                builder.append(begin);
            }

            if (end != -1) {
                builder.append(Command.SPACE);
                builder.append(END_ARG);
                builder.append(end);
            }

            return builder.toString();
        }
        private String fileUri;
        private int begin;
        private int end;
    }

    public static class SourceResponse extends ResponseMessage {
        SourceResponse(Node node) {
            super(node);
        }

        public boolean isSusccess() {
            return getBoolean(getNode(), SUCCESS);
        }

        public Encoding getEncoding(){
            String enc = getAttribute(getNode(), Property.ENCODING);
            return enc != null ? Encoding.valueOf(enc.toUpperCase()) : null;
        }

        public byte[] getSourceCode(boolean stripBeginCharacter) {
            String sourceValue = stripBeginCharacter ? getSourceText(getNode()) : getNodeValue(getNode());
            return Message.getDecodedBytes(getEncoding(), sourceValue);
        }        
        
        private static String getSourceText(Node node) {
            StringBuilder builder = getNodeValueImpl(node);
            //Remove prepended character to preserve leading new lines
            if (builder.length() > 0) {
                builder.delete(0, 1);
            }
            return replaceHtmlEntities(builder.toString());
        }
    }
}
