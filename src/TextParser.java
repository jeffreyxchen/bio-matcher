import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

public class TextParser {
	// move files from allPeople into appropriate folders
	static void getFilesIntoFolder() throws IOException {
		File allDir = new File("allPeople");

		File[] allFiles = allDir.listFiles();

		if (allFiles == null) {
			throw new IOException("Add text files");
		}

		String os = System.getProperty("os.name");
		String osName = os.trim().substring(0, os.indexOf(' '));

		for (int i = 0; i < allFiles.length; i++) {
			String filename = allFiles[i].getPath();
			// male
			if (filename.contains("m_")) {
				if (osName.equals("Windows")) {
					Path temp = Files.move(Paths.get(filename), 
							Paths.get("maleBlurb\\" + filename.substring(filename.indexOf('\\') + 1, 
									filename.length())));
				} else {
					Path temp = Files.move(Paths.get(filename),
							Paths.get("maleBlurb/" + filename.substring(filename.indexOf('/') + 1, 
									filename.length())));
				}
			}
			// female
			else {
				if (osName.equals("Windows")) {
					Path temp = Files.move(Paths.get(filename), 
							Paths.get("femaleBlurb\\" + filename.substring(filename.indexOf('\\') + 1, 
									filename.length())));
				} else {
					Path temp = Files.move(Paths.get(filename), 
							Paths.get("femaleBlurb/" + filename.substring(filename.indexOf('/') + 1, 
									filename.length())));
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		getFilesIntoFolder();

		// gender segregated folders
		File maleDir = new File("maleBlurb");
		File femaleDir = new File("femaleBlurb");

		HashSet<Male> orgMale = new HashSet<Male>();
		HashSet<Female> orgFemale = new HashSet<Female>();

		// file names
		ArrayList<personInfo> male = new ArrayList<personInfo>();
		ArrayList<personInfo> female = new ArrayList<personInfo>();
		
		String currFilePath = null;
		
		// populate male files
		File[] maleFiles = maleDir.listFiles();
		for (int i = 0; i < maleFiles.length; i++) {
			currFilePath = maleFiles[i].getPath();
			if (!currFilePath.contains(".txt")) {
				continue;
			}
			String nameOfPerson = currFilePath.substring(currFilePath.indexOf('_') + 1, 
					currFilePath.indexOf('.'));
			personInfo temp = new personInfo(currFilePath, nameOfPerson);
			male.add(temp);
		}

		// populate female files
		File[] femaleFiles = femaleDir.listFiles();
		for (int i = 0; i < femaleFiles.length; i++) {
			currFilePath = femaleFiles[i].getPath();
			if (!currFilePath.contains(".txt")) {
				continue;
			}
			String nameOfPerson = currFilePath.substring(currFilePath.indexOf('_') + 1, 
					currFilePath.indexOf('.'));
			personInfo temp = new personInfo(currFilePath, nameOfPerson);
			female.add(temp);
		}

		// male comparisons
		ArrayList<Document> toCompare = new ArrayList<Document>();
		ArrayList<tempRankings> toSort = new ArrayList<tempRankings>();
		for (personInfo m : male) {
			toCompare.add(m.document);
			for (personInfo f : female) {
				toCompare.add(f.document);
			}
			Corpus corpus = new Corpus(toCompare);
			VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
			
			// cosine similarities
			for (int i = 1; i < toCompare.size(); i++) {
				tempRankings newComp = new tempRankings(female.get(i - 1).name,
						vectorSpace.cosineSimilarity(toCompare.get(0), toCompare.get(i)));
				toSort.add(newComp);
			}
			tempRankings[] sorted = toSort.toArray(new tempRankings[toSort.size()]);

			Arrays.sort(sorted, new Comparator<tempRankings>() {
				@Override
				public int compare(tempRankings entry1, tempRankings entry2) {
					if (entry1.rank == entry2.rank) {
						return 0;
					} else if (entry1.rank > entry2.rank) {
						return -1;
					} else
						return 1;
				}
			});
			
			Male toAdd = new Male(m.name);
			orgMale.add(toAdd);
			toAdd.setPreference(new LinkedList<Female>());
			
			for (int k = 0; k < sorted.length; k++) {
				Female added = new Female(sorted[k].name);
				toAdd.addPreference(added);
			}
			
			toCompare.clear();
			toSort.clear();
		}

		// female comparisons
		ArrayList<Document> toCompareFemale = new ArrayList<Document>();
		ArrayList<tempRankings> toSortFemale = new ArrayList<tempRankings>();
		for (personInfo m : female) {
			toCompare.add(m.document);
			for (personInfo f : male) {
				toCompare.add(f.document);
			}
			Corpus corpus = new Corpus(toCompare);
			VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);
			
			// cosine similarities
			for (int i = 1; i < toCompare.size(); i++) {
				tempRankings newComp = new tempRankings(male.get(i - 1).name,
						vectorSpace.cosineSimilarity(toCompare.get(0), toCompare.get(i)));
				toSort.add(newComp);
			}
			tempRankings[] sorted = toSort.toArray(new tempRankings[toSort.size()]);
			
			Arrays.sort(sorted, new Comparator<tempRankings>() {
				@Override
				public int compare(tempRankings entry1, tempRankings entry2) {
					// sort on rank, based on the tdiff!
					if (entry1.rank == entry2.rank) {
						return 0;
					} else if (entry1.rank > entry2.rank) {
						return -1;
					} else
						return 1;
				}
			});

			Female toAdd = new Female(m.name);
			orgFemale.add(toAdd);
			toAdd.setPreference(new LinkedList<Male>());
			
			for (int k = 0; k < sorted.length; k++) {
				Male added = new Male(sorted[k].name);
				toAdd.addPreference(added);
			}
			
			toCompare.clear();
			toSort.clear();
		}

		StableMatching boeing = new StableMatching(orgMale, orgFemale);
		boeing.GaleShapley();

		TreeSet<Male> resultMale = (TreeSet<Male>) boeing.getMales();

		// printed results
		for (Male m : resultMale) {
			System.out.println("(" + m.getName() + ") is matched with (" + m.getPartner().getName() + ")");
		}
	}

	static class personInfo {
		String path = "";
		String name = "";
		Document document;

		public personInfo(String path, String name) {
			this.name = name;
			document = new Document(path);
		}
	}

	static class tempRankings {
		String name = "";
		double rank = 0.0;

		public tempRankings(String name, double rank) {
			this.name = name;
			this.rank = rank;
		}
	}
}
