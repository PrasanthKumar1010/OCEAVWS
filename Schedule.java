package utils;

import org.workflowsim.Task;
import java.util.*;
import java.util.ArrayList;

public class Schedule {
    public List <Integer> resources;
    public List<Event> mappings;
    public double TEC;
    public double TET;
  

    public Schedule() {
        this.resources = new ArrayList<>();
        this.mappings = new ArrayList<>();
        this.TEC = 0.0;
        this.TET = 0.0;
        
    }

  
}
