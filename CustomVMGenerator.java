package vm;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.workflowsim.CondorVM;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class CustomVMGenerator {
    private static List<CondorVM> createCondorVM(int userId, int vms) {

        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<CondorVM> list = new LinkedList<>();

        //VM utils.Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 1000;
        long bw = 20;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs
        CondorVM[] vm = new CondorVM[vms];
        Random bwRandom = new Random(System.currentTimeMillis());
        for (int i = 0; i < vms; i++) {
            double ratio = 0.5;
            //double ratio = bwRandom.nextDouble();
            vm[i] = new CondorVM(i, userId, mips * ratio, pesNumber, ram, (long) (bw), size, vmm, new CloudletSchedulerSpaceShared());
            list.add(vm[i]);
        }
        return list;
    }
    /**
     * Adds parameters like cost, freq, voltage based on type to the given condorvm
     * to return CustomVM
     */
    private static CustomVM getVm(int type, CondorVM vm) {
    	double pesNumber,memory,bandwidth ,mips,cost,maxFreq,minFreq,minVoltage,maxVoltage,lambda;
        double costPerMem = 0.05;		// the cost of using memory in this vm
        double costPerStorage = 0.1;	// the cost of using storage in this vm
        double costPerBw = 0.1;
        
        if(type==0){
        	pesNumber=2;
        	memory=3.75;//GiB
        	bandwidth=500; //Mbps
            cost=0.116;
            mips=1000;
            maxFreq=1.0;
            minFreq=0.50;
            minVoltage=5.0;
            maxVoltage=7.0;
            lambda=0.000150;
        }
        else if(type==1){
        	pesNumber=4;
        	memory=7.5;//GiB
        	bandwidth=750; //Mbps
            cost=0.232;
            mips=2000;
            maxFreq=2.0;
            minFreq=1.00;
            minVoltage=9.0;
            maxVoltage=11.0;
            lambda=0.000080;
        }

        else if(type==2){
        	pesNumber=8;
        	memory=15;//GiB
        	bandwidth=1000; //Mbps
            cost=0.464;
            mips=3000;
            maxFreq=3.0;
            minFreq=1.50;
            minVoltage=13;
            maxVoltage=15;
            lambda=0.000040;
        }

        else if(type==3){
        	pesNumber=36;
        	memory=60;//GiB
        	bandwidth=4000; //Mbps
            cost=1.856;
            mips=5000;
            maxFreq=5.0;
            minFreq=2.50;
            minVoltage=21;
            maxVoltage=23;
            lambda=0.000002;
        	
        }

        else if (type==4) {
        	pesNumber=16;
        	memory=30;//GiB
        	bandwidth=2000; //Mbps
            cost=0.928;
            mips=4000;
            maxFreq=4.0;
            minFreq=2.00;
            minVoltage=17;
            maxVoltage=19;
            lambda=0.000010;
        }
        else if(type==5){
        	pesNumber=2;
        	memory=3.75;//GiB
        	bandwidth=500; //Mbps
            cost=0.116;
            mips=1000;
            maxFreq=1.0;
            minFreq=0.50;
            minVoltage=5.0;
            maxVoltage=7.0;
            lambda=0.000150;
        }
        else if(type==6){
        	pesNumber=4;
        	memory=7.5;//GiB
        	bandwidth=750; //Mbps
            cost=0.232;
            mips=2000;
            maxFreq=2.0;
            minFreq=1.00;
            minVoltage=9.0;
            maxVoltage=11.0;
            lambda=0.000080;
        }

        else if(type==7){
        	pesNumber=8;
        	memory=15;//GiB
        	bandwidth=1000; //Mbps
            cost=0.464;
            mips=3000;
            maxFreq=3.0;
            minFreq=1.50;
            minVoltage=13;
            maxVoltage=15;
            lambda=0.000040;
        }

        else if(type==8){
        	pesNumber=36;
        	memory=60;//GiB
        	bandwidth=4000; //Mbps
            cost=1.856;
            mips=5000;
            maxFreq=5.0;
            minFreq=2.50;
            minVoltage=21;
            maxVoltage=23;
            lambda=0.000002;
        	
        }

        else {
        	pesNumber=16;
        	memory=30;//GiB
        	bandwidth=2000; //Mbps
            cost=0.928;
            mips=4000;
            maxFreq=4.0;
            minFreq=2.00;
            minVoltage=17;
            maxVoltage=19;
            lambda=0.000010;
        }
        //TODO: try removing the following 2 lines
        maxVoltage = 30;
        minVoltage = 15;
        CustomVM cvm = new CustomVM(vm,cost,costPerMem, costPerStorage, costPerBw, minFreq, maxFreq, minVoltage, maxVoltage, lambda);
        return cvm;
    }


    /**
     * creates custom vms 
     * also look at getVm() function
     * @param userid
     * @param vms
     * @return
     */
    
   
 public static List<CustomVM> createCustomVMs(int userid, int vms){
        List<CustomVM> list = new ArrayList<>();

        //First create regular CondorVMs
        List<CondorVM> list0 = createCondorVM(userid, vms);

        CustomVM cvm;
        int type = 0;//type of vm
        for(CondorVM vm : list0) {
            type %= 10; //max number of types
            cvm = getVm(type, vm);
           type++;
            list.add(cvm);
        }
        return list;
    }
    
}
