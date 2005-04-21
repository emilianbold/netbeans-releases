/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;

import java.io.UnsupportedEncodingException;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.StringTokenizer;


/**
 * @author Radek Matous
 */
final class NbfsUtil {
    /** url separator */
    private static final char SEPARATOR = '/';

    /**
     * Gets URL with nbfs protocol for passes fo
     * @param fo
     * @return url with nbfs protocol
     * @throws FileStateInvalidException if FileObject somehow corrupted
     */
    static URL getURL(FileObject fo) throws FileStateInvalidException {
        final String fsPart = encodeFsPart(fo);
        final String foPart = encodeFoPart(fo);

        final String host = "nbhost"; //NOI18N
        final String file = combine(fsPart, foPart);

        // #13038: the URL constructor accepting a handler is a security-sensitive
        // operation. Sometimes a user class loaded internally (customized bean...),
        // which has no privileges, needs to make and use an nbfs: URL, since this
        // may be the URL used by e.g. ClassLoader.getResource for resources.
        try {
            return (URL) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        // #30397: the fsPart name cannot be null
                        return new URL(FileURL.PROTOCOL, host, -1, file, FileURL.HANDLER); // NOI18N
                    }
                }
            );
        } catch (PrivilegedActionException pae) {
            // MalformedURLException is declared but should not happen.
            IllegalStateException ise = new IllegalStateException(pae.toString());
            ExternalUtil.annotate(ise, pae);
            throw ise;
        }
    }

    private static String combine(final String host, final String file) {
        StringBuffer sb = new StringBuffer();
        sb.append(SEPARATOR).append(host);
        sb.append(file);

        return sb.toString();
    }

    private static String[] split(URL url) {
        String file = url.getFile();
        int idx = file.indexOf("/", 1);
        String fsPart = "";
        String foPart = file;

        if (idx > 1) {
            fsPart = file.substring(1, idx);
            foPart = file.substring(idx + 1);
        }

        return new String[] { fsPart, foPart };
    }

    /**
     *  Gets FileObject for passed url.
     * @param url
     * @return appropriate FileObject. Can return null for other protocol than nbfs or
     * if such FileObject isn't reachable via Repository.
     */
    static FileObject getFileObject(URL url) {
        if (!url.getProtocol().equals(FileURL.PROTOCOL)) {
            return null;
        }

        if (isOldEncoding(url)) {
            return oldDecode(url);
        }

        String[] urlParts = split(url);

        String fsName = decodeFsPart(urlParts[0]);
        String foName = decodeFoPart(urlParts[1]);

        FileSystem fsys = ExternalUtil.getRepository().findFileSystem(fsName);

        return (fsys == null) ? null : fsys.findResource(foName);
    }

    private static String encodeFsPart(FileObject fo) throws FileStateInvalidException {
        FileSystem fs = fo.getFileSystem();

        return encoder(fs.getSystemName());
    }

    private static String encodeFoPart(FileObject fo) {
        StringTokenizer elemsEnum;
        StringBuffer sBuff = new StringBuffer();
        elemsEnum = new StringTokenizer(fo.getPath(), String.valueOf(SEPARATOR));

        while (elemsEnum.hasMoreElements()) {
            sBuff.append(SEPARATOR);
            sBuff.append(encoder((String) elemsEnum.nextElement()));
        }

        String retVal = sBuff.toString();

        if ((retVal.length() == 0) || (fo.isFolder() && (retVal.charAt(retVal.length() - 1) != SEPARATOR))) {
            retVal += SEPARATOR;
        }

        return retVal;
    }

    private static String decodeFsPart(String encodedStr) {
        return decoder(encodedStr);
    }

    private static String decodeFoPart(String encodedStr) {
        if (encodedStr == null) {
            return ""; //NOI18N
        }

        StringTokenizer elemsEnum;
        StringBuffer sBuff = new StringBuffer();
        elemsEnum = new StringTokenizer(encodedStr, String.valueOf(SEPARATOR));

        while (elemsEnum.hasMoreElements()) {
            sBuff.append(SEPARATOR);
            sBuff.append(decoder((String) elemsEnum.nextElement()));
        }

        return sBuff.toString();
    }

    private static String encoder(String elem) {
        try {
            return URLEncoder.encode(elem, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException e) {
            ExternalUtil.log(e.getLocalizedMessage());

            return URLEncoder.encode(elem);
        }
    }

    private static String decoder(String elem) {
        try {
            return URLDecoder.decode(elem, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException e) {
            ExternalUtil.log(e.getLocalizedMessage());

            return URLDecoder.decode(elem);
        }
    }

    // backward compatibility
    private static boolean isOldEncoding(URL url) {
        String host = url.getHost();

        return (host == null) || (host.length() == 0);
    }

    private static FileObject oldDecode(URL u) {
        String resourceName = u.getFile();

        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1); // NOI18N
        }

        // first part is FS name
        int first = resourceName.indexOf('/'); // NOI18N

        if (first == -1) {
            return null;
        }

        String fileSystemName = oldDecodeFSName(resourceName.substring(0, first));
        resourceName = resourceName.substring(first);

        FileSystem fsys = ExternalUtil.getRepository().findFileSystem(fileSystemName);

        return (fsys == null) ? null : fsys.findResource(resourceName);
    }

    /** Decodes name to FS one.
     * @param name encoded name
     * @return original name of the filesystem
     */
    private static String oldDecodeFSName(String name) {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int len = name.length();

        while (i < len) {
            char ch = name.charAt(i++);

            if ((ch == 'Q') && (i < len)) {
                switch (name.charAt(i++)) {
                case 'B':
                    sb.append('/');

                    break;

                case 'C':
                    sb.append(':');

                    break;

                case 'D':
                    sb.append('\\');

                    break;

                case 'E':
                    sb.append('#');

                    break;

                default:
                    sb.append('Q');

                    break;
                }
            } else {
                // not Q
                sb.append(ch);
            }
        }

        return sb.toString();
    }
}
