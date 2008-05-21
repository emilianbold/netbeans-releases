package org.netbeans.modules.web.client.javascript.debugger.http.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.web.client.javascript.debugger.http.api.HttpRequest.METHOD;
import org.openide.util.Exceptions;


public class HttpActivity {
    
    private HttpRequest request;
    private HttpResponse response;
    
    
    
    public HttpActivity(HttpRequest request) {
        this(request, null);
    }

    public HttpActivity(HttpRequest request, HttpResponse response) {
        if( request == null ){
            throw new NullPointerException("Request can not be null");
        }
        
        this.request = request;
        this.response = response;
    }
    
    public HttpResponse getResponse() {
        return response;
    }
    
    public void setResponse(HttpResponse response) {
        this.response = response;
    }
    
    public HttpRequest getRequest() {
        return request;
    }
    
    public static HttpActivity createDummyActivity() {
        URL url = null;
        try {
            url = new URL("http://www.google.com");
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("prop1", "value1");
        map.put("prop2", "value2");
        HttpRequest request = new HttpRequest( url, METHOD.GET, new Date(), map, "value1=a&value2=b");
        return new HttpActivity( request );
    }
    
    public static HttpActivity createDummyActivity1() {
        URL url = null;
        try {
            url = new URL("http://www.google.com");
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("Anthorprop1", "value1");
        HttpRequest request = new HttpRequest( url, METHOD.POST, new Date(), map, "value1=a&value2=b");

        Map<String, String> headerMap = new HashMap<String, String>();
        map.put("header1", "value1");
        HttpResponse response = new HttpResponse(headerMap, "{ \"something\",\"somethingelse\"}");
        return new HttpActivity( request, response );
    }
}
