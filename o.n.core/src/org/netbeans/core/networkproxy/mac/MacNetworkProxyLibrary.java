package org.netbeans.core.networkproxy.mac;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 *
 * @author lfischme
 */
public interface MacNetworkProxyLibrary extends Library {
    MacNetworkProxyLibrary LIBRARY = (MacNetworkProxyLibrary) Native.loadLibrary("CFNetwork", MacNetworkProxyLibrary.class);
    
    public Pointer CFNetworkCopySystemProxySettings();
}
