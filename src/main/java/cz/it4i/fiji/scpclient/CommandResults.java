
package cz.it4i.fiji.scpclient;

import java.util.List;

public class CommandResults {

	private final List<String> results;
	private final List<String> errors;

	public CommandResults(List<String> newResults, List<String> newErrors) {
		this.results = newResults;
		this.errors = newErrors;
	}

	public List<String> getResults() {
		return this.results;
	}

	public List<String> getErrors() {
		return this.errors;
	}
}
