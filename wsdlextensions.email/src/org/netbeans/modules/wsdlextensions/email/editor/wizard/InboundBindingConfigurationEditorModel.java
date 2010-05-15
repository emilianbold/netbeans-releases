package org.netbeans.modules.wsdlextensions.email.editor.wizard;

	/**
	*
	* 
	*/

	public class InboundBindingConfigurationEditorModel implements InboundBindingConfigurationEditorForm.Model {

	    private String emailServer = "";
	    private String port = "";
	    private String userName = "";
	    private String password = "";
	    private boolean useSSL;
		private String mailFolder = "";
		private String maxMessageCount = "";
		private String messageAckMode = "";
		private String messageAckOperation = "";
		private String pollingInterval = "";
	    private String saveAttachmentsToDir = "";
	   // private boolean handleNMAttachments;
	            	
		public String getEmailServer() {
			return emailServer;
		}

		public void setEmailServer(String emailServer){
			this.emailServer = Utils.safeString(emailServer);
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port){
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

		public void setPassword(String password){
			this.password = Utils.safeString(password);
		}

		public boolean getUseSSL() {
			return useSSL;
		}

		public void setUseSSL(boolean useSSL){
			this.useSSL = useSSL;
		}

		public String getMailFolder() {
			return mailFolder;
		}

		public void setMailFolder(String mailFolder){
			this.mailFolder = mailFolder;
		}

		public String getMaxMessageCount() {
			return maxMessageCount;
		}

		public void setMaxMessageCount(String maxMsgCount){
			this.maxMessageCount = maxMsgCount;
		}

		public String getMessageAckMode() {
			return messageAckMode;
		}

		public void setMessageAckMode(String msgAckMode){
			this.messageAckMode = Utils.safeString(msgAckMode);
		}

		public String getMessageAckOperation() {
			return messageAckOperation;
		}

		public void setMessageAckOperation(String msgAckOperation) {
			this.messageAckOperation = Utils.safeString(msgAckOperation);
		}

		public String getPollingInterval() {
			return pollingInterval;
		}

		public void setPollingInterval(String pollingInterval) {
			this.pollingInterval = Utils.safeString(pollingInterval);
		}

		public String getSaveAttachmentsToDir() {
			return saveAttachmentsToDir;
		}

		public void setSaveAttachmentsToDir(String saveAttDir){
			this.saveAttachmentsToDir = Utils.safeString(saveAttDir);
		}

	/*	public boolean getHandleNMAttachments() {
			return handleNMAttachments;
		}

		public void setHandleNMAttachments(boolean handleNMAtt){
			this.handleNMAttachments = handleNMAtt;
		} */

	}
