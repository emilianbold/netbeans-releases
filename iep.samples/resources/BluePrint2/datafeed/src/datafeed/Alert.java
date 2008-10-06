/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datafeed;

public class Alert {

    String deviceId;
    int sequenceNum;
    String message;

    public static String makeKey(String deviceId, String sequenceNum) {
        return deviceId + "." + sequenceNum;
    }

    public Alert(String deviceId, int sequenceNum, String message) {
        this.deviceId = deviceId;
        this.sequenceNum = sequenceNum;
        this.message = message;
    }

    public String toXml() {
        return "<rec:DeviceAlerts_MsgObj xmlns:rec=\"sequencer_iep\">" +
                "<rec:deviceID>" + deviceId + "</rec:deviceID>" +
                "<rec:sequenceNum>" + sequenceNum + "</rec:sequenceNum>" +
                "<rec:message>" + message + "</rec:message>" +
                "</rec:DeviceAlerts_MsgObj>";

    }
    
    public String getKey() {
        return makeKey(deviceId, "" + sequenceNum);
    }
    
    public String toString() {
        return deviceId + "." + sequenceNum + ".(" + message + ")";
    }
}
