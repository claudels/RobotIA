import java.util.ArrayList;
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
	
	/*
	 * @see RobotListener#zoneChanged(Robot)
	 * Affecter un changement de zone � un robot en le retirant de son ancienne zone et en l'ajoutant � sa nouvelle zone.
	 */

	@Override
	public void zoneChanged(Robot robot) {
		if(robots.contains(robot) && robot.getAffectedZone() != this)
			this.robots.remove(robot);
		if(!robots.contains(robot) && robot.getAffectedZone() == this)
			this.robots.add(robot);
	}
	
	/*
	 * R�cup�ration de la somme des vitesse moyennes des robots par zone
	 */
	public double getVmoySum(){
		return this.robots.parallelStream().mapToDouble(robot -> robot.getVMoy()).sum();
	}
	
	/*
	 * Moyenne des vitesses moyennes des robots par zones.
	 */
	public double getMoyenneVitesses(){
		return this.getVmoySum() / (double)(this.robots.size());
	}
	
	/*
	 * R�cup�ration du nombres de robots par zone.
	 */
	public int countRobots(){
		return this.robots.size();
	}
	
	/*
	 * R�cup�ration de l'identifiant de zone
	 */
	public int getIdZone() {
		return idZone;
	}
	
	/*
	 * Liste des robots associ�s � une zone
	 */
	public LinkedList<Robot> getRobots() {
		return robots;
	}
	
	/*
	 * Permutation al�atoire de robots entre zones. Cette fonction est appel� lorsque l'am�lioration de l'�cart type
	 * est inf�rieur � un param�tre : "toleranceGainEcart"
	 * 
	 */
	public static void doPermutationAleatoire(LinkedList<Zone> zones, int nombrePermutations, LinkedList<Robot> robots){
		Random rand = new Random();
		for(int i=0; i < nombrePermutations; i++){
			Comparator <Zone> comparatorZones = (z1, z2) -> Double.compare(z1.getMoyenneVitesses(), z2.getMoyenneVitesses());
			Zone zoneSource = zones.parallelStream().min(comparatorZones).get();
			
			int indexZoneSource = zones.parallelStream().filter(zone -> zone.idZone == zoneSource.idZone).map(zone -> zone.getIdZone()).findAny().orElse(-1);
			int indexZoneDest = indexZoneSource;
			while(indexZoneDest == indexZoneSource){  indexZoneDest = rand.nextInt(zones.size()); }
			
			Zone zoneDest = zones.get(indexZoneDest);
			
			/*int indexRobotSource = rand.nextInt((robots.size() / zones.size()));
			int indexRobotDest = indexRobotSource;
			while(indexRobotDest == indexRobotSource){  indexRobotDest = rand.nextInt((robots.size() / zones.size())); }*/
			Comparator <Robot> comparatorRobots = (r1, r2) -> Double.compare(r1.getVMoy(), r2.getVMoy());
			Robot robotSource = zoneSource.getRobots().parallelStream().max(comparatorRobots).get();
			Robot robotDest = zoneDest.getRobots().parallelStream().min(comparatorRobots).get();
			
			robotSource.permute(robotDest);
		}
	}
	
	/*
	 * Calcule de l'�cart type des vitesse moyenne des zones 
	 */
	
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
	
	/*
	 * affectation des zones aux robots de fa�on al�atoire
	 */
	public static void affectationAleatoire(LinkedList<Zone> zones, LinkedList<Robot> robots){
		Logger.getGlobal().log(Level.INFO, "D�marrage affectation al�atoire.");
		Random rand = new Random();
		
		//Affectation aleatoire
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(rand.nextInt(zones.size())));
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation al�atoire, dernier ecart : " + Zone.calculerEcartType(zones))); //Affichage ecart type

	}
	
	/*
	 * Affectation des robots de fa�on s�quentiel.  
	 */
	public static void affectionSequentielle(LinkedList<Zone> zones, LinkedList<Robot> robots){
		Logger.getGlobal().log(Level.INFO, "D�marrage affectation s�quentielle.");
		
		//Affection s�quentielle
		int curseur = 0;
		
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(curseur));
			curseur = (curseur == zones.size()-1) ? 0 : curseur + 1;
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation s�quentielle, dernier ecart : " + Zone.calculerEcartType(zones))); //Affichage ecart type

	}
	
	/*
	 * La premi�re affectation est faite de fa�on s�quentiel.
	 *  Ensuite on compare une zone al�atoire avec un autre. On test le r�sultat de l'�cart type si on remplace un robot par un autre.
	 *  Si le r�sultat est meilleur alors on applique le remplacement.
	 *  
	 *  Si on atteint un minimum local on fait un �change al�atoire.
	 */
	public static void affectationPermutation(double ecartTypeCible, int tempsMaxSeconde, LinkedList<Robot> robots, LinkedList<Zone> zones){
		Logger.getGlobal().log(Level.INFO, "D�marrage affectation permutation.");
		
		//REGLAGES ALGO
		final double toleranceGainEcart = 0.1;
		final int nombrePermutationAleatoire = 12;
		
		//Temps en MS
		int tempsMaxMS = tempsMaxSeconde*1000;
		
		//Affection s�quentielle
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
			Logger.getGlobal().log(Level.INFO,"Pourcentage amelioration : " + pourcentageAmeliorationEcartType);
			//Si on ne gagne l'�cart type ne baisse plus suffisamment, on ajoute de l'al�atoire en faisant des permutations al�atoires
			if(pourcentageAmeliorationEcartType < toleranceGainEcart){
				Zone.doPermutationAleatoire(zones, nombrePermutationAleatoire, robots);
				permutationsCount+=nombrePermutationAleatoire;
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
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation par permutation, dernier ecart : " + Zone.calculerEcartType(zones)) + " Meilleur ecart : " + bestEcartType + " Permutations : " + permutationsCount + " Temps pass� : " + (double)(System.currentTimeMillis() - startTime)/(double)1000 + " secondes"); //Affichage ecart type
		
	}
}
