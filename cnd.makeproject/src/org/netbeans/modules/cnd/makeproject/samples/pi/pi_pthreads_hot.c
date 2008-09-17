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

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

#define THREADS 2


/*
 * 
 */

#define num_steps 200000000
double pi = 0;

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;


void *work(void *arg)
{
  int start;
  int end;
  int i;

  double local_pi = 0;

    start = (num_steps/THREADS) * ((int )arg) ;
    end = start + num_steps/THREADS;

    for (i = start; i < end; i++) {
        local_pi += 1.0/(i*4.0 + 1.0);
        local_pi -= 1.0/(i*4.0 + 3.0);
    }

    pthread_mutex_lock(&mutex);
    pi += local_pi;
    pthread_mutex_unlock(&mutex);
    return NULL;
}

int
main(int argc, char** argv) {
    
    
    int i;
    pthread_t tids[THREADS-1];
    
    for (i = 0; i < THREADS - 1 ; i++) {
         pthread_create(&tids[i], NULL, work, (void *)i);
    }

    i = THREADS-1;
    work((void *)i);

    for (i = 0; i < THREADS - 1 ; i++) {
        pthread_join(tids[i], NULL);

    }
    
    pi = pi * 4.0;
    printf("pi done - %f \n", pi);    
    
    return (EXIT_SUCCESS);
}

