/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.identity.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 
 */
public class GetQuote {

    public GetQuote() {
        init();
    }

    /**
     * Returns the stock quote for the stock symbol.
     * If unable to obtain the real time quote, returns a cached value
     */
    public Map getQuote(String symbol) {
        // Obtain the quote from Yahoo! Service
        Map data = getYahooQuote(symbol);
        if (data == null) {
            // Unable to obtain from Yahoo! Get from local cache
            data = getCachedQuote(symbol);
        }

        // Return the results
        return (data);
    }

    private Map getYahooQuote(String ticker) {
        URL url = null;
        try {
            // URL for the stock quote from Yahoo! service
            url = new URL("http://download.finance.yahoo.com/d/quotes.csv?s=" +
                    ticker + "&d=t&f=sl1d1t1c1ohgvj1pp2wern");

            // Set the timeouts for connection and read
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);

            // Request for the stock quote
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String[] values = null;
            String str;
            if ((str = in.readLine()) != null) {
                values = str.split(",");
            }
            in.close();
            if ((values == null) || values.length < 16) {
                return (null);
            }
            // Populate stock values
            Map map = new HashMap();
            map.put("symbol", removeQuotes(values[0]));
            map.put("company", removeQuotes(values[15]));
            map.put("realValue", values[1]);
            map.put("time", removeQuotes(values[2]) + " " +
                    removeQuotes(values[3]));
            map.put("volume", values[8]);
            map.put("open", values[5]);
            map.put("change", values[4]);
            map.put("dayHigh", values[6]);
            map.put("dayLow", values[7]);
            map.put("yearRange", removeQuotes(values[12]));
            map.put("marketCap", values[9]);
            map.put("message", "");
            return map;
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Map getCachedQuote(String symbol) {
        Map data = (Map) stockData.get(symbol);
        if (data == null) {
            data = (Map) stockData.get("ORCL");
        }
        data.put("symbol", symbol);
        data.put("company", symbol.toUpperCase());
        return (data);
    }

    /**
     * Assign static values for stock quotes.
     * Used as fall-back if quote cannot be
     * obtained from Yahoo!
     */
    private void init() {
        Map stockValues = new HashMap();
        stockValues.put("company", "");
        stockValues.put("realValue", "7.36");
        stockValues.put("volume", "31,793,369");
        stockValues.put("open", "7.37");
        stockValues.put("change", "-0.01");
        stockValues.put("dayHigh", "7.38");
        stockValues.put("dayLow", "7.12");
        stockValues.put("yearRange", "N/A");
        stockValues.put("marketCap", "N/A");
        stockValues.put("message", "Quote AUTO Generated");
        stockValues.put("time", getTime());
        stockData.put("SUNW", stockValues);

        stockValues = new HashMap();
        stockValues.put("realValue", "16.35");
        stockValues.put("company", "");
        stockValues.put("volume", "38,544,715");
        stockValues.put("open", "16.35");
        stockValues.put("change", "-0.27");
        stockValues.put("dayHigh", "16.64");
        stockValues.put("dayLow", "16.31");
        stockValues.put("yearRange", "N/A");
        stockValues.put("marketCap", "N/A");
        stockValues.put("message", "Quote AUTO Generated");
        stockValues.put("time", getTime());
        stockData.put("ORCL", stockValues);
    }

    private String getTime() {
        GregorianCalendar time = new GregorianCalendar();
        return (time.get(Calendar.MONTH) + "/" +
                time.get(Calendar.DAY_OF_MONTH) + "/" +
                time.get(Calendar.YEAR) + " " +
                time.get(Calendar.HOUR) + ":" +
                time.get(Calendar.MINUTE) +
                time.get(Calendar.AM_PM));
    }

    private String removeQuotes(String key) {
        return (key.replaceAll("\"", ""));
    }
    
    private static Map stockData = new HashMap();
}
