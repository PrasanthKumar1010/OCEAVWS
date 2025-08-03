package planning;

import org.cloudbus.cloudsim.Log;
import org.apache.commons.math3.special.Gamma;
import org.cloudbus.cloudsim.Cloudlet;
import org.workflowsim.Task;
import utils.Metrics;
import utils.Parameters;
import utils.Vulture;
import vm.CustomVM;
import utils.Schedule;

import java.util.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * Enhanced African Vultures Optimization Algorithm with Dimension Learning-based Hunting (DLH)
 */
public class Start extends Metrics {
    // AVOA parameters
    public int populationSize = 100;
    public int maxIterations = 200;
    public double L1 = 0.8, L2 = 0.2;      // probabilities for best vultures
    public double P1 = 0.6;                // exploration strategy selector
    public double P2 = 0.4, P3 = 0.6;      // exploitation strategy selectors
    public double w = 2.5;                 // phase disruption parameter
    public double beta = 1.5;              // Levy flight exponent

    public Vulture bestVulture1, bestVulture2;
    public Random random = new Random();
    double a=4;

    @Override
    public void run() {
         Log.printLine("OCEAVWS planner started with " + getTaskList().size() + " tasks and " + getVmList().size() + " VMs.");
        calculateAverageBandwidth();
        calculateComputationCosts();
        calculateTransferCosts();
        create_vulture();
        updateBestTwo();
        double[] l_t=new double[maxIterations+1];
        l_t[0]=random.nextDouble();
        while(l_t[0]==0.0 || l_t[0]==0.25 || l_t[0]==0.5 || l_t[0]==0.75 || l_t[0]==1.0)l_t[0]=random.nextDouble();
        for (int iter = 1; iter <= maxIterations; iter++) {
        	l_t[iter]=a*l_t[iter-1]*(1-l_t[iter-1]);
        	System.out.println(l_t[iter]);
            System.out.println("--------------------------- Epoch " + iter +  "-------------------------");
        	Vulture[] updated_population=new Vulture[populationSize];
            for (int i = 0; i < populationSize; i++) {
                Vulture curr = population[i];
                double k= random.nextDouble()*4-2;
                double z = k * (Math.pow(Math.sin((Math.PI/2.0) * iter/maxIterations),w)
                              + (Math.cos((Math.PI/2.0) * iter/maxIterations)) - 1);
                double h = random.nextDouble() * 2 - 1;
                double Y = (2*random.nextDouble()+1) * h * (1 - (double)iter/maxIterations) + z;
                Vulture candAVOA = canonicalAVOA(curr, Y, l_t[iter]);
                Vulture candDLH = dlhCandidate(curr, candAVOA);
                Vulture chosen = (candAVOA.fitness > candDLH.fitness) ? candAVOA : candDLH;
                updated_population[i]=(chosen.fitness>population[i].fitness)? chosen: population[i];
            }
            for(int i=0;i<populationSize;i++) {
            	population[i]=updated_population[i];
            }
        }
        Random random = new Random();
        int r = random.nextInt(1, 5);
        switch (r) {
            case 1:
                bestVulture1 = drobl(bestVulture1);
                break;
            case 2:
                bestVulture1 = obl(bestVulture1);
                break;
            case 3:
                bestVulture1 = drquasi(bestVulture1);
                break;
            case 4:
                bestVulture1 = quasiref(bestVulture1);
                break;
        }
        int idx = 0;
        for (Task task : getTaskList()) {
            task.setVmId(bestVulture1.position[idx++]);
        }
        List<Cloudlet> jobs = exec(getTaskList());
		List<CustomVM> vms = getVmList();
                printMetrics(jobs, vms);
                double makespan = getMKSP(jobs);
		double cost = getCost(getVmList(), jobs);
	        double utilization = getUtil(jobs, getVmList());

        System.out.println("---------------------------Metrics-------------------------");
    	System.out.println("Makespan: " + makespan);
        System.out.println("Cost: " + cost);
        System.out.println("Resource Utilization: " + utilization);
    	}
    public Vulture canonicalAVOA(Vulture curr, double Y , double l_t) {
        Vulture newVulture = copyVulture(curr);
        double absY = Math.abs(Y);
        int[] Pi = curr.position;
        Vulture R = (random.nextDouble() < fitnessProbability(curr)) ? bestVulture1 : bestVulture2;
        Vulture worst = population[0];
        for (Vulture p : population) {
            if (p.fitness < worst.fitness) {
                worst = p;
            }
        }

        if (absY >= 1) {
            if (P1 >= l_t) {
                    for (int d = 0; d < Pi.length; d++) {
                    double D =Math.abs(2*random.nextDouble()*(R.position[d]) - Pi[d]);
                    newVulture.position[d] = (int) Math.round(R.position[d] - D * Y);
                }
            } else {
                int lb = 0, ub = getVmList().size() - 1;
                for (int d = 0; d < Pi.length; d++) {
                    double rand2 = random.nextDouble();
                    double rand3 = random.nextDouble();
                    double jump = rand2 * ((ub - lb) * rand3 + lb);
                    newVulture.position[d] = (int) Math.round(R.position[d] - Y + jump);
                }
            }
        } else {
            if (absY >= 0.5) {
                if (P2 >= l_t) {
                        for (int d = 0; d < Pi.length; d++) {
                        double Db = Math.abs(R.position[d] - Pi[d]);
                        double D =Math.abs(2*random.nextDouble()*(R.position[d]) - Pi[d]);
                        double rand4=random.nextDouble();
                        newVulture.position[d] = (int) Math.round(D * (Y+rand4) - Db);
                    }
                } else {
                       for (int d = 0; d < Pi.length; d++) {
                    	double rand5=random.nextDouble();
                    	double rand6=random.nextDouble();
                        double S1=R.position[d]*((rand5*curr.position[d])/(2*Math.PI))*Math.cos(curr.position[d]);
                        double S2=R.position[d]*((rand6*curr.position[d])/(2*Math.PI))*Math.sin(curr.position[d]);
                        newVulture.position[d] = (int) Math.round(R.position[d] - (S1+S2));
                    }
                }
            } else {
                    if (P3 >= l_t) {
                    for (int d = 0; d < Pi.length; d++) {
                    double A1=bestVulture1.position[d]-((bestVulture1.position[d]*Pi[d])/(bestVulture1.position[d]-Math.pow(Pi[d], 2)))*Y;
                    double A2=bestVulture2.position[d]-((bestVulture2.position[d]*Pi[d])/(bestVulture2.position[d]-Math.pow(Pi[d], 2)))*Y;
                    newVulture.position[d] = (int) Math.round((A1+A2)/2);
                    }
                } else {

                    for (int d = 0; d < Pi.length; d++) {
                        newVulture.position[d]= (int)Math.round(R.position[d] - Math.abs(R.position[d] -Pi[d]) * Y * computeLevy());               
                    }
                }
            }
        } 
        evaluateVulture(newVulture);
        if(newVulture.fitness>curr.fitness)
        	return newVulture;
        else
        	return curr;
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
    public Vulture dlhCandidate(Vulture curr, Vulture avoaVulture) {
        Vulture dlh = copyVulture(curr);
        double R_i = euclidean(curr.position, avoaVulture.position);
        List<Vulture> neighbors = new ArrayList<>();
        for (Vulture p : population) {
            if (p != curr && euclidean(curr.position, p.position) <= R_i)
                neighbors.add(p);
        }
        if (!neighbors.isEmpty()) {
            Vulture Pn = neighbors.get(random.nextInt(neighbors.size()));
            Vulture Pr = population[random.nextInt(populationSize)];
            for (int d = 0; d < curr.position.length; d++) {
                dlh.position[d] = (int)Math.round(
                    curr.position[d] + random.nextDouble() * (Pn.position[d] - Pr.position[d])
                );
            }
            evaluateVulture(dlh);
        }
        return dlh;
    }
 private void updateBestTwo() {
        Vulture first = population[0];
        Vulture second = population[1];
        for (Vulture p : population) {
            if (p.fitness > first.fitness) {
                second = first;
                first = p;
            } else if (p.fitness > second.fitness && p != first) {
                second = p;
            }
        }
        bestVulture1 = first;
        bestVulture2 = second;
    }

public double fitnessProbability(Vulture p) {
        return p.fitness / Arrays.stream(population).mapToDouble(q -> q.fitness).sum();
    }
public double euclidean(int[] a, int[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) sum += Math.pow(a[i]-b[i],2);
        return Math.sqrt(sum);
    }
public Vulture copyVulture(Vulture p) {
        Vulture c = new Vulture(p.position.length);
        System.arraycopy(p.position, 0, c.position, 0, p.position.length);
        c.fitness = p.fitness;
        return c;
    }
public void evaluateVulture(Vulture p) {
        Schedule schedule = generate_schedule(p);
        p.fitness = 1/(gama * (schedule.TET) + (1-gama)*(schedule.TEC));
        p.cost = schedule.TEC;
        p.makespan = schedule.TET;
    }
}

