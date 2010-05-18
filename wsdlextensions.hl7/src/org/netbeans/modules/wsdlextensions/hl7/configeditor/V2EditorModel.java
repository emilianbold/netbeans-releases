/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.hl7.configeditor;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class V2EditorModel implements V2EditorForm.Model{

    private String acknowledgementMode = "";
    private String processingId = "";
    private String versionId = "";
    private boolean validateMSH;
    private boolean sftEnabled;
    private boolean seqNoEnabled;
    private String softwareVendorOrganization = "";
    private String softwareCertifiedVersionOrReleaseNo = "";
    private String softwareProductName = "";
    private String softwareBinaryId = "";
    private String softwareProductInformation = "";
    private String sendingApplication = "";
    private String sendingFacility =  "";
    private String encodingCharacters = "";
    private Byte fieldSeparator = null;
    private String softwareInstallDate = "";
    private boolean journallingEnabled;
    private boolean persistenceEnabled;
    

    public String getAcknowledgementMode() {
        return this.acknowledgementMode;
    }

    public void setAcknowledgementMode(String ackMode) {
        this.acknowledgementMode = Utils.safeString(ackMode);
    }


    public String getProcessingId() {
        return this.processingId;
    }

    public void setProcessingId(String processingId) {
        this.processingId = Utils.safeString(processingId);
    }

    public String getVersionId() {
        return this.versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = Utils.safeString(versionId);
                
    }

    public boolean isValidateMSH() {
        return this.validateMSH;
    }

    public void setValidateMSH(boolean validate) {
        this.validateMSH = validate;
    }

    public boolean isSFTEnabled() {
        return this.sftEnabled;
    }

    public void setSFTEnabled(boolean enabled) {
        this.sftEnabled = enabled;
    }

    public String getSoftwareVendorOrganization() {
        return this.softwareVendorOrganization;
    }

    public void setSoftwareVendorOrganization(String softOrg) {
        this.softwareVendorOrganization = Utils.safeString(softOrg);
    }

    public String getSoftwareCertifiedVersionOrReleaseNo() {
        return this.softwareCertifiedVersionOrReleaseNo;
    }

    public void setSoftwareCertifiedVersionOrReleaseNo(String versionOrReleaseNo) {
        this.softwareCertifiedVersionOrReleaseNo = Utils.safeString(versionOrReleaseNo);
    }

    public String getSoftwareProductName() {
        return this.softwareProductName;
    }

    public void setSoftwareProductName(String name) {
        this.softwareProductName = Utils.safeString(name);
    }

    public String getSoftwareBinaryId() {
        return this.softwareBinaryId;
    }

    public void setSoftwareBinaryId(String id) {
        this.softwareBinaryId = Utils.safeString(id);
    }

    public String getSoftwareProductInformation() {
        return this.softwareProductInformation;
    }

    public void setSoftwareProductInformation(String info) {
        this.softwareProductInformation = Utils.safeString(info);
    }

    public void setMllpv2TimeToWaitForAckNak(int duration) {
        this.setMllpv2TimeToWaitForAckNak(duration);;
    }

    public Byte getFieldSeparator() {
        return this.fieldSeparator;
    }

    public void setFieldSeparator(Byte fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String getSendingApplication() {
        return this.sendingApplication;
    }

    public void setSendingApplication(String sendingApplication) {
        this.sendingApplication = Utils.safeString(sendingApplication);
    }

    public String getSendingFacility() {
        return this.sendingFacility;
    }

    public void setSendingFacility(String sendingFacility) {
        this.sendingFacility = Utils.safeString(sendingFacility);
    }

    public String getSoftwareInstallDate() {
        return this.softwareInstallDate;
    }

    public void setSoftwareInstallDate(String softwareInstallDate) {
        this.softwareInstallDate = Utils.safeString(softwareInstallDate);
    }

    public String getEncodingCharacters() {
        return this.encodingCharacters;
    }

    public void setEncodingCharacters(String encodingCharacters) {
        this.encodingCharacters = Utils.safeString(encodingCharacters);
    }

    public boolean isSequenceNoEnabled() {
        return this.seqNoEnabled;
    }

    public void setSequenceNoEnabled(boolean enabled) {
        this.seqNoEnabled = enabled;
    }

    public boolean isJournallingEnabled() {
        return this.journallingEnabled;
    }

    public void setJournallingEnabled(boolean enabled) {
        this.journallingEnabled = enabled;
    }
    public boolean isPersistenceEnabled() {
        return this.persistenceEnabled;
    }

    public void setPersistenceEnabled(boolean enabled) {
        this.persistenceEnabled = enabled;
    }
    

}
