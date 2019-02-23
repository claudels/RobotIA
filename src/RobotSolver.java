import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RobotSolver {
	
	private final static String CHEMIN_FICHIER_ROBOTS = "res/initial.csv";
	private final static int NOMBRE_ZONES = 6;
	private final static String SEPARATEUR_CSV_SORTIE = ";";
	private final static String CHEMIN_FICHIER_SORTIE = "res/complet.csv";

	public static void main(String[] args) {
		//Table des robots avec id comme clé
		LinkedList<Robot> robots = chargementRobot(CHEMIN_FICHIER_ROBOTS);
		
		TU_calculEcartType();
		
		affectionSequentielle(robots);
		affectationAleatoire(robots);
		affectationPermutation(0.00001, 10, robots);
		
		
		//Affichage des robots
		//Logger.getGlobal().log(Level.INFO, robots.toString());
	}
	
	public static void TU_calculEcartType(){
		//Création des zone
		ArrayList<Zone> zones = new ArrayList<Zone>();
		
		for(int i=0; i<3; i++){
			zones.add(new Zone(i));
		}
		
		//Robot zone 1
		Robot robot01 = new Robot(1, 0);
		Robot robot2 = new Robot(2, 2);
		robot01.setAffectedZone(zones.get(0));
		robot2.setAffectedZone(zones.get(0));
		
		//Robot zone 2
		Robot robot02 = new Robot(1, 0);
		Robot robot4 = new Robot(2, 4);
		robot02.setAffectedZone(zones.get(1));
		robot4.setAffectedZone(zones.get(1));
		
		//Robot zone 3
		Robot robot03 = new Robot(1, 0);
		Robot robot6 = new Robot(2, 6);
		robot03.setAffectedZone(zones.get(2));
		robot6.setAffectedZone(zones.get(2));
		
		Logger.getGlobal().log(Level.INFO, "Test calcul ecart type, attendu E = 0,816497, Reel E = " + calculateEcartTypeZones(zones));
	}
	
	public static void affectationPermutation(double ecartTypeCible, int tempsMaxSeconde, LinkedList<Robot> robots){
		//Création des zone
		ArrayList<Zone> zones = new ArrayList<Zone>();
		
		for(int i=0; i<NOMBRE_ZONES; i++){
			zones.add(new Zone(i));
		}
		
		//Affection séquentielle
		int curseur = 0;
		
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(curseur));
			curseur = (curseur == NOMBRE_ZONES-1) ? 0 : curseur + 1;
		}
		
		/**Collections.sort(robots, Comparator.comparingDouble(Robot::getVMoy).reversed());
		int flag=1;
		int curseur=0;
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(curseur));
			
		if (flag==1) {
			if (curseur == NOMBRE_ZONES-1) {
				flag=0;
			}
			curseur+=1;
		}	
		if (flag==0) {
			if (curseur == 1) {
				flag=1;
			}
			curseur-=1;
		}
		}*/
		
		long startTime = System.currentTimeMillis();
		long permutationsCount = 0;
		double ecartTypeAvantPermut = -1;
		double toleranceGainEcart = 0.01;
		double nombrePermutationAleatoire = 8;
		double bestEcartType = 10;
		int counterForcePermute = 0;
		int nombreIterationAvantForcePermute = 10;
		
		Comparator <Zone> comparator = (z1, z2) -> Double.compare(z1.getMoyenneVitesses(), z2.getMoyenneVitesses());
		Random rand = new Random();
		//Permutation
		while(calculateEcartTypeZones(zones) > ecartTypeCible && ((double)(System.currentTimeMillis() - startTime)/(double)1000) < (double)tempsMaxSeconde){
			Zone zoneActuelle = zones.stream().min(comparator).get();
			Zone zoneSuivante = zones.stream().max(comparator).get();
			
			counterForcePermute = (counterForcePermute == 0) ? 0 : counterForcePermute - 1;
			
			double pourcentageAmeliorationEcartType = 1 - (calculateEcartTypeZones(zones) / ecartTypeAvantPermut);
			Logger.getGlobal().log(Level.INFO, "Pourcentage amélioriation : " + pourcentageAmeliorationEcartType);
			
			if(pourcentageAmeliorationEcartType < toleranceGainEcart && counterForcePermute == 0){
				//Reinitialise le compteur avant qui temporise les iteration avant de repermuter
				counterForcePermute = nombreIterationAvantForcePermute;
				for(int i=0; i < nombrePermutationAleatoire; i++){
					int indexZoneSource = rand.nextInt(NOMBRE_ZONES);
					int indexZoneDest = indexZoneSource;
					while(indexZoneDest == indexZoneSource){  indexZoneDest = rand.nextInt(NOMBRE_ZONES); }
					
					int indexRobotSource = rand.nextInt((robots.size() / zones.size()));
					int indexRobotDest = indexRobotSource;
					while(indexRobotDest == indexRobotSource){  indexRobotDest = rand.nextInt((robots.size() / zones.size())); }
					
					//Zone zoneSource = zones.get(indexZoneSource);
					Zone zoneSource = zoneActuelle;
					Zone zoneDest = zones.get(indexZoneDest);
					
					zoneSource.getRobots().get(indexRobotSource).setAffectedZone(zoneDest);
					zoneDest.getRobots().get(indexRobotDest).setAffectedZone(zoneSource);
					permutationsCount++;
				}
			}
			else if(pourcentageAmeliorationEcartType < toleranceGainEcart && counterForcePermute > 0){
				zoneSuivante = zones.get(rand.nextInt(NOMBRE_ZONES));
			}
			
			
			ecartTypeAvantPermut = calculateEcartTypeZones(zones);
			
			for(int i = 0; i < zoneActuelle.getRobots().size(); i++){
				double ancienEcartType = calculateEcartTypeZones(zones);
				
				/*double bestTempEcartType = ancienEcartType;
				final Robot bestRobotToSwitchId[] = new Robot[1];
				bestRobotToSwitchId[0] = null;*/
				
				for(int j = 0; j < zoneSuivante.getRobots().size(); j++){
					/*zoneActuelle.getRobots().get(i).setAffectedZone(zoneSuivante);
					zoneSuivante.getRobots().get(j).setAffectedZone(zoneActuelle);
					
					double nouvelEcartType = calculateEcartTypeZones(zones);
					if(nouvelEcartType < bestTempEcartType && nouvelEcartType < ancienEcartType){
						bestEcartType = calculateEcartTypeZones(zones);
						bestRobotToSwitchId[0] = zoneSuivante.getRobots().get(zoneSuivante.countRobots() - 2);
					}
					
					zoneActuelle.getRobots().get(zoneActuelle.countRobots() - 1).setAffectedZone(zoneSuivante);
					zoneSuivante.getRobots().get(zoneSuivante.countRobots() - 2).setAffectedZone(zoneActuelle);*/
					zoneActuelle.getRobots().get(i).setAffectedZone(zoneSuivante);
					zoneSuivante.getRobots().get(j).setAffectedZone(zoneActuelle);
					
					double nouvelEcartType = calculateEcartTypeZones(zones);
					if(nouvelEcartType > ancienEcartType){
						zoneActuelle.getRobots().get(zoneActuelle.countRobots() - 1).setAffectedZone(zoneSuivante);
						zoneSuivante.getRobots().get(zoneSuivante.countRobots() - 2).setAffectedZone(zoneActuelle);
					}else{
						permutationsCount++;
						break;
					}
				}
				
				/*if(bestRobotToSwitchId[0] != null){
					zoneActuelle.getRobots().get(i).setAffectedZone(zoneSuivante);
					/*int indexRobot = zoneSuivante.getRobots().indexOf(bestRobotToSwitchId[0]);
					zoneSuivante.getRobots().get(indexRobot).setAffectedZone(zoneActuelle);
					bestRobotToSwitchId[0].setAffectedZone(zoneActuelle);
					permutationsCount++;
				}*/
			}
			
			if(calculateEcartTypeZones(zones) < bestEcartType){
				bestEcartType = calculateEcartTypeZones(zones);
				enregistrerRobots(CHEMIN_FICHIER_SORTIE, zones);
			}
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Affectation par permutation, dernier ecart : " + calculateEcartTypeZones(zones)) + " Meilleur ecart : " + bestEcartType + " Permutations : " + permutationsCount + " Temps passé : " + (double)(System.currentTimeMillis() - startTime)/(double)1000 + " secondes"); //Affichage ecart type

	}
	
	public static void affectationAleatoire(LinkedList<Robot> robots){
		//Création des zone
		ArrayList<Zone> zones = new ArrayList<Zone>();
		
		for(int i=0; i<NOMBRE_ZONES; i++){
			zones.add(new Zone(i));
		}
		
		Random rand = new Random();
		
		//Affectation aleatoire
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(rand.nextInt(NOMBRE_ZONES)));
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Affectation aléatoire : " + calculateEcartTypeZones(zones))); //Affichage ecart type
	}
	
	
	public static void affectionSequentielle(LinkedList<Robot> robots){
		//Création des zone
		ArrayList<Zone> zones = new ArrayList<Zone>();
		
		for(int i=0; i<NOMBRE_ZONES; i++){
			zones.add(new Zone(i));
		}
		
		//Affection séquentielle
		int curseur = 0;
		
		for(Robot robot : robots){
			robot.setAffectedZone(zones.get(curseur));
			curseur = (curseur == NOMBRE_ZONES-1) ? 0 : curseur + 1;
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Affectation séquentielle : " + calculateEcartTypeZones(zones))); //Affichage ecart type
	}
	
	public static double calculateEcartTypeZones(ArrayList<Zone> zones){
		
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
	
	public static LinkedList<Robot> chargementRobot(String cheminFichierCSV){
		LinkedList<Robot> robots = new LinkedList<Robot>();
		
		BufferedReader br = null;
        String ligne = "";
        String splitter = ",";
		
		try{
			br = new BufferedReader(new FileReader(cheminFichierCSV));
			
			//Lecture de la première ligne
			br.readLine();
			while ((ligne = br.readLine()) != null) {
				Robot newRobot = new Robot(Integer.valueOf(ligne.split(splitter)[0]), Double.valueOf(ligne.split(splitter)[1]));
				robots.add(newRobot);
			}
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		
		return robots;
	}
	
	public static void enregistrerRobots(String chemin, ArrayList<Zone> zones){
		StringBuilder builder = new StringBuilder();
		PrintWriter writer = null;
		
		try{
			writer = new PrintWriter(new File(chemin));
			
			builder.append("ECART TYPE" + SEPARATEUR_CSV_SORTIE + calculateEcartTypeZones(zones) + SEPARATEUR_CSV_SORTIE + "\n");
			builder.append("ID" + SEPARATEUR_CSV_SORTIE + "Vitesse" + SEPARATEUR_CSV_SORTIE + "Zone affectée" + SEPARATEUR_CSV_SORTIE + "\n");
			
			for (Zone zone : zones){
				for (Robot robot : zone.getRobots()){
					builder.append(robot.getId() + SEPARATEUR_CSV_SORTIE);
					builder.append(robot.getVMoy() + SEPARATEUR_CSV_SORTIE);
					builder.append(zone.getIdZone() + SEPARATEUR_CSV_SORTIE);
					builder.append("\n");
				}
			}
			
			writer.write(builder.toString());
		}catch (FileNotFoundException e) {
		      System.out.println(e.getMessage());
	    }finally{
	    	writer.close();
	    }
	}

}
