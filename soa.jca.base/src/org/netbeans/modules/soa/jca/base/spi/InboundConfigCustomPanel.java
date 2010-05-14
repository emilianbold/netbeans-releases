/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base.spi;

import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author echou
 */
public abstract class InboundConfigCustomPanel extends JPanel {

    // start ChangeListener related code
    private ChangeSupport changeSupport = new ChangeSupport(this);

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /**
     * Subclass will call this method to indicating that its state has change,
     * and framework wizard will call isPanelValid() to valid the latest state
     * of this panel.
     *
     */
    public final void fireChangeEvent() {
        changeSupport.fireChange();
    }
    // end changeListener related code


    /**
     * framework wizard will call this method to check if this panel is valid,
     * and if wizard can proceed to the next step.
     *
     * @return returns null or empty string if this panel is valid, else return
     * an error string indicating invalid reason
     */
    public abstract String isPanelValid();

    /**
     * this method is called when user is modifying an existing MDB's inbound
     * activation configuration, this panel will initialize its swing components
     * from the existing values.
     *
     * @param data inbound config data to populate swing components
     */
    public abstract void initFromInboundConfigData(InboundConfigData data);

    /**
     * this method is called when framework wizard asks this panel for all the
     * user input data for the inbound configuration.  Panel is responsible to
     * populate the input param with the necessary activation configuration
     * and ejb-pool settings.
     *
     * @param data inbound config to store to
     */
    public abstract void storeToInboundConfigData(InboundConfigData data);





    public interface InboundConfigData {

        // MDB activation-spec property/value pairs
        public String getActivationProperty(String propertyName);
        public void addActivationProperty(String propertyName, String propertyValue);
        public Set<Map.Entry<String, String>> getActivationProps();

        // ejb-pool settings related methods
        public int getMaxPoolSize();
        public void setMaxPoolSize(int n);

        public int getSteadyPoolSize();
        public void setSteadPoolSize(int n);

        public int getResizeQuantity();
        public void setResizeQuantity(int n);

        public long getMaxWaitTime();
        public void setMaxWaitTime(long n);

        public long getPoolIdleTimeout();
        public void setPoolIdleTimeout(long n);

    }

}
