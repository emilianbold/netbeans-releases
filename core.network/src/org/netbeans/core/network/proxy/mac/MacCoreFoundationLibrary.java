package org.netbeans.core.network.proxy.mac;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 *
 * @author lfischme
 */
public interface MacCoreFoundationLibrary extends Library {
    MacCoreFoundationLibrary LIBRARY = (MacCoreFoundationLibrary) Native.loadLibrary("CoreFoundation", MacCoreFoundationLibrary.class);
    
    public boolean CFDictionaryGetValueIfPresent(Pointer dictionary, Pointer key, Pointer[] returnValue);
    
    public Pointer CFDictionaryGetValue(Pointer dictionary, Pointer key);
    
    public Pointer CFStringCreateWithCString(Pointer alloc, byte[] string, Pointer encoding);
    
    public long CFStringGetLength(Pointer cfStringRef);
    
    public long CFStringGetMaximumSizeForEncoding(long lenght, int encoding);
    
    public boolean CFStringGetCString(Pointer cfStringRef, Pointer buffer, long maxSize, int encoding);
    
    public Pointer CFNumberGetType(Pointer cfNumberRef);
    
    public boolean CFNumberGetValue(Pointer cfNumberRef, Pointer cfNumberType, Pointer value);

    public long CFNumberGetByteSize(Pointer cfNumberRef);
    
    public long CFArrayGetCount(Pointer cfArrayRef);
    
    public Pointer CFArrayGetValueAtIndex(Pointer cfArrayRef, Pointer cfIndex);
}
