namespace bug244524 {
    typedef struct {
        int bb;
    } test_t_0244524;

    typedef struct {
        int aa;
    } test_t_1244524;

    typedef struct {
        test_t_0244524 t0;
        test_t_1244524 t1;
    } example_t244524;

    example_t244524 example244524[] = { 
        {        
            .t0 = {
                bb : 0
            },
            t1 : {       
                aa : 0   
            }           
        },              
        {               
            t0 : {
                bb : 0
            },        
            .t1 = {       
                aa : 1   
            }           
        },
        {               
            t0 : {
                bb : 0
            },        
            t1 : {       
                aa : 1   
            }           
        }    
    };
}