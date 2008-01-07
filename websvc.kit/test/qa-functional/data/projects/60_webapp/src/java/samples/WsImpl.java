/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samples;

import javax.jws.WebService;

/**
 *
 * @author lukas
 */
@WebService(endpointInterface="samples.EndpointI")
public class WsImpl implements EndpointI {

    public int getAge() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
