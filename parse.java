import java.util.*;
import java.io.*;

class parse {
	public static void main(String[] args){
		//Used to store the information of the NFA
		int numStates;
		ArrayList<String> inputs;
		ArrayList<ArrayList<String>> transitions = new ArrayList<ArrayList<String>>();
		int startState;
		String[] finalStates;
		try{
			//Parsing the NFA file
			Scanner file = new Scanner(new File(args[0]));
			String line = file.nextLine();
			
			numStates = Integer.parseInt(line);
			line = file.nextLine();
			inputs = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
			inputs.add(" "); //Adds in the Lamda input (mostly for the transitions variable)

			for(int i = 0; i < numStates; i++){
				//System.out.println(Arrays.toString(file.nextLine().replace("{","").replace("}","").split("\\s+")));
				String[] firstParse = file.nextLine().split("\\s+");
				transitions.add(new ArrayList<String>());
				for(int k = 1; k < firstParse.length; k++){
					//System.out.println(Arrays.toString(firstParse));
					//System.out.println(Arrays.toString(firstParse[k].split(",")));
					//System.out.println(firstParse[k]);
					transitions.get(i).add(firstParse[k]);
				}
			}
			//for (int i = 0; i < transitions.size(); i++){
			//	System.out.println(i + ": " + Arrays.toString(transitions.get(i).toArray()));
			//}
			startState = Integer.parseInt(file.nextLine());

			line = file.nextLine();
			finalStates = line.replace("{","").replace("}","").split(",");
			//System.out.println(Arrays.toString(finalStates));

			file.close();
			//Conversion to DFA
			ArrayList<ArrayList<Integer>> DFAStates = new ArrayList<ArrayList<Integer>>();
			String[] initState = transitions.get(startState).get(inputs.size() -1).replace("{","").replace("}","").split(",");
			//System.out.println(Arrays.toString(initState));
			DFAStates.add(new ArrayList<Integer>());
			//Lambda transition of the start state is turned into the initial state of the DFA
			for(int i = 0; i < initState.length; i++){
				//Makes sure not to include a lambda transition that does not go anywhere (i.e. it does not have a lambda transition)
				if(!initState[i].equals("")){
					DFAStates.get(0).add(Integer.parseInt(initState[i]));
				}
			}
			//Makes sure to include the start state into the new DFA state
			DFAStates.get(0).add(startState);

			//Creates a 3-D array that stores the different transitons for the different inputs.
			ArrayList<ArrayList<ArrayList<Integer>>> DFATransitions = new ArrayList<ArrayList<ArrayList<Integer>>>();
			DFATransitions.add(new ArrayList<ArrayList<Integer>>()); //Adds the transition array for the initial DFA state
			//System.out.println(Arrays.toString(DFAStates.get(0).toArray()));
			for(int i = 0; i < DFAStates.size(); i++){
				//System.out.println(DFAStates.get(i));
				for(int k = 0; k < inputs.size() - 1; k++){
					DFATransitions.get(i).add(new ArrayList<Integer>()); //Adds the array for each of the inputs excluding the lambda transition
				}
				for(Integer state: DFAStates.get(i)){
					//System.out.println(state);
					for(int k = 0; k < transitions.get(state).size(); k++){
						//System.out.println(transitions.get(state).size());
						if (k != transitions.get(state).size() -1){
							String[] temp = transitions.get(state).get(k).replace("{","").replace("}","").split(","); //Gets the transition for this state and the k input
							for(String s: temp){
								if(!s.equals("")){
									//As long as the transition is not empty convert it and if it's not in the DFA transition add it to the transition for this state and
									//the k input.
									Integer curr = Integer.parseInt(s);
									if(!DFATransitions.get(i).get(k).contains(curr)){
										DFATransitions.get(i).get(k).add(curr);
									}
								}
							}
						}//else{ //Deals with the lambda transitions in the NFA
							//No matter what your input is you are able to go to the next state because it does not require anything if it is lambda
							//thus it is added to the transition for this state on both inputs (May end up in different DFA states since the differe
							//-nt inputs will create unique transitions lists even though they both contain lambda transitions).
							//String[] temp = transitions.get(state).get(k).replace("{","").replace("}","").split(",");
							//for(int m = 0; m < transitions.get(state).size() -1; m++){
							//	for(String s: temp){
							//		if(!s.equals("")){
							//			Integer curr = Integer.parseInt(s);
							//			if(!DFATransitions.get(i).get(m).contains(curr)){
							//				DFATransitions.get(i).get(m).add(curr);
							//			}
							//		}
							//	}
							//}
						//}
					}
				}	
				//Deals with the turning of transitions to states
				for(int k = 0; k < DFATransitions.get(i).size(); k++){ //Adds all of the lambda transitions of member states in this transition to the current transition.
					for(int n = 0; n < DFATransitions.get(i).get(k).size(); n++){
						String[] temp = transitions.get(DFATransitions.get(i).get(k).get(n)).get(inputs.size() -1).replace("{","").replace("}","").split(",");
						for(String curr: temp){
							if(!curr.equals("") && !DFATransitions.get(i).get(k).contains(Integer.parseInt(curr))){
								DFATransitions.get(i).get(k).add(Integer.parseInt(curr));
							}
						}
					}
					//Sorts my transitions for comparisons
					Collections.sort(DFATransitions.get(i).get(k));
					if(!DFAStates.contains(DFATransitions.get(i).get(k))){
						//After creating the transitions you compare them to the states and only add them if they are not already apart of the states.
						//System.out.println(DFAStates.get(i));
						DFAStates.add(DFATransitions.get(i).get(k));
						DFATransitions.add(new ArrayList<ArrayList<Integer>>());
					}
				}
			}
			
			ArrayList<ArrayList<Integer>> DFAFinalStates = new ArrayList<ArrayList<Integer>>();
			for(int i = 0; i < DFAStates.size(); i++){
				for(int k = 0; k < finalStates.length; k++){
					if(DFAStates.get(i).contains(Integer.parseInt(finalStates[k])) && !DFAFinalStates.contains(DFAStates.get(i))){
						DFAFinalStates.add(DFAStates.get(i));	
					}
				}
			}
			
			//Start of minimization
			int[][] distinguishableTable = new int[DFAStates.size()][DFAStates.size()];

			for(int i = 0; i < distinguishableTable.length; i++)
			{
				distinguishableTable[i][i] = 0;
			}

			ArrayList<ArrayList<Integer>> minimizedDFAStates = new ArrayList<ArrayList<Integer>>();

			//Goes through every pair and if one is not a final state and the other is it marks them as distinguishable
			for(int i = 0; i < distinguishableTable.length; i++){
				for(int k = 0; k < distinguishableTable[i].length; k++){
					//System.out.println(i + " " + k);
					if((DFAFinalStates.contains(DFAStates.get(i)) && !DFAFinalStates.contains(DFAStates.get(k))) || (!DFAFinalStates.contains(DFAStates.get(i)) && DFAFinalStates.contains(DFAStates.get(k)))){
						//System.out.println(i + " " + k);
						distinguishableTable[i][k] = 1;
					}
				}
			}

			boolean changed = true;
	
			//checks if any of the state pair that a indistinguishable state pair produces is distinguishable then the original is also distinguishable
			while(changed){
				changed = false;
				for(int s = 0; s < inputs.size() -1; s++){
					for(int i = 0; i < distinguishableTable.length; i++){
						for(int k = 0; k < distinguishableTable.length; k++){
							if(distinguishableTable[i][k] == 0){
								//System.out.println("Sigma " + inputs.get(s));
								int row = DFAStates.indexOf(DFATransitions.get(i).get(s));
								int collumn = DFAStates.indexOf(DFATransitions.get(k).get(s));
								if(distinguishableTable[row][collumn] == 1){
									distinguishableTable[i][k] = 1;
									changed = true;
								}
							}
						}
					}
				}
			}

			//Combines all the indistinguishable states
			for(int i = 0; i < distinguishableTable.length; i++){
				ArrayList<Integer> combined = new ArrayList<Integer>();
				combined.add(i);
				for(int k = 0; k <distinguishableTable[i].length; k++){
					//System.out.print(distinguishableTable[i][k] + "	");
					if((i != k) && (distinguishableTable[i][k] == 0)){
						combined.add(k);
					}
				}
				Collections.sort(combined);
				if(!minimizedDFAStates.contains(combined)){
					minimizedDFAStates.add(combined);
				}
			}
			
			//Computes the final states
			ArrayList<ArrayList<Integer>> minimizedFinalStates = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> fStates = new ArrayList<Integer>();
			for(ArrayList<Integer> dfaFinal: DFAFinalStates){
				fStates.add(DFAStates.indexOf(dfaFinal));
			}
			for(ArrayList<Integer> state: minimizedDFAStates){
				boolean isFinal = true;
				for(Integer dfaState: state){
					if(!fStates.contains(dfaState)){
						isFinal = false;
					}
				}
				if(isFinal){minimizedFinalStates.add(state);}
			}

			Scanner inputMin = new Scanner(new File(args[1]));

			ArrayList<String> acceptedMin = new ArrayList<String>();
			while(inputMin.hasNextLine()){
				String data = inputMin.nextLine();
				char[] string = data.toCharArray();
				int currState = 0;
				for(char a: string){
					if(inputs.contains(String.valueOf(a))){
						Integer dfaTrans = DFAStates.indexOf(DFATransitions.get(minimizedDFAStates.get(currState).get(0)).get(inputs.indexOf(String.valueOf(a))));
						for(int i = 0; i < minimizedDFAStates.size(); i++){
							if(minimizedDFAStates.get(i).contains(dfaTrans)){
								currState = i;
							}
						}
						//currState = DFAStates.indexOf(DFATransitions.get(currState).get(inputs.indexOf(String.valueOf(a))));
					} else {
						currState = -1;
						break;
					}
				}
				boolean accept = false;
				for(ArrayList<Integer> f: minimizedFinalStates){
					if(minimizedDFAStates.indexOf(f) == currState){
						accept = true;
					}
				}
				if(accept){
					acceptedMin.add(data);
				}
			}

			System.out.println(minimizedDFAStates.size());
			System.out.println("Minimized DFA from " + args[0] + ":");
			System.out.print("  Sigma:	");
			for(String sigma: inputs){
				System.out.print(sigma + "	");
			}
			System.out.println();
			System.out.println("------------------");
			for(int i = 0; i < minimizedDFAStates.size(); i++){
				System.out.print("	" + i + ":	");
				for (int l = 0; l < inputs.size()-1; l++){
					Integer currState = 0;
					Integer dfaTrans = DFAStates.indexOf(DFATransitions.get(minimizedDFAStates.get(i).get(0)).get(l));
					for(int k = 0; k < minimizedDFAStates.size(); k++){
							if(minimizedDFAStates.get(k).contains(dfaTrans)){
								currState = k;
							}
					}
					System.out.print(currState + "        ");
				}
				System.out.println();
			}
			System.out.println("------------------");
			System.out.println("0: Initial State");
			for(ArrayList<Integer> f: minimizedFinalStates){
				System.out.print(minimizedDFAStates.indexOf(f) + " ");
			}
			System.out.println(": Accepting State(s)");
			System.out.println("The following are accepted:");
			for(String a: acceptedMin){
				System.out.println(a);
			}

		} catch (FileNotFoundException e) {
			System.out.println(e);
			System.out.println("Errors have happened (either in filename or format of files)");
		}
	}
}
