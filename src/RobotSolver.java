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
	
	public final static String CHEMIN_FICHIER_SORTIE = "res/complet.csv";

	public static void main(String[] args) {
		//Table des robots avec id comme clé
		LinkedList<Robot> robots = CSVManager.chargerRobot(CHEMIN_FICHIER_ROBOTS);
		
		//Création des zone
		LinkedList<Zone> zones = new LinkedList<Zone>();
		
		for(int i=0; i<NOMBRE_ZONES; i++){
			zones.add(new Zone(i));
		}
		
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(TestPrincipal.class);
		if (result.getFailureCount() > 0){
			Logger.getGlobal().log(Level.SEVERE, "Le test de calcul de l'écart ne passe pas.");
		}
		
		Zone.affectionSequentielle(zones, robots);
		Zone.affectationAleatoire(zones, robots);
		Zone.affectationPermutation(0.000001, 40, robots, zones);
		
	}

	
}
