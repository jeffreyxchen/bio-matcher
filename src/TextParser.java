import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class TextParser {
	// method to move files from allPeople into the appropro folders
	static void getFilesIntoFolder() throws IOException{
		File allDir = new File("allPeople");
		
		File[] allFiles = allDir.listFiles();
		
		String os = System.getProperty("os.name");
		String osName = os.trim().substring(0, os.indexOf(' '));
		
		// check OS to format path correctly
		if (osName.equals("Windows")) {
			// iterate through the added files
			for(int i = 0; i < allFiles.length; i++){
				String filename = allFiles[i].getPath();
				// male case
				if(filename.contains("m_")){
					Path temp = Files.move(Paths.get(filename), 
							Paths.get("maleBlurb\\" + filename.substring(filename.indexOf('\\') + 1, 
							filename.length()) ));
				}
				// female
				else{
					Path temp = Files.move(Paths.get(filename), 
							Paths.get("femaleBlurb\\" + filename.substring(filename.indexOf('\\') + 1, 
							filename.length()) ));
				}
			}
		} else {
			for(int i = 0; i < allFiles.length; i++){
				String filename = allFiles[i].getPath();
				// male case
				if(filename.contains("m_")){
					Path temp = Files.move(Paths.get(filename), 
							Paths.get("maleBlurb/" + filename.substring(filename.indexOf('/') + 1, filename.length()) ));
				}
				// female
				else{
					Path temp = Files.move(Paths.get(filename), 
							Paths.get("femaleBlurb/" + filename.substring(filename.indexOf('/') + 1, filename.length()) ));
				}
			}
		}
		
		
	}
	
	public static void main(String[] args) throws IOException{
		getFilesIntoFolder();
		// folders with all the data we care about
		File maleDir = new File("maleBlurb");
		File femaleDir = new File("femaleBlurb");
		// store the males and female sets for use in Stable matching
		HashSet<Male> orgMale = new HashSet<Male>();
		HashSet<Female> orgFemale = new HashSet<Female>();

		// hold all of the file names
		ArrayList<personInfo> male = new ArrayList<personInfo>();
		ArrayList<personInfo> female = new ArrayList<personInfo>();

		// populate the male files
		File[] maleFiles = maleDir.listFiles();
		String currFilePath = null;
		for(int i = 0; i < maleFiles.length; i++){
			// get the file path
			currFilePath = maleFiles[i].getPath();
			// file format f_name or h_name . txt files, get the name out
			if (!currFilePath.contains(".txt")) {
				continue;
			}
			String nameOfPerson = currFilePath.substring(currFilePath.indexOf('_') + 1, currFilePath.indexOf('.'));
			personInfo temp = new personInfo(currFilePath, nameOfPerson); 
			male.add(temp);
		}

		// populate the female file
		File[] femaleFiles = femaleDir.listFiles();
		for(int i = 0; i < femaleFiles.length; i++){
			currFilePath = femaleFiles[i].getPath();
			if (!currFilePath.contains(".txt")) {
				continue;
			}
			String nameOfPerson = currFilePath.substring(currFilePath.indexOf('_') + 1, currFilePath.indexOf('.'));
			personInfo temp = new personInfo(currFilePath, nameOfPerson); 
			female.add(temp);
		}
		// start doing male comparisons
		ArrayList<Document> toCompare= new ArrayList<Document>();
		ArrayList<tempRankings> toSort = new ArrayList<tempRankings>();
		for(personInfo m : male){
			toCompare.add(m.document);
			// add all the female comparisons!!
			for(personInfo f : female){
				toCompare.add(f.document);
			}
			Corpus corpus = new Corpus(toCompare);
			VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
			// now we can get Cosine similairties 	
			for(int i = 1; i < toCompare.size(); i++){
				tempRankings newComp = new tempRankings(female.get(i-1).name, vectorSpace.cosineSimilarity(toCompare.get(0), toCompare.get(i)));
				toSort.add(newComp);
			}
			// put the people into an array
			tempRankings[] sorted =  toSort.toArray(new tempRankings[toSort.size()]);

			Arrays.sort(sorted, new Comparator<tempRankings>() {

				@Override
				public int compare(tempRankings entry1, tempRankings entry2) {
					// compare the ranks
					if( entry1.rank == entry2.rank){return 0;}		
					else if (entry1.rank > entry2.rank){return -1;}
					else return 1;
				}
			});
			// create male object
			Male toAdd = new Male(m.name);
			orgMale.add(toAdd);
			// set prefences by iterating thru the linked list 
			toAdd.preference = new LinkedList<Female>();
			for(int k = 0; k < sorted.length; k++){
				Female added = new Female(sorted[k].name);
				toAdd.preference.add(added);

			}
			toCompare.clear();
			toSort.clear();

		}

		// start doing female comparisons
		ArrayList<Document> toCompareFemale= new ArrayList<Document>();
		ArrayList<tempRankings> toSortFemale = new ArrayList<tempRankings>();
		for(personInfo m : female){
			toCompare.add(m.document);
			// add all the female comparisons!!
			for(personInfo f : male){
				toCompare.add(f.document);
			}
			Corpus corpus = new Corpus(toCompare);
			VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
			// now we can get Cosine similairties 	
			for(int i = 1; i < toCompare.size(); i++){
				tempRankings newComp = new tempRankings(male.get(i-1).name, vectorSpace.cosineSimilarity(toCompare.get(0), toCompare.get(i)));
				toSort.add(newComp);
			}
			tempRankings[] sorted =  toSort.toArray(new tempRankings[toSort.size()]);
			// sort with a compare to method
			Arrays.sort(sorted, new Comparator<tempRankings>() {
				@Override
				public int compare(tempRankings entry1, tempRankings entry2) {
					// sort on rank, based on the tdiff!
					if( entry1.rank == entry2.rank){return 0;}		
					else if (entry1.rank > entry2.rank){return -1;}
					else return 1;
				}
			});
			// store 
			Female toAdd = new Female(m.name);
			orgFemale.add(toAdd);
			toAdd.preference = new LinkedList<Male>();
			for(int k = 0; k < sorted.length; k++){
				Male added = new Male(sorted[k].name);
				toAdd.preference.add(added);

			}
			// clear lists to go to next person
			toCompare.clear();
			toSort.clear();
		}

		// perform stable matching
		StableMatching boeing = new StableMatching(orgMale, orgFemale);
		boeing.GaleShapley();

		// get the results
		TreeSet<Male> resultMale = (TreeSet<Male>) boeing.getMales();

		// print out the results to console
		for(Male m : resultMale){
			System.out.println("(" + m.getName() + ") is matched with (" + m.getPartner().getName() + ")");
		}
	}

	static class personInfo{
		String path = "";
		String name = "";
		Document document;
		public personInfo(String path, String name){
			this.name = name;
			document = new Document(path);
		}
	}

	static class tempRankings{
		String name = "";
		double rank = 0.0;

		public tempRankings(String name, double rank){
			this.name = name;
			this.rank = rank;
		}
	}
}
