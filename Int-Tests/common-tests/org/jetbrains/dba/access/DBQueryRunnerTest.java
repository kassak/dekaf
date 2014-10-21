package org.jetbrains.dba.access;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.dba.Rdbms;
import org.jetbrains.dba.sql.SQLQuery;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jetbrains.dba.TestDB.FACADE;



public class DBQueryRunnerTest {


  @Test
  public void query_1_inSession() {
    final String ourSelect1 = FACADE.rdbms() == Rdbms.ORACLE ? "select 1 from dual" : "select 1";
    final SQLQuery<Integer> query = FACADE.sql().query(ourSelect1, RowsCollectors.oneRow(Integer.class));
    FACADE.inSession(new InSessionNoResult() {
      @Override
      public void run(@NotNull DBSession session) {
        Integer result = session.query(query).run();
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(1);
      }
    });
  }

  @Test
  public void query_1_inTransaction() {
    final String ourSelect1 = FACADE.rdbms() == Rdbms.ORACLE ? "select 1 from dual" : "select 1";
    final SQLQuery<Integer> query = FACADE.sql().query(ourSelect1, RowsCollectors.oneRow(Integer.class));
    FACADE.inTransaction(new InTransactionNoResult() {
      @Override
      public void run(@NotNull DBTransaction transaction) {
        Integer result = transaction.query(query).run();
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(1);
      }
    });
  }


}