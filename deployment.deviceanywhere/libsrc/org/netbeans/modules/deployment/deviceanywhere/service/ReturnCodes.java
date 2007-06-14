package org.netbeans.modules.deployment.deviceanywhere.service;

public class ReturnCodes {
    
    // for all services
    public static final int SUCCESS = 0;
    public static final int INTERNAL_ERROR = 1;
    public static final int LOGIN_FAILED = 2;
    
    // for uploadApplication
    public static final int INVALID_APPLICATION_NAME = 3;
    public static final int JAD_FILE_PARSE_ERROR = 4;
    
    // for startDownloadScript
    public static final int DEVICE_NOT_FOUND = 5;
    public static final int APPLICATION_NOT_FOUND = 6;
    
}
