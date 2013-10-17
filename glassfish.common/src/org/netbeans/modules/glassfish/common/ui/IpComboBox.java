/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.glassfish.tools.ide.utils.NetUtils;

/**
 *
 * @author kratz
 */
public class IpComboBox extends JComboBox<IpComboBox.InetAddr> {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Encapsulate {@see InetAddress} object and provide human readable
     * <code>toString()</code> output for combo box.
     */
    public static class InetAddr {

        /** IP address reference. */
        private final InetAddress ip;

        /** Mark default IP address. */
        private boolean def;

        /**
         * Creates an instance of <code>InetAddr</code> object and sets provided
         * {@see InetAddress} reference.
         * <p/>
         * @param addr IP address reference.
         */
        InetAddr(final InetAddress ip, final boolean def) {
            this.ip = ip;
            this.def = def;
        }

        /**
         * Get IP address reference.
         * <p/>
         * @return IP address reference.
         */
        public InetAddress getIp() {
            return ip;
        }

        /**
         * Get {@see String} representation of this object.
         * <p/>
         * @return {@see String} representation of this object.
         */
        @Override
        public String toString() {
            return isLocalhost(ip) ? IP_4_127_0_0_1_NAME : ip.getHostAddress();
        }

        /**
         * Check if this platform is the default platform.
         * <p/>
         * @return Value of <code>true</code> if this platform is the default
         *         platform or <code>false</code> otherwise.
         */
        public boolean isDefault() {
            return def;
        }

    }

    /**
     * Comparator for <code>InetAddr</code> instances to be sorted in combo box.
     */
    public static class InetAddrComparator implements Comparator<InetAddr> {

        /** Comparator for {@link InetAddress} instances to be sorted. */
        private static final NetUtils.InetAddressComparator
                INET_ADDRESS_COMPARATOR = new NetUtils.InetAddressComparator();

        /**
         * Compares values of <code>InetAddr</code> instances.
         * <p/>
         * @param ip1 First <code>InetAddr</code> instance to be compared.
         * @param ip2 Second <code>InetAddr</code> instance to be compared.
         * @return A negative integer, zero, or a positive integer as the first
         *         argument is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(final InetAddr ip1, final InetAddr ip2) {
            return INET_ADDRESS_COMPARATOR.compare(ip1.getIp(), ip2.getIp()); 
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**  <code>127.0.0.1</code> host name. */
    public static final String IP_4_127_0_0_1_NAME = "localhost";

    /** RAW byte sequence for <code>127.0.0.1</code>. */
    private static final byte[] IP_4_127_0_0_1 = initIp127_0_0_1();

    /** Comparator for <code>InetAddr</code> instances to be sorted
     *  in combo box. */
    private static final IpComboBox.InetAddrComparator ipComparator
            = new IpComboBox.InetAddrComparator();

    /** Exception message for disabled constructors. */
    private static final String CONSTRUCTOR_EXCEPTION_MSG =
            "Data model for a combo box shall not be supplied in constructor.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * RAW byte sequence for <code>127.0.0.1</code> address initializer.
     * <p/>
     * Helper method used to initialize {@link IP_4_127_0_0_1} constant value.
     * <p/>
     * @return RAW byte sequence for <code>127.0.0.1</code> address
     */
    private static byte[] initIp127_0_0_1() {
        try {
            return InetAddress.getByName("127.0.0.1").getAddress();
        // This shall be unreachable.
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(
                    "Could not initialize raw byte sequence for 127.0.0.1");
        }
    }

    /**
     * Verify if provided IP address represents <code>127.0.0.1</code>.
     * <p/>
     * @returns Value of <code>true</code> when provided IP address represents
     *          <code>127.0.0.1</code> or <code>false</code> otherwise.
     */
    public static boolean isLocalhost(InetAddress ip) {
        byte[] ipBytes = ip.getAddress();
        boolean result = IP_4_127_0_0_1.length == ipBytes.length;
        for (byte i = 0; result && i < ip.getAddress().length; i++)
            result = IP_4_127_0_0_1[i] == ipBytes[i];
        return result;
    }
    /**
     * Count number of loop back IP addresses in provided (@link Set).
     * <p/>
     * @param ips (@link Set) of IP addresses to analyze. Shall not be null.
     * @return Number of loop back IP addresses in provided (@link Set).
     */
    private static int loopBackCount(final Set<? extends InetAddress> ips) {
        int count = 0;
        for (InetAddress ip : ips) {
            if (ip.isLoopbackAddress()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Convert array of {@see InetAddress} objects to array of {@see InetAddr}
     * objects.
     * <p/>
     * @param ipsIn An array of {@see InetAddress} objects to be converted.
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     * @return An array of {@see InetAddr} objects containing
     *         <code>ipsIn</code>.
     */
    private static IpComboBox.InetAddr[] toInetAddr(
            final Set<? extends InetAddress> ipsIn, final boolean lopbackOnly) {
        int size = ipsIn == null ? 0
                : lopbackOnly ? loopBackCount(ipsIn) : ipsIn.size();
        IpComboBox.InetAddr[] ipsOut = new IpComboBox.InetAddr[size];
        int i = 0;
        for (InetAddress ip : ipsIn) {
            if (!lopbackOnly || ip.isLoopbackAddress()) {
                ipsOut[i++] = new IpComboBox.InetAddr(ip, false);
            }
        }
        Arrays.sort(ipsOut, ipComparator);
        return ipsOut;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see InetAddr}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(InetAddr[])} instead.
     * @param comboBoxModel Data model for this combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public IpComboBox(final ComboBoxModel comboBoxModel)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see InetAddr}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(InetAddr[])} instead.
     * @param items An array of objects to insert into the combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public IpComboBox(final Object items[])
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see InetAddr}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(InetAddr[])} instead.
     * @param items {@see Vector} of objects to insert into the combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public IpComboBox(final Vector<?> items)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Creates an instance of <code>IpComboBox</code> that contains
     * all IP addresses of this host.
     * <p/>
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     */
    public IpComboBox(final boolean lopbackOnly) {
        super(new DefaultComboBoxModel(
                toInetAddr(NetUtils.getHostIP4s(), lopbackOnly)));
    }

    /**
     * Creates an instance of <code>IpComboBox</code> that contains
     * supplied list of IP addresses.
     * <p/>
     * @param ips IP addresses to be set as data model for combo box.
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     */
    public IpComboBox(final Set<? extends InetAddress> ips,
            final boolean lopbackOnly) {
        super(new DefaultComboBoxModel(toInetAddr(ips, lopbackOnly)));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Update content of data model to contain all IP addresses of this host.
     * <p/>
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     */
    public void updateModel(final boolean lopbackOnly) {
        setModel(new DefaultComboBoxModel(
                toInetAddr(NetUtils.getHostIP4s(), lopbackOnly)));
    }

    /**
     * Update content of data model to contain supplied list
     * of IP addresses.
     * <p/>
     * @param ips IP addresses to be set as data model for combo box.
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     */
    public void updateModel(final Set<? extends InetAddress> ips,
            final boolean lopbackOnly) {
        setModel(new DefaultComboBoxModel(toInetAddr(ips, lopbackOnly)));
    }

    /**
     * Set selected item in the combo box display area to the provided
     * IP address.
     * <p/>
     * @param ip IP address to be set as selected. Default IP address
     *           will be used when <code>null</code> value is supplied.
     */
    @Override
    public void setSelectedItem(Object ip) {
        if (ip == null) {
            int i, count = dataModel.getSize();
            for (i = 0; i < count; i++) {
                if (dataModel.getElementAt(i).isDefault()) {
                    super.setSelectedItem(dataModel.getElementAt(i));
                    break;
                }
            }
        }
        if (ip instanceof InetAddress) {
            int i, count = dataModel.getSize();
            for (i = 0; i < count; i++) {
                if (((InetAddress) ip).equals(
                        (dataModel.getElementAt(i)).getIp())) {
                    super.setSelectedItem(dataModel.getElementAt(i));
                    break;
                }
            }
        } else {
            super.setSelectedItem(ip);
        }
    }

}
