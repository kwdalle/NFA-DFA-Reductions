import java.util.*;
import java.io.*;

class NFA {
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
			
			//Prints out the NFA information in the format specified
			System.out.print("Sigma:");
			for(int i = 0; i < inputs.size(); i++){
				System.out.print(inputs.get(i) + " ");
			}
			System.out.println();
			System.out.println("------");

			for(int i = 0; i < transitions.size(); i++){
				System.out.print(i + ":  ");
				for(int k = 0; k < transitions.get(i).size(); k++){
					System.out.print("(" + inputs.get(k) + "," + transitions.get(i).get(k) + ") ");
				}
				System.out.println();
			}
			System.out.println("------");
			System.out.println(startState + ":	Initial State");
			for(int i = 0; i < finalStates.length; i++){
				System.out.print(finalStates[i] + " ");
			}
			System.out.println(":	Accepting State(s)\n");

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
						}else{ //Deals with the lambda transitions
							//No matter what your input is you are able to go to the next state because it does not require anything if it is lambda
							//thus it is added to the transition for this state on both inputs (May end up in different DFA states since the differe
							//-nt inputs will create unique transitions lists even though they both contain lambda transitions).
							String[] temp = transitions.get(state).get(k).replace("{","").replace("}","").split(",");
							for(int m = 0; m < transitions.get(state).size() -1; m++){
								for(String s: temp){
									if(!s.equals("")){
										Integer curr = Integer.parseInt(s);
										if(!DFATransitions.get(i).get(m).contains(curr)){
											DFATransitions.get(i).get(m).add(curr);
										}
									}
								}
							}
						}
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
			System.out.println("To DFA:");
			System.out.print(" Sigma:	");
			for(String input: inputs){
				System.out.print("	" + input);
			}
			System.out.println();
			System.out.println("------------------");
			for(int i = 0; i < DFAStates.size(); i++){
				System.out.print("	" + i + ":");
				for(int k = 0; k < inputs.size()-1; k++){
					System.out.print("	" + DFAStates.indexOf(DFATransitions.get(i).get(k)));
				}
				System.out.println();
			}
			System.out.println("------------------");
			System.out.println("0:	Initial State");
			for(ArrayList<Integer> f: DFAFinalStates){
				System.out.print(DFAStates.indexOf(f) + " ");
			}
			System.out.println(":	Accepting State(s)");


			//Going through input strings given
			Scanner input = new Scanner(new File(args[1]));

			ArrayList<String> accepted = new ArrayList<String>();
		
			while(input.hasNextLine()){
				String data = input.nextLine();
				char[] string = data.toCharArray();
				int currState = 0;
				for(char a: string){
					if(inputs.contains(String.valueOf(a))){
						currState = DFAStates.indexOf(DFATransitions.get(currState).get(inputs.indexOf(String.valueOf(a))));
					} else {
						currState = -1;
						break;
					}
				}
				boolean accept = false;
				for(ArrayList<Integer> f: DFAFinalStates){
					if(DFAStates.indexOf(f) == currState){
						accept = true;
					}
				}
				if(accept){
					accepted.add(data);
				}
			}

			System.out.println("The following Strings are accepted:");
			for(String data: accepted){
				System.out.println(data);
			}
		} catch (Exception e) {
			System.out.println("Errors have happened (either in filename or format of files)");
		}
	}
}
