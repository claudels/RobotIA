import java.util.ArrayList;

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
	
	public ArrayList<Robot> getRobots() {
		return robots;
	}
}
