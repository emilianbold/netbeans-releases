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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.web.client.tools.common.dbgp.Message.Encoding;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

import sun.misc.BASE64Encoder;

/**
 * @author ads, jdeva
 *
 */
public class Property extends BaseMessageChildElement {

    static final String         PROPERTY    = "property";     // NOI18N
    private static final String NUMCHILDREN = "numchildren";// NOI18N
    static final String         ENCODING    = "encoding";   // NOI18N
    private static final String KEY         = "key";        // NOI18N
    private static final String ADDRESS     = "address";    // NOI18N
    private static final String PAGESIZE    = "pagesize";   // NOI18N
    private static final String PAGE        = "page";       // NOI18N
    private static final String NAME        = "name";       // NOI18N
    private static final String FULL_NAME   = "fullname";   // NOI18N
    private static final String TYPE        = "type";       // NOI18N
    private static final String CLASS_NAME  = "classname";  // NOI18N
    private static final String CONSTANT    = "constant";   // NOI18N
    private static final String CHILDREN    = "children";   // NOI18N
    private static final String FACET       = "facet";      // NOI18N
    static final         String SIZE        = "size";       // NOI18N

    Property( Node node ){
        super( node );
    }

    public String getName(){
        return getAttribute( NAME );
    }

    public void setName( String value ) {
        Node node = getNode().getAttributes().getNamedItem( NAME );
        if ( node == null ) {
            node = getNode().getOwnerDocument().createAttribute( NAME );
            getNode().appendChild(node);
        }
        node.setNodeValue(value );
    }

    public String getFullName(){
        return getAttribute( FULL_NAME );
    }

    public String getType(){
        return getAttribute( TYPE );
    }

    public String getClassName(){
        return getAttribute(  CLASS_NAME );
    }

    public boolean isConstant(){
        return getInt( CONSTANT ) >0;
    }

    public boolean hasChildren(){
        return getInt( CHILDREN ) >0;
    }

    public int getSize(){
        return getInt( SIZE );
    }

    public int getPage(){
        return getInt( PAGE );
    }

    public int getPageSize(){
        return getInt( PAGESIZE );
    }

    public int getAddress(){
        return getInt( ADDRESS );
    }

    public String getKey(){
        return getAttribute( KEY );
    }

    public String getFacet() {
        return getAttribute( FACET );
    }

    public Encoding getEncoding(){
        String enc = getAttribute( ENCODING );
        return Encoding.valueOf( enc.toUpperCase() );
    }

    public int getChildrenSize(){
        return getInt( NUMCHILDREN );
    }

    public List<Property> getChildren(){
        List<Node> nodes = getChildren( PROPERTY );
        List<Property> result = new ArrayList<Property>( nodes.size() );
        for (Node node : nodes) {
            result.add( new Property( node ) );
        }
        return result;
    }

    public byte[] getValue() throws UnsufficientValueException {
        byte[] result = Message.getDecodedBytes(getEncoding(), Message.getNodeValue(getNode()));
        Message.checkValue(result, getSize());
        return result;
    }
    
    public String getStringValue() throws UnsufficientValueException {
        try {
            return new String(getValue(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return new String(getValue());
        }
    }

    public static boolean equals( Property one , Property two ) {
        if ( one == null ) {
            return two == null;
        }
        else {
            byte[] value;
            try {
                value = one.getValue();
            }
            catch (UnsufficientValueException e) {
                return false;
            }
            if ( two == null ) {
                return false;
            }
            byte[] secondValue;
            try {
                secondValue = two.getValue();
            }
            catch (UnsufficientValueException e) {
                return false;
            }
            return Arrays.equals(value, secondValue);
        }
    }

    public static abstract class PropertyCommand extends Command {
        private static final String NAME_ARG = "-n ";        // NOI18N
        private static final String MAX_SIZE_ARG = "-m ";        // NOI18N
        private static final String CONTEXT_ARG = "-c ";        // NOI18N
        private static final String DEPTH_ARG = "-d ";        // NOI18N
        private static final String PAGE_ARG = "-p ";        // NOI18N

       PropertyCommand(String command, int transactionId, String name, int stackDepth) {
            super(command, transactionId);
            this.stackDepth = stackDepth;
            this.name = name;
        }

        public void setStackDepth(int depth) {
            this.stackDepth = depth;
        }

        public void setContext(int id) {
            context = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setMaxDataSize(int size) {
            this.size = size;
        }

        public void setDataPage(int page) {
            this.page = page;
        }

        protected String getName() {
            return name;
        }

        protected int getContext() {
            return context;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder(NAME_ARG);
            builder.append(name);

            setDepth(builder);
            setContext(builder);
            setMaxSize(builder);
            setPage(builder);

            return builder.toString();
        }

        private void setPage(StringBuilder builder) {
            if (page == -1) {
                return;
            }
            builder.append(Command.SPACE);
            builder.append(PAGE_ARG);
            builder.append(page);
        }

        private void setMaxSize(StringBuilder builder) {
            if (size == -1) {
                return;
            }
            builder.append(Command.SPACE);
            builder.append(MAX_SIZE_ARG);
            builder.append(size);
        }

        private void setContext(StringBuilder builder) {
            if (context == -1) {
                return;
            }
            builder.append(Command.SPACE);
            builder.append(CONTEXT_ARG);
            builder.append(context);
        }

        private void setDepth(StringBuilder builder) {
            if (stackDepth == -1) {
                return;
            }
            builder.append(Command.SPACE);
            builder.append(DEPTH_ARG);
            builder.append(stackDepth);
        }
        private int page = -1;
        private int context = -1;
        private int stackDepth;
        private String name;
        private int size = -1;
    }

    public static class PropertyGetCommand extends PropertyCommand {
        private static final String KEY_ARG = "-k ";                 // NOI18N

        public PropertyGetCommand(int transactionId, String name, int stackDepth) {
            this(CommandMap.PROPERTY_GET.getCommand(), transactionId, name, stackDepth);
        }

        protected PropertyGetCommand(String command, int transactionId, String name, int stackDepth) {
            super(command, transactionId, name, stackDepth);
        }

        @Override
        public boolean wantAcknowledgment() {
            return true;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder(super.getArguments());
            if (key != null) {
                builder.append(Command.SPACE);
                builder.append(KEY_ARG);
                builder.append(key);
            }
            return builder.toString();
        }
        private String key;
    }

    public static class PropertyGetResponse extends ResponseMessage {

        PropertyGetResponse(Node node) {
            super(node);
        }

        public Property getProperty() {
            Node node = getChild(getNode(), Property.PROPERTY);
            if (node != null) {
                return new Property(node);
            }
            return null;
        }
    }

    public static class PropertySetCommand extends PropertyCommand {
        private static final String TYPE_ARG = "-t ";               // NOI18N
        static final String ADDRESS_ARG = "-a ";               // NOI18N
        private static final String LENGTH_ARG = "-l ";               // NOI18N
        private static final String VALUE_ARG = "-- ";               // NOI18N        

        public PropertySetCommand(int transactionId, String name, int stackDepth) {
            super(CommandMap.PROPERTY_SET.getCommand(), transactionId, name , stackDepth);
        }
        
        public PropertySetCommand(int transactionId, String name, String value, int stackDepth) {
            super(CommandMap.PROPERTY_SET.getCommand(), transactionId, name, stackDepth);
            this.value = value;
        }        

        @Override
        public boolean wantAcknowledgment() {
            return true;
        }

        public void setDataType(String type) {
            dataType = type;
        }

        public void setAddress(int address) {
            propAddress = address;
        }

        public void setData(String data) {
            this.data = data;
        }
        
        public void setValue(String value) {
            this.value = value;
        }        

        @Override
        public String getName() {
            return super.getName();
        }

        @Override
        protected String getData() {
            return data;
        }
        
        protected String getValue() {
            return value;
        }        

        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder(super.getArguments());
            if (dataType != null) {
                builder.append(Command.SPACE);
                builder.append(TYPE_ARG);
                builder.append(dataType);
            }

            if (propAddress != -1) {
                builder.append(Command.SPACE);
                builder.append(ADDRESS_ARG);
                builder.append(propAddress);
            }

            if (getData() != null && getData().length() > 0) {
                try {
                    BASE64Encoder encoder = new BASE64Encoder();
                    int size = encoder.encode(getData().getBytes(
                            Message.ISO_CHARSET)).length();
                    //int size = data.getBytes( DbgpMessage.ISO_CHARSET ).length;
                    builder.append(Command.SPACE);
                    builder.append(LENGTH_ARG);
                    builder.append(size);
                } catch (UnsupportedEncodingException e) {
                    assert false;
                }
            }else {
                builder.append(Command.SPACE);
                builder.append(VALUE_ARG);
                builder.append(value);                
            }

            return builder.toString();
        }
        private String dataType;
        private int propAddress = -1;
        private String data;
        private String value;
    }

    public static class PropertySetResponse extends ResponseMessage {
        PropertySetResponse(Node node) {
            super(node);
        }

        public boolean isSet() {
            return getBoolean(getNode(), SUCCESS);
        }
    }

    public static class PropertyValueCommand extends PropertyGetCommand {
        public PropertyValueCommand(int transactionId, String name, int stackDepth) {
            super(CommandMap.PROPERTY_VALUE.getCommand(), transactionId, name, stackDepth);
        }

        public void setAddress(int address) {
            propAddress = address;
        }

        @Override
        protected String getArguments() {
            StringBuilder builder = new StringBuilder(super.getArguments());

            if (propAddress != -1) {
                builder.append(Command.SPACE);
                builder.append(PropertySetCommand.ADDRESS_ARG);
                builder.append(propAddress);
            }

            return builder.toString();
        }
        private int propAddress = -1;
    }

    public static class PropertyValueResponse extends ResponseMessage {

        PropertyValueResponse(Node node) {
            super(node);
        }

        public Property getProperty() {
            Node node = getChild(getNode(), Property.PROPERTY);
            if (node != null) {
                return new Property(node);
            }
            return null;
        }
    }
}
