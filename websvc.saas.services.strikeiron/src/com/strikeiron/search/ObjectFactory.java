/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package com.strikeiron.search;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.strikeiron.search package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LicenseInfo_QNAME = new QName("http://ws.strikeiron.com", "LicenseInfo");
    private final static QName _SubscriptionInfo_QNAME = new QName("http://ws.strikeiron.com", "SubscriptionInfo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.strikeiron.search
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ServiceInfoOutput }
     * 
     */
    public ServiceInfoOutput createServiceInfoOutput() {
        return new ServiceInfoOutput();
    }

    /**
     * Create an instance of {@link GetPricingOutPut }
     * 
     */
    public GetPricingOutPut createGetPricingOutPut() {
        return new GetPricingOutPut();
    }

    /**
     * Create an instance of {@link GetRemainingHitsResponse }
     * 
     */
    public GetRemainingHitsResponse createGetRemainingHitsResponse() {
        return new GetRemainingHitsResponse();
    }

    /**
     * Create an instance of {@link SIServiceInfoResult }
     * 
     */
    public SIServiceInfoResult createSIServiceInfoResult() {
        return new SIServiceInfoResult();
    }

    /**
     * Create an instance of {@link LicenseInfo }
     * 
     */
    public LicenseInfo createLicenseInfo() {
        return new LicenseInfo();
    }

    /**
     * Create an instance of {@link SearchOutPut }
     * 
     */
    public SearchOutPut createSearchOutPut() {
        return new SearchOutPut();
    }

    /**
     * Create an instance of {@link SIWsResult }
     * 
     */
    public SIWsResult createSIWsResult() {
        return new SIWsResult();
    }

    /**
     * Create an instance of {@link ArrayOfMarketPlaceService }
     * 
     */
    public ArrayOfMarketPlaceService createArrayOfMarketPlaceService() {
        return new ArrayOfMarketPlaceService();
    }

    /**
     * Create an instance of {@link SIWsStatus }
     * 
     */
    public SIWsStatus createSIWsStatus() {
        return new SIWsStatus();
    }

    /**
     * Create an instance of {@link GetRemainingHits }
     * 
     */
    public GetRemainingHits createGetRemainingHits() {
        return new GetRemainingHits();
    }

    /**
     * Create an instance of {@link ArrayOfPricingInformation }
     * 
     */
    public ArrayOfPricingInformation createArrayOfPricingInformation() {
        return new ArrayOfPricingInformation();
    }

    /**
     * Create an instance of {@link GetAllStatuses }
     * 
     */
    public GetAllStatuses createGetAllStatuses() {
        return new GetAllStatuses();
    }

    /**
     * Create an instance of {@link Search }
     * 
     */
    public Search createSearch() {
        return new Search();
    }

    /**
     * Create an instance of {@link SubscriptionInfo }
     * 
     */
    public SubscriptionInfo createSubscriptionInfo() {
        return new SubscriptionInfo();
    }

    /**
     * Create an instance of {@link SearchResponse }
     * 
     */
    public SearchResponse createSearchResponse() {
        return new SearchResponse();
    }

    /**
     * Create an instance of {@link ServiceInfoRecord }
     * 
     */
    public ServiceInfoRecord createServiceInfoRecord() {
        return new ServiceInfoRecord();
    }

    /**
     * Create an instance of {@link PricingInformation }
     * 
     */
    public PricingInformation createPricingInformation() {
        return new PricingInformation();
    }

    /**
     * Create an instance of {@link MarketPlaceService }
     * 
     */
    public MarketPlaceService createMarketPlaceService() {
        return new MarketPlaceService();
    }

    /**
     * Create an instance of {@link GetServiceInfo }
     * 
     */
    public GetServiceInfo createGetServiceInfo() {
        return new GetServiceInfo();
    }

    /**
     * Create an instance of {@link GetPricingResponse }
     * 
     */
    public GetPricingResponse createGetPricingResponse() {
        return new GetPricingResponse();
    }

    /**
     * Create an instance of {@link GetServiceInfoResponse }
     * 
     */
    public GetServiceInfoResponse createGetServiceInfoResponse() {
        return new GetServiceInfoResponse();
    }

    /**
     * Create an instance of {@link RegisteredUser }
     * 
     */
    public RegisteredUser createRegisteredUser() {
        return new RegisteredUser();
    }

    /**
     * Create an instance of {@link GetAllStatusesResponse }
     * 
     */
    public GetAllStatusesResponse createGetAllStatusesResponse() {
        return new GetAllStatusesResponse();
    }

    /**
     * Create an instance of {@link StatusCodeOutput }
     * 
     */
    public StatusCodeOutput createStatusCodeOutput() {
        return new StatusCodeOutput();
    }

    /**
     * Create an instance of {@link StatusCodeResult }
     * 
     */
    public StatusCodeResult createStatusCodeResult() {
        return new StatusCodeResult();
    }

    /**
     * Create an instance of {@link ArrayOfSIWsStatus }
     * 
     */
    public ArrayOfSIWsStatus createArrayOfSIWsStatus() {
        return new ArrayOfSIWsStatus();
    }

    /**
     * Create an instance of {@link ArrayOfServiceInfoRecord }
     * 
     */
    public ArrayOfServiceInfoRecord createArrayOfServiceInfoRecord() {
        return new ArrayOfServiceInfoRecord();
    }

    /**
     * Create an instance of {@link GetPricing }
     * 
     */
    public GetPricing createGetPricing() {
        return new GetPricing();
    }

    /**
     * Create an instance of {@link SIWsOutput }
     * 
     */
    public SIWsOutput createSIWsOutput() {
        return new SIWsOutput();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LicenseInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.strikeiron.com", name = "LicenseInfo")
    public JAXBElement<LicenseInfo> createLicenseInfo(LicenseInfo value) {
        return new JAXBElement<LicenseInfo>(_LicenseInfo_QNAME, LicenseInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubscriptionInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.strikeiron.com", name = "SubscriptionInfo")
    public JAXBElement<SubscriptionInfo> createSubscriptionInfo(SubscriptionInfo value) {
        return new JAXBElement<SubscriptionInfo>(_SubscriptionInfo_QNAME, SubscriptionInfo.class, null, value);
    }

}
