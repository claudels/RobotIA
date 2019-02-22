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
		
		//Création des zone
		Zone zones[] = new Zone[NOMBRE_ZONES];
		
		for(int i=0; i<NOMBRE_ZONES; i++){
			zones[i] = new Zone(i);
		}
		
		//Affection
		
		
		//Affichage des robots
		Logger.getGlobal().log(Level.INFO, robots.toString());
	}
	
	public double calculateEcartTypeZones(Zone zones[]){
		
		double sum = 0.0, standardDeviation = 0.0;
        int length = zones.length;
		
		for(Zone zone : zones){
			sum += zone.getMoyenneVitesses();
		}
		
		double mean = sum/length;
		
		for(Zone zone: zones) {
            standardDeviation += Math.pow(zone.getMoyenneVitesses() - mean, 2);
        }

        return Math.sqrt(standardDeviation/length);
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
