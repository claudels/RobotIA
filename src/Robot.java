import java.util.ArrayList;

public class Robot {
	private int id;
	
	private double vMoy;
	
	private Zone affectedZone = null;
	
	private ArrayList<RobotListener> listeners;
	
	private Robot lastRobotPermuted;
	
	/*
	 * constructeur du robot
	 */
	public Robot(int id, double vMoy) {
		this.id = id;
		this.vMoy = vMoy;
		this.listeners = new ArrayList<RobotListener>();
	}
	
	
	/**
	 *  constructeur du robot
	 * 
	 * @param id identifiant du robot
	 * @param vMoy vitese moyenne associé au robot
	 * @param zone zone dans laquelle se trouve le robot
	 */
	public Robot(int id, double vMoy, Zone zone) {
		this(id, vMoy);
		this.affectedZone = zone;
	}
	
	/*
	 * retourne l'ID du robot
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * retourne la zone dans laquelle se trouve le robot
	 */
	public Zone getAffectedZone() {
		return affectedZone;
	}
	
	/*
	 * retourne la vitesse moyenne du robot
	 */
	public double getVMoy() {
		return vMoy;
	}
	
	/**
	 * écouteur du robot
	 */
	public void addRobotListener(RobotListener listener){
		this.listeners.add(listener);
	}
	
	/**
	 * Changement d'affectation de zone d'un robot
	 */
	public void setAffectedZone(Zone newZone) {
		this.affectedZone = newZone;
		this.addRobotListener(newZone);
		this.listeners.forEach(robotListener -> robotListener.zoneChanged(this));
	}
	
	/**
	 * permutation d'un robot avec un autre
	 */
	public void permute(Robot robot){
		this.lastRobotPermuted = robot;
		Zone lastZone = robot.getAffectedZone();
		robot.setAffectedZone(this.getAffectedZone());
		this.setAffectedZone(lastZone);
	}
	
	/*
	 * Annulation de la permutation si celle-ci n'améliore pas l'écart type
	 */
	public void cancelPermute(){
		this.permute(this.lastRobotPermuted);
	}
	
	@Override
	public String toString() {
		return "ID : " + this.id + " VMOY : " + this.vMoy + "\n";
	}
	
}
