import java.util.ArrayList;

public class Robot {
	private int id;
	
	private double vMoy;
	
	private Zone affectedZone = null;
	
	private ArrayList<RobotListener> listeners;
	
	public Robot(int id, double vMoy) {
		this.vMoy = vMoy;
		this.listeners = new ArrayList<RobotListener>();
	}
	
	public Robot(int id, double vMoy, Zone zone) {
		this(id, vMoy);
		this.affectedZone = zone;
	}
	
	public int getId() {
		return id;
	}
	
	public Zone getAffectedZone() {
		return affectedZone;
	}
	
	public double getVMoy() {
		return vMoy;
	}
	
	public void addRobotListener(RobotListener listener){
		this.listeners.add(listener);
	}
	
	public void setAffectedZone(Zone newZone) {
		this.affectedZone = newZone;
		this.listeners.forEach(robotListener -> robotListener.zoneChanged(this));
	}
	
	@Override
	public String toString() {
		return "ID : " + this.id + " VMOY : " + this.vMoy + "\n";
	}
	
}
