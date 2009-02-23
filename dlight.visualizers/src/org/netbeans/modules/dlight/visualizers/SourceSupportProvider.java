/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.core.stack.spi.SourceFileInfoProvider.LineInfo;


public interface SourceSupportProvider {
    public void showSource(LineInfo lineInfo, boolean isReadOnly);
    public void showSource(LineInfo lineInfo);

}