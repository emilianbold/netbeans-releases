/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <time.h>

/*
 * 
 */

#define num_steps 200000000

double pi = 0;

int
main(int argc, char** argv) {
    
    int i;
    double start, stop;
    
#ifdef _OPENMP
    omp_set_num_threads(4);
    omp_set_dynamic(0);
#endif
    
    start = clock();
    
//           we want 1/1 - 1/3 + 1/5 - 1/7 etc.
//            therefore we count by fours (0, 4, 8, 12...) and take
//              1/(0+1) =  1/1
//            - 1/(0+3) = -1/3
//              1/(4+1) =  1/5
//            - 1/(4+3) = -1/7 and so on 
    
    #pragma omp parallel for  //reduction(+:pi) 
    for (i = 0; i < num_steps ; i++) {
         
         pi += 1.0/(i*4.0 + 1.0);
         pi -= 1.0/(i*4.0 + 3.0);
       }
    stop = clock();
    
       pi = pi * 4.0;
       printf("pi done - %f in %.3f seconds\n", pi, (stop-start)/1000000);    
    
    return (EXIT_SUCCESS);
}

