package org.sunflower.containers.graph;

import java.util.List;

public class GraphPath
{
	String concept;
	
	double score;
	int length;
	List<GraphLink> edges;
	
	public GraphPath() {
		
	}
	
	public GraphPath(String concept, List<GraphLink> edges) {
		this.concept = concept;
		this.edges = edges;
		length = edges.size();
		score = edges.get(0).getTotalScore();
	}
	
	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public List<GraphLink> getEdges() {
		return edges;
	}

	public void setEdges(List<GraphLink> edges) {
		this.edges = edges;
	}
}
