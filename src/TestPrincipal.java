import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

public class TestPrincipal {

	/*
	 * Vérification de la fonction de calcule de l'acart type.
	 * Cette fonction permet de vérifier que la fonction de calcule d'acart type retourne le bon résultat.
	 */
	@Test
	public void TU_EcartType() {
		//Création des zone
		LinkedList<Zone> zones = new LinkedList<Zone>();
		
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
		
		double ecartType = Zone.calculerEcartType(zones);
		Logger.getGlobal().log(Level.INFO, "Test unitaire du calcul de l'écart type.");
		assertTrue(ecartType > 0.816496 && ecartType < 0.816498);
	}

}
