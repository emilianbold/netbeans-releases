/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author ads, jdeva
 *
 */
public class Context {
    private Context(String contextName, int id) {
        name = contextName;
        this.id = id;
    }

    public String getContext() {
        return name;
    }

    public int getId() {
        return id;
    }
    private String name;
    private int id;
        
    public static class ContextGetCommand extends Command {
        private static final String CONTEXT_ARG = "-c ";            // NOI18N

        public ContextGetCommand(int transactionId, int stackDepth) {
            super(CommandMap.CONTEXT_GET.getCommand(), transactionId);
            this.stackDepth = stackDepth;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder();
            if (this.stackDepth > -1) {
                builder.append(ContextNamesCommand.DEPTH_ARG);
                builder.append(this.stackDepth);
            }

            if (context != null) {
                if (builder.length() != 0) {
                    builder.append(Command.SPACE);
                }
                builder.append(CONTEXT_ARG);
                builder.append(context.getId());
            }

            return builder.toString();
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }
        private int stackDepth;
        private Context context;
    }
    
    
    public static class ContextGetResponse extends ResponseMessage {

        public ContextGetResponse(Node node) {
            super(node);
        }

        public int getContextId() {
            return getInt(getNode(), ContextNamesResponse.CONTEXT);
        }

        public List<Property> getProperties() {
            List<Node> nodes = getChildren(getNode(), Property.PROPERTY);
            List<Property> result = new ArrayList<Property>(nodes.size());
            for (Node node : nodes) {
                result.add(new Property(node));
            }
            return result;
        }
    }
    
    public static class ContextNamesCommand extends Command {
        static final String DEPTH_ARG = "-d ";                // NOI18N

        public ContextNamesCommand(int transactionId, int depth) {
            super(CommandMap.CONTEXT_NAMES.getCommand(), transactionId);
            this.depth = depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public int getDepth() {
            return depth;
        }

        @Override
        protected String getArguments() {
            if (depth > -1) {
                return DEPTH_ARG + depth;
            } else {
                return "";
            }
        }
        private int depth;
    }
    
    public static class ContextNamesResponse extends ResponseMessage {
        static final String CONTEXT = "context";        // NOI18N
        private static final String NAME = "name";           // NOI18N
        private static final String ID = "id";             // NOI18N

        ContextNamesResponse(Node node) {
            super(node);
        }

        Collection<Context> getContexts() {
            List<Context> result = new LinkedList<Context>();
            NodeList list = getNode().getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (CONTEXT.equals(node.getNodeName())) {
                    String name = getAttribute(node, NAME);
                    int id = getInt(node, ID);
                    result.add(new Context(name, id));
                }
            }
            return result;
        }
    }
}
