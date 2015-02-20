/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class MessagePart {

    Properties props = new Properties();
    String message;

    List<MessagePart> children = new ArrayList<MessagePart>();

    public List<MessagePart> getChildren() {
        return children;
    }

    public String getMessage() {
        return message;
    }

    public Properties getProps() {
        return props;
    }
}
