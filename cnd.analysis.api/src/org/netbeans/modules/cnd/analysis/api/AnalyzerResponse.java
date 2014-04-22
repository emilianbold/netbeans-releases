/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.analysis.api;

import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander Simon
 */
public interface AnalyzerResponse {
    public enum AnalyzerSeverity {
        DetectedError,
        ToolError,
        FileError,
        ProjectError
    }

    void addError(AnalyzerSeverity severity, String message, FileObject file, CsmErrorInfo errorInfo);
}
