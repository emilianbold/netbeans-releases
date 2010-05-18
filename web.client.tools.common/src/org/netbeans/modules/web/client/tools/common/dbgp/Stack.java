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

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;

/**
 * @author ads, jdeva
 *
 */
public class Stack extends Input {
    private static final String INPUT       = "input";         // NOI18N
    private static final String CMDEND      = "cmdend";        // NOI18N
    private static final String CMDBEGIN    = "cmdbegin";      // NOI18N
    private static final String WHERE       = "where";         // NOI18N

    public enum Type {
        FILE,
        EVAL,
        NATIVE,
        QUEST;

        @Override
        public String toString() {
            return this != QUEST ? super.toString().toLowerCase() : "?";     // NOI18N
        }
    }

    Stack( Node node ) {
        super( node );
    }

    public String getWhere(){
        return getAttribute( WHERE );
    }

    public Position getBeginOffset(){
        return getPosition( CMDBEGIN );
    }

    public Position getEndOffset(){
        return getPosition( CMDEND );
    }

    public Input getInput(){
        Node node = getChild( INPUT );
        if ( node == null ){
            return null;
        }
        return new Input( node );
    }

    private Position getPosition( String attrName ){
        String value = getAttribute(attrName);
        if ( value == null ){
            return null;
        }
        String[] values = value.split(":");
        assert values.length == 2;
        return new Position( values[0] , values[1] );
    }

    public static class Position {
        private Position( String line , String offset ){
            try {
                this.line = Integer.parseInt( line );
                this.offset = Integer.parseInt( offset );
            } catch( NumberFormatException e ){
                this.line = -1;
                this.offset = -1;
            }
        }

        public int getLine(){
            return line;
        }

        public int getOffset(){
            return offset;
        }

        private int line;

        private int offset;
    }

    public static class StackDepthCommand extends Command {
        public StackDepthCommand(int transactionId) {
            super(CommandMap.STACK_DEPTH.getCommand(), transactionId);
        }
    }

    public static class StackDepthResponse extends ResponseMessage {
        private static final String DEPTH = "depth";        // NOI18N

        StackDepthResponse(Node node) {
            super(node);
        }

        public int getDepth() {
            return getInt(getNode(), DEPTH);
        }
    }

    public static class StackGetCommand extends Command {
        public StackGetCommand(int transactionId, int depth) {
            super(CommandMap.STACK_GET.getCommand(), transactionId);
            this.depth = depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        @Override
        protected String getArguments() {
            if (depth > -1) {
                return StackDepthCommand.DEPTH_ARG + depth;
            }
            return super.getArguments();
        }

        private int depth;
    }

    public static class StackGetResponse extends ResponseMessage {
        private static final String STACK = "stack";        // NOI18N

        StackGetResponse(Node node) {
            super(node);
        }

        public List<Stack> getStackElements() {
            List<Stack> result = new LinkedList<Stack>();
            List<Node> nodes = getChildren(getNode(), STACK);
            for (Node node : nodes) {
                result.add(new Stack(node));
            }
            return result;
        }
    }
}
