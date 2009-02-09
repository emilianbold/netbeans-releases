/* 
 * File:   test_wait.h
 * Author: vk155633
 *
 * Created on December 11, 2008, 7:37 PM
 */

#ifndef _TEST_WAIT_H
#define	_TEST_WAIT_H

#if __cplusplus
extern "C" {
#endif

void test_sync_init();
void test_sync_step(int step);
void test_sync_shutdown();

#if __cplusplus
}
#endif

#endif	/* _TEST_WAIT_H */
