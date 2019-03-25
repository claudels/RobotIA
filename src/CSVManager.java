import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

public class CSVManager {
	
	private final static String SEPARATEUR_CSV_SORTIE = ";";
	
	public static LinkedList<Robot> chargerRobot(String cheminFichierCSV){
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
	
	public static void enregistrerRobots(String chemin, ArrayList<Zone> zones, double meilleurEcartType){
		StringBuilder builder = new StringBuilder();
		PrintWriter writer = null;
		
		try{
			writer = new PrintWriter(new File(chemin));
			
			builder.append("ECART TYPE" + SEPARATEUR_CSV_SORTIE + meilleurEcartType + SEPARATEUR_CSV_SORTIE + "\n");
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
