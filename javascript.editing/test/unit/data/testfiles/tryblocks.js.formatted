function myfunc() {
    try { 
        in_try_block();
    } catch ( e if e == "InvalidNameException"  ) { 
        in_first_catch(); 
    } catch ( e if e == "InvalidIdException"    ) { 
        in_second_catch(); 
    } catch ( e if e == "InvalidEmailException" ) { 
        in_third_catch(); 
    } catch ( e ) {
        in_default_catch(); 
    } finally {
        in_finally();
    }
}
   

// Simple - no catch
try {
    in_try_block_2();
} finally {
    in_finally_2();
}

try {
    in_try_block_2();
} catch(e) {
    in_catch();
}

// Empty blocks
try {
} catch(e) {
} finally {
}


