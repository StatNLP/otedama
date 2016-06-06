//import Reader;

import java.io.IOException;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Rule Tester class
 * 
 * 
 */
public class Rule implements Comparable<Rule> {

    private int ruleID;
    private Map<String, String> context = new TreeMap<String, String>();
    private Map<Integer, Integer> action = new TreeMap<Integer, Integer>();
    private Map<String, Double> score = new TreeMap<String, Double>();

    public Rule() {
    }
    
    public Rule fromRuleFormatAndScores(String ruleformat){
	
	Rule output = new Rule();
	String[] id = ruleformat.split("##");
	output.setRuleID(Integer.parseInt(id[0]));
	String[] stringParts = id[1].split("}");
	String mappings = stringParts[0];
	String reordering = stringParts[1];
	String scoreListString = stringParts[2];
	String[] nodeMapStrings = mappings.substring(1).split("#");
	for (String nodeString : nodeMapStrings) {
	    String[] attributeValuePair = nodeString.split("=");
	    output.setContextValue(attributeValuePair[0], attributeValuePair[1]);
	}
	String[] reorderingMapString = reordering.substring(1).split("=>");
	String[] previousSequence = reorderingMapString[0].split(",");
	String[] newSequence = reorderingMapString[1].split(",");
	for (int j = 0; j < previousSequence.length; j++) {
	    int index = Integer.parseInt(previousSequence[j]);
	    int newIndex = Integer.parseInt(newSequence[j]);
	    output.setActionPair(index, newIndex);
	}
	String[] scoreMapString = scoreListString.substring(1).split("#");
	for (String scoreString : scoreMapString) {
	    String[] nameAndScore = scoreString.split("=");
	    String scoreName = nameAndScore[0];
	    double score = Double.parseDouble(nameAndScore[1]);
	    output.setScore(scoreName, score);
	}
	return output;
	    
    }

    /**
     * Constructor to read .rules file
     * 
     * @param ruleformat
     */
    public Rule(String ruleformat) {

	//Rule output = new Rule();
	String[] id = ruleformat.split("##");
	this.setRuleID(Integer.parseInt(id[0]));
	String[] stringParts = id[1].split("}");
	String mappings = stringParts[0];
	String reordering = stringParts[1];
	String scoreListString = stringParts[2];
	String[] nodeMapStrings = mappings.substring(1).split("#");
	for (String nodeString : nodeMapStrings) {
	    String[] attributeValuePair = nodeString.split("=");
	    this.setContextValue(attributeValuePair[0], attributeValuePair[1]);
	}
	String[] reorderingMapString = reordering.substring(1).split("=>");
	String[] previousSequence = reorderingMapString[0].split(",");
	String[] newSequence = reorderingMapString[1].split(",");
	for (int j = 0; j < previousSequence.length; j++) {
	    int index = Integer.parseInt(previousSequence[j]);
	    int newIndex = Integer.parseInt(newSequence[j]);
	    this.setActionPair(index, newIndex);
	}
	String[] scoreMapString = scoreListString.substring(1).split("#");
	for (String scoreString : scoreMapString) {
	    String[] nameAndScore = scoreString.split("=");
	    String scoreName = nameAndScore[0];
	    double score = Double.parseDouble(nameAndScore[1]);
	    this.setScore(scoreName, score);
	}
	//return output;
    }

    // getter and setter
    public Integer getActionValue(int initialIndex) {
	return this.action.get(initialIndex);
    }

    public void setActionPair(int oldIndex, int newIndex) {
	this.action.put(oldIndex, newIndex);
    }

    public void setContextValue(String attribute, String value) {
	this.context.put(attribute, value);
    }

    public String getValue(String attribute) {
	return this.context.get(attribute);
    }

    public int getRuleID() {
	return ruleID;
    }

    public void setRuleID(int ruleID) {
	this.ruleID = ruleID;
    }

    public Map<String, String> getContext() {
	return context;
    }

    public void setContext(Map<String, String> context) {
	this.context = context;
    }

    public Map<Integer, Integer> getAction() {
	return action;
    }

    public void setAction(Map<Integer, Integer> action) {
	this.action = action;
    }

    public double getScore(String name) {
	return this.score.get(name);
    }

    public void setScore(String name, double scoreValue) {
	this.score.put(name, scoreValue);
    }

    @Override
    public String toString() {
	//return this.context.toString() + this.action.toString();
	List<String> keyList = new ArrayList<String>(this.context.keySet());
	Collections.sort(keyList);
	String output = "{";
        for (String key : keyList) {
            output = output + key + "=" + this.getValue(key) + "#";
        }
        output = output.substring(0, output.length() - 1);
        output = output + "}{";
        List<Integer> actionKeyList = new ArrayList<Integer>( this.action.keySet());
        Collections.sort(actionKeyList);
        for (int key : actionKeyList) {
            output = output + key + ",";
        }
        output = output.substring(0, output.length() - 1);
        output = output + "=>";
        for (int key : actionKeyList) {
            output = output + this.getActionValue(key) + ",";
        }
        output = output.substring(0, output.length() - 1);
        output = output + "}";
        
        return output;
    }

    public String toRuleFormat() {
	String output = ruleID + "##{";
	for (String key : this.context.keySet()) {
	    output = output + key + "=" + this.getValue(key) + "#";
	}
	output = output.substring(0, output.length() - 1);
	output = output + "}{";
	Set<Integer> keys = this.action.keySet();
	for (int key : keys) {
	    output = output + key + ",";
	}
	output = output.substring(0, output.length() - 1);
	output = output + "=>";
	for (int key : keys) {
	    output = output + this.getActionValue(key) + ",";
	}
	output = output.substring(0, output.length() - 1);
	output = output + "}{";
	for (String key : this.score.keySet()) {
	    output = output + key + "=" + this.getScore(key) + "#";
	}
	output = output.substring(0, output.length() - 1);
	output = output + "}";
	return output;
    }
    
    public boolean equals(Object o){
      if (o == null){
	return false;
      }
      if (o == this){
	return true;
      } 
      if (o.getClass() == this.getClass()){
	Rule r = (Rule)o;
	if (this.context.equals(r.getContext()) && this.action.equals(r.getAction())){
	  return true;
	}
      }
      return false;
    }
    @Override
    public int compareTo(Rule r){
        if (r.getContext().size() == this.getContext().size()){
            return 0;
        } else if (this.getContext().size() < r.getContext().size()){
            return -1;
        }         
        return 1;
    }
}
