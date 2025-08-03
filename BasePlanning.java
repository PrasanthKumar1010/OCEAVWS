

package planning;

import org.cloudbus.cloudsim.Consts;
import org.workflowsim.FileItem;
import org.workflowsim.Task;
import org.workflowsim.planning.BasePlanningAlgorithm;
import org.workflowsim.utils.Parameters;
import utils.Event;

import vm.CustomVM;

import java.util.*;

public abstract class BasePlanning extends BasePlanningAlgorithm {

    public Map<Task, Map<Integer, Double>> computationCosts;
    public Map<Task, Map<Task, Double>> transferCosts;
    public double averageBandwidth;
    public HashMap<Integer, CustomVM> id_vms;
    private List<CustomVM> vms;
    public Map<CustomVM, List<Event>> schedules;
  	
	public double time_constraint=100000;
	
    public BasePlanning() {
        computationCosts = new HashMap<>();
        transferCosts = new HashMap<>();
       
        
    }

   
    public double calculateAverageBandwidth() {
        double avg = 0.0;
        for (CustomVM vm : getVmList()) {
            avg += vm.getBw();
        }
        return avg / getVmList().size();
    }

    
    public void resetSchedules() {
        schedules.clear();
        for (CustomVM vm : getVmList()) {
            schedules.put(vm, new ArrayList<>());
        }
    }
    
    
    public void calculateComputationCosts() {
        for (Task task : getTaskList()) {
            Map<Integer, Double> costsVm = new HashMap<>();
            for (CustomVM vm : getVmList()) {
                if (vm.getNumberOfPes() < task.getNumberOfPes()) {
                    costsVm.put(vm.getId(), Double.MAX_VALUE);
                } else {
                    costsVm.put(vm.getId(),
                            task.getCloudletTotalLength() / vm.getMips());
                }
            }
            computationCosts.put(task, costsVm);
        }
    }

    
    public void calculateTransferCosts() {
       
        for (Task task1 : getTaskList()) {
            Map<Task, Double> taskTransferCosts = new HashMap<>();
            for (Task task2 : getTaskList()) {
                taskTransferCosts.put(task2, 0.0);
            }
            transferCosts.put(task1, taskTransferCosts);
        }

    
        for (Task parent : getTaskList()) {
            for (Task child : parent.getChildList()) {
                transferCosts.get(parent).put(child,
                        calculateTransferCost(parent, child));
            }
        }
    }

   
    public double calculateTransferCost(Task parent, Task child) {
        List<FileItem> parentFiles = parent.getFileList();
        List<FileItem> childFiles = child.getFileList();

        double acc = 0.0;

        for (FileItem parentFile : parentFiles) {
            if (parentFile.getType() != Parameters.FileType.OUTPUT) {
                continue;
            }

            for (FileItem childFile : childFiles) {
                if (childFile.getType() == Parameters.FileType.INPUT
                        && childFile.getName().equals(parentFile.getName())) {
                    acc += childFile.getSize();
                    break;
                }
            }
        }


        
        acc = acc / Consts.MILLION;
        return acc * 8 / averageBandwidth;
    }

    
    @Override
    public void setVmList(List vmList) {
        this.vms = new LinkedList<>();
        this.id_vms = new HashMap<>();
        for(Object vm: vmList) {
            vms.add((CustomVM) vm);
            id_vms.put(((CustomVM) vm).getId(), (CustomVM) vm);
        }
    }

    @Override
    public List<CustomVM> getVmList() {
        return vms;
    }


   
}
