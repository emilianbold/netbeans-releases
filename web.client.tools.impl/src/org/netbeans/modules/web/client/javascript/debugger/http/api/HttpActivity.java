package org.netbeans.modules.web.client.javascript.debugger.http.api;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
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

    public String getResponseText() {
        if( response != null ){
            return response.getResponseText();
        }
        return "";
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
        mimeType = response.getMimeType();
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
        endTime = convertLongStringToTime(jSHttpProgress.getTimeStamp());
        lastProgress = jSHttpProgress;
        mimeType = jSHttpProgress.getMimeType();
    }

    String mimeType;
    public String getMimeType() {
        return mimeType;
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

    public Map<String,String> getResponseHeader () {
        Map<String,String> map = Collections.EMPTY_MAP;
        if ( response != null ){
            map = response.getHeader();
        } else if ( lastProgress != null ) {
            map =  lastProgress.getHeader();
        }
        return map;
    }

    public Map<String,String> getRequestHeader() {
        return request.getHeader();
    }
}
