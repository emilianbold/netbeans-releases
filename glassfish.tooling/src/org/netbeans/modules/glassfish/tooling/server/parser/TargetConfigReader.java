/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.server.parser;

import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser.NodeListener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Marks that the xml parser is currently inside config element with
 * give name.
 * This information is used by descendants of this class.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
class TargetConfigReader extends NodeListener {

    public static final String CONFIG_PATH =
            "/domain/configs/config";

    public static final String DEFAULT_TARGET = "server";

    protected static boolean readData = false;
    
    private String targetConfigName = null;

    public TargetConfigReader(String targetConfigName) {
        this.targetConfigName = targetConfigName;
        // TODO all parsing has to be rewritten at some point
        this.readData = false;
    }

    class TargetConfigMarker extends NodeListener {
 
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws
                SAXException {
            if ((targetConfigName != null) && attributes.getValue("name").equalsIgnoreCase(targetConfigName)) {
                readData = true;
            }
        }

        @Override
        public void endNode(String qname) throws SAXException {
            if ("config".equals(qname)) {
                readData = false;
            }
        }

    }

}
