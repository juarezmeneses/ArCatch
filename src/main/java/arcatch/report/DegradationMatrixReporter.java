package arcatch.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arcatch.dsl.rule.drift.grammar.AntiDriftRule;
import arcatch.dsl.rule.drift.impl.AntiDriftRuleViolation;
import arcatch.dsl.rule.erosion.grammar.AntiErosionRule;
import arcatch.dsl.rule.erosion.impl.AntiErosionRuleViolation;
import arcatch.model.Model;
import arcatch.model.Unit;
import arcatch.util.Matrix;
import arcatch.util.Util;

public class DegradationMatrixReporter implements Reporter {

	@Override
	public void print() {
		int rows = Model.getUnits().size();
		int columns = Model.getFailingAntiDriftRules().size() + Model.getFailingAntiErosionRules().size();
		Matrix matrix = new Matrix("Unit", rows, columns);

		List<AntiDriftRule> antiDriftRules = new ArrayList<>(Model.getFailingAntiDriftRules());
		Collections.sort(antiDriftRules);
		double averageDivisor = 0.0;
		for (AntiDriftRule rule : antiDriftRules) {
			averageDivisor += rule.getCriticality().getValue();
			AntiDriftRuleViolation violation = rule.getViolation();
			List<Unit> unis = new ArrayList<Unit>(violation.getUnits());
			Collections.sort(unis);
			for (Unit unit : unis) {
				matrix.addData(unit.getQualifiedName(), rule.getId(), rule.getCriticality().getValue());
			}
		}

		List<AntiErosionRule> antiErosionRules = new ArrayList<>(Model.getFailingAntiErosionRules());
		Collections.sort(antiErosionRules);
		for (AntiErosionRule rule : antiErosionRules) {
			averageDivisor += rule.getCriticality().getValue();
			AntiErosionRuleViolation violation = rule.getViolation();
			List<Unit> unis = violation.getUnits();
			Collections.sort(unis);
			for (Unit unit : unis) {
				matrix.addData(unit.getQualifiedName(), rule.getId(), rule.getCriticality().getValue());
			}
		}
		
		matrix.setAverageDivisor(averageDivisor);
		matrix.comput();
		String projectLabel = Model.getConfiguration().getProjectName().toLowerCase().trim();
		projectLabel += Model.getConfiguration().getProjectVersion().toLowerCase().trim();
		Util.generateCSVFile(projectLabel + "-degradation-matrix", matrix.toCSV());
	}

}
