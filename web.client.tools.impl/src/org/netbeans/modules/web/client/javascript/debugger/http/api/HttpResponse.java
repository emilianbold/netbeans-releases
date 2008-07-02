package org.netbeans.modules.web.client.javascript.debugger.http.api;


import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

public class HttpResponse {

    private final Map<String, String>header;
    private final String body;
    private final Map<String, String>cache;
    private final Date responseDate;
    
    
    /**
     * Create a HttpResponse
     * @param header
     * @param body
     */
    public HttpResponse(Map<String, String> header, String body) {
        this(header, body, null);
    }

    /**
     * Create a HttpResponse
     * @param header 
     * @param body
     * @param cache Map of the cash data.
     */
    public HttpResponse(Map<String, String> header, String body,
            Map<String, String> cache) {
        if (header == null ){
            throw new NullPointerException("HttpResponse can not have a null header");
        }
        if (body == null ){
            throw new NullPointerException("HttpResponse can not have a null body");
        }
        this.header = header;
        this.body = body;
        this.cache = cache;
        this.responseDate = new Date(); /* Needs to be completed */
    }
    
    public final Map<String, String> getHeader() {
        return header;
    }
    public final String getBody() {
        return body;
    }
    public final Map<String, String> getCache() {
        return cache;
    }
    public final Date getResponseDate() {
        return responseDate;
    }
    

}
