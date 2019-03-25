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

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class RobotSolver {
	
	private final static String CHEMIN_FICHIER_ROBOTS = "res/initial.csv";
	private final static int NOMBRE_ZONES = 6;
	
	private final static String CHEMIN_FICHIER_SORTIE = "res/complet.csv";

	public static void main(String[] args) {
		//Table des robots avec id comme clé
		LinkedList<Robot> robots = CSVManager.chargerRobot(CHEMIN_FICHIER_ROBOTS);
		
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(TestPrincipal.class);
		if (result.getFailureCount() > 0){
			Logger.getGlobal().log(Level.SEVERE, "Le test de calcul de l'écart ne passe pas.");
		}
		
		/*affectionSequentielle(robots);
		affectationAleatoire(robots);
		affectationPermutation(0.000001, 20, robots);*/
		
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
		
		long startTime = System.currentTimeMillis();
		long permutationsCount = 0;
		double ecartTypeAvantPermut = -1;
		double toleranceGainEcart = 0.1;
		double nombrePermutationAleatoire = 8;
		double bestEcartType = 10;
		
		Comparator <Zone> comparator = (z1, z2) -> Double.compare(z1.getMoyenneVitesses(), z2.getMoyenneVitesses());
		Random rand = new Random();
		while(Zone.calculerEcartType(zones) > ecartTypeCible && ((double)(System.currentTimeMillis() - startTime)/(double)1000) < (double)tempsMaxSeconde){
			Zone zoneActuelle = zones.stream().min(comparator).get();
			Zone zoneSuivante = zones.stream().max(comparator).get();
			
			double pourcentageAmeliorationEcartType = 1 - (Zone.calculerEcartType(zones) / ecartTypeAvantPermut);
			
			if(pourcentageAmeliorationEcartType < toleranceGainEcart){
				//Reinitialise le compteur avant qui temporise les iteration avant de repermuter
				for(int i=0; i < nombrePermutationAleatoire; i++){
					int indexZoneSource = rand.nextInt(NOMBRE_ZONES);
					int indexZoneDest = indexZoneSource;
					while(indexZoneDest == indexZoneSource){  indexZoneDest = rand.nextInt(NOMBRE_ZONES); }
					
					int indexRobotSource = rand.nextInt((robots.size() / zones.size()));
					int indexRobotDest = indexRobotSource;
					while(indexRobotDest == indexRobotSource){  indexRobotDest = rand.nextInt((robots.size() / zones.size())); }
					
					Zone zoneSource = zoneActuelle;
					Zone zoneDest = zones.get(indexZoneDest);
					
					/*zoneSource.getRobots().get(indexRobotSource).setAffectedZone(zoneDest);
					zoneDest.getRobots().get(indexRobotDest).setAffectedZone(zoneSource);*/
					zoneSource.getRobots().get(indexRobotSource).permute(zoneDest.getRobots().get(indexRobotDest));
					permutationsCount++;
				}
			}
			else if(pourcentageAmeliorationEcartType < toleranceGainEcart){
				zoneSuivante = zones.get(rand.nextInt(NOMBRE_ZONES));
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
			
			if(Zone.calculerEcartType(zones) < bestEcartType){
				bestEcartType = Zone.calculerEcartType(zones);
				CSVManager.enregistrerRobots(CHEMIN_FICHIER_SORTIE, zones, bestEcartType);
			}
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf("Affectation par permutation, dernier ecart : " + Zone.calculerEcartType(zones)) + " Meilleur ecart : " + bestEcartType + " Permutations : " + permutationsCount + " Temps passé : " + (double)(System.currentTimeMillis() - startTime)/(double)1000 + " secondes"); //Affichage ecart type

	}
	}
