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
public class V3EditorModel implements V3EditorForm.Model{

    private String location = "";
    private String transportProtocol = "";
    private String use = "";
    private String encodingStyle = "";
    private String acknowledgementMode = "";
    private String llpType = "";
    private String startBlockCharacter = "";
    private String endBlockCharacter = "";
    private String endDataCharacter = "";
    private boolean hllpChecksumEnabled;
    private boolean seqNoEnabled;
    private String processingId = "";
    private String versionId = "";
    private boolean validateMSH;
    private boolean sftEnabled;
    private String softwareVendorOrganization = "";
    private String softwareCertifiedVersionOrReleaseNo = "";
    private String softwareProductName = "";
    private String softwareBinaryId = "";
    private String softwareProductInformation = "";
    private int mllpv2RetriesCountOnNak = 0;
    private int mllpv2RetryInterval = 0;
    private int mllpv2TimeToWaitForAckNak = 0 ;
    
            
    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = Utils.safeString(location);
    }

    public String getTransportProtocol() {
        return this.transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = Utils.safeString(transportProtocol);
    }

    public String getUse() {
        return this.use;
    }

    public void setUse(String use) {
        this.use = Utils.safeString(use);
    }

    public String getEncodingStyle() {
        return this.encodingStyle;
    }

    public void setEncodingStyle(String encodingStyle) {
        this.encodingStyle = Utils.safeString(encodingStyle);
    }

    public String getAcknowledgementMode() {
        return this.acknowledgementMode;
    }

    public void setAcknowledgementMode(String ackMode) {
        this.acknowledgementMode = Utils.safeString(ackMode);
    }

    public String getLLPType() {
        return this.llpType;
    }

    public void setLLPType(String llpType) {
        this.llpType = Utils.safeString(llpType);
    }

    public String getStartBlockCharacter() {
        return this.startBlockCharacter;
    }

    public void setStartBlockCharacter(String startBlockChar) {
        this.startBlockCharacter = Utils.safeString(startBlockChar);
    }

    public String getEndBlockCharacter() {
        return this.endBlockCharacter;
    }

    public void setEndBlockCharacter(String endBlockChar) {
        this.endBlockCharacter = Utils.safeString(endBlockChar);
    }

    public String getEndDataCharacter() {
        return this.endDataCharacter;
    }

    public void setEndDataCharacter(String endDataChar) {
        this.endDataCharacter = Utils.safeString(endDataChar);
    }

    public boolean isHLLPChecksumEnabled() {
        return this.hllpChecksumEnabled;
    }

    public void setHLLPChecksumEnabled(boolean enabled) {
        this.hllpChecksumEnabled = enabled;
    }

    public boolean isSequenceNoEnabled() {
        return this.seqNoEnabled;
    }

    public void setSequenceNoEnabled(boolean enabled) {
        this.seqNoEnabled = enabled;
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
        this.softwareProductInformation = info;
    }

    public int getMllpv2RetriesCountOnNak() {
        return this.mllpv2RetriesCountOnNak;
    }

    public void setMllpv2RetriesCountOnNak(int count) {
        this.mllpv2RetriesCountOnNak = count;
    }

    public int getMllpv2RetryInterval() {
        return this.mllpv2RetryInterval;
    }

    public void setMllpv2RetryInterval(int interval) {
        this.mllpv2RetryInterval = interval;
    }

    public int getMllpv2TimeToWaitForAckNak() {
        return this.mllpv2TimeToWaitForAckNak;
    }

    public void setMllpv2TimeToWaitForAckNak(int duration) {
        this.setMllpv2TimeToWaitForAckNak(duration);;
    }

}
