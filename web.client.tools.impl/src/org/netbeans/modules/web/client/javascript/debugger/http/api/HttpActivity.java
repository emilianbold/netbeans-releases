package org.netbeans.modules.web.client.javascript.debugger.http.api;

import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpProgress;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpRequest;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpResponse;


public class HttpActivity {
    
    private JSHttpRequest request;
    private JSHttpResponse response;
    
    
    
    public HttpActivity(JSHttpRequest request) {
        this(request, null);
    }

    public HttpActivity(JSHttpRequest request, JSHttpResponse response) {
        if( request == null ){
            throw new NullPointerException("Request can not be null");
        }
        
        this.request = request;
        this.response = response;
    }
    
    public JSHttpResponse getResponse() {
        return response;
    }
    
    public void setResponse(JSHttpResponse response) {
        this.response = response;
    }
    
    public JSHttpRequest getRequest() {
        return request;
    }

    JSHttpProgress lastProgress;
    public void updateProgress(JSHttpProgress jSHttpProgress) {
        lastProgress = jSHttpProgress;
    }

    public JSHttpProgress getProgress() {
        return lastProgress;
    }
    
//    public static HttpActivity createDummyActivity() {
//        URL url = null;
//        try {
//            url = new URL("http://www.google.com");
//        } catch (MalformedURLException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("prop1", "value1");
//        map.put("prop2", "value2");
//        HttpRequest request = new HttpRequest( url, METHOD.GET, new Date(), map, "value1=a&value2=b");
//        JSHttpRequest request = new JSHttpRequest();
//        return new HttpActivity( request );
//    }
//    
//    public static HttpActivity createDummyActivity1() {
//        URL url = null;
//        try {
//            url = new URL("http://www.google.com");
//        } catch (MalformedURLException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        Map<String, String> map = new HashMap<String, String>();
//        map.put("Anthorprop1", "value1");
//        HttpRequest request = new HttpRequest( url, METHOD.POST, new Date(), map, "value1=a&value2=b");
//
//        Map<String, String> headerMap = new HashMap<String, String>();
//        map.put("header1", "value1");
//        HttpResponse response = new HttpResponse(headerMap, "{ \"something\",\"somethingelse\"}");
//        return new HttpActivity( request, response );
//    }
}
