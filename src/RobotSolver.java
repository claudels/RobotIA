import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RobotSolver {
	
	private final static String CHEMIN_FICHIER_ROBOTS = "res/initial.csv";
	private final static int NOMBRE_ZONES = 6;

	public static void main(String[] args) {
		//Table des robots avec id comme clé
		LinkedList<Robot> robots = chargementRobot(CHEMIN_FICHIER_ROBOTS);
		
		TU_calculEcartType();
		
		affectionSequentielle(robots);
		
		
		//Affichage des robots
		//Logger.getGlobal().log(Level.INFO, robots.toString());
	}
	
	public static void TU_calculEcartType(){
		//Création des zone
		Zone zones[] = new Zone[3];
		
		for(int i=0; i<3; i++){
			zones[i] = new Zone(i);
		}
		
		//Robot zone 1
		Robot robot01 = new Robot(1, 0);
		Robot robot2 = new Robot(2, 2);
		robot01.setAffectedZone(zones[0]);
		robot2.setAffectedZone(zones[0]);
		double moyZone1 = zones[0].getMoyenneVitesses();
		
		//Robot zone 2
		Robot robot02 = new Robot(1, 0);
		Robot robot4 = new Robot(2, 4);
		robot02.setAffectedZone(zones[1]);
		robot4.setAffectedZone(zones[1]);
		double moyZone2 = zones[1].getMoyenneVitesses();
		
		//Robot zone 3
		Robot robot03 = new Robot(1, 0);
		Robot robot6 = new Robot(2, 6);
		robot03.setAffectedZone(zones[2]);
		robot6.setAffectedZone(zones[2]);
		double moyZone3 = zones[2].getMoyenneVitesses();
		
		Logger.getGlobal().log(Level.INFO, "Test calcul ecart type, attendu E = 0,816497, Reel E = " + calculateEcartTypeZones(zones));
	}
	
	public static void affectionSequentielle(LinkedList<Robot> robots){
		//Création des zone
		Zone zones[] = new Zone[NOMBRE_ZONES];
		
		for(int i=0; i<NOMBRE_ZONES; i++){
			zones[i] = new Zone(i);
		}
		
		//Affection séquentielle
		int curseur = 0;
		
		for(Robot robot : robots){
			robot.setAffectedZone(zones[curseur]);
			curseur = (curseur == NOMBRE_ZONES-1) ? 0 : curseur + 1;
		}
		
		Logger.getGlobal().log(Level.INFO, String.valueOf(calculateEcartTypeZones(zones))); //Affichage ecart type
	}
	
	public static double calculateEcartTypeZones(Zone zones[]){
		
		double sum = 0.0, standardDeviation = 0.0;
        int length = zones.length;
		
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

}
