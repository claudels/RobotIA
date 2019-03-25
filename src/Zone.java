import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Zone implements RobotListener{
	private int idZone;
	
	private ArrayList<Robot> robots;
	
	public Zone(int idZone) {
		this.idZone = idZone;
		this.robots = new ArrayList<Robot>();
	}

	@Override
	public void zoneChanged(Robot robot) {
		if(robots.contains(robot) && robot.getAffectedZone() != this)
			this.robots.remove(robot);
		if(!robots.contains(robot) && robot.getAffectedZone() == this)
			this.robots.add(robot);
	}
	
	public double getVmoySum(){
		return this.robots.stream().mapToDouble(robot -> robot.getVMoy()).sum();
	}
	
	public double getMoyenneVitesses(){
		return this.getVmoySum() / (double)(this.robots.size());
	}
	
	public int countRobots(){
		return this.robots.size();
	}
	
	public int getIdZone() {
		return idZone;
	}
	
	public ArrayList<Robot> getRobots() {
		return robots;
	}
	
	public static double calculerEcartType(ArrayList<Zone> zones){
		double sum = 0.0, standardDeviation = 0.0;
        int length = zones.size();
		
		for(Zone zone : zones){
			sum += zone.getMoyenneVitesses();
		}
		
		double mean = sum/(double)length;
		
		for(Zone zone: zones) {
            standardDeviation += Math.pow(zone.getMoyenneVitesses() - mean, 2);
        }

        return Math.sqrt(standardDeviation/(double)length);
	}
	
	public static void affectationAleatoire(ArrayList<Zone> zones, ArrayList<Robot> robots){
		Random rand = new Random();
		
		//Affectation aleatoire
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(rand.nextInt(zones.size())));
		}
		
	}
	
	public static void affectionSequentielle(LinkedList<Zone> zones, LinkedList<Robot> robots){
		
		//Affection séquentielle
		int curseur = 0;
		
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(curseur));
			curseur = (curseur == zones.size()-1) ? 0 : curseur + 1;
		}
		
	}
}
