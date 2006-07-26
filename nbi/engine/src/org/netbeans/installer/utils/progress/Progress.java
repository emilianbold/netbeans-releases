/*
 * Progress.java
 *
 * $Id$
 */
package org.netbeans.installer.utils.progress;

import java.util.Vector;

/**
 *
 * @author ks152834
 */
public class Progress {
    private String title;
    private String detail;
    private int percentage;
    
    private Vector<ProgressListener> progressListeners;
    
    public Progress() {
        progressListeners = new Vector<ProgressListener>();
    }
    
    public Progress(ProgressListener initialListener) {
        this();
        
        addProgressListener(initialListener);
    }
    
    public void addProgressListener(ProgressListener listener) {
        synchronized (progressListeners) {
            progressListeners.add(listener);
        }
    }
    
    public void removeProgressListener(ProgressListener listener) {
        synchronized (progressListeners) {
            progressListeners.remove(listener);
        }
    }
    
    private void notifyListeners() {
        synchronized (progressListeners) {
            for (ProgressListener listener: progressListeners) {
                listener.progressUpdated(this);
            }
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String aTitle) {
        title = aTitle;
        
        notifyListeners();
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(String aDetail) {
        detail = aDetail;
        
        notifyListeners();
    }
    
    public int getPercentage() {
        return percentage;
    }
    
    public void setPercentage(int aPercentage) {
        if ((aPercentage < START) || (aPercentage > COMPLETE)) {
            throw new IllegalArgumentException("The percentage should be between 0 and 100 inclusive");
        }
        
        percentage = aPercentage;
        
        notifyListeners();
    }
    
    public void setPercentage(double aPercentage) {
        if ((aPercentage < 0.0) || (aPercentage > 1.0)) {
            throw new IllegalArgumentException("The percentage should be between 0.0 and 1.0 inclusive");
        }
        
        percentage = (int) (100.0 * aPercentage);
        
        notifyListeners();
    }
    
    public boolean isComplete() {
        return percentage == COMPLETE;
    }
    
    public static final int START = 0;
    public static final int COMPLETE = 100;
}
