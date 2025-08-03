package utils;

import org.workflowsim.Task;
import planning.BasePlanning;

import java.util.*;
import java.util.ArrayList;

public class  Vulture{  
	
	public int[] position;
	public double fitness;
	public double cost;
	public double makespan;
	public ArrayList<Vulture> neighbor;
	
	
	public Vulture(int dim) {
		this.position = new int[dim];
		this.fitness=-1;
		this.cost=0.0;
		this.makespan=0.0;
		neighbor = new ArrayList<>();
        
    }
}
