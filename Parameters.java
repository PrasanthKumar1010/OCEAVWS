package utils;

import org.workflowsim.planning.BasePlanningAlgorithm;
import planning.*;

public interface Parameters {
    double deadline = 1000000;
    int num_vms = 20;
    String dax_path = "C:resources\\dax\\Montage_1000.xml";
    BasePlanning planningAlgorithm = new Start();
}
