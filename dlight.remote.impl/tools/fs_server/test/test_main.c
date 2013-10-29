
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <unistd.h>
//#include <assert.h>

#include "../src/fs_common.h"
#include "../src/queue.h"
#include "../src/dirtab.h"
#include "../src/array.h"

static void _assert_true(bool condition, const char* conditionText) {
    if (condition) {
        fprintf(stdout, "Check for (%s) passed.\n", conditionText);
        fflush(stdout);
    } else {
        fprintf(stdout, "Check for (%s) failed.\n", conditionText);
        fflush(stdout);
        exit(1);
    }
}

#define assert_true(condition) _assert_true(condition, #condition)

void test_list() {
    queue l;
    queue_init(&l);
    assert_true(queue_size(&l) == 0);
    assert_true(l.head == NULL);
    assert_true(l.tail == NULL);
    assert_true(queue_poll(&l)== NULL);
    queue_add(&l, "a");
    assert_true(queue_size(&l) == 1);
    assert_true(l.head== l.tail);
    const char* s;
    s = queue_poll(&l);
    assert_true(s != NULL);
    assert_true(queue_size(&l) == 0);
    assert_true(strcmp(s, "a") == 0);
    queue_add(&l, "a");
    s = queue_poll(&l);
    assert_true(strcmp(s, "a") == 0);
    assert_true(l.head== l.tail);    
    queue_add(&l, "A");
    queue_add(&l, "B");
    assert_true(queue_size(&l) == 2);
    queue_add(&l, "C");
    assert_true(queue_size(&l) == 3);
    
    s = queue_poll(&l);
    assert_true(s != NULL);
    assert_true(strcmp(s, "A") == 0);
    
    s = queue_poll(&l);
    assert_true(s != NULL);
    assert_true(strcmp(s, "B") == 0);
    
    s = queue_poll(&l);
    assert_true(s != NULL);
    assert_true(strcmp(s, "C") == 0);    

//    assert_true(list_size(&l) == 3);
//    assert_true(strcmp(l.tail->data, "c") == 0);
}

void test_dirtab_get_cache(const char* path, const char* reference_cache_path, int passes) {
    int i;
    for (i = 0; i < passes; i++) {
        const char *cache_path = dirtab_get_cache(path);
        fprintf(stdout, "%s -> %s\n", path, cache_path);
        assert_true(strcmp(cache_path, reference_cache_path) == 0);        
    }
    fflush(stdout);
}

void test_dirtab_2() {
    fprintf(stdout, "testing dirtab persistence...\n");
    dirtab_init();
    assert_true(chdir(dirtab_get_basedir())== 0);
    test_dirtab_get_cache("/home/xxx123", "1024", 3);
}

void test_dirtab_1() {
    assert_true(system("rm -rf ~/.netbeans/remotefs") == 0);        
    fprintf(stdout, "testing dirtab...\n");
    dirtab_init();
    assert_true(chdir(dirtab_get_basedir())== 0);
    test_dirtab_get_cache("/home", "0", 2);
    
    int i;
    for (i = 1; i < 1024; i++) {
        char path[32];
        sprintf(path, "/home/%d", i);
        char reference_cache_path[32];
        sprintf(reference_cache_path, "%d", i);
        test_dirtab_get_cache(path, reference_cache_path, 3);
    }
    
    test_dirtab_get_cache("/home/xxx123", "1024", 3);
       
    fprintf(stdout, "storing dirtab...\n");
    fflush(stdout);
    assert_true(dirtab_flush());
}

static void test_array() {
    
   array a;
   array_init(&a, 4);
   assert_true(array_size(&a) == 0);
   
   array_add(&a, "4");
   assert_true(array_size(&a) == 1);
   assert_true(strcmp(array_get(&a, 0), "4") == 0);
   
   array_add(&a, "2");
   assert_true(array_size(&a) == 2);
   assert_true(strcmp(array_get(&a, 0), "4") == 0);
   assert_true(strcmp(array_get(&a, 1), "2") == 0);
   
   array_add(&a, "1");
   assert_true(array_size(&a) == 3);
   assert_true(strcmp(array_get(&a, 0), "4") == 0);
   assert_true(strcmp(array_get(&a, 1), "2") == 0);
   assert_true(strcmp(array_get(&a, 2), "1") == 0);
   
   array_add(&a, "3");
   assert_true(array_size(&a) == 4);
   assert_true(strcmp(array_get(&a, 0), "4") == 0);
   assert_true(strcmp(array_get(&a, 1), "2") == 0);
   assert_true(strcmp(array_get(&a, 2), "1") == 0);
   assert_true(strcmp(array_get(&a, 3), "3") == 0);
   
   int string_comparator (const void *element1, const void *element2) {
       const char *str1 = *((char**)element1);
       const char *str2 = *((char**)element2);
       int res = strcmp(str1, str2);
       return res;
   }
   array_qsort(&a, string_comparator);

   assert_true(strcmp(array_get(&a, 0), "1") == 0);
   assert_true(strcmp(array_get(&a, 1), "2") == 0);
   assert_true(strcmp(array_get(&a, 2), "3") == 0);
   assert_true(strcmp(array_get(&a, 3), "4") == 0);
   
   array_add(&a, "a");
   array_add(&a, "x");
   array_add(&a, "b");
   array_add(&a, "y");
   array_add(&a, "c");
   array_add(&a, "z");
   array_qsort(&a, string_comparator);

   const void *finder(const void *element, void* arg) {
       const char *p = element;
       if (strcmp(p, arg) == 0) {
           return p;
       }
       return NULL;
   }
   
   assert_true(strcmp(array_iterate(&a, finder, "z"), "z") == 0);
}

int main(int argc, char** argv) {
    test_array();
    test_list();
    test_dirtab_1();
    test_dirtab_2();
    return (EXIT_SUCCESS);
}

