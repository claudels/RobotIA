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
	/**
	 * Affecter un changement de zone � un robot en le retirant de son ancienne zone et en l'ajoutant � sa nouvelle zone.
	 *  
	 * @param robot robot que l'on souhaite d�placer d'une zone vers une autre
	 */
	public void zoneChanged(Robot robot) {
		if(robots.contains(robot) && robot.getAffectedZone() != this)
			this.robots.remove(robot);
		if(!robots.contains(robot) && robot.getAffectedZone() == this)
			this.robots.add(robot);
	}
	
	/**
	 * R�cup�ration de la somme des vitesse moyennes des robots par zone
	 */
	public double getVmoySum(){
		return this.robots.parallelStream().mapToDouble(robot -> robot.getVMoy()).sum();
	}
	
	/**
	 * Moyenne des vitesses moyennes des robots par zones.
	 */
	public double getMoyenneVitesses(){
		return this.getVmoySum() / (double)(this.robots.size());
	}
	
	/**
	 * R�cup�ration du nombres de robots par zone.
	 */
	public int countRobots(){
		return this.robots.size();
	}
	
	/**
	 * R�cup�ration de l'identifiant de zone
	 */
	public int getIdZone() {
		return idZone;
	}
	
	/**
	 * Liste des robots associ�s � une zone
	 */
	public LinkedList<Robot> getRobots() {
		return robots;
	}
	
	
	
	/**
	 * Permutation al�atoire de robots entre zones. Cette fonction est appel� lorsque l'am�lioration de l'�cart type
	 * est inf�rieur � un param�tre : "toleranceGainEcart"
	 * 
	 * @param zones liste des zones ou placer les robots
	 * @param nombrePermutations Nombre de permutation al�atoire qui doivent �tre effectu�
	 * @param robots
	 */
	public static void doPermutationAleatoire(LinkedList<Zone> zones, int nombrePermutations, LinkedList<Robot> robots){
		Random rand = new Random();
		for(int i=0; i < nombrePermutations; i++){
			//Utilise la zone avec la plus petite moyenne des vitesse comme zone de gauche
			Comparator <Zone> comparator = (z1, z2) -> Double.compare(z1.getMoyenneVitesses(), z2.getMoyenneVitesses());
			Zone zoneSource = zones.parallelStream().min(comparator).get();
			
			//La zone de droite est d�fini� al�atoirement
			int indexZoneSource = zones.indexOf(zoneSource);
			int indexZoneDest = indexZoneSource;
			while(indexZoneDest == indexZoneSource){  indexZoneDest = rand.nextInt((zones.size())); }
			
			//On permute le robot de la zone gauche ayant la plus grande vitesse avec celui qui a la plus petite vitesse dans la zone droite
			Comparator <Robot> comparatorRobot = (r1, r2) -> Double.compare(r1.getVMoy(), r2.getVMoy());
			/*Robot robotSource = zoneSource.getRobots().parallelStream().max(comparatorRobot).get();
			Robot robotDest = zones.get(indexZoneDest).getRobots().parallelStream().min(comparatorRobot).get();*/
			Robot robotSource = zoneSource.getRobots().parallelStream().max(comparatorRobot).get();
			Robot robotDest = zones.get(indexZoneDest).getRobots().get(rand.nextInt(robots.size()/zones.size()));
			robotSource.permute(robotDest);
		}
	}
	
	/**
	 * 
	 * @param zones Liste des zones qui rentrent dans le calcul de l'�cart type
	 * @return L'�cart type des vitesses moyennes des zones
	 */
	public static double calculerEcartType(LinkedList<Zone> zones){
		double sum = 0.0, standardDeviation = 0.0;
        int length = zones.size();
		
        //Somme des vitesse moyennes
		for(Zone zone : zones){
			sum += zone.getMoyenneVitesses();
		}
		
		//Moyenne
		double mean = sum/(double)length;
		
		//Deviation
		for(Zone zone: zones) {
            standardDeviation += Math.pow(zone.getMoyenneVitesses() - mean, 2);
        }
		
		//Ecart type
        return Math.sqrt(standardDeviation/(double)length);
	}
	
	
	/**
	 * Placement des robots sur les zones de fa�on al�atoire 
	 * 
	 * @param zones Liste des zones ou il est possible de placer un robot
	 * @param robots Liste des robots qu'il faut placer
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
	
	
	
	/**
	 * Affectation des robots de fa�on s�quentielle.
	 * Bas� sur l'ordre de la liste de robot, on affecte un robot � chaque zone.  
	 * 
	 * @param zones Liste des zones ou il est possible de placer un robot
	 * @param robots Liste des robots qu'il faut placer
	 */
	public static void affectionSequentielle(LinkedList<Zone> zones, LinkedList<Robot> robots){
		Logger.getGlobal().log(Level.INFO, "D�marrage affectation s�quentielle.");
		int curseur = 0;
		
		//Triage des robots par vitesse moyenne decroissante
		Comparator <Robot> comparatorRobot = (r1, r2) -> Double.compare(r1.getVMoy(), r2.getVMoy());
		robots.sort(comparatorRobot);
		Collections.reverse(robots);
		
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(curseur));
			curseur = (curseur == zones.size()-1) ? 0 : curseur + 1;
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation s�quentielle, dernier ecart : " + Zone.calculerEcartType(zones))); //Affichage ecart type

	}

	
	/**
	 * La premi�re affectation est faite de fa�on s�quentielle.
	 * Ensuite on compare une zone avec une autre al�atoirement.
	 * On test le r�sultat de l'�cart type si on remplace un robot par un autre.
	 * Si le r�sultat est meilleur alors on applique le remplacement.
	 * 
	 * @param ecartTypeCible Ecart type qui doit �tre atteint. Si cet �cart tye est atteint, on stop le programme
	 * @param tempsMaxSeconde On arr�te la permutation au bout de temps "tempsMaxSeconde"
	 * @param zones Liste des zones ou il est possible de placer un robot
	 * @param robots Liste des robots qu'il faut placer
	 */
	public static void affectationPermutation(double ecartTypeCible, int tempsMaxSeconde, LinkedList<Robot> robots, LinkedList<Zone> zones){
Logger.getGlobal().log(Level.INFO, "D�marrage affectation permutation.");
		
		//REGLAGES ALGO
		final double toleranceGainEcart = 0.05;
		final int nombrePermutationAleatoire = 2;
		
		//Temps en MS
		int tempsMaxMS = tempsMaxSeconde*1000;
		
		//Affection s�quentielle
		Zone.affectionSequentielle(zones, robots);
		
		long startTime = System.currentTimeMillis();
		long permutationsCount = 0;
		double ecartTypeAvantPermut = -1;
		double bestEcartType = 10;
		
		Comparator <Zone> comparator = (z1, z2) -> Double.compare(z1.getMoyenneVitesses(), z2.getMoyenneVitesses());
		
		//On continue tant que on a pas atteint l'ecart type cible ou que le temps maximum est atteint
		while(Zone.calculerEcartType(zones) > ecartTypeCible && ((System.currentTimeMillis() - startTime) < tempsMaxMS)){
			//Zones � permuter : Celle avec la moyenne des vitesse la plus petite et celle qui � la plus grande
			Zone zoneActuelle = zones.parallelStream().min(comparator).get();
			Zone zoneSuivante = zones.parallelStream().max(comparator).get();
			
			double pourcentageAmeliorationEcartType = 1 - (Zone.calculerEcartType(zones) / ecartTypeAvantPermut);
			
			//Si on ne gagne l'�cart type ne baisse plus suffisamment, on ajoute de l'al�atoire en faisant des permutations al�atoires
			if(pourcentageAmeliorationEcartType < toleranceGainEcart){
				//On Permute des robots al�atoirement
				Zone.doPermutationAleatoire(zones, nombrePermutationAleatoire, robots);
				permutationsCount+=nombrePermutationAleatoire;
				
				//On choisi la zone de droite de fa�on al�atoire
				int indexZoneSource = zones.indexOf(zoneSuivante);
				int indexZoneDest = indexZoneSource;
				while(indexZoneDest == indexZoneSource){  indexZoneDest = (new Random()).nextInt((zones.size())); }
			}
			
			ecartTypeAvantPermut = Zone.calculerEcartType(zones);
			
			//On teste une permutation entre chaque robot jusqu'a ce que l'�cart type d'am�liore
			outer: for(int i = 0; i < zoneActuelle.getRobots().size(); i++){
				double ancienEcartType = Zone.calculerEcartType(zones);
				Robot currentRobotZoneActuelle = zoneActuelle.getRobots().get(i);
				
				for(int j = 0; j < zoneSuivante.getRobots().size(); j++){
					Robot currentRobotZoneSuivante = zoneSuivante.getRobots().get(j);
					
					currentRobotZoneActuelle.permute(currentRobotZoneSuivante);
					
					//Si on a am�lior� l'ecart type, on sort des boucle sinon on annule la permutation et on continue
					double nouvelEcartType = Zone.calculerEcartType(zones);
					if(nouvelEcartType >= ancienEcartType){
						currentRobotZoneActuelle.cancelPermute();
					}else{
						permutationsCount++;
						break outer;
					}
				}
			}
			
			
			double currentEcartType = Zone.calculerEcartType(zones);
			
			//Si l'eacrt type est meilleur que avant, on l'enregistre dans le csv
			if(currentEcartType < bestEcartType){
				bestEcartType = currentEcartType;
				CSVManager.enregistrerRobots(RobotSolver.CHEMIN_FICHIER_SORTIE, zones, bestEcartType);
			}
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Fin affectation par permutation, dernier ecart : " + Zone.calculerEcartType(zones)) + " Meilleur ecart : " + bestEcartType + " Permutations : " + permutationsCount + " Temps pass� : " + (double)(System.currentTimeMillis() - startTime)/(double)1000 + " secondes"); //Affichage ecart type
		
	}
}


