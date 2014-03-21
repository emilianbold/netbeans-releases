/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.analysis.api;

import java.util.prefs.Preferences;
import javax.swing.JComponent;

/**
 *
 * @author Alexander Simon
 */
public interface AbstractCustomizerProvider {
    public JComponent createComponent(Preferences context);
}
