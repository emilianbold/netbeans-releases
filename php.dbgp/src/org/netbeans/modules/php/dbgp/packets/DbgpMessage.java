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
package org.netbeans.modules.php.dbgp.packets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.openide.util.NbBundle;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author ads
 *
 */
public abstract class DbgpMessage {
    

    private static final String ERR_PACKET_ERROR 
                                                = "ERR_PacketError";    // NOI18N

    private static final String     INIT        = "init";               // NOI18N
    
    private static final String     RESPONSE    = "response";           // NOI18N
    
    private static final String     STREAM      = "stream";             // NOI18N
    
    static final String             ISO_CHARSET = "ISO-8859-1";         // NOI18N
    
    private static final int        MAX_PACKET_SIZE = 1024;
    
    
    protected static final String     HTML_APOS       = "&apos;";       // NOI18N
    
    protected static final String     HTML_QUOTE      = "&quot;";       // NOI18N
    
    protected static final String     HTML_AMP        = "&amp";         // NOI18N
    
    protected static final String     HTML_LT         = "&lt";          // NOI18N
    
    protected static final String     HTML_GT         = "&gt";          // NOI18N
    
    protected static final java.util.Map<String,Character> 
                                    ENTITIES          = 
            new HashMap<String,Character>( );
    
    static {
        ENTITIES.put( HTML_APOS , '\'');
        ENTITIES.put( HTML_QUOTE , '"');
        ENTITIES.put( HTML_AMP , '&' );
        ENTITIES.put( HTML_LT , '<');
        ENTITIES.put( HTML_GT  , '>');
    }
    
    DbgpMessage( Node node ){
        myNode = node;
    }

    public static DbgpMessage create( InputStream inputStream ) throws SocketException{
        
        try {
            int size = getDataSize(inputStream);
            if ( size <0 ) {
                notifyPacketError( null );
                Logger.getLogger( DbgpMessage.class.getName() ).log( 
                        Level.FINE, "Got " +size+" as data size" ); // NOI18N
                return null;
            }
            byte[] bytes = getContent( inputStream , size );
            Node node = getNode( bytes );
            logDebugInfo(bytes);
            return create( node );
        } catch (SocketException e) {
            throw e;
        } catch (IOException e) {
            log(e);
        }
        return null;
    }
    
    public abstract void process( DebugSession session, DbgpCommand command );
    
    public static int getMaxDataSize() {
        return myMaxDataSize.get();
    }
    
    public static void setMaxDataSize( int size ) {
        int maxSize = myMaxDataSize.get();
        if ( maxSize <size ) {
            myMaxDataSize.compareAndSet( maxSize, size);
        }
    }
    
    public static DbgpMessage create( Node node ) {
        if ( node == null ) {
            return null;
        }
        String rootName = node.getNodeName();
        if ( INIT.equals( rootName) ) {
            return new InitMessage( node );
        }
        else if ( STREAM.equals( rootName )) {
            return MessageBuilder.createStream( node );
        }
        else if ( RESPONSE.equals(rootName) ) {
            return MessageBuilder.createResponse( node );
        }
        return null;
    }

    protected static void log( IOException e ) {
        Logger.getLogger( DbgpMessage.class.getName() ).log( 
                Level.SEVERE, null, e );
    }
    
    protected static String getNodeValue( Node node ){
        NodeList list = node.getChildNodes();
        StringBuilder builder = new StringBuilder();
        for ( int i=0;  i<list.getLength() ;  i++) {
            Node child = list.item( i );
            if ( child instanceof Text ){
                builder.append( child.getNodeValue() );
            }
            else if ( child instanceof CDATASection ){
                builder.append( child.getNodeValue() );
            }
        }
        return replaceHtmlEntities( builder.toString() );
    }
    
    protected static String getAttribute( Node node , String attrName ){
        Node attr = node.getAttributes().getNamedItem( attrName );
        return attr == null ? null : replaceHtmlEntities( attr.getNodeValue() );
    }
    
    protected static boolean getBoolean( Node node , String attrName ){
       String value = getAttribute(node, attrName);
       if ( value == null ){
           return false;
       }
       try {
           return Integer.parseInt( value ) >0;
       }
       catch( NumberFormatException e ){
           return false;
       }
    }
    
    protected static Node getChild( Node node , String nodeName ) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if ( nodeName.equals( child.getNodeName() )) {
                return child;
            }
        }
        return null;
    }
    
    protected static List<Node> getChildren( Node node , String nodeName ) {
        List<Node> result = new LinkedList<Node>();
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if ( nodeName.equals( child.getNodeName() )) {
                result.add( child );
            }
        }
        return result;
    }
    
    protected Node getNode(){
        return myNode;
    }
    
    private static void log( ParserConfigurationException e ) {
        Logger.getLogger( DbgpMessage.class.getName() ).log( 
                Level.SEVERE, null, e );
    }
    
    private static void logDebugInfo( byte[] bytes ) {
        try {
            Logger.getLogger( DbgpMessage.class.getName() ).log( 
                    Level.FINE, new String( bytes , ISO_CHARSET));
        }
        catch (UnsupportedEncodingException e) {
            assert false;
        }
    }

    /*
     * Notify user about unexpected format of received packet.
     */
    private static void notifyPacketError( Exception e ) {
        Exception exception = e;
        if ( exception == null ) {
            exception = new Exception( NbBundle.getMessage( DbgpMessage.class, 
                    ERR_PACKET_ERROR));
        }
        Logger.getLogger( DbgpMessage.class.getName() ).log( 
                Level.SEVERE, null, e );
    }
    
    private static byte[] getContent( InputStream inputStream, int size )
        throws IOException 
    {
        byte[] bytes = new byte[ size ];
        int count = 0; 
        while( count < size ) {
            int awaitedBytes = size - count ;
            int length = awaitedBytes < getMaxDataSize() ? 
                    awaitedBytes : getMaxDataSize(); 
            count+= inputStream.read( bytes , count , length);
        }
        if ( count != size ) {
            notifyPacketError( null );
            Logger.getLogger( DbgpMessage.class.getName() ).log( 
                    Level.FINE, "Red " +count+" bytes from socket input stream," +
                    		" but expected " +size +" bytes" );       // NOI18N
            return null;
        }
        int nullByte = inputStream.read();
        assert nullByte == 0;
        return bytes;
    }

    private static int getDataSize( InputStream inputStream ) throws IOException {
        List<Integer> list = new LinkedList<Integer>();
        int next;
        while ((next = inputStream.read()) > 0) {
            list.add( next );
        }
        byte[] bytes = new byte[ list.size() ];
        int i = 0;
        for (Integer integer : list) {
            byte byt = integer.byteValue();
            bytes[i++] = byt;
        }
        String str = new String( bytes , ISO_CHARSET );
        try {
            return Integer.parseInt(str);
        }
        catch( NumberFormatException e ) {
            return -1;
        }
    }
    
    private static Node getNode( byte[] bytes ) throws IOException {
        if ( BUILDER == null || bytes == null ) {
            return null;
        }
        try {
            InputSource is = new InputSource(new ByteArrayInputStream( bytes ));
            is.setEncoding("UTF-8");
            Document doc = BUILDER.parse( is );
            return doc.getDocumentElement();
        }
        catch (SAXException e) {
            notifyPacketError(e);
        }
        return null;
    }
    
    private static String replaceHtmlEntities( String str ) {
        if ( str.indexOf( "&" ) == -1 ) {
            return str;
        }
        else {
            for( Entry<String, Character> entry : ENTITIES.entrySet() ) {
                String entity = entry.getKey();
                Character ch = entry.getValue();
                str = str.replace( entity , ch +"" );
            }
            return str;
        }
    }
    
    private Node myNode;
    
    private static DocumentBuilder BUILDER;
    
    private static AtomicInteger myMaxDataSize = new AtomicInteger( MAX_PACKET_SIZE);
    
    static {
        try {
            BUILDER = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            BUILDER.setEntityResolver( new StubResolver() );
        }
        catch( ParserConfigurationException  e) {
            log( e );
        }
    }

    private static class StubResolver implements EntityResolver {

        /* (non-Javadoc)
         * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
         */
        public InputSource resolveEntity( String publicId, String systemId ) 
            throws SAXException, IOException 
        {
            return null;
        }
        
    }
}
