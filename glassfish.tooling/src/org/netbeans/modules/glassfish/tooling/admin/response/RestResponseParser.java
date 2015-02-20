/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.admin.response;

import java.io.InputStream;

/**
 * Base implementation for REST parsers.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
abstract public class RestResponseParser implements ResponseParser {

    @Override
    public abstract RestActionReport parse(InputStream in);

}
