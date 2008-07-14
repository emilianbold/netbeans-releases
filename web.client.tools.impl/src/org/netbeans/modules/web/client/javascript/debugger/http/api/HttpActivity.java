package org.netbeans.modules.web.client.javascript.debugger.http.api;

import java.util.Calendar;
import java.util.Date;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpProgress;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpRequest;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSHttpResponse;


public class HttpActivity {
    
    private JSHttpRequest request;
    private JSHttpResponse response;

    private Date startTime;
    private Date endTime;
    
    private HttpActivity() { }


    
    public HttpActivity(JSHttpRequest request) {
        this(request, null);
    }

    public HttpActivity(JSHttpRequest request, JSHttpResponse response) {
        
        this.request = request;
        if ( request != null ){
            startTime = convertLongStringToTime(request.getTimeStamp());
        }
        if ( response != null ){
            setResponse(response);
        }
    }

    public static final Date convertLongStringToTime(String longString ){
        Calendar cal = Calendar.getInstance();
        long l = Long.parseLong(longString);
        cal.setTimeInMillis(l);
        return cal.getTime();
    }


    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        if ( endTime != null ){
            return endTime;
        } else {
            return startTime; //With images you may not get a notification
        }
    }

    public String getMethod() {
        return request.getMethod().toString().toUpperCase();
    }

    public JSHttpResponse getResponse() {
        return response;
    }
    
    public void setResponse(JSHttpResponse response) {
        assert response != null;
        this.response = response;
        endTime = convertLongStringToTime(response.getTimeStamp());
    }
    
    public JSHttpRequest getRequest() {
        return request;
    }

    private JSHttpProgress lastProgress;
    public void updateProgress(JSHttpProgress jSHttpProgress) {
        assert this.response == null;
        /* I am not for sure this is the right thing to do */
        if( lastProgress != null && 
                lastProgress.getCurrent() == lastProgress.getMax() &&
                jSHttpProgress.getCurrent() == jSHttpProgress.getMax() ){
            endTime = convertLongStringToTime(jSHttpProgress.getTimeStamp());
        }
        lastProgress = jSHttpProgress;
    }

    public JSHttpProgress getProgress() {
        return lastProgress;
    }

    @Override
    public String toString() {
        if ( request != null ) {
            return request.getUrl().toLowerCase().toString();
        } else if ( response != null ) {
            return response.getUrl().toLowerCase().toString(); // I need to add URl to response as well.
        } else {
            return lastProgress.getId();
        }
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
