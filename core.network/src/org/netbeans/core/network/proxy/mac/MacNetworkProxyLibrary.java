package org.netbeans.core.network.proxy.mac;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 *
 * @author lfischme
 */
public interface MacNetworkProxyLibrary extends Library {
    MacNetworkProxyLibrary LIBRARY = (MacNetworkProxyLibrary) Native.loadLibrary("CoreServices", MacNetworkProxyLibrary.class);
    
    public Pointer CFNetworkCopySystemProxySettings();
}
