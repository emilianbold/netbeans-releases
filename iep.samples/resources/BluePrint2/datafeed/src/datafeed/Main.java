/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datafeed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Traditional database management systems run one-time queries over 
        // stored data sets. However, many modern applications require 
        // long-running, or continuous, queries over continuous unbounded 
        // streams of data. 
        ArrayList<Alert> tmpList = new ArrayList<Alert>();
        tmpList.add(new Alert("A", 1, "Traditional"));
        tmpList.add(new Alert("A", 3, "management"));
        tmpList.add(new Alert("A", 2, "database"));
        tmpList.add(new Alert("A", 4, "systems"));
        tmpList.add(new Alert("A", 25, "data."));
        tmpList.add(new Alert("A", 5, "run"));
        tmpList.add(new Alert("A", 7, "queries"));
        tmpList.add(new Alert("A", 8, "over"));
        tmpList.add(new Alert("A", 10, "data"));
        tmpList.add(new Alert("A", 9, "stored"));
        tmpList.add(new Alert("A", 11, "sets."));
        tmpList.add(new Alert("A", 12, "However"));
        tmpList.add(new Alert("A", 13, "many"));
        tmpList.add(new Alert("A", 14, "modern"));
        tmpList.add(new Alert("A", 16, "require"));
        tmpList.add(new Alert("A", 15, "applications"));
        tmpList.add(new Alert("A", 17, "long-running,"));
        tmpList.add(new Alert("A", 18, "or"));
        tmpList.add(new Alert("A", 20, "queries"));
        tmpList.add(new Alert("A", 21, "over"));
        tmpList.add(new Alert("A", 6, "one-time"));
        tmpList.add(new Alert("A", 22, "continuous"));
        tmpList.add(new Alert("A", 19, "continuous,"));
        tmpList.add(new Alert("A", 23, "unbounded"));
        tmpList.add(new Alert("A", 24, "of"));

        //Event processing involves the continuous processing and analysis of 
        //high volume, high-speed data streams from inside and outside an 
        //organization. The need exists to detect business-critical issues as 
        //they happen, and to route, filter and pre-process data continuously 
        //over an indeterminate period of time         
        tmpList.add(new Alert("B", 1, "Event"));
        tmpList.add(new Alert("B", 3, "involves"));
        tmpList.add(new Alert("B", 2, "processing"));
        tmpList.add(new Alert("B", 4, "the"));
        tmpList.add(new Alert("B", 5, "continuous"));
        tmpList.add(new Alert("B", 8, "analysis"));
        tmpList.add(new Alert("B", 9, "of"));
        tmpList.add(new Alert("B", 7, "and"));
        tmpList.add(new Alert("B", 10, "high"));
        tmpList.add(new Alert("B", 12, "high-speed"));
        tmpList.add(new Alert("B", 11, "volume,"));
        tmpList.add(new Alert("B", 13, "data"));
        tmpList.add(new Alert("B", 14, "streams"));
        tmpList.add(new Alert("B", 15, "from"));
        tmpList.add(new Alert("B", 17, "and"));
        tmpList.add(new Alert("B", 18, "outside"));
        tmpList.add(new Alert("B", 20, "organization."));
        tmpList.add(new Alert("B", 19, "an"));
        tmpList.add(new Alert("B", 21, "The"));
        tmpList.add(new Alert("B", 16, "inside"));
        tmpList.add(new Alert("B", 22, "need"));
        tmpList.add(new Alert("B", 23, "exists"));
        tmpList.add(new Alert("B", 24, "to"));
        tmpList.add(new Alert("B", 25, "detect"));
        tmpList.add(new Alert("B", 26, "business-critical"));
        tmpList.add(new Alert("B", 6, "processing"));
        tmpList.add(new Alert("B", 27, "issues"));
        tmpList.add(new Alert("B", 28, "as"));
        tmpList.add(new Alert("B", 29, "they"));
        tmpList.add(new Alert("B", 30, "happen,"));
        tmpList.add(new Alert("B", 31, "and"));
        tmpList.add(new Alert("B", 36, "pre-process"));
        tmpList.add(new Alert("B", 32, "to"));
        tmpList.add(new Alert("B", 33, "route,"));
        tmpList.add(new Alert("B", 34, "filter"));
        tmpList.add(new Alert("B", 37, "data"));
        tmpList.add(new Alert("B", 38, "continuously"));
        tmpList.add(new Alert("B", 39, "over"));
        tmpList.add(new Alert("B", 35, "and"));
        tmpList.add(new Alert("B", 40, "an"));
        tmpList.add(new Alert("B", 41, "indeterminate"));
        tmpList.add(new Alert("B", 42, "period of time"));
        
        Map<String, Alert> alertRegistry = new HashMap<String, Alert>();
        List<Alert> alertList = new ArrayList<Alert>();
        for (int i = 0, I = tmpList.size(); i < I; i++) {
            Alert alert = tmpList.get(i);
            
            // Keep all alerts in the registry
            alertRegistry.put(alert.getKey(), alert);
            
            // Alerts with index: 0, 10, 20, 30, 40, 50, and 60 will be lost
            if (i % 10  != 0) {
                alertList.add(alert);
            }
        }
        MissingAlertRequestHandler requestHandler = new MissingAlertRequestHandler(alertRegistry);
        AlertSender alertSender = new AlertSender(alertList);
        BufferedReader userIn = null;
        try {
            userIn = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("stop?(y/n)");
                String ans = userIn.readLine();
                if (ans == null) {
                    break;
                }
                if (ans.trim().equals("")) {
                    continue;
                }
                if (ans.equalsIgnoreCase("y")) {
                    requestHandler.stop();
                    alertSender.stop();
                    break;
                }
            }    
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            try {
                if (userIn != null) {
                    userIn.close();
                }
            } catch (Exception e) {
            }
        } 
    }    
    
}
