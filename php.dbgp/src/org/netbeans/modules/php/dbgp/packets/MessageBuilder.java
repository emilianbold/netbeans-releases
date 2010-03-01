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
package org.netbeans.modules.php.dbgp.packets;

import org.netbeans.modules.php.dbgp.packets.DbgpStream.StreamType;
import org.w3c.dom.Node;


/**
 * @author ads
 *
 */
class MessageBuilder {
    
    private static final String TYPE    = "type";       // NOI18N
    
    private MessageBuilder() {
        // avoid inst-ion 
    }

    static DbgpMessage createStream( Node node ) {
        Node attr = node.getAttributes().getNamedItem( TYPE );
        assert attr!=null;
        String type = attr.getNodeValue();
        if ( StreamType.STDOUT.equals(type) ) {
            return new DbgpStream( node , StreamType.STDOUT );
        }
        else if ( StreamType.STDERR.equals(type) ) {
            return new DbgpStream( node , StreamType.STDERR );
        }
        else {
            assert false;
            return null;
        }
    }
    
    static DbgpMessage createResponse( Node node ) {
        String command = DbgpMessage.getAttribute(node, DbgpResponse.COMMAND );
        assert command != null;
        if ( command.equals( RunCommand.RUN ) || 
                command.equals( StatusCommand.STATUS ) || 
                command.equals( StepOutCommand.STEP_OUT ) ||
                command.equals( StepOverCommand.STEP_OVER )||
                command.equals( StepIntoCommand.STEP_INTO ) ||
                command.equals( StopCommand.COMMAND ) )
        {
            return new StatusResponse( node );
        }
        else if( command.equals( BrkpntSetCommand.BREAKPOINT_SET )) {
            return new BrkpntSetResponse( node );
        }
        else if( command.equals( BrkpntUpdateCommand.UPDATE )) {
            return new BrkpntUpdateResponse( node );
        }
        else if( command.equals( BrkpntRemoveCommand.REMOVE )) {
            return new BrkpntRemoveResponse( node );
        }
        else if ( command.equals( ContextNamesCommand.CONTEXT_NAMES) ){
            return new ContextNamesResponse( node );
        }
        else if ( command.equals( ContextGetCommand.CONTEXT_GET ) ){
            return new ContextGetResponse( node );
        }
        else if ( command.equals( StackDepthCommand.STACK_DEPTH ) ){
            return new StackDepthResponse( node );
        }
        else if ( command.equals( StackGetCommand.STACK_GET )){
            return new StackGetResponse( node );
        }
        else if ( command.equals( TypeMapGetCommand.TYPEMAP_GET )){
            return new TypeMapGetResponse( node );
        }
        else if ( command.equals( PropertySetCommand.PROPERTY_SET )){
            return new PropertySetResponse( node );
        }
        else if ( command.equals( PropertyGetCommand.PROPERTY_GET )){
            return new PropertyGetResponse( node );
        }
        else if ( command.equals( PropertyValueCommand.PROPERTY_VALUE )){
            return new PropertyValueResponse( node );
        }
        else if ( command.equals( SourceCommand.SOURCE )){
            return new SourceResponse( node );
        }
        else if ( command.equals( StreamType.STDERR.toString() )
                || command.equals( StreamType.STDOUT.toString() ))
        {
            return new StreamResponse( node );
        }
        else if( command.equals( FeatureGetCommand.FEATURE_GET )){
            return new FeatureGetResponse( node );
        }
        else if( command.equals( FeatureSetCommand.FEATURE_SET )){
            return new FeatureSetResponse( node );
        }
        else if( command.equals( BreakCommand.BREAK )){
            return new BreakResponse( node );
        }
        else if( command.equals( EvalCommand.EVAL )){
            return new EvalResponse( node );
        }
        else if( command.equals( ExprCommand.EXPR )){
            return new ExprResponse( node );
        }
        else if( command.equals( ExecCommand.EXEC )){
            return new ExecResponse( node );
        }
        return null;
    }
    
}
