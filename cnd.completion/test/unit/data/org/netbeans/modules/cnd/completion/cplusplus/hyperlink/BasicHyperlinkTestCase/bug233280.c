#define GOTO_MACRO(statement) \
if (!statement) { \
    goto done; \
} \

int myFunction() {
    
    return 1;
}
/*
 * 
 */
int main(int argc, char** argv) {

    /* If we click on the function to go to definition, it works correctly */
    myFunction();
    
    /* If we click on the function to go the definition, it will go to done 
     * Indeed, if we position the mouse over the function call, ww'll see the
     * tool tip pointing to "label done" instead of to "int myFunction()", as
     * occurs in the previous call.
     */
    GOTO_MACRO(myFunction());
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 done:
    
    return 0;
}
