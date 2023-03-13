package de.buw.fm4se;

import static edu.mit.csail.sdg.alloy4.A4Reporter.NOP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Func;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;

public class AlloySemanticDifferencesChecker {

	public static void main(String[] args) throws Err {

		String filename = "/home/shuma/Documents/Bauhaus/02-Formal-Methods/Final_Proyect/dreadbury-facts.als";
		String nameExpressionToEvaluate01 = "someoneKilledAgatha";
		String nameExpressionToEvaluate02 = "someoneKilledAgatha_ALT";

		A4Reporter rep = new A4Reporter() {
			@Override
			public void warning(ErrorWarning msg) {
				System.out.print("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
				System.out.flush();
			}
		};

		Module world = CompUtil.parseEverything_fromFile(rep, null, filename);
		A4Options options = new A4Options();
		options.solver = A4Options.SatSolver.SAT4J;

		ConstList<Sig> sigUser = world.getAllReachableUserDefinedSigs();
		SafeList<Pair<String, Expr>> facts = world.getAllFacts();
		SafeList<Func> predicates = world.getAllFunc();

		List<Sig> sigs = new ArrayList<>();
		Map<String, Expr> mapFacts = new HashMap<>();
		Map<String, Func> mapPredicates = new HashMap<>();

		System.out.println(world.getAllCommands());

		Iterable<Sig> sigsa = sigUser.make(null);
		
		for (int i = 0; i < sigUser.size(); i++) {
			sigs.add(sigUser.get(i));
		}

		if (facts.size() == 0) {
			for (int i = 0; i < predicates.size(); i++) {
				if (predicates.get(i).label.equals("this/" + nameExpressionToEvaluate01)
						|| predicates.get(i).label.equals("this/" + nameExpressionToEvaluate02)) {
					mapPredicates.put(predicates.get(i).label, predicates.get(i));
				}
			}
		} else {
			for (int i = 0; i < facts.size(); i++) {
				if (facts.get(i).a.equals(nameExpressionToEvaluate01)
						|| facts.get(i).a.equals(nameExpressionToEvaluate02)) {
					mapFacts.put(facts.get(i).a, facts.get(i).b);
				}
			}
		}

		System.out.println(getEvaluationPredicates(sigsa, mapFacts, mapPredicates, options));

	}

	public static String getEvaluationPredicates(Iterable<Sig> sigs, Map<String, Expr> mapFacts,
			Map<String, Func> mapPredicates, A4Options options) {

		String result = "";

		Expr expression01 = null;
		Expr expression02 = null;

		if (mapFacts.size() == 0) {
			expression01 = mapPredicates.get(mapPredicates.keySet().toArray()[0]).returnDecl;
			expression02 = mapPredicates.get(mapPredicates.keySet().toArray()[1]).returnDecl;
		} else {
			expression01 = mapFacts.get(mapFacts.keySet().toArray()[0]);
			expression02 = mapFacts.get(mapFacts.keySet().toArray()[1]);
		}

		// run {not (someoneKilledAgatha iff someoneKilledAgatha_ALT)} for 3
		Expr equivalenceExpression = (expression01.iff(expression02)).not();
		
		Command cmd = new Command(false, 3, 3, 3, equivalenceExpression);

		A4Solution sol1 = TranslateAlloyToKodkod.execute_command(NOP, sigs, cmd, options);
		
		// run { some A && atMostThree[B,B] } for 3 but 3 int, 3 seq
//		Expr equivalenceExpressiona = A.some().and(atMost3.call(B, B));
//		Command cmd1 = new Command(false, 3, 3, 3, expr1);
//		A4Solution sol1 = TranslateAlloyToKodkod.execute_command(NOP, sigs, cmd1, opt);

		return result;
	}
}