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

	/*
	 * @see RobotListener#zoneChanged(Robot)
	 * Affecter un changement de zone à un robot en le retirant de son ancienne zone et en l'ajoutant à sa nouvelle zone.
	 */
	@Override
	public void zoneChanged(Robot robot) {
		if(robots.contains(robot) && robot.getAffectedZone() != this)
			this.robots.remove(robot);
		if(!robots.contains(robot) && robot.getAffectedZone() == this)
			this.robots.add(robot);
	}
	
	/*
	 * Récupération de la somme des vitesse moyennes des robots par zone
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
	 * Récupération du nombres de robots par zone.
	 */
	public int countRobots(){
		return this.robots.size();
	}
	
	/*
	 * Récupération de l'identifiant de zone
	 */
	public int getIdZone() {
		return idZone;
	}
	
	/*
	 * Liste des robots associés à une zone
	 */
	public LinkedList<Robot> getRobots() {
		return robots;
	}
	
	/*
	 * Permutation aléatoire de robots entre zones. Cette fonction est appelé lorsque l'amélioration de l'écart type
	 * est inférieur à un paramétre : "toleranceGainEcart"
	 * 
	 */
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
	
	/*
	 * Calcule de l'écart type des vitesse moyenne des zones 
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
	
	public static void affectationAleatoire(LinkedList<Zone> zones, LinkedList<Robot> robots){
		Logger.getGlobal().log(Level.INFO, "DÃ©marrage affectation alÃ©atoire.");
		Random rand = new Random();
		
		//Affectation aleatoire
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(rand.nextInt(zones.size())));
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation alÃ©atoire, dernier ecart : " + Zone.calculerEcartType(zones))); //Affichage ecart type

	}
	
	/*
	 * Affectation des robots de façon séquentiel.
	 * Basé sur l'ordre de la liste de robot, on affecte un robot à chaque zone.  
	 */
	public static void affectionSequentielle(LinkedList<Zone> zones, LinkedList<Robot> robots){
		Logger.getGlobal().log(Level.INFO, "DÃ©marrage affectation sÃ©quentielle.");
		
		//Affection sÃ©quentielle
		int curseur = 0;
		
		//Triage des robots par vitesse moyenne decroissante
		Comparator <Robot> comparatorRobot = (r1, r2) -> Double.compare(r1.getVMoy(), r2.getVMoy());
		robots.sort(comparatorRobot);
		Collections.reverse(robots);
		
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(curseur));
			curseur = (curseur == zones.size()-1) ? 0 : curseur + 1;
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation sÃ©quentielle, dernier ecart : " + Zone.calculerEcartType(zones))); //Affichage ecart type

	}
	
	/*
	 * La première affectation est faite de façon séquentiel.
	 * Ensuite on compare une zone avec une autre aléatoirement.
	 * On test le résultat de l'écart type si on remplace un robot par un autre.
	 * Si le résultat est meilleur alors on applique le remplacement.
	 *  
	 * Si on atteint un minimum local on fait un échange aléatoire et on recommence ensuite la permutation décrite ci-dessus.
	 * 
	 * On arrête la permutation au bout d'un temps "tempsMaxSeconde"
	 * 
	 */
	public static void affectationPermutation(double ecartTypeCible, int tempsMaxSeconde, LinkedList<Robot> robots, LinkedList<Zone> zones){
		Logger.getGlobal().log(Level.INFO, "DÃ©marrage affectation permutation.");
		
		//REGLAGES ALGO
		final double toleranceGainEcart = 0.1;
		final int nombrePermutationAleatoire = 1;
		
		//Temps en MS
		int tempsMaxMS = tempsMaxSeconde*1000;
		
		//Affection sÃ©quentielle
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
			
			//Si on ne gagne l'Ã©cart type ne baisse plus suffisamment, on ajoute de l'alÃ©atoire en faisant des permutations alÃ©atoires
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
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation par permutation, dernier ecart : " + Zone.calculerEcartType(zones)) + " Meilleur ecart : " + bestEcartType + " Permutations : " + permutationsCount + " Temps passÃ© : " + (double)(System.currentTimeMillis() - startTime)/(double)1000 + " secondes"); //Affichage ecart type
		
	}
}

