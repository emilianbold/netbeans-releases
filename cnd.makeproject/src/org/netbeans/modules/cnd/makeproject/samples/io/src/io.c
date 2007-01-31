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

#include <stdio.h>

void go();

int main(int argc, char**argv) {
    go();
    return 0;
}

void go() {
    int i;
    
    char *prompt = "\nType a string (or 'q' when done) >>>\n";
    for (i = 0; i < 25; i++) {
	char line[132];
	if (feof(stdin))
	    break;
	printf(prompt);
	fflush(stdout);
	scanf("%s", line);
	printf("Read: %s\n", line);
	if (line[0] == 'q' || line[0] == 'Q')
	    break;
    }
}
