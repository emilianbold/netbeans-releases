package org.netbeans.modules.web.client.javascript.debugger.http.api;

import java.net.URL;
import java.util.Date;
import java.util.Map;

public class HttpRequest {
    
    public enum METHOD {
        GET,
        POST
    }
    
    private final URL url;
    private final METHOD method;
    private final Date sentDate;
    private final Map<String, String> header;
    private final String params;
    
    public final URL getUrl() {
        return url;
    }

    public final METHOD getMethod() {
        return method;
    }

    public final Date getSentDate() {
        return sentDate;
    }

    public final Map<String, String> getHeader() {
        return header;
    }

    public final String getParams() {
        return params;
    }

    public HttpRequest(URL url, METHOD method, Date sentDate,
            Map<String, String> header, String params) {
        if (url == null ){
            throw new NullPointerException("HttpRequest must have a valid url.");
        }
        if (method == null ){
            throw new NullPointerException("HttpRequest must have a method type.");
        }
        if (sentDate == null ){
            throw new NullPointerException("HttpRequest must have a setn time.");
        }
        this.url = url;
        this.method = method;
        this.sentDate = sentDate;
        this.header = header;
        this.params = params;
    }
}
