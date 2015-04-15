/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.URLMapper.class)
public class RemoteFileUrlMapper extends URLMapper {

    @Override
    public FileObject[] getFileObjects(URL url) {
        if (url.getProtocol().equals(RemoteFileURLStreamHandler.PROTOCOL)) {
            ExecutionEnvironment env;
            String user = url.getUserInfo();
            if (user != null) {
                env = ExecutionEnvironmentFactory.createNew(user, url.getHost(), url.getPort());
            } else {
                RemoteLogger.assertTrue(false, "Trying to access remote file system without user name");
                env = RemoteFileSystemUtils.getExecutionEnvironment(url.getHost(), url.getPort());
                if (env == null) {
                    user = System.getProperty("user.name");
                    if (user != null) {
                        env = ExecutionEnvironmentFactory.createNew(user, url.getHost(), url.getPort());
                    }
                }
            }
            if (env != null) {
                RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
                FileObject fo = fs.findResource(unescapePath(url));
                return new FileObject[] { fo };
            }
        }
        return null;
    }

    @Override
    public URL getURL(FileObject fo, int type) {
        if (fo instanceof RemoteFileObject) {
            RemoteFileObject rfo = (RemoteFileObject) fo;
            try {
                ExecutionEnvironment env = rfo.getExecutionEnvironment();
                return toURL(env, rfo.getPath(), rfo.isFolder());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public static URI toURI(ExecutionEnvironment env, String path, boolean folder) throws URISyntaxException {
        return new URI(toURLString(env, path, folder));
    }
    
    public static URL toURL(ExecutionEnvironment env, String path, boolean folder) throws MalformedURLException {
        return new URL(toURLString(env, path, folder));
    }

    private static String toURLString(ExecutionEnvironment env, String path, boolean folder) {
        /*
         * Prepare URL here as a string to be used in the URL(String spec)
         * constructor as it works with userinfo as expected (ipv6 address case).
         * URL(String protocol, String host, int port, String file) cannot be
         * used here, as 'host' should contain only host information, without 
         * username/password etc... 
         */
        StringBuilder sb = new StringBuilder(RemoteFileURLStreamHandler.PROTOCOL);
        sb.append("://"); // NOI18N
        sb.append(env.getUser()).append('@').append(env.getHost());
        sb.append(':').append(env.getSSHPort()).append(escapePath(path));
        if (folder && !(path.endsWith("/"))) { // NOI18N
            sb.append('/'); // NOI18N
        }
        return sb.toString();
    }

    private final static char[] hexDigits = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    
    private static void appendEscaped(StringBuilder sb, char c) {
        byte b = (byte) c;
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 0x0f]);
        sb.append(hexDigits[(b >> 0) & 0x0f]);
    }
    
    private static CharSequence escapeImpl(String path, int firstSpecial) {
        StringBuilder sb = (firstSpecial == 0) ? 
                new StringBuilder() : 
                new StringBuilder(path.subSequence(0, firstSpecial));
        for (int i = firstSpecial; i < path.length(); i++) {
            appendEscaped(sb, path.charAt(i));
        }
        return sb;
    }

    /**
     * Determines whether we need to escape the given character
     * See section 2.2. and below at  http://www.ietf.org/rfc/rfc2396.txt
     * reserved    = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" | "$" | ","
     * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     * delims      = "<" | ">" | "#" | "%" | <">
     * unwise      = "{" | "}" | "|" | "\" | "^" | "[" | "]" | "`"
     * control     = <US-ASCII coded characters 00-1F and 7F hexadecimal>
     * space       = <US-ASCII coded character 20 hexadecimal>
     **/
    
    //<editor-fold defaultstate="collapsed" desc="ascii table to bolean">
    private static final boolean[] charsToEscape = new boolean[] {
        // Char  Dec  Oct  Hex
        // -------------------
        true,  // (nul)   0 0000 0x00
        true,  // (soh)   1 0001 0x01
        true,  // (stx)   2 0002 0x02
        true,  // (etx)   3 0003 0x03
        true,  // (eot)   4 0004 0x04
        true,  // (enq)   5 0005 0x05
        true,  // (ack)   6 0006 0x06
        true,  // (bel)   7 0007 0x07
        true,  // (bs)    8 0010 0x08
        true,  // (ht)    9 0011 0x09
        true,  // (nl)   10 0012 0x0a
        true,  // (vt)   11 0013 0x0b
        true,  // (np)   12 0014 0x0c
        true,  // (cr)   13 0015 0x0d
        true,  // (so)   14 0016 0x0e
        true,  // (si)   15 0017 0x0f
        true,  // (dle)  16 0020 0x10
        true,  // (dc1)  17 0021 0x11
        true,  // (dc2)  18 0022 0x12
        true,  // (dc3)  19 0023 0x13
        true,  // (dc4)  20 0024 0x14
        true,  // (nak)  21 0025 0x15
        true,  // (syn)  22 0026 0x16
        true,  // (etb)  23 0027 0x17
        true,  // (can)  24 0030 0x18
        true,  // (em)   25 0031 0x19
        true,  // (sub)  26 0032 0x1a
        true,  // (esc)  27 0033 0x1b
        true,  // (fs)   28 0034 0x1c
        true,  // (gs)   29 0035 0x1d
        true,  // (rs)   30 0036 0x1e
        true,  // (us)   31 0037 0x1f
        true,  // (sp)   32 0040 0x20
        true,  // !      33 0041 0x21
        true,  // "      34 0042 0x22
        true,  // #      35 0043 0x23
        true,  // $      36 0044 0x24
        true,  // %      37 0045 0x25
        true,  // &      38 0046 0x26
        true,  // '      39 0047 0x27
        true,  // (      40 0050 0x28
        true,  // )      41 0051 0x29
        true,  // *      42 0052 0x2a
        true,  // +      43 0053 0x2b
        true,  // ,      44 0054 0x2c
        true,  // -      45 0055 0x2d
        false, // .      46 0056 0x2e
        false, // /      47 0057 0x2f
        false, // 0      48 0060 0x30
        false, // 1      49 0061 0x31
        false, // 2      50 0062 0x32
        false, // 3      51 0063 0x33
        false, // 4      52 0064 0x34
        false, // 5      53 0065 0x35
        false, // 6      54 0066 0x36
        false, // 7      55 0067 0x37
        false, // 8      56 0070 0x38
        false, // 9      57 0071 0x39
        true,  // :      58 0072 0x3a
        true,  // ;      59 0073 0x3b
        true,  // <      60 0074 0x3c
        true,  // =      61 0075 0x3d
        true,  // >      62 0076 0x3e
        true,  // ?      63 0077 0x3f
        true,  // @      64 0100 0x40
        false, // A      65 0101 0x41
        false, // B      66 0102 0x42
        false, // C      67 0103 0x43
        false, // D      68 0104 0x44
        false, // E      69 0105 0x45
        false, // F      70 0106 0x46
        false, // G      71 0107 0x47
        false, // H      72 0110 0x48
        false, // I      73 0111 0x49
        false, // J      74 0112 0x4a
        false, // K      75 0113 0x4b
        false, // L      76 0114 0x4c
        false, // M      77 0115 0x4d
        false, // N      78 0116 0x4e
        false, // O      79 0117 0x4f
        false, // P      80 0120 0x50
        false, // Q      81 0121 0x51
        false, // R      82 0122 0x52
        false, // S      83 0123 0x53
        false, // T      84 0124 0x54
        false, // U      85 0125 0x55
        false, // V      86 0126 0x56
        false, // W      87 0127 0x57
        false, // X      88 0130 0x58
        false, // Y      89 0131 0x59
        false, // Z      90 0132 0x5a
        true,  // [      91 0133 0x5b
        true,  // \      92 0134 0x5c
        true,  // ]      93 0135 0x5d
        true,  // ^      94 0136 0x5e
        true,  // _      95 0137 0x5f
        true,  // `      96 0140 0x60
        false, // a      97 0141 0x61
        false, // b      98 0142 0x62
        false, // c      99 0143 0x63
        false, // d     100 0144 0x64
        false, // e     101 0145 0x65
        false, // f     102 0146 0x66
        false, // g     103 0147 0x67
        false, // h     104 0150 0x68
        false, // i     105 0151 0x69
        false, // j     106 0152 0x6a
        false, // k     107 0153 0x6b
        false, // l     108 0154 0x6c
        false, // m     109 0155 0x6d
        false, // n     110 0156 0x6e
        false, // o     111 0157 0x6f
        false, // p     112 0160 0x70
        false, // q     113 0161 0x71
        false, // r     114 0162 0x72
        false, // s     115 0163 0x73
        false, // t     116 0164 0x74
        false, // u     117 0165 0x75
        false, // v     118 0166 0x76
        false, // w     119 0167 0x77
        false, // x     120 0170 0x78
        false, // y     121 0171 0x79
        false, // z     122 0172 0x7a
        true,  // {     123 0173 0x7b
        true,  // |     124 0174 0x7c
        true,  // }     125 0175 0x7d
        true,  // ~     126 0176 0x7e
        true  // (del) 127 0177 0x7f
    };
    //</editor-fold>
    
    private static boolean needToEscape(char c) {
        if (c < charsToEscape.length) {
            return charsToEscape[c];
        }
        return false;
    }

    private static CharSequence escapePath(String path) {
        for (int i = 0; i < path.length(); i++) {
            if (needToEscape(path.charAt(i))) {
                return escapeImpl(path, i);
            }            
        }
        return path;
    }

    private static String unescapePath(URL url) {
        String path = url.getFile();
        if (path.contains("%")) { //NOI18N
            try {
                return url.toURI().getPath();
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return path;
    }
}
