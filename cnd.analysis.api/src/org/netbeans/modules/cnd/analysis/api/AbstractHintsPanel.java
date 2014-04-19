/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.analysis.api;

import java.util.prefs.Preferences;
import javax.swing.JPanel;

/**
 *
 * @author Alexander Simon
 */
public abstract class AbstractHintsPanel extends JPanel {

    public abstract void setSettings(Preferences settings);
}
