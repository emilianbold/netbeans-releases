package org.netbeans.modules.wsdlextensions.email.editor.wizard;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;

/**
 *
 *
 */
public class OutboundBindingConfigurationEditorModel implements OutboundBindingConfigurationEditorForm.Model {

    private String location = "";
    private String emailServer = "";
    private String port = "";
    private String userName = "";
    private String password = "";
    private boolean useSSL;
    private String charset = "";
    private String encodingStyle = "";
    private String use = "";
    private String messageType = "";
    private String sendOption = "";
    private boolean embedImagesInHtml;
    private boolean handleNMAttachments;
    private String xsdElementOrType = "";
    private GlobalElement elementType;
    private GlobalType partType;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = Utils.safeString(location);
    }

    public String getEmailServer() {
        return emailServer;
    }

    public void setEmailServer(String emailServer) {
        this.emailServer = Utils.safeString(emailServer);
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = Utils.safeString(port);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = Utils.safeString(userName);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = Utils.safeString(password);
    }

    public boolean getUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String msgType) {
        this.messageType = Utils.safeString(msgType);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = Utils.safeString(charset);
    }

    public String getEncodingStyle() {
        return encodingStyle;
    }

    public void setEncodingStyle(String encodingStyle) {
        this.encodingStyle = Utils.safeString(encodingStyle);
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = Utils.safeString(use);
    }

    public String getSendOption() {
        return sendOption;
    }

    public void setSendOption(String sendOption) {
        this.sendOption = Utils.safeString(sendOption);
    }

    public boolean getEmbedImagesInHtml() {
        return embedImagesInHtml;
    }

    public void setEmbedImagesInHtml(boolean embedImgHTML) {
        this.embedImagesInHtml = embedImgHTML;
    }

    public boolean getHandleNMAttachments() {
        return handleNMAttachments;
    }

    public void setHandleNMAttachments(boolean handleNMAtt) {
        this.handleNMAttachments = handleNMAtt;
    }

    public String getXsdElementOrType() {
        return xsdElementOrType;
    }

    public void setXsdElementOrType(String xsdEOT) {
        this.xsdElementOrType = Utils.safeString(xsdEOT);
    }

    public GlobalElement getElementType() {
        return elementType;
    }

    public void setElementType(GlobalElement gElement) {
        this.elementType = gElement;
    }
    
    public GlobalType getPartType() {
        return partType;
    }

    public void setPartType(GlobalType gType) {
        this.partType = gType;
    }
}
