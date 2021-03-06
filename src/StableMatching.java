import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class StableMatching {
	private Set<Female> females;
	private Set<Female> finalFemales;
	private Set<Male> finalMales;
	private Queue<Male> freeMales;
	private Map<String, Female> updated;
	boolean ran;
	
	public StableMatching(Set<Male> males, Set<Female> females) {
		if (males.size() != females.size()) {
			throw new IllegalArgumentException("Oh no! You need an equal number of males and females!" +
					" No one should be lonely!");
		}
		this.females = females;
		
		finalFemales = new TreeSet<Female>();
		finalMales = new TreeSet<Male>();
		
		freeMales = new LinkedList<Male>();
		freeMales.addAll(males);
		
		updated = new HashMap<String, Female>();
		
		Iterator<Female> it = females.iterator();
		while (it.hasNext()) {
			Female curr = it.next();
			updated.put(curr.getName(), curr);
		}
		
		ran = false;
	}
	
	public void GaleShapley() {
		ran = true;
		
		while (!freeMales.isEmpty() && freeMales.peek().getProposed().size() != females.size()) {
			Male curr = freeMales.peek();
			List<Female> preferences = curr.getPreference();
			Set<Female> proposed = curr.getProposed();
			Iterator<Female> it = preferences.iterator();
			while (it.hasNext()) {
				Female currFemale = updated.get(it.next().getName());
				
				// check whether female has been proposed
				if (!proposed.contains(currFemale)) {
					proposed.add(currFemale);
					curr.setProposed(proposed);
					
					// check whether female is free
					if (currFemale.isFree()) {
						if (finalMales.contains(curr)) finalMales.remove(curr);
						if (finalFemales.contains(currFemale)) finalFemales.remove(currFemale);
						
						curr.setPartner(currFemale);
						currFemale.setPartner(curr);
						
						finalMales.add(curr);
						finalFemales.add(currFemale);
						
						updated.put(currFemale.getName(), currFemale);
						
						freeMales.remove(curr);
						
						break;
					} else {
						boolean br = false;
						Iterator<Male> itMale = currFemale.getPreference().iterator();
						boolean discovered = false;
						while (itMale.hasNext()) {
							Male currMale = itMale.next();
							if (currMale.equals(currFemale.getPartner())) discovered = true;
							else if (currMale.equals(curr)) {
								
								// check preference
								if (!discovered) {
									Male m = currFemale.getPartner();
									if (finalMales.contains(m)) finalMales.remove(m);
									if (finalMales.contains(curr)) finalMales.remove(curr);
									if (finalFemales.contains(currFemale)) {
										finalFemales.remove(currFemale);
									}
									
									m.setPartner(null);
									freeMales.add(m);
									curr.setPartner(currFemale);
									currFemale.setPartner(curr);
									
									updated.put(currFemale.getName(), currFemale);
									
									finalFemales.add(currFemale);
									finalMales.add(curr);
									
									freeMales.remove(curr);
									
									br = true;
									break;
								}
							}
						}
						if (br) break;
					}
				}
			}
		}
	}

	public Set<Male> getMales() {
		if (!ran) throw new IllegalArgumentException("Run Gale Shapley First");
		return finalMales;
	}

	public Set<Female> getFemales() {
		if (!ran) throw new IllegalArgumentException("Run Gale Shapley First");
		return finalFemales;
	}
}
