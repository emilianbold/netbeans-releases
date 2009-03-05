/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import javax.swing.UIManager;
import org.netbeans.modules.kenai.ui.dashboard.ColorManager;

/**
 * Abstraction of a single build process (a line in Builds section)
 *
 * @author S. Aubrecht
 */
public abstract class BuildHandle {

    /**
     * The name of property which is fired when the status of this build has changed.
     * The property value is the new build status.
     */
    public static final String PROP_STATUS = "status";

    public enum Status {
        
        RUNNING( "running", ColorManager.defaultForeground ),
        FAILED( "failed", ColorManager.errorColor ),
        STABLE( "stable", ColorManager.stableBuildColor ),
        UNSTABLE( "unstable", ColorManager.unstableBuildColor ),
        UNKNOWN( "unknown", ColorManager.disabledColor );

        private final Color c;
        private final String displayName;

        private Status( String displayName, Color c ) {
            this.displayName = displayName;
            this.c = c;
        }

        public Color getColor() {
            return c;
        }

        public String toString() {
            return displayName;
        }
    }

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    /**
     *
     * @return Build status
     */
    public abstract Status getStatus();

    public abstract void addPropertyChangeListener( PropertyChangeListener l );

    public abstract void removePropertyChangeListener( PropertyChangeListener l );
}
