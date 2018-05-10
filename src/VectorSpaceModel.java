import java.util.HashMap;
import java.util.Set;

public class VectorSpaceModel {
	private Corpus corpus;
	private HashMap<Document, HashMap<String, Double>> tfIdfWeights;
	
	public VectorSpaceModel(Corpus corpus) {
		this.corpus = corpus;
		tfIdfWeights = new HashMap<Document, HashMap<String, Double>>();
		
		createTfIdfWeights();
	}

	private void createTfIdfWeights() {
		Set<String> terms = corpus.getInvertedIndex().keySet();
		
		for (Document document : corpus.getDocuments()) {
			HashMap<String, Double> weights = new HashMap<String, Double>();
			
			for (String term : terms) {
				double tf = document.getTermFrequency(term);
				double idf = corpus.getInverseDocumentFrequency(term);
				
				double weight = tf * idf;
				
				weights.put(term, weight);
			}
			tfIdfWeights.put(document, weights);
		}
	}
	
	private double getMagnitude(Document document) {
		double magnitude = 0;
		HashMap<String, Double> weights = tfIdfWeights.get(document);
		
		for (double weight : weights.values()) {
			magnitude += weight * weight;
		}
		
		return Math.sqrt(magnitude);
	}

	private double getDotProduct(Document d1, Document d2) {
		double product = 0;
		HashMap<String, Double> weights1 = tfIdfWeights.get(d1);
		HashMap<String, Double> weights2 = tfIdfWeights.get(d2);
		
		for (String term : weights1.keySet()) {
			product += weights1.get(term) * weights2.get(term);
		}
		
		return product;
	}

	public double cosineSimilarity(Document d1, Document d2) {
		return getDotProduct(d1, d2) / (getMagnitude(d1) * getMagnitude(d2));
	}
}