package com.goeswhere.sqlitelulz;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.goeswhere.sqlitelulz.DAO.TransactionManager;

public class SqliteLulz {
	public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
		int count = 200;
		String db = "jdbc:sqlite:foo.db";
		String driver = "org.sqlite.JDBC";

		System.out.println("Default arguments: " + count + " " + db + " " + driver);

		if (args.length > 0)
			count = Integer.parseInt(args[0]);

		if (args.length > 1)
			db = args[1];

		if (args.length > 2)
			driver = args[2];

		Class.forName(driver);
		final DAO dao = new DAO(DriverManager.getConnection(db));

		final List<Object[]> l = listOfLength(count);


		reset(dao);

		stupidsleep();

		System.out.print("Autocommit");
		time(new Runnable() { @Override public void run() {
				dao.executeUpdate("insert into foo values (?)", l);
			}});

		reset(dao);

		stupidsleep();

		System.out.print("Manual commit");
		time(new Runnable() { @Override public void run() {
				final TransactionManager tm = dao.new TransactionManager();
				dao.executeUpdate("insert into foo values (?)", l);
				tm.commit();
				tm.close();
			}});
	}

	private static void stupidsleep() throws InterruptedException {
		System.out.println("[sleeping for two seconds for no reason]");
		Thread.sleep(2000);
	}

	private static List<Object[]> listOfLength(int count) {
		final List<Object[]> l = new ArrayList<Object[]>();
		for (int i = 0; i < count; ++i)
			l.add(new Object[] { i });
		return l;
	}

	private static void time(Runnable runnable) {
		long start = System.nanoTime();
		runnable.run();
		time(start);
	}

	private static void time(long start) {
		System.out.println("  " + ((System.nanoTime()-start)/1e9) + " seconds");
	}

	private static void reset(DAO dao) {
		dao.execute("drop table if exists foo");
		dao.execute("create table foo (bar integer)");
	}
}
