/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
*
* Oracle and Java are registered trademarks of Oracle and/or its affiliates.
* Other names may be trademarks of their respective owners.
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
* nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
* particular file as subject to the "Classpath" exception as provided
* by Oracle in the GPL Version 2 section of the License file that
* accompanied this code. If applicable, add the following below the
* License Header, with the fields enclosed by brackets [] replaced by
* your own identifying information:
* "Portions Copyrighted [year] [name of copyright owner]"
*
* Contributor(s):
*
* The Original Software is NetBeans. The Initial Developer of the Original
* Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.identity.samples;

import com.sun.stockquote.PriceType;
import com.sun.stockquote.QuoteResponseType;
import com.sun.stockquote.StockQuotePortType;
import java.util.Map;
import javax.jws.HandlerChain;
import javax.jws.WebService;

/**
 *
 * @author Peter Liu
 */
@WebService(serviceName = "StockService", portName = "StockQuotePortTypePort", endpointInterface = "com.sun.stockquote.StockQuotePortType", targetNamespace = "http://sun.com/stockquote.wsdl", wsdlLocation = "WEB-INF/wsdl/StockService/StockService.wsdl")
@HandlerChain(file = "StockService_handler.xml")
public class StockService implements StockQuotePortType {

    public com.sun.stockquote.QuoteResponseType getStockQuote(com.sun.stockquote.QuoteRequestType body) {
        QuoteResponseType retVal = new QuoteResponseType();
        try {
            String symbol = body.getSymbol().trim();
            GetQuote retriever = new GetQuote();
            Map data =  retriever.getQuote(symbol);
            
            // Convert from Map to QuoteResponseType            
            retVal.setSymbol((String) data.get("symbol"));
            retVal.setCompany((String) data.get("company"));
            retVal.setMessage((String) data.get("message"));
            retVal.setTime((String) data.get("time"));
            retVal.setVolume((String)data.get("volume"));
            retVal.setDelay((String) data.get("delay"));
            retVal.setMarketCap((String) data.get("marketCap"));
            PriceType priceType = new PriceType();
            String last = null;
            last = (String)data.get("realValue");
            float lastft = Float.parseFloat(last);
            priceType.setLast(lastft);
            String open = (String)data.get("open");
            float openft = Float.parseFloat(open);
            priceType.setOpen(openft);
            String dayHigh = (String)data.get("dayHigh");
            float dayHighFt = Float.parseFloat(dayHigh);
            priceType.setDayHigh(dayHighFt);
            String dayLow = (String)data.get("dayLow");
            float dayLowFt = Float.parseFloat(dayLow);
            priceType.setDayLow(dayLowFt);
            priceType.setYearRange((String) data.get("yearRange"));
            retVal.setPrice(priceType);            
            String change = (String)data.get("change");
            retVal.setChange(change);
        } catch (Exception e) {
            // Handle exception
        }
        return retVal;
    }

    
}
