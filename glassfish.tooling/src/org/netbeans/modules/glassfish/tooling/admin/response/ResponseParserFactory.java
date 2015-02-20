/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.admin.response;

import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;



/**
 * Factory that returns appropriate response parser implementation
 * based on content type of the response.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ResponseParserFactory {

    private static RestXMLResponseParser xmlParser;
    
    private static RestJSONResponseParser jsonParser;

    public static synchronized RestResponseParser getRestParser(ResponseContentType contentType) {
        switch (contentType) {
            case APPLICATION_XML:
                if (xmlParser == null) {
                    xmlParser = new RestXMLResponseParser();
                }
                return  xmlParser;
            case APPLICATION_JSON:
                if (jsonParser == null) {
                    jsonParser = new RestJSONResponseParser();
                }
                return jsonParser;
            case TEXT_PLAIN:
                return null;
            default: throw new GlassFishIdeException("Not supported content type. Cannot create response parser!");
        }
    }

}
