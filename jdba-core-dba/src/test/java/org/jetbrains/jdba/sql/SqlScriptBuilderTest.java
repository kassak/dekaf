package org.jetbrains.jdba.sql;

import org.jetbrains.jdba.junitft.FineRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.assertj.core.api.Assertions.assertThat;



/**
 * @author Leonid Bushuev from JetBrains
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(FineRunner.class)
public class SqlScriptBuilderTest {


  @Test
  public void parse_1() {
    String commandText = "select * from dual";
    final SqlScript script = build(commandText);

    assertThat((Integer)script.myCount).isEqualTo((Integer)1);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo(commandText);
  }


  /*
  @Test
  public void parse_2_in_one_line() {
    String text = "create table X; drop table X";
    final SQLScript script = build(text);

    assertEquals(script.myCount, 2);
    assertEquals(script.getCommands().get(0).getSourceText(), "create table X");
    assertEquals(script.getCommands().get(1).getSourceText(), "drop table X");
  }
  */


  @Test
  public void parse_2_in_2_lines() {
    String text = "create table X;\n drop table X";
    final SqlScript script = build(text);

    assertThat((Integer)script.myCount).isEqualTo((Integer)2);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo("create table X");
    assertThat(script.getStatements().get(1).getSourceText()).isEqualTo("drop table X");
  }


  @Test
  public void parse_singleLineComment() {
    String text = "-- a single line comment \n" +
                  "do something";
    final SqlScript script = build(text);

    assertThat((Integer)script.myCount).isEqualTo((Integer)1);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo("do something");
  }


  @Test
  public void parse_singleLineComment_preserveOracleHint() {
    String text = "select --+index(i) \n" +
                  "      all fields   \n" +
                  "from my_table      \n";
    final SqlScript script = build(text);

    assertThat((Integer)script.myCount).isEqualTo((Integer)1);
    final String queryText = script.getStatements().get(0).getSourceText();
    assertThat(queryText).contains("--+index(i)");
  }


  @Test
  public void parse_multiLineComment_1() {
    String text = "/* a multi-line comment*/\n" +
                  "do something";
    final SqlScript script = build(text);

    assertThat((Integer)script.myCount).isEqualTo((Integer)1);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo("do something");
  }


  @Test
  public void parse_multiLineComment_3() {
    String text = "/*                      \n" +
                  " * a multi-line comment \n" +
                  " */                     \n" +
                  "do something            \n";
    final SqlScript script = build(text);

    assertThat((Integer)script.myCount).isEqualTo((Integer)1);
    assertThat(script.getStatements().get(0).getSourceText()).isEqualTo("do something");
  }


  @Test
  public void parse_multiLineComment_preserveOracleHint() {
    String text = "select /*+index(i)*/ * \n" +
                  "from table             \n";
    final SqlScript script = build(text);

    assertThat((Integer)script.myCount).isEqualTo((Integer)1);
    final String queryText = script.getStatements().get(0).getSourceText();
    assertThat(queryText).contains("/*+index(i)*/");
  }


  private SqlScript build(String text) {
    SqlScriptBuilder b = new SqlScriptBuilder();
    b.parse(text);
    return b.build();
  }


}
