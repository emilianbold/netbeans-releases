/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.admin.response;

/**
 * Enum that represents possible content types that runners accept responses
 * in.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public enum ResponseContentType {

    APPLICATION_XML("application/xml"),
    APPLICATION_JSON("application/json"),
    TEXT_PLAIN("text/plain");

    private String type;

    ResponseContentType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
