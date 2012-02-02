grammar NewCpp;

options {
    tokenVocab = APTTokenTypes;
}

@header {
package org.netbeans.modules.cnd.modelimpl.parser.generated;

import java.util.HashMap;
}

@members {
    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        // do nothing
    }
}

compilation_unit: ;

