/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author Peter Liu
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
