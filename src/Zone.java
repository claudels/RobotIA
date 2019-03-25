import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Zone implements RobotListener{
	private int idZone;
	
	private LinkedList<Robot> robots;
	
	public Zone(int idZone) {
		this.idZone = idZone;
		this.robots = new LinkedList<Robot>();
	}

	@Override
	public void zoneChanged(Robot robot) {
		if(robots.contains(robot) && robot.getAffectedZone() != this)
			this.robots.remove(robot);
		if(!robots.contains(robot) && robot.getAffectedZone() == this)
			this.robots.add(robot);
	}
	
	public double getVmoySum(){
		return this.robots.parallelStream().mapToDouble(robot -> robot.getVMoy()).sum();
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
	
	public LinkedList<Robot> getRobots() {
		return robots;
	}
	
	public static void doPermutationAleatoire(LinkedList<Zone> zones, int nombrePermutations, LinkedList<Robot> robots){
		Random rand = new Random();
		for(int i=0; i < nombrePermutations; i++){
			Comparator <Zone> comparator = (z1, z2) -> Double.compare(z1.getMoyenneVitesses(), z2.getMoyenneVitesses());
			Zone zoneSource = zones.parallelStream().min(comparator).get();
			
			int indexZoneSource = zones.indexOf(zoneSource);
			int indexZoneDest = indexZoneSource;
			while(indexZoneDest == indexZoneSource){  indexZoneDest = rand.nextInt((zones.size())); }
			
			Comparator <Robot> comparatorRobot = (r1, r2) -> Double.compare(r1.getVMoy(), r2.getVMoy());
			Robot robotSource = zoneSource.getRobots().parallelStream().max(comparatorRobot).get();
			Robot robotDest = zones.get(indexZoneDest).getRobots().parallelStream().min(comparatorRobot).get();
			robotSource.permute(robotDest);
		}
	}
	
	public static double calculerEcartType(LinkedList<Zone> zones){
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
	
	public static void affectationAleatoire(LinkedList<Zone> zones, LinkedList<Robot> robots){
		Logger.getGlobal().log(Level.INFO, "Démarrage affectation aléatoire.");
		Random rand = new Random();
		
		//Affectation aleatoire
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(rand.nextInt(zones.size())));
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation aléatoire, dernier ecart : " + Zone.calculerEcartType(zones))); //Affichage ecart type

	}
	
	public static void affectionSequentielle(LinkedList<Zone> zones, LinkedList<Robot> robots){
		Logger.getGlobal().log(Level.INFO, "Démarrage affectation séquentielle.");
		
		//Affection séquentielle
		int curseur = 0;
		
		//Triage des robots par vitesse moyenne decroissante
		Comparator <Robot> comparatorRobot = (r1, r2) -> Double.compare(r1.getVMoy(), r2.getVMoy());
		robots.sort(comparatorRobot);
		Collections.reverse(robots);
		
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(curseur));
			curseur = (curseur == zones.size()-1) ? 0 : curseur + 1;
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation séquentielle, dernier ecart : " + Zone.calculerEcartType(zones))); //Affichage ecart type

	}
	
	public static void affectationPermutation(double ecartTypeCible, int tempsMaxSeconde, LinkedList<Robot> robots, LinkedList<Zone> zones){
		Logger.getGlobal().log(Level.INFO, "Démarrage affectation permutation.");
		
		//REGLAGES ALGO
		final double toleranceGainEcart = 0.1;
		final int nombrePermutationAleatoire = 1;
		
		//Temps en MS
		int tempsMaxMS = tempsMaxSeconde*1000;
		
		//Affection séquentielle
		Zone.affectionSequentielle(zones, robots);
		
		long startTime = System.currentTimeMillis();
		long permutationsCount = 0;
		double ecartTypeAvantPermut = -1;
		double bestEcartType = 10;
		
		Comparator <Zone> comparator = (z1, z2) -> Double.compare(z1.getMoyenneVitesses(), z2.getMoyenneVitesses());
		while(Zone.calculerEcartType(zones) > ecartTypeCible && ((System.currentTimeMillis() - startTime) < tempsMaxMS)){
			Zone zoneActuelle = zones.parallelStream().min(comparator).get();
			Zone zoneSuivante = zones.parallelStream().max(comparator).get();
			
			double pourcentageAmeliorationEcartType = 1 - (Zone.calculerEcartType(zones) / ecartTypeAvantPermut);
			
			//Si on ne gagne l'écart type ne baisse plus suffisamment, on ajoute de l'aléatoire en faisant des permutations aléatoires
			if(pourcentageAmeliorationEcartType < toleranceGainEcart){
				Zone.doPermutationAleatoire(zones, nombrePermutationAleatoire, robots);
				permutationsCount+=nombrePermutationAleatoire;
				int indexZoneSource = zones.indexOf(zoneSuivante);
				int indexZoneDest = indexZoneSource;
				while(indexZoneDest == indexZoneSource){  indexZoneDest = (new Random()).nextInt((zones.size())); }
			}
			
			ecartTypeAvantPermut = Zone.calculerEcartType(zones);
			
			for(int i = 0; i < zoneActuelle.getRobots().size(); i++){
				double ancienEcartType = Zone.calculerEcartType(zones);
				Robot currentRobotZoneActuelle = zoneActuelle.getRobots().get(i);
				
				for(int j = 0; j < zoneSuivante.getRobots().size(); j++){
					Robot currentRobotZoneSuivante = zoneSuivante.getRobots().get(j);
					
					currentRobotZoneActuelle.permute(currentRobotZoneSuivante);
					
					double nouvelEcartType = Zone.calculerEcartType(zones);
					if(nouvelEcartType > ancienEcartType){
						currentRobotZoneActuelle.cancelPermute();
					}else{
						permutationsCount++;
						break;
					}
				}
			}
			
			double currentEcartType = Zone.calculerEcartType(zones);
			if(currentEcartType < bestEcartType){
				bestEcartType = currentEcartType;
				CSVManager.enregistrerRobots(RobotSolver.CHEMIN_FICHIER_SORTIE, zones, bestEcartType);
			}
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation par permutation, dernier ecart : " + Zone.calculerEcartType(zones)) + " Meilleur ecart : " + bestEcartType + " Permutations : " + permutationsCount + " Temps passé : " + (double)(System.currentTimeMillis() - startTime)/(double)1000 + " secondes"); //Affichage ecart type
		
	}
}
