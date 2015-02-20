/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.server.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser.NodeListener;
import org.netbeans.modules.glassfish.tooling.server.parser.TreeParser.Path;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Reads resources from domain.xml.
 * User has to specify an {@link ResourceType} which specifies
 * path and name of attribute value of which will be the key
 * in returned map.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ResourcesReader extends NodeListener implements
        XMLReader {

    /**
     * Paths and key names for various resource types.
     */
    public enum ResourceType {

        JDBC_RESOURCE("/resources/jdbc-resource", "jndi-name"),
        JDBC_CONNECTION_POOL("/resources/jdbc-connection-pool", "name"),
        JAVA_MAIL("/resources/mail-resource", "jndi-name"),
        CONNECTOR_RESOURCE("/resources/connector-resource", "jndi-name"),
        CONNECTOR_POOL("/resources/connector-connection-pool", "name"),
        ADMIN_OBJECT_RESOURCE("/resources/admin-object-resource", "jndi-name");

        private String defaultKeyName;

        private String defaultPath;

        private ResourceType(String defaultPath, String defaultKeyName) {
            this.defaultPath = defaultPath;
            this.defaultKeyName = defaultKeyName;
        }

        public String getDefaultPath() {
            return defaultPath;
        }

        public String getDefaultKeyName() {
            return defaultKeyName;
        }
    }

    private String path;

    private String keyName;

    private Map<String, String> properties = null;

    private Map<String, Map<String, String>> resourceData =
            new HashMap<String, Map<String, String>>();

    public ResourcesReader(ResourceType type) {
        this(type.getDefaultPath(), type.getDefaultKeyName());
    }

    public ResourcesReader(String path, String keyName) {
        this.path = path;
        this.keyName = keyName;
    }

    @Override
    public void readAttributes(String qname, Attributes attributes) throws
            SAXException {
        properties = new HashMap<String, String>();

        String resourceName = attributes.getValue(keyName);
        properties.put(keyName, resourceName);

        int attrLen = attributes.getLength();
        for (int i = 0 ; i < attrLen ; i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(i);
            if (name != null && name.length() > 0 && value != null && value.
                    length() > 0) {
                properties.put(name, value);
            }
        }
    }

    @Override
    public void readChildren(String qname, Attributes attributes) throws
            SAXException {
        String propName = qname + "." + attributes.getValue("name"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put(propName, attributes.getValue("value"));  //$NON-NLS-1$
    }

    @Override
    public void endNode(String qname) throws SAXException {
        String poolName = properties.get(keyName);
        resourceData.put(poolName, properties);
    }

    @Override
    public List<TreeParser.Path> getPathsToListen() {
        LinkedList<TreeParser.Path> paths = new LinkedList<TreeParser.Path>();
        paths.add(new Path(path, this));
        return paths;
    }

    public Map<String, Map<String, String>> getResourceData() {
        return resourceData;
    }
}
