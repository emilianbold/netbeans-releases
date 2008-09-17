
void foo1(int i) {
    goto label;    
    label: i++;    
}

void foo2(int i) {
    goto label;    
    if(true) {
        label: i++;
    }    
}

void foo3(int i) {
    goto label;    
    if(true) {
        i++;
    } else {
        label: i++;
    }   
}

void foo4(int i) {
    goto label;    
    for(;;) {
        label: i++;
    }   
}

void foo5(int i) {
    goto label;    
    while(true) {
        label: i++;
    }   
}

void foo6(int i) {
    goto label;    
    do {
        label: i++;
    } while(true);
}

void foo7(int i) {
    goto label;    
    {
        label: i++;
    }  
}

void foo8(int i) {
    switch (i) {
        case 1:
            label: i++;
            break;
        case 2:
            goto label; 
            break;
    }
}

