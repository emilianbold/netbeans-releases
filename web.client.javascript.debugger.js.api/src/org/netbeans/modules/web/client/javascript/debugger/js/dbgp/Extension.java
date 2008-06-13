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

package org.netbeans.modules.web.client.javascript.debugger.js.dbgp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;

/**
 *
 * @author jdeva
 */
public class Extension {
    static final String FILE_URI    = "fileuri";        // NOI18N
    public static class OpenURICommand extends Command {
        String fileURI;
        public OpenURICommand(int transactionId, String fileURI) {
            super(CommandMap.OPEN_URI.getCommand(), transactionId);
            this.fileURI = fileURI;
        }
        
        @Override
        public boolean wantAcknowledgment() {
            return false;
        }                
        
        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder();
            builder.append(Command.FILE_ARG);
            builder.append(fileURI);
            return builder.toString();
        }        
    }
    
    public static class OpenURIResponse extends ResponseMessage {
        public OpenURIResponse(Node node) {
            super(node);
        }
    }
    
    public static class Source extends BaseMessageChildElement {
        public Source(Node node) {
            super(node);
        }
        
        public String getURI() {
            return getAttribute(FILE_URI);
        }        
    }
    
    public static class SourceGetCommand extends Command {
        public SourceGetCommand(int transactionId) {
            super(CommandMap.SOURCE_GET.getCommand(), transactionId);
        }
        
    }
    
    public static class SourceGetResponse extends ResponseMessage {
        private static final String SOURCE = "source";        // NOI18N

        SourceGetResponse(Node node) {
            super(node);
        }

        public List<Source> getSources() {
            List<Source> result = new LinkedList<Source>();
            List<Node> nodes = getChildren(getNode(), SOURCE);
            for (Node node : nodes) {
                result.add(new Source(node));
            }
            return result;
        }
    }
    
    public static class Window extends BaseMessageChildElement {
        static final String WINDOW = "window";        // NOI18N
        public Window(Node node) {
            super(node);
        }
        
        public String getURI() {
            return getAttribute(FILE_URI);
        }
        
        public List<Window> getChildren() {
            List<Node> nodes = getChildren(WINDOW);
            List<Window> result = new ArrayList<Window>(nodes.size());
            for (Node node : nodes) {
                result.add(new Window(node));
            }
            return result;
        }        
    }
    
   public static class WindowGetCommand extends Command {
        public WindowGetCommand(int transactionId) {
            super(CommandMap.WINDOW_GET.getCommand(), transactionId);
        }
    }
   
   public static class WindowGetResponse extends ResponseMessage {
        WindowGetResponse(Node node) {
            super(node);
        }

        public List<Window> getWindows() {
            List<Window> result = new LinkedList<Window>();
            List<Node> nodes = getChildren(getNode(), Window.WINDOW);
            for (Node node : nodes) {
                result.add(new Window(node));
            }
            return result;
        }
    }   
}
