package com.goeswhere.sqlitelulz;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DAO implements Closeable {
	private final Connection conn;
	private final Statement stat;

	public DAO(Connection conn) {
		this.conn = conn;
		try {
			this.stat = conn.createStatement();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	int executeUpdate(String sql, List<Object[]> args) {
		try {
			final PreparedStatement ps = conn.prepareStatement(sql);
			try {
				int ret = 0;
				for (Object[] arg : args) {
					prepare(ps, arg);
					ret += ps.executeUpdate();
					if (0 == ret % 50)
						System.out.print(".");
				}
				return ret;
			} finally {
				ps.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void prepare(final PreparedStatement ps, Object[] args) throws SQLException {
		for (int i = 0; i < args.length; i++)
			ps.setObject(i + 1, args[i]);
	}

	void execute(String command) {
		try {
			stat.execute(command);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	class TransactionManager implements Closeable {
		private boolean committed;

		TransactionManager() {
			begin();
		}

		public void commit() {
			DAO.this.commit();
			committed = true;
		}

		@Override
		public void close() {
			if (!committed)
				rollback();
		}
	}

	private void begin() {
		execute("begin");
	}

	private void commit() {
		execute("commit");
	}

	private void rollback() {
		execute("rollback");
	}

	@Override
	public void close() {
		try {
			stat.close();
			conn.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
