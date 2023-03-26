package com.tngtech.jgiven.report.asciidoc;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.jgiven.annotation.Table;
import com.tngtech.jgiven.report.CasesTable;
import com.tngtech.jgiven.report.model.DataTable;
import com.tngtech.jgiven.report.model.ExecutionStatus;
import com.tngtech.jgiven.report.model.ReportStatistics;
import com.tngtech.jgiven.report.model.ScenarioCaseModel;
import com.tngtech.jgiven.report.model.ScenarioModel;
import com.tngtech.jgiven.report.model.StepStatus;
import com.tngtech.jgiven.report.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class AsciiDocReportBlockConverterTest {

    private AsciiDocReportBlockConverter converter;


    @Before
    public void setUp() {
        converter = new AsciiDocReportBlockConverter();
    }

    @Test
    public void convert_feature_header_without_description() {
        // arrange
        final ReportStatistics statistics = new ReportStatistics();
        statistics.numScenarios = 42;
        statistics.numFailedScenarios = 21;
        statistics.numPendingScenarios = 13;
        statistics.numSuccessfulScenarios = 8;

        // act
        final String block = converter.convertFeatureHeaderBlock("My first feature", statistics, null);

        // assert
        assertThatBlockContainsLines(block,
                "=== My first feature",
                "",
                "8 Successful, 21 Failed, 13 Pending, 42 Total (0s 0ms)");
    }

    @Test
    public void convert_feature_header_with_description() {
        // arrange
        final ReportStatistics statistics = new ReportStatistics();
        statistics.numScenarios = 42;
        statistics.numFailedScenarios = 21;
        statistics.numPendingScenarios = 13;
        statistics.numSuccessfulScenarios = 8;

        // act
        final String block = converter.convertFeatureHeaderBlock("My first feature", statistics, "A very nice feature.");

        // assert
        assertThatBlockContainsLines(block,
                "=== My first feature",
                "",
                "8 Successful, 21 Failed, 13 Pending, 42 Total (0s 0ms)",
                "",
                "++++",
                "A very nice feature.",
                "++++");
    }

    @Test
    @DataProvider({"SUCCESS, successful, SUCCESS", "FAILED, failed, FAILED", "SCENARIO_PENDING, pending, PENDING",
            "SOME_STEPS_PENDING, pending, PENDING"})
    public void convert_scenario_header_without_tags_or_description(final ExecutionStatus status, final String scenarioTag,
            final String humanStatus) {
        // arrange
        List<String> tagNames = new ArrayList<>();

        // act
        String block = converter.convertScenarioHeaderBlock("my first scenario", status, 1000300000L, tagNames, null);

        // assert
        assertThatBlockContainsLines(block,
                "// tag::scenario-" + scenarioTag + "[]",
                "",
                "==== My first scenario",
                "",
                "[" + humanStatus + "] (1s 0ms)");
    }

    @Test
    public void convert_scenario_header_with_tags_and_no_description() {
        // arrange
        List<String> tagNames = new ArrayList<>();
        tagNames.add("Best Tag");

        // act
        String block = converter.convertScenarioHeaderBlock("my first scenario",
                ExecutionStatus.SCENARIO_PENDING, 9000000L, tagNames, "");

        // assert
        assertThatBlockContainsLines(block,
                "// tag::scenario-pending[]",
                "",
                "==== My first scenario",
                "",
                "[PENDING] (0s 9ms)",
                "",
                "Tags: _Best Tag_");
    }

    @Test
    public void convert_scenario_header_with_description_and_no_tags() {
        // arrange
        List<String> tagNames = new ArrayList<>();

        // act
        String block = converter.convertScenarioHeaderBlock("my first scenario",
                ExecutionStatus.SOME_STEPS_PENDING, 2005000000L, tagNames, "Best scenario ever!!!");

        // assert
        assertThatBlockContainsLines(block,
                "// tag::scenario-pending[]",
                "",
                "==== My first scenario",
                "",
                "[PENDING] (2s 5ms)",
                "",
                "++++",
                "Best scenario ever!!!",
                "++++");
    }

    @Test
    public void convert_scenario_header_with_tags_and_description() {
        // arrange
        List<String> tagNames = new ArrayList<>();
        tagNames.add("Best Tag");

        // act
        String block = converter.convertScenarioHeaderBlock("my first scenario", ExecutionStatus.SUCCESS, 3000000000L, tagNames,
                "Best scenario ever!!!");

        // assert
        assertThatBlockContainsLines(block,
                "// tag::scenario-successful[]",
                "",
                "==== My first scenario",
                "",
                "[SUCCESS] (3s 0ms)",
                "",
                "++++",
                "Best scenario ever!!!",
                "++++",
                "",
                "Tags: _Best Tag_");
    }

    @Test
    public void convert_case_header_without_parameters_and_description() {
        // arrange
        List<String> parameterNames = Collections.emptyList();
        List<String> parameterValues = Collections.emptyList();

        // act
        String block = converter.convertCaseHeaderBlock(1, parameterNames, parameterValues, null);

        // assert
        assertThat(block).isEqualTo("===== Case 1");
    }

    @Test
    public void convert_case_header_with_description_and_without_parameters() {
        // arrange
        List<String> parameterNames = Collections.singletonList("description");
        List<String> parameterValues = Collections.singletonList("First case");

        // act
        String block = converter.convertCaseHeaderBlock(1, parameterNames, parameterValues, "First case");

        // assert
        assertThatBlockContainsLines(block,
                "===== Case 1 First case",
                "",
                "====",
                "description = First case",
                "====");
    }

    @Test
    public void convert_case_header_with_one_parameter() {
        // arrange
        List<String> parameterNames = Collections.singletonList("foo");
        List<String> parameterValues = Collections.singletonList("42");

        // act
        String block = converter.convertCaseHeaderBlock(2, parameterNames, parameterValues, null);

        // assert
        assertThatBlockContainsLines(block,
                "===== Case 2",
                "",
                "====",
                "foo = 42",
                "====");
    }

    @Test
    public void convert_case_header_with_two_parameters() {
        // arrange
        List<String> parameterNames = new ArrayList<>();
        parameterNames.add("foo");
        parameterNames.add("bar");
        List<String> parameterValues = new ArrayList<>();
        parameterValues.add("42");
        parameterValues.add("on");

        // act
        String block = converter.convertCaseHeaderBlock(2, parameterNames, parameterValues, null);

        // assert
        assertThatBlockContainsLines(block,
                "===== Case 2",
                "",
                "====",
                "foo = 42, bar = on",
                "====");
    }

    @Test
    public void convert_step_without_description() {
        // arrange
        List<Word> words = ImmutableList.of(Word.introWord("given"), new Word("a coffee machine"));

        // act
        String block = converter.convertStepBlock(0, words, StepStatus.PASSED, 3899, null, false, null, false);

        // assert
        assertThat(block).isEqualTo("* [.jg-introWord]*Given* a coffee machine");
    }

    @Test
    public void convert_step_with_description() {
        // arrange
        List<Word> words = ImmutableList.of(Word.introWord("given"), new Word("a coffee machine"));

        // act
        String block =
                converter.convertStepBlock(0, words, StepStatus.PASSED, 3899, "It is a brand new machine.", false, null, false);

        // assert
        assertThatBlockContainsLines(block,
                "* [.jg-introWord]*Given* a coffee machine +",
                "  _+++It is a brand new machine.+++_");
    }

    @Test
    public void convert_failed_step_without_description() {
        // arrange
        List<Word> words = Collections.singletonList(Word.introWord("given"));

        // act
        String block = converter.convertStepBlock(0, words, StepStatus.FAILED, 3000899, null, true, null, false);

        // assert
        assertThat(block).isEqualTo("* [.jg-introWord]*Given* [.right]#[FAILED] (0s 3ms)#");
    }

    @Test
    public void convert_first_step_in_section() {
        // arrange
        List<Word> words = Collections.singletonList(Word.introWord("given"));

        // act
        String block = converter.convertStepBlock(0, words, StepStatus.PASSED, 3899, null, false, "First section", false);

        // assert
        assertThatBlockContainsLines(block,
                ".First section",
                "* [.jg-introWord]*Given*");
    }


    @Test
    public void convert_step_with_simple_argument() {
        // arrange
        List<Word> words =
                ImmutableList.of(Word.introWord("given"), new Word("a coffee machine with"), Word.argWord("ncoffees", "0", "0"),
                        new Word("coffees"));

        // act
        String block = converter.convertStepBlock(0, words, StepStatus.PASSED, 3899, null, false, null, false);

        // assert
        assertThat(block).isEqualTo("* [.jg-introWord]*Given* a coffee machine with [.jg-argument]_0_ coffees");
    }

    @Test
    public void convert_step_with_multiline_argument() {
        // arrange
        List<Word> words = ImmutableList.of(Word.introWord("given"), new Word("a coffee machine with"),
                Word.argWord("description", "0", "very nice text\nand also more text"));

        // act
        String block = converter.convertStepBlock(0, words, StepStatus.PASSED, 3899, null, false, null, false);

        // assert
        assertThatBlockContainsLines(block,
                "* [.jg-introWord]*Given* a coffee machine with",
                "+",
                "[.jg-argument]",
                "....",
                "very nice text",
                "and also more text",
                "....");
    }

    @Test
    public void convert_step_with_parameter() {
        // arrange
        Word ncoffees = Word.argWord("coffee count", "0", "0");
        ncoffees.getArgumentInfo().setParameterName("coffee count");

        List<Word> words =
                ImmutableList.of(Word.introWord("given"), new Word("a coffee machine with"), ncoffees, new Word("coffees"));

        // act
        String block = converter.convertStepBlock(0, words, StepStatus.PASSED, 3899, null, false, null, false);

        // assert
        assertThat(block).isEqualTo("* [.jg-introWord]*Given* a coffee machine with [.jg-argument]*<coffee count>* coffees");
    }

    @Test
    public void convert_step_with_data_table_with_horizontal_header() {
        // arrange
        ImmutableList<List<String>> productsTable =
                ImmutableList.of(ImmutableList.of("product", "price"), ImmutableList.of("apples", "23"),
                        ImmutableList.of("pears", "42"));
        List<Word> words = ImmutableList.of(Word.introWord("given"), new Word("the products"),
                Word.argWord("products", productsTable.toString(), new DataTable(Table.HeaderType.HORIZONTAL, productsTable)));

        // act
        String block = converter.convertStepBlock(0, words, StepStatus.PASSED, 3899, null, false, null, false);

        // assert
        assertThatBlockContainsLines(block,
                "* [.jg-introWord]*Given* the products",
                "+",
                "[.jg-argumentTable%header,cols=\"1,1\"]",
                "|===",
                "| product | price ",
                "| apples | 23 ",
                "| pears | 42 ",
                "|===");
    }

    @Test
    public void convert_step_with_data_table_vertical_header() {
        // arrange
        ImmutableList<List<String>> productsTable =
                ImmutableList.of(ImmutableList.of("product", "apples", "pears"), ImmutableList.of("price", "23", "42"));
        List<Word> words = ImmutableList.of(Word.introWord("given"), new Word("the products"),
                Word.argWord("products", productsTable.toString(), new DataTable(Table.HeaderType.VERTICAL, productsTable)));

        // act
        String block = converter.convertStepBlock(0, words, StepStatus.PASSED, 3899, null, false, null, false);

        // assert

        assertThatBlockContainsLines(block,
                "* [.jg-introWord]*Given* the products",
                "+",
                "[.jg-argumentTable,cols=\"h,1,1\"]",
                "|===",
                "| product | apples | pears ",
                "| price | 23 | 42 ",
                "|===");
    }

    @Test
    public void convert_cases_table_without_descriptions() {
        // arrange
        ScenarioCaseModel case1 = new ScenarioCaseModel();
        case1.setCaseNr(1);
        case1.setStatus(ExecutionStatus.SUCCESS);
        case1.setDerivedArguments(ImmutableList.of("1", "2"));

        ScenarioCaseModel case2 = new ScenarioCaseModel();
        case2.setCaseNr(2);
        case2.setStatus(ExecutionStatus.FAILED);
        case2.setDerivedArguments(ImmutableList.of("3", "4"));

        ScenarioModel scenario = new ScenarioModel();
        scenario.addCase(case1);
        scenario.addCase(case2);
        scenario.addDerivedParameter("foo");
        scenario.addDerivedParameter("bar");

        CasesTable casesTable = new CasesTableImpl(scenario);

        // act
        String block = converter.convertCasesTableBlock(casesTable);

        // assert
        assertThatBlockContainsLines(block,
                ".Cases",
                "[.jg-casesTable%header,cols=\"h,1,1,>1\"]",
                "|===",
                "| # | foo | bar | Status",
                "| 1 | 1 | 2 | SUCCESS",
                "| 2 | 3 | 4 | FAILED",
                "|===");
    }

    @Test
    public void convert_cases_table_with_descriptions() {
        // arrange
        ScenarioCaseModel case1 = new ScenarioCaseModel();
        case1.setCaseNr(1);
        case1.setStatus(ExecutionStatus.SUCCESS);
        case1.setDerivedArguments(ImmutableList.of("1", "2"));
        case1.setDescription("First case");

        ScenarioCaseModel case2 = new ScenarioCaseModel();
        case2.setCaseNr(2);
        case2.setStatus(ExecutionStatus.FAILED);
        case2.setDerivedArguments(ImmutableList.of("3", "4"));
        case2.setDescription("Second case");

        ScenarioModel scenario = new ScenarioModel();
        scenario.addCase(case1);
        scenario.addCase(case2);
        scenario.addDerivedParameter("foo");
        scenario.addDerivedParameter("bar");

        CasesTable casesTable = new CasesTableImpl(scenario);

        // act
        String block = converter.convertCasesTableBlock(casesTable);

        // assert
        assertThatBlockContainsLines(block,
                ".Cases",
                "[.jg-casesTable%header,cols=\"h,1,1,1,>1\"]",
                "|===",
                "| # | Description | foo | bar | Status",
                "| 1 | First case | 1 | 2 | SUCCESS",
                "| 2 | Second case | 3 | 4 | FAILED",
                "|===");
    }

    @Test
    public void convert_scenario_case_footer() {
        // arrange
        final ImmutableList<String> stackTraceLines = ImmutableList.of("broken line 1", "broken line 2");

        // act
        final String block =
                converter.convertCaseFooterBlock("Something is broken", stackTraceLines);

        // assess
        assertThatBlockContainsLines(block,
                ".Something is broken",
                "[.jg-exception%collapsible]",
                "====",
                "....",
                "broken line 1",
                "broken line 2",
                "....",
                "====");
    }

    @Test
    public void convert_scenario_case_footer_without_stacktrace() {
        // arrange

        // act
        final String block =
                converter.convertCaseFooterBlock("Something is broken", null);

        // assess
        assertThatBlockContainsLines(block,
                ".Something is broken",
                "[.jg-exception%collapsible]",
                "====",
                "No stacktrace provided",
                "====");
    }

    @Test
    @DataProvider({"SUCCESS, successful", "FAILED, failed", "SCENARIO_PENDING, pending", "SOME_STEPS_PENDING, pending"})
    public void convert_scenario_footer(final ExecutionStatus status, final String scenarioTag) {
        // arrange

        // act
        String block = converter.convertScenarioFooterBlock(status);

        // assert
        assertThat(block).isEqualTo("// end::scenario-" + scenarioTag + "[]");
    }

    @Test
    public void convert_statistics() {
        // arrange
        final ReportStatistics statisticsOne = new ReportStatistics();
        statisticsOne.numClasses = 1;
        statisticsOne.numScenarios = 3;
        statisticsOne.numSuccessfulScenarios = 2;
        statisticsOne.numFailedScenarios = 1;
        statisticsOne.numCases = 3;
        statisticsOne.numFailedCases = 1;
        statisticsOne.numSteps = 13;

        final ReportStatistics statisticsTwo = new ReportStatistics();
        statisticsTwo.numClasses = 1;
        statisticsTwo.numScenarios = 2;
        statisticsTwo.numSuccessfulScenarios = 1;
        statisticsTwo.numPendingScenarios = 1;
        statisticsTwo.numCases = 2;
        statisticsTwo.numFailedCases = 1;
        statisticsTwo.numSteps = 8;

        final ReportStatistics totalStatistics = new ReportStatistics();
        totalStatistics.numClasses = 2;
        totalStatistics.numScenarios = 5;
        totalStatistics.numSuccessfulScenarios = 3;
        totalStatistics.numPendingScenarios = 1;
        totalStatistics.numFailedScenarios = 1;
        totalStatistics.numCases = 5;
        totalStatistics.numFailedCases = 1;
        totalStatistics.numSteps = 21;

        final ImmutableMap<String, ReportStatistics> featureStatistics =
                ImmutableMap.of("Feature One", statisticsOne, "Feature Two", statisticsTwo);

        // act
        final String block = converter.convertStatisticsBlock(featureStatistics, totalStatistics);

        // assess
        assertThatBlockContainsLines(block,
                ".Total Statistics",
                "[options=\"header,footer\"]",
                "|===",
                "| feature | total classes | successful scenarios | failed scenarios | pending scenarios | total scenarios | failed cases | total cases | total steps | duration",
                "| Feature One | 1 | 2 | 1 | 0 | 3 | 1 | 3 | 13 | 0s 0ms",
                "| Feature Two | 1 | 1 | 0 | 1 | 2 | 1 | 2 | 8 | 0s 0ms",
                "| sum | 2 | 3 | 1 | 1 | 5 | 1 | 5 | 21 | 0s 0ms",
                "|===");
    }

    private static void assertThatBlockContainsLines(final String block, final String... expectedLines) {
        final String[] blockLines = block.split(System.lineSeparator());
        assertThat(blockLines).hasSize(expectedLines.length).containsExactly(expectedLines);
    }
}
