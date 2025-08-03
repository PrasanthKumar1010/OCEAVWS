package planning;

import org.apache.commons.math3.special.Gamma;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.workflowsim.Task;
import org.workflowsim.CondorVM;
import vm.CustomVM;
import vm.CustomVMGenerator;
import java.text.DecimalFormat;
import java.util.*;
import utils.Event;
import utils.Vulture;
import utils.Schedule;

public abstract class Swarm extends BasePlanning {

    public int dim; // number of tasks
    public int numOfVulture = 100; // population size
    public Vulture first_best;
    public Vulture second_best;
    public double gama = 0.5;
    public Vulture[] population = new Vulture[numOfVulture];
    public Vulture[] population_dlh = new Vulture[numOfVulture];
    public double[] radius = new double[numOfVulture];

    private List<Cloudlet> cloudletList;

    private double randomDouble(double min, double max) {
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }
public Vulture obl(Vulture p) {
        Vulture com_i = new Vulture(dim);
        for (int j = 0; j < dim; j++) {
            com_i.position[j] = getVmList().size() - 1 - p.position[j];
        }
        Schedule schedule_Vulture = generate_schedule(com_i);
        com_i.fitness = 1 / (gama * (schedule_Vulture.TET) + (1 - gama) * (schedule_Vulture.TEC));
        com_i.cost = schedule_Vulture.TEC;
        com_i.makespan = schedule_Vulture.TET;
        return com_i.fitness > p.fitness ? com_i : p;
    }
 public Vulture drobl(Vulture p) {
        Random random = new Random();
        Vulture com_i = new Vulture(dim);
        double[] Xr = new double[dim];
        for (int j = 0; j < dim; j++)
            Xr[j] = 1;
        for (int j = 0; j < dim; j++) {
            double rd = Math.random();
            if (rd > 0.5) {
                com_i.position[j] = getVmList().size() - 1 - p.position[j];
                Xr[j] = random.nextDouble();
            } else {
                com_i.position[j] = p.position[j];
            }
        }
        for (int j = 0; j < dim; j++) {
            com_i.position[j] = ((int) (Math.round(Xr[j] * com_i.position[j]))) % getVmList().size();
        }
        Schedule schedule_Vulture = generate_schedule(com_i);
        com_i.fitness = 1 / (gama * (schedule_Vulture.TET) + (1 - gama) * (schedule_Vulture.TEC));
        com_i.cost = schedule_Vulture.TEC;
        com_i.makespan = schedule_Vulture.TET;
        return com_i.fitness > p.fitness ? com_i : p;
    }
public Vulture drquasi(Vulture p) {
        Random random = new Random();
        Vulture com_i = new Vulture(dim);
        double[] Xr = new double[dim];
        for (int j = 0; j < dim; j++)
            Xr[j] = 1;
        for (int j = 0; j < dim; j++) {
            double rd = Math.random();
            if (rd > 0.5) {
                double C = (getVmList().size() - 1) / 2.0;
                com_i.position[j] = getVmList().size() - 1 - p.position[j];
                double val = com_i.position[j];
                double minVal = Math.min(val, C);
                double maxVal = Math.max(val, C);
                double X = randomDouble(minVal, maxVal);
                com_i.position[j] = (int) Math.round(random.nextDouble() * X);
                com_i.position[j] = com_i.position[j] % getVmList().size();
            } else {
                com_i.position[j] = p.position[j];
            }
        }
        Schedule schedule_Vulture = generate_schedule(com_i);
        com_i.fitness = 1 / (gama * (schedule_Vulture.TET) + (1 - gama) * (schedule_Vulture.TEC));
        com_i.cost = schedule_Vulture.TEC;
        com_i.makespan = schedule_Vulture.TET;
        return com_i.fitness > p.fitness ? com_i : p;
    }
public Vulture quasiref(Vulture p) {
        Random random = new Random();
        Vulture com_i = new Vulture(dim);
       for (int j = 0; j < dim; j++) {
            double C = (getVmList().size() - 1) / 2.0;
            double val = com_i.position[j];
            double minVal = Math.min(val, C);
            double maxVal = Math.max(val, C);
            double X = randomDouble(minVal, maxVal);
            com_i.position[j] = (int) Math.round(X);
        }
        Schedule schedule_Vulture = generate_schedule(com_i);
        com_i.fitness = 1 / (gama * (schedule_Vulture.TET) + (1 - gama) * (schedule_Vulture.TEC));
        com_i.cost = schedule_Vulture.TEC;
        com_i.makespan = schedule_Vulture.TET;
        return com_i.fitness > p.fitness ? com_i : p;
    }
public void create_vulture() {
        dim = getTaskList().size();
        first_best = new Vulture(dim);
        second_best = new Vulture(dim);
        for (int i = 0; i < numOfVulture; i++) {
            Vulture c_rand = new Vulture(dim);
            generate(c_rand);
            population[i] = c_rand;
            Random random = new Random();
            int r = random.nextInt(1, 5);
            switch (r) {
                case 1:
                    population[i] = drobl(population[i]);
                    break;
                case 2:
                    population[i] = obl(population[i]);
                    break;
                case 3:
                    population[i] = drquasi(population[i]);
                    break;
                case 4:
                    population[i] = quasiref(population[i]);
                    break;
            }
        }
    }
public void generate(Vulture vulture) {
        Random random = new Random();
        for (int i = 0; i < dim; i++) {
            int assigned_vm = random.nextInt(getVmList().size());
            vulture.position[i] = assigned_vm;
        }
        Schedule schedule_particle = generate_schedule(vulture);
        vulture.fitness = 1 / (gama * (schedule_particle.TET) + (1 - gama) * (schedule_particle.TEC));
        vulture.cost = schedule_particle.TEC;
        vulture.makespan = schedule_particle.TET;
    }
public Schedule generate_schedule(Vulture particle) {
        Schedule schedule = new Schedule();
        int p_size = getTaskList().size();
        List<Task> tasklist = getTaskList();
        Map<Task, Double> ET = new HashMap<>();
        Map<Integer, Double> LET = new HashMap<>();
        Map<Integer, Double> LST = new HashMap<>();
        Map<Task, Integer> task_no = new HashMap<>();
        int k = 0;
        for (Task task : getTaskList()) {
            task_no.put(task, k);
            k++;
        }
        for (CustomVM vm : getVmList()) {
            LET.put(vm.getId(), 0.0);
            LST.put(vm.getId(), 0.0);
        }
        for (Task task : getTaskList()) {
            ET.put(task, 0.0);
        }
        double boot_time = 0.0;
        for (int i = 0; i < p_size; i++) {
            Task t = tasklist.get(i);
            double start_time = 0.0;
            if (t.getParentList().size() != 0) {
                double max_ET = 0.0;
                for (Task parent_task : t.getParentList()) {
                    max_ET = Math.max(max_ET, ET.get(parent_task));
                }
                start_time = Math.max(LET.get(particle.position[i]), max_ET);
            }
            double exec = computationCosts.get(t).get(particle.position[i]);
            double transfer = 0.0;
            for (Task child_task : t.getChildList()) {
                if (particle.position[task_no.get(child_task)] != particle.position[i]) {
                    transfer += transferCosts.get(t).get(child_task);
                }
            }
            ET.remove(t);
            ET.put(t, exec + transfer + start_time);
            Event event = new Event(t, particle.position[i], start_time, ET.get(t));
            schedule.mappings.add(event);
            if (!schedule.resources.contains(particle.position[i])) {
                LST.put(particle.position[i], Math.max(start_time, boot_time));
                schedule.resources.add(particle.position[i]);
            }
            LET.put(particle.position[i],
                    exec + transfer + Math.max(Math.max(start_time, boot_time), LET.get(particle.position[i])));
        }
        for (Task task1 : getTaskList()) {
            schedule.TET = Math.max(schedule.TET, ET.get(task1));
        }
        double starting = Double.MAX_VALUE;
        int x = 0;
        for (Task task1 : getTaskList()) {
            starting = Math.min(schedule.mappings.get(x).start_time, starting);
            x++;
        }
        schedule.TET -= starting;
        for (int i = 0; i < schedule.resources.size(); i++) {
            schedule.TEC += (getVmList().get(schedule.resources.get(i)).getCost())
                    * (LET.get(getVmList().get(schedule.resources.get(i)).getId())
                            - LST.get(getVmList().get(schedule.resources.get(i)).getId()));
        }
        return schedule;
    }


private double computeLevy() {
        Random random = new Random();
        double b = 1.5;
        double u = random.nextDouble();
        double v = random.nextDouble();
        double gamma1 = Gamma.gamma(1 + b);
        double gamma2 = Gamma.gamma((1 + b) / 2);
        double val = (gamma1 * Math.sin(Math.PI * b / 2)) / (gamma2 * b * Math.pow(2, (b - 1) / 2));
        double sigma = Math.pow(val, (1 / b));
        return (0.01 * u * sigma) / Math.pow(v, 1 / b);
    }


public List<Cloudlet> exec(List<Task> tasklist) {
        List<Cloudlet> newList = new ArrayList<>();
        Log.printLine("Starting Hi CloudSim...");
        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);
            List<Host> hostList = new ArrayList<>();
            List<Pe> peList = new ArrayList<>();
            int mips = 1000 * 12;
            peList.add(new Pe(0, new PeProvisionerSimple(mips)));
            int hostId = 0;
            int ram = 12288;
            long storage = 1000000;
            int bw = 10000;
            hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
                    new VmSchedulerTimeShared(peList)));
            String arch = "x86";
            String os = "Linux";
            String vmm = "Xen";
            double time_zone = 10.0;
            double cost = 3.0;
            double costPerMem = 0.05;
            double costPerStorage = 0.001;
            double costPerBw = 0.0;
            LinkedList<Storage> storageList = new LinkedList<>();
            DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList,
                    time_zone, cost, costPerMem, costPerStorage, costPerBw);
            Datacenter datacenter = new Datacenter("Datacenter_0", characteristics, new VmAllocationPolicySimple(hostList),
                    storageList, 0);
            System.out.println("Success!! DatacenterCreator is executed!!");
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();
            List<CustomVM> vmlist0 = CustomVMGenerator.createCustomVMs(brokerId, utils.Parameters.num_vms);
            broker.submitVmList(vmlist0);
            cloudletList = new ArrayList<>();
            for (Task task : tasklist) {
                int id = task.getCloudletId();
                long length = task.getCloudletLength();
                long fileSize = task.getCloudletFileSize();
                long outputSize = task.getCloudletOutputSize();
                UtilizationModel utilizationModel = task.getUtilizationModelCpu();
                int pesNumber = task.getNumberOfPes();
                Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel,
                        utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                cloudlet.setVmId(task.getVmId());
                cloudletList.add(cloudlet);
            }
            broker.submitCloudletList(cloudletList);
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
            newList = broker.getCloudletReceivedList();
            Log.printLine("Hi CloudSim finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
        return newList;
    }

    private DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }
}

