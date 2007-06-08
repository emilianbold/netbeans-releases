/*
 * AddNumbers.java
 *
 * Created on March 29, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hello;

import javax.jws.WebService;

/**
 *
 * @author mkuchtiak
 */
@WebService(endpointInterface="hello.HelloInterface")
public class Hello implements HelloInterface {

    /**
     * hello Method
     * @param x name
     * @return echo string
     */    
    public String hello(String s) {
        return "Hello "+s;
    }
}
