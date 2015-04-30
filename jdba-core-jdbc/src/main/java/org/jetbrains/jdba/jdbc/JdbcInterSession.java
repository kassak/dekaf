package org.jetbrains.jdba.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jdba.core.DBErrorRecognizer;
import org.jetbrains.jdba.core.DBInterSession;
import org.jetbrains.jdba.core.ParameterDef;
import org.jetbrains.jdba.core.exceptions.DBException;
import org.jetbrains.jdba.core.exceptions.DBSessionIsClosedException;

import java.sql.Connection;
import java.sql.SQLException;



/**
 * @author Leonid Bushuev from JetBrains
 */
public class JdbcInterSession implements DBInterSession {

  @NotNull
  private final DBErrorRecognizer myErrorRecognizer;

  @NotNull
  private final Connection myConnection;

  private final boolean myOwnConnection;

  private boolean myInTransaction;

  private boolean myClosed;


  protected JdbcInterSession(final @NotNull DBErrorRecognizer errorRecognizer,
                             @NotNull final Connection connection,
                             final boolean ownConnection) {
    myErrorRecognizer = errorRecognizer;
    myConnection = connection;
    myOwnConnection = ownConnection;
    myClosed = false;
  }


  @Override
  public void beginTransaction() {
    checkNotClosed();

    if (!myInTransaction) {
      try {
        myConnection.setAutoCommit(false);
      }
      catch (SQLException sqle) {
        throw recognizeException(sqle, "set autocommit = false");
      }
    }
    else {
      throw new IllegalStateException("The sessions is already in a transaction");
    }

    myInTransaction = true;
  }

  @Override
  public void commit() {
    if (myInTransaction) {
      myInTransaction = false;
      try {
        myConnection.commit();
      }
      catch (SQLException sqle) {
        rollback();
        throw myErrorRecognizer.recognizeError(sqle, "commit");
      }
    }
    else {
      throw new IllegalStateException("The sessions is not in a transaction");
    }
  }

  @Override
  public void rollback() {
    if (myClosed) return;

    // TODO close all seances here

    myInTransaction = false;

    try {
      myConnection.rollback();
      myConnection.setAutoCommit(true);
    }
    catch (SQLException sqle) {
      // TODO log it somehow
      close(); // this connection is broken
    }
  }

  @NotNull
  @Override
  public JdbcInterSeance openSeance(@NotNull final String statementText,
                                    @Nullable final ParameterDef[] outParameterDefs) {
    checkNotClosed();
    if (outParameterDefs == null) {
      return openSimpleStatementSeance(statementText);
    }
    else {
      return openPreparedStatementSeance(statementText, outParameterDefs);
    }
  }

  @NotNull
  protected JdbcSimpleSeance openSimpleStatementSeance(@NotNull final String statementText) {
    return new JdbcSimpleSeance(this, statementText);
  }

  @NotNull
  protected JdbcCallableStatementSeance openPreparedStatementSeance(@NotNull final String statementText,
                                                                    @NotNull final ParameterDef[] outParameterDefs) {
    return new JdbcCallableStatementSeance(this, statementText, outParameterDefs);
  }


  @Override
  public void close() {
    if (myClosed) return;

    if (myInTransaction) {
      rollback();
    }

    myClosed = true;

    if (myOwnConnection) {
      try {
        myConnection.close();
      }
      catch (SQLException sqle) {
        // TODO log it somehow
      }
    }
  }


  //// INTERNAL METHODS \\\\

  @NotNull
  protected Connection getConnection() {
    return myConnection;
  }



  //// USEFUL METHODS \\\\

  private void checkNotClosed() {
    if (myClosed) throw new DBSessionIsClosedException("The session is closed");
  }

  @NotNull
  protected DBException recognizeException(@NotNull final SQLException sqle) {
    return myErrorRecognizer.recognizeError(sqle, null);
  }

  @NotNull
  protected DBException recognizeException(@NotNull final SQLException sqle,
                                           @Nullable final String statementText) {
    return myErrorRecognizer.recognizeError(sqle, statementText);
  }


}
